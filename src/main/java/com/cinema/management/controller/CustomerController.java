package com.cinema.management.controller;

import com.cinema.management.model.entity.Customer;
import com.cinema.management.model.entity.Invoice;
import com.cinema.management.model.entity.MemberTier;
import com.cinema.management.repository.AuditLogRepository;
import com.cinema.management.repository.CustomerRepository;
import com.cinema.management.repository.PointHistoryRepository;
import com.cinema.management.service.ICustomerService;
import com.cinema.management.service.IPointService;
import com.cinema.management.service.impl.AuditLogServiceImpl;
import com.cinema.management.service.impl.CustomerServiceImpl;
import com.cinema.management.service.impl.PointServiceImpl;

import java.math.BigDecimal;
import java.util.List;

public class CustomerController {

    private final ICustomerService customerService;
    private final IPointService pointService;

    public CustomerController() {
        // DI thủ công
        var auditLogService = new AuditLogServiceImpl(new AuditLogRepository());
        this.customerService = new CustomerServiceImpl(new CustomerRepository(), auditLogService);
        this.pointService = new PointServiceImpl(new PointHistoryRepository());
    }

    // ===== Tra cứu khách hàng =====

    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    public Customer findByPhone(String phone) {
        return customerService.findByPhone(phone);
    }

    public void createCustomer(Customer customer) {
        customerService.createCustomer(customer);
    }

    public void updateCustomer(Customer customer) {
        customerService.updateCustomer(customer);
    }

    // ===== Logic tính giảm giá hạng thành viên =====

    /**
     * Tính giảm giá theo hạng thành viên của khách.
     */
    public BigDecimal calculateTierDiscount(Customer customer, BigDecimal totalAmount) {
        return customerService.calculateTierDiscount(customer, totalAmount);
    }

    /**
     * Lấy tên hạng hiển thị (VD: "Vàng", "Bạc", "Thường").
     */
    public String getTierDisplayName(Customer customer) {
        if (customer == null) return MemberTier.REGULAR.getDisplayName();
        return MemberTier.fromString(customer.getMemberTier()).getDisplayName();
    }

    /**
     * Lấy % giảm giá của hạng hiện tại.
     */
    public BigDecimal getTierDiscountPercent(Customer customer) {
        if (customer == null) return BigDecimal.ZERO;
        return MemberTier.fromString(customer.getMemberTier()).getDiscountPercent();
    }

    // ===== Tích điểm thưởng =====

    /**
     * Tích điểm & cập nhật hạng sau khi thanh toán.
     * Nếu KM là exclusive → không tích điểm.
     */
    public void processRewardPoints(Customer customer, Invoice invoice, BigDecimal finalAmount,
                                     boolean isExclusivePromotion) {
        if (customer == null || finalAmount == null) return;

        if (!isExclusivePromotion) {
            // Tích điểm = 5% finalAmount
            int earnedPoints = MemberTier.calculateEarnedPoints(finalAmount);
            customerService.addRewardPoints(customer, finalAmount);
            pointService.earnPoints(customer, invoice, earnedPoints);
        } else {
            // KM exclusive → chỉ cộng totalSpent, không tích điểm
            customer.setTotalSpent(customer.getTotalSpent().add(finalAmount));
            customerService.updateMemberTier(customer);
            customerService.updateCustomer(customer);
        }
    }
}
