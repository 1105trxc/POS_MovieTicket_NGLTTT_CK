package com.cinema.management.service;

import com.cinema.management.model.entity.Customer;
import com.cinema.management.model.entity.Invoice;
import com.cinema.management.model.entity.PointHistory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface nghiệp vụ tích điểm và đổi điểm (FR-CR-01, FR-CR-02, BR-01).
 */
public interface IPointService {

    /**
     * <<<<<<< HEAD
     * Tính số điểm thưởng sẽ được cộng sau giao dịch.
     * Quy tắc: 5% giá trị hoá đơn (làm tròn xuống).
     */
    int calculateEarnedPoints(BigDecimal totalAmount);

    /**
     * Tính số tiền giảm từ điểm thưởng (1 điểm = 1 VNĐ, giới hạn tối đa 50% hoá đơn).
     *
     * @param customer   khách hàng
     * @param usedPoints số điểm muốn dùng
     * @param subTotal   tổng tiền trước khi dùng điểm
     * @throws IllegalArgumentException nếu usedPoints > customer.rewardPoints
     */
    BigDecimal calculatePointDiscount(Customer customer, int usedPoints, BigDecimal subTotal);

    /**
     * Cộng điểm và cập nhật TotalSpent, tự động nâng hạng (FR-CR-02).
     *
     * @param customer     khách hàng
     * @param invoice      hoá đơn vừa thanh toán
     * @param earnedPoints điểm cộng thêm
     */
    void addPoints(Customer customer, Invoice invoice, int earnedPoints);

    /**
     * Trừ điểm khi khách dùng điểm đổi giảm giá.
     *
     * @throws IllegalArgumentException nếu không đủ điểm
     */
    void redeemPoints(Customer customer, Invoice invoice, int usedPoints);

    void earnPoints(Customer customer, Invoice invoice, int points);

    List<PointHistory> getPointHistory(String customerId);
}

