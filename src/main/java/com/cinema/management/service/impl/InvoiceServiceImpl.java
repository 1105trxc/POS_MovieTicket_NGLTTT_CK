package com.cinema.management.service.impl;

import com.cinema.management.model.dto.InvoiceDto;
import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.dto.TicketDto;
import com.cinema.management.model.entity.*;
import com.cinema.management.repository.*;
import com.cinema.management.service.IInvoiceService;
import com.cinema.management.service.IPointService;
import com.cinema.management.service.IPromotionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Triển khai luồng Thanh toán & Xuất vé (FR-ST-04, Module 3).
 *
 * Thứ tự ghi DB trong 1 transaction:
 *   1. Kiểm tra ghế chưa bị mua lại (race condition lần cuối).
 *   2. Tạo Invoice (giá snapshot).
 *   3. Lưu BookingSeat (snapshot price từ SeatType.basePrice).
 *   4. Lưu OrderDetail (snapshot price từ Product.currentPrice).
 *   5. Lưu Payment.
 *   6. Xoá SeatLock.
 *   7. Cập nhật Customer: điểm thưởng, tổng chi tiêu, nâng hạng.
 *   8. Ghi PointHistory.
 */
public class InvoiceServiceImpl implements IInvoiceService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final InvoiceRepository      invoiceRepository;
    private final BookingSeatRepository  bookingSeatRepository;
    private final OrderDetailRepository  orderDetailRepository;
    private final PaymentRepository      paymentRepository;
    private final SeatLockRepository     seatLockRepository;
    private final ProductRepository      productRepository;
    private final CustomerRepository     customerRepository;
    private final ShowTimeRepository     showTimeRepository;
    private final UserRepository         userRepository;
    private final IPromotionService      promotionService;
    private final IPointService          pointService;

    public InvoiceServiceImpl() {
        this.invoiceRepository     = new InvoiceRepository();
        this.bookingSeatRepository = new BookingSeatRepository();
        this.orderDetailRepository = new OrderDetailRepository();
        this.paymentRepository     = new PaymentRepository();
        this.seatLockRepository    = new SeatLockRepository();
        this.productRepository     = new ProductRepository();
        this.customerRepository    = new CustomerRepository();
        this.showTimeRepository    = new ShowTimeRepository();
        this.userRepository        = new UserRepository();
        this.promotionService      = new PromotionServiceImpl();
        this.pointService          = new PointServiceImpl();
    }

    // ── checkout ─────────────────────────────────────────────────────────────

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
            throw new IllegalArgumentException("Chưa chọn ghế nào để thanh toán.");
        }

        // ── 1. Load entities cần thiết ─────────────────────────────────────
        ShowTime showTime = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại."));
        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại."));
        Customer customer = (customerId != null)
                ? customerRepository.findById(customerId).orElse(null) : null;

        // ── 2. Race condition check cuối cùng ─────────────────────────────
        for (SeatStatusDto seat : selectedSeats) {
            if (bookingSeatRepository.existsById(new BookingSeatId(showTimeId, seat.getSeatId()))) {
                throw new IllegalStateException(
                        "Ghế " + seat.getLabel() + " vừa được mua bởi nhân viên khác. Vui lòng chọn lại.");
            }
        }

        // ── 3. Tính giá ghế (subtotal) ─────────────────────────────────────
        BigDecimal seatTotal = selectedSeats.stream()
                .map(SeatStatusDto::getBasePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ── 4. Tính F&B total ──────────────────────────────────────────────
        Map<Product, Integer> fbProductQtyMap = new LinkedHashMap<>();
        BigDecimal fbTotal = BigDecimal.ZERO;
        List<String> fbLines = new ArrayList<>();
        if (fbItems != null) {
            for (Map.Entry<String, Integer> entry : fbItems.entrySet()) {
                Product product = productRepository.findById(entry.getKey())
                        .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại: " + entry.getKey()));
                int qty = entry.getValue();
                fbProductQtyMap.put(product, qty);
                BigDecimal lineTotal = product.getCurrentPrice().multiply(BigDecimal.valueOf(qty));
                fbTotal = fbTotal.add(lineTotal);
                fbLines.add(product.getProductName() + " x" + qty
                        + " = " + String.format("%,.0f", lineTotal) + " VNĐ");
            }
        }

        BigDecimal subTotal = seatTotal.add(fbTotal);

        // ── 5. Áp dụng Promo (FR-ST-03) ───────────────────────────────────
        Promotion promotion = null;
        BigDecimal promoDiscount = BigDecimal.ZERO;
        String usedPromoCode = null;
        if (promoCode != null && !promoCode.isBlank()) {
            promotion = promotionService.validatePromoCode(promoCode, showTime);
            promoDiscount = promotionService.calculateDiscount(promotion, subTotal);
            usedPromoCode = promotion.getCode();
        }

        // ── 6. Áp dụng điểm thưởng (BR-01) ───────────────────────────────
        BigDecimal pointDiscount = BigDecimal.ZERO;
        if (usedPoints > 0 && customer != null) {
            if (promotion != null && promotionService.isExclusive(promotion)) {
                throw new IllegalArgumentException(
                        "Mã khuyến mãi '" + usedPromoCode + "' là độc quyền, không thể dùng kèm điểm thưởng (BR-01).");
            }
            pointDiscount = pointService.calculatePointDiscount(customer, usedPoints, subTotal);
        }

        // ── 7. Tính tổng cuối ─────────────────────────────────────────────
        BigDecimal grandTotal = subTotal.subtract(promoDiscount).subtract(pointDiscount);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) grandTotal = BigDecimal.ZERO;

        // Điểm sẽ được tích (5% tổng cuối)
        int earnedPoints = (customer != null) ? pointService.calculateEarnedPoints(grandTotal) : 0;

        // ── 8. Tạo Invoice ────────────────────────────────────────────────
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

        // ── 9. Lưu BookingSeat (snapshot price) ──────────────────────────
        List<BookingSeat> bookingSeats = new ArrayList<>();
        List<TicketDto> tickets = new ArrayList<>();
        for (SeatStatusDto seat : selectedSeats) {
            BookingSeat bs = BookingSeat.builder()
                    .id(new BookingSeatId(showTimeId, seat.getSeatId()))
                    .showTime(showTime)
                    .seat(buildSeatRef(seat.getSeatId()))
                    .invoice(invoice)
                    .price(seat.getBasePrice())   // Snapshot
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
                    .customerName(customer != null ? customer.getFullName() : "Khách lẻ")
                    .build());
        }
        bookingSeatRepository.saveAll(bookingSeats);

        // ── 10. Lưu OrderDetail (snapshot price F&B) ─────────────────────
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (Map.Entry<Product, Integer> entry : fbProductQtyMap.entrySet()) {
            Product product = entry.getKey();
            int qty = entry.getValue();
            orderDetails.add(OrderDetail.builder()
                    .id(new OrderDetailId(invoiceId, product.getProductId()))
                    .invoice(invoice)
                    .product(product)
                    .quantity(qty)
                    .price(product.getCurrentPrice())   // Snapshot
                    .build());
        }
        if (!orderDetails.isEmpty()) orderDetailRepository.saveAll(orderDetails);

        // ── 11. Lưu Payment ───────────────────────────────────────────────
        Payment payment = Payment.builder()
                .paymentId(UUID.randomUUID().toString())
                .invoice(invoice)
                .amount(grandTotal)
                .paymentMethod(paymentMethod)
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // ── 12. Xoá SeatLock ──────────────────────────────────────────────
        seatLockRepository.deleteUserLocksForShowTime(showTimeId, staffUserId);

        // ── 13. Tích điểm / Đổi điểm ─────────────────────────────────────
        if (customer != null) {
            if (usedPoints > 0) {
                pointService.redeemPoints(customer, invoice, usedPoints);
            }
            if (earnedPoints > 0) {
                pointService.addPoints(customer, invoice, earnedPoints);
            }
        }

        // ── 14. Build và trả InvoiceDto ───────────────────────────────────
        String staffDisplay = staff.getUsername();
        String customerDisplay = customer != null ? customer.getFullName() : "Khách lẻ";
        String customerPhone   = customer != null ? customer.getPhone()    : "";

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

    // ── Private helper ────────────────────────────────────────────────────────

    private Seat buildSeatRef(String seatId) {
        Seat s = new Seat();
        s.setSeatId(seatId);
        return s;
    }
}

