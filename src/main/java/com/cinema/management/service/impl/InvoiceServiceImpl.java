package com.cinema.management.service.impl;

import com.cinema.management.model.dto.InvoiceDto;
import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.dto.TicketDto;
import com.cinema.management.model.entity.BookingSeat;
import com.cinema.management.model.entity.BookingSeatId;
import com.cinema.management.model.entity.Customer;
import com.cinema.management.model.entity.Invoice;
import com.cinema.management.model.entity.OrderDetail;
import com.cinema.management.model.entity.OrderDetailId;
import com.cinema.management.model.entity.Payment;
import com.cinema.management.model.entity.Product;
import com.cinema.management.model.entity.Promotion;
import com.cinema.management.model.entity.Seat;
import com.cinema.management.model.entity.ShowTime;
import com.cinema.management.model.entity.User;
import com.cinema.management.repository.BookingSeatRepository;
import com.cinema.management.repository.CustomerRepository;
import com.cinema.management.repository.InvoiceRepository;
import com.cinema.management.repository.OrderDetailRepository;
import com.cinema.management.repository.PaymentRepository;
import com.cinema.management.repository.ProductRepository;
import com.cinema.management.repository.SeatLockRepository;
import com.cinema.management.repository.ShowTimeRepository;
import com.cinema.management.repository.UserRepository;
import com.cinema.management.service.IInvoiceService;
import com.cinema.management.service.IPointService;
import com.cinema.management.service.IPromotionService;

import com.cinema.management.service.IAuditLogService;
import com.cinema.management.repository.AuditLogRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Trien khai luong Thanh toan va Xuat ve.
 */
public class InvoiceServiceImpl implements IInvoiceService {
    private static final int SELLABLE_AFTER_START_MINUTES = 30;

    private final InvoiceRepository invoiceRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final SeatLockRepository seatLockRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final ShowTimeRepository showTimeRepository;
    private final UserRepository userRepository;
    private final IPromotionService promotionService;
    private final IPointService pointService;
    private final IAuditLogService auditLogService;

    public InvoiceServiceImpl() {
        this.invoiceRepository = new InvoiceRepository();
        this.bookingSeatRepository = new BookingSeatRepository();
        this.orderDetailRepository = new OrderDetailRepository();
        this.paymentRepository = new PaymentRepository();
        this.seatLockRepository = new SeatLockRepository();
        this.productRepository = new ProductRepository();
        this.customerRepository = new CustomerRepository();
        this.showTimeRepository = new ShowTimeRepository();
        this.userRepository = new UserRepository();
        this.promotionService = new PromotionServiceImpl();
        this.pointService = new PointServiceImpl();
        this.auditLogService = new AuditLogServiceImpl(new AuditLogRepository());
    }

    @Override
    public InvoiceDto checkout(String showTimeId,
            String staffUserId,
            String customerId,
            List<SeatStatusDto> selectedSeats,
            Map<String, Integer> fbItems,
            String promoCode,
            int usedPoints,
            String paymentMethod) {

        if (selectedSeats == null || selectedSeats.isEmpty()) {
            throw new IllegalArgumentException("Chua chon ghe nao de thanh toan.");
        }

        ShowTime showTime = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suat chieu khong ton tai."));
        validateShowTimeIsSellable(showTime);

        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new IllegalArgumentException("Nhan vien khong ton tai."));
        Customer customer = (customerId != null)
                ? customerRepository.findById(customerId).orElse(null)
                : null;

        for (SeatStatusDto seat : selectedSeats) {
            if (bookingSeatRepository.existsById(new BookingSeatId(showTimeId, seat.getSeatId()))) {
                throw new IllegalStateException(
                        "Ghe " + seat.getLabel() + " vua duoc mua boi nhan vien khac. Vui long chon lai.");
            }
        }

        BigDecimal seatTotal = selectedSeats.stream()
                .map(SeatStatusDto::getBasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Product, Integer> fbProductQtyMap = new LinkedHashMap<>();
        BigDecimal fbTotal = BigDecimal.ZERO;
        List<String> fbLines = new ArrayList<>();
        if (fbItems != null) {
            for (Map.Entry<String, Integer> entry : fbItems.entrySet()) {
                Product product = productRepository.findById(entry.getKey())
                        .orElseThrow(() -> new IllegalArgumentException("San pham khong ton tai: " + entry.getKey()));
                int qty = entry.getValue();
                fbProductQtyMap.put(product, qty);
                BigDecimal lineTotal = product.getCurrentPrice().multiply(BigDecimal.valueOf(qty));
                fbTotal = fbTotal.add(lineTotal);
                fbLines.add(product.getProductName() + " x" + qty
                        + " = " + String.format("%,.0f", lineTotal) + " VND");
            }
        }

        BigDecimal subTotal = seatTotal.add(fbTotal);

        Promotion promotion = null;
        BigDecimal promoDiscount = BigDecimal.ZERO;
        String usedPromoCode = null;
        if (promoCode != null && !promoCode.isBlank()) {
            promotion = promotionService.validatePromoCode(promoCode, showTime);
            promoDiscount = promotionService.calculateDiscount(promotion, subTotal);
            usedPromoCode = promotion.getCode();
        }

        BigDecimal pointDiscount = BigDecimal.ZERO;
        if (usedPoints > 0 && customer != null) {
            if (promotion != null && promotionService.isExclusive(promotion)) {
                throw new IllegalArgumentException(
                        "Ma khuyen mai '" + usedPromoCode + "' la doc quyen, khong the dung kem diem thuong.");
            }
            pointDiscount = pointService.calculatePointDiscount(customer, usedPoints, subTotal);
        }

        BigDecimal grandTotal = subTotal.subtract(promoDiscount).subtract(pointDiscount);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            grandTotal = BigDecimal.ZERO;
        }

