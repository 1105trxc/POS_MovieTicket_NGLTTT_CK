package com.cinema.management.service;

import com.cinema.management.model.entity.Customer;

import java.math.BigDecimal;
import java.util.List;

public interface ICustomerService {

    List<Customer> getAllCustomers();

    Customer getCustomerById(String id);

    /**
     * Tìm khách hàng theo SĐT — dùng khi checkout nhập SĐT.
     */
    Customer findByPhone(String phone);

    void createCustomer(Customer customer);

    void updateCustomer(Customer customer);

    /**
     * Tính giảm giá hạng thành viên cho 1 tổng tiền.
     * VD: Hạng Vàng → totalAmount * 10%.
     */
    BigDecimal calculateTierDiscount(Customer customer, BigDecimal totalAmount);

    /**
     * Tích điểm thưởng cho khách = 5% * finalAmount.
     * Cập nhật rewardPoints + totalSpent → kiểm tra nâng hạng.
     */
    void addRewardPoints(Customer customer, BigDecimal finalAmount);

    /**
     * Cập nhật hạng thành viên dựa trên tổng chi tiêu hiện tại.
     */
    void updateMemberTier(Customer customer);
}
