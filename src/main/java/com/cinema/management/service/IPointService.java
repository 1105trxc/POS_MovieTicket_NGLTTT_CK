package com.cinema.management.service;

import com.cinema.management.model.entity.Customer;
import com.cinema.management.model.entity.Invoice;
import com.cinema.management.model.entity.PointHistory;

import java.util.List;

public interface IPointService {

    /**
     * Ghi lịch sử tích điểm (EARN).
     */
    void earnPoints(Customer customer, Invoice invoice, int points);

    /**
     * Lấy lịch sử tích/dùng điểm của 1 khách hàng.
     */
    List<PointHistory> getPointHistory(String customerId);
}
