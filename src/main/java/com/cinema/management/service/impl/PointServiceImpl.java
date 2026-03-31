package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Customer;
import com.cinema.management.model.entity.Invoice;
import com.cinema.management.model.entity.PointHistory;
import com.cinema.management.repository.PointHistoryRepository;
import com.cinema.management.service.IPointService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PointServiceImpl implements IPointService {

    private final PointHistoryRepository pointHistoryRepo;

    public PointServiceImpl(PointHistoryRepository pointHistoryRepo) {
        this.pointHistoryRepo = pointHistoryRepo;
    }

    /**
     * Ghi 1 bản ghi lịch sử tích điểm (EARN).
     */
    @Override
    public void earnPoints(Customer customer, Invoice invoice, int points) {
        if (customer == null || points <= 0) return;

        PointHistory history = PointHistory.builder()
                .historyId("PH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customer(customer)
                .invoice(invoice)
                .pointAmount(points)
                .transactionType("EARN")
                .description("Tích điểm từ hóa đơn " + (invoice != null ? invoice.getInvoiceId() : "N/A"))
                .createdAt(LocalDateTime.now())
                .build();

        pointHistoryRepo.save(history);
    }

    @Override
    public List<PointHistory> getPointHistory(String customerId) {
        return pointHistoryRepo.findByCustomerId(customerId);
    }
}
