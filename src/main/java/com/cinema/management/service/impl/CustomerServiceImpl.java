package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Customer;
import com.cinema.management.model.entity.MemberTier;
import com.cinema.management.repository.CustomerRepository;
import com.cinema.management.service.IAuditLogService;
import com.cinema.management.service.ICustomerService;

import java.math.BigDecimal;
import java.util.List;

public class CustomerServiceImpl implements ICustomerService {

    private final CustomerRepository customerRepo;
    private final IAuditLogService auditLogService;

    public CustomerServiceImpl(CustomerRepository customerRepo, IAuditLogService auditLogService) {
        this.customerRepo = customerRepo;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }

    @Override
    public Customer getCustomerById(String id) {
        return customerRepo.findById(id);
    }

    @Override
    public Customer findByPhone(String phone) {
        return customerRepo.findByPhone(phone);
    }

    @Override
    public void createCustomer(Customer customer) {
        // Mặc định hạng Thường cho khách mới
        if (customer.getMemberTier() == null || customer.getMemberTier().isBlank()) {
            customer.setMemberTier(MemberTier.REGULAR.name());
        }
        customerRepo.save(customer);
        auditLogService.logAction("CREATE", "Customer",
                "ID: " + customer.getCustomerId(), "None", customer.getFullName());
    }

    @Override
    public void updateCustomer(Customer customer) {
        customerRepo.update(customer);
    }

    /**
     * Tính giảm giá theo hạng thành viên.
     * Parse memberTier từ DB → dùng MemberTier enum để tính.
     */
    @Override
    public BigDecimal calculateTierDiscount(Customer customer, BigDecimal totalAmount) {
        if (customer == null || totalAmount == null) {
            return BigDecimal.ZERO;
        }
        MemberTier tier = MemberTier.fromString(customer.getMemberTier());
        return tier.calculateDiscount(totalAmount);
    }

    /**
     * Tích điểm thưởng + cập nhật tổng chi tiêu + kiểm tra nâng hạng.
     * Điểm = 5% * finalAmount (làm tròn xuống).
     */
    @Override
    public void addRewardPoints(Customer customer, BigDecimal finalAmount) {
        if (customer == null || finalAmount == null || finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        int earnedPoints = MemberTier.calculateEarnedPoints(finalAmount);

        // Cộng điểm thưởng
        customer.setRewardPoints(customer.getRewardPoints() + earnedPoints);

        // Cộng tổng chi tiêu
        customer.setTotalSpent(customer.getTotalSpent().add(finalAmount));

        // Kiểm tra và cập nhật hạng
        updateMemberTier(customer);

        // Lưu vào DB
        customerRepo.update(customer);
    }

    /**
     * Tự động nâng/giữ hạng dựa trên totalSpent.
     */
    @Override
    public void updateMemberTier(Customer customer) {
        if (customer == null) return;

        MemberTier newTier = MemberTier.determineTier(customer.getTotalSpent());
        String oldTier = customer.getMemberTier();
        String newTierName = newTier.name();

        if (!newTierName.equals(oldTier)) {
            customer.setMemberTier(newTierName);
            auditLogService.logAction("UPDATE", "Customer",
                    "MemberTier", oldTier, newTierName);
        }
    }
}
