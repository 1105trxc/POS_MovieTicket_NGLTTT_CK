package com.cinema.management.controller;

import com.cinema.management.model.dto.InvoiceDto;
import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.entity.Customer;
import com.cinema.management.model.entity.Promotion;
import com.cinema.management.model.entity.ShowTime;
import com.cinema.management.repository.CustomerRepository;
import com.cinema.management.service.IInvoiceService;
import com.cinema.management.service.IPromotionService;
import com.cinema.management.service.impl.InvoiceServiceImpl;
import com.cinema.management.service.impl.PromotionServiceImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller cho luồng Thanh toán & Xuất vé (Module 3).
 * Không chứa business logic – chỉ cầu nối View ↔ Service.
 */
public class InvoiceController {

    private final IInvoiceService invoiceService;
    private final IPromotionService promotionService;
    private final CustomerRepository customerRepository;

    public InvoiceController() {
        this.invoiceService = new InvoiceServiceImpl();
        this.promotionService = new PromotionServiceImpl();
        this.customerRepository = new CustomerRepository();
    }

    public InvoiceController(IInvoiceService invoiceService,
            IPromotionService promotionService,
            CustomerRepository customerRepository) {
        this.invoiceService = invoiceService;
        this.promotionService = promotionService;
        this.customerRepository = customerRepository;
    }

    // ── Customer lookup ───────────────────────────────────────────────────────

    /** Tìm khách hàng theo số điện thoại để điền thông tin lên form. */
    public Optional<Customer> findCustomerByPhone(String phone) {
        return customerRepository.findByPhone(phone);
    }

    // ── Promo validation (real-time check khi nhập mã) ────────────────────────

    /**
     * Kiểm tra nhanh mã promo trước khi bấm thanh toán.
     * 
     * @return Promotion nếu hợp lệ
     * @throws IllegalArgumentException với thông báo lỗi cụ thể
     */
    public Promotion validatePromoCode(String code, ShowTime showTime) {
        return promotionService.validatePromoCode(code, showTime);
    }

    /** Lấy danh sách khuyến mãi đang hoạt động (trong thời gian hiện tại). */
    public List<Promotion> getActivePromotions() {
        return promotionService.getActivePromotions();
    }

    /** Tính số tiền giảm giá từ 1 Promotion cho 1 tổng tiền. */
    public java.math.BigDecimal calculatePromoDiscount(Promotion promo, java.math.BigDecimal subTotal) {
        return promotionService.calculateDiscount(promo, subTotal);
    }

    // ── Checkout ──────────────────────────────────────────────────────────────

    /**
     * Thực hiện thanh toán toàn bộ đơn.
     * 
     * @return InvoiceDto đầy đủ để hiển thị và in vé
     */
    public InvoiceDto checkout(String showTimeId,
            String staffUserId,
            String customerId,
            List<SeatStatusDto> selectedSeats,
            Map<String, Integer> fbItems,
            String promoCode,
            int usedPoints,
            String paymentMethod) {
        return invoiceService.checkout(
                showTimeId, staffUserId, customerId,
                selectedSeats, fbItems, promoCode, usedPoints, paymentMethod);
    }

    public String getPaymentStatus(String paymentId) {
        return invoiceService.getPaymentStatus(paymentId);
    }

    public boolean confirmQrPayment(String paymentId, String transactionCode) {
        return invoiceService.confirmQrPayment(paymentId, transactionCode);
    }

    public InvoiceDto findPendingInvoiceByPaymentId(String paymentId, String staffUserId) {
        return invoiceService.findPendingInvoiceByPaymentId(paymentId, staffUserId);
    }
}