        int earnedPoints = (customer != null) ? pointService.calculateEarnedPoints(grandTotal) : 0;

        String invoiceId = "INV-" + System.currentTimeMillis();
        Invoice invoice = Invoice.builder()
                .invoiceId(invoiceId)
                .user(staff)
                .customer(customer)
                .promotion(promotion)
                .totalAmount(grandTotal)
                .usedPoints(usedPoints)
                .discountFromPoints(pointDiscount)
                .earnedPoints(earnedPoints)
                .createdAt(LocalDateTime.now())
                .build();
        invoiceRepository.save(invoice);

        List<BookingSeat> bookingSeats = new ArrayList<>();
        List<TicketDto> tickets = new ArrayList<>();
        for (SeatStatusDto seat : selectedSeats) {
            BookingSeat bs = BookingSeat.builder()
                    .id(new BookingSeatId(showTimeId, seat.getSeatId()))
                    .showTime(showTime)
                    .seat(buildSeatRef(seat.getSeatId()))
                    .invoice(invoice)
                    .price(seat.getBasePrice())
                    .build();
            bookingSeats.add(bs);

            tickets.add(TicketDto.builder()
                    .invoiceId(invoiceId)
                    .movieTitle(showTime.getMovie() != null ? showTime.getMovie().getTitle() : "")
                    .roomName(showTime.getRoom() != null ? showTime.getRoom().getRoomName() : "")
                    .showTime(showTime.getStartTime())
                    .seatLabel(seat.getLabel())
                    .seatTypeName(seat.getSeatTypeName())
                    .price(seat.getBasePrice())
                    .customerName(customer != null ? customer.getFullName() : "Khach le")
                    .build());
        }
        bookingSeatRepository.saveAll(bookingSeats);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (Map.Entry<Product, Integer> entry : fbProductQtyMap.entrySet()) {
            Product product = entry.getKey();
            int qty = entry.getValue();
            orderDetails.add(OrderDetail.builder()
                    .id(new OrderDetailId(invoiceId, product.getProductId()))
                    .invoice(invoice)
                    .product(product)
                    .quantity(qty)
                    .price(product.getCurrentPrice())
                    .build());
        }
        if (!orderDetails.isEmpty()) {
            orderDetailRepository.saveAll(orderDetails);
        }

        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .invoice(invoice)
                .amount(grandTotal)
                .paymentMethod(paymentMethod)
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        seatLockRepository.deleteUserLocksForShowTime(showTimeId, staffUserId);

        if (customer != null) {
            if (usedPoints > 0) {
                pointService.redeemPoints(customer, invoice, usedPoints);
            }
            if (earnedPoints > 0) {
                pointService.addPoints(customer, invoice, earnedPoints);
            }
        }

        String staffDisplay = staff.getUsername();
        String customerDisplay = customer != null ? customer.getFullName() : "Khach le";
        String customerPhone = customer != null ? customer.getPhone() : "";

        auditLogService.logAction("TRANSACTION", "Invoice", "Xác nhận thanh toán", "None",
                invoiceId + " (" + String.format("%,.0f VND", grandTotal) + ")");

        return InvoiceDto.builder()
                .invoiceId(invoiceId)
                .createdAt(invoice.getCreatedAt())
                .staffName(staffDisplay)
                .customerName(customerDisplay)
                .customerPhone(customerPhone)
                .tickets(tickets)
                .fbLines(fbLines)
                .seatTotal(seatTotal)
                .fbTotal(fbTotal)
                .promotionDiscount(promoDiscount)
                .pointDiscount(pointDiscount)
                .grandTotal(grandTotal)
                .promoCode(usedPromoCode)
                .earnedPoints(earnedPoints)
                .paymentMethod(paymentMethod)
                .build();
    }

    private Seat buildSeatRef(String seatId) {
        Seat s = new Seat();
        s.setSeatId(seatId);
        return s;
    }

    private void validateShowTimeIsSellable(ShowTime showTime) {
        if (showTime.getStartTime() == null) {
            throw new IllegalArgumentException("Suat chieu khong hop le de ban ve.");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sellableUntil = showTime.getStartTime().plusMinutes(SELLABLE_AFTER_START_MINUTES);
        if (now.isAfter(sellableUntil)) {
            throw new IllegalArgumentException("Da qua 30 phut ke tu gio bat dau suat chieu. Khong the dat ve.");
        }
    }
}
