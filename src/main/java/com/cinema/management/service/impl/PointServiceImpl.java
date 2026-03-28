package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Customer;
import com.cinema.management.model.entity.Invoice;
import com.cinema.management.model.entity.PointHistory;
import com.cinema.management.repository.CustomerRepository;
import com.cinema.management.repository.PointHistoryRepository;
import com.cinema.management.service.IPointService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Triển khai nghiệp vụ tích điểm và đổi điểm (FR-CR-01, FR-CR-02, BR-01).
 *
 * Quy tắc:
 *   - EARN: 5% giá trị hoá đơn (làm tròn xuống).
 *   - REDEEM: 1 điểm = 1 VNĐ, tối đa 50% hoá đơn.
 *   - Ngưỡng nâng hạng: VIP ≥ 2.000.000 VNĐ, Diamond ≥ 5.000.000 VNĐ (FR-CR-02).
 */
public class PointServiceImpl implements IPointService {

    private static final BigDecimal EARN_RATE          = new BigDecimal("0.05");
    private static final BigDecimal MAX_REDEEM_RATE    = new BigDecimal("0.50");
    private static final BigDecimal VIP_THRESHOLD      = new BigDecimal("2000000");
    private static final BigDecimal DIAMOND_THRESHOLD  = new BigDecimal("5000000");

    private final CustomerRepository     customerRepository;
    private final PointHistoryRepository pointHistoryRepository;

    public PointServiceImpl() {
        this.customerRepository     = new CustomerRepository();
        this.pointHistoryRepository = new PointHistoryRepository();
    }

    public PointServiceImpl(CustomerRepository customerRepository,
                             PointHistoryRepository pointHistoryRepository) {
        this.customerRepository     = customerRepository;
        this.pointHistoryRepository = pointHistoryRepository;
    }

    @Override
    public int calculateEarnedPoints(BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) return 0;
        return totalAmount.multiply(EARN_RATE).setScale(0, RoundingMode.FLOOR).intValue();
    }

    @Override
    public BigDecimal calculatePointDiscount(Customer customer, int usedPoints, BigDecimal subTotal) {
        if (usedPoints <= 0) return BigDecimal.ZERO;
        if (customer == null) throw new IllegalArgumentException("Khách hàng không tồn tại.");
        if (usedPoints > customer.getRewardPoints()) {
            throw new IllegalArgumentException(
                    "Không đủ điểm. Hiện có: " + customer.getRewardPoints() + " điểm.");
        }
        BigDecimal discount = BigDecimal.valueOf(usedPoints);
        BigDecimal maxAllowed = subTotal.multiply(MAX_REDEEM_RATE).setScale(0, RoundingMode.FLOOR);
        return discount.min(maxAllowed);
    }

    @Override
    public void addPoints(Customer customer, Invoice invoice, int earnedPoints) {
        if (customer == null || earnedPoints <= 0) return;

        customer.setRewardPoints(customer.getRewardPoints() + earnedPoints);
        customer.setTotalSpent(customer.getTotalSpent().add(invoice.getTotalAmount()));

        // Tự động nâng hạng (FR-CR-02)
        upgradeTierIfEligible(customer);
        customerRepository.save(customer);

        // Ghi lịch sử điểm
        PointHistory history = PointHistory.builder()
                .historyId(UUID.randomUUID().toString())
                .customer(customer)
                .invoice(invoice)
                .pointAmount(earnedPoints)
                .transactionType("EARN")
                .description("Tích điểm từ hoá đơn " + invoice.getInvoiceId())
                .createdAt(LocalDateTime.now())
                .build();
        pointHistoryRepository.save(history);
    }

    @Override
    public void redeemPoints(Customer customer, Invoice invoice, int usedPoints) {
        if (customer == null || usedPoints <= 0) return;
        if (usedPoints > customer.getRewardPoints()) {
            throw new IllegalArgumentException("Không đủ điểm để đổi.");
        }
        customer.setRewardPoints(customer.getRewardPoints() - usedPoints);
        customerRepository.save(customer);

        PointHistory history = PointHistory.builder()
                .historyId(UUID.randomUUID().toString())
                .customer(customer)
                .invoice(invoice)
                .pointAmount(-usedPoints)
                .transactionType("REDEEM")
                .description("Dùng " + usedPoints + " điểm giảm giá hoá đơn " + invoice.getInvoiceId())
                .createdAt(LocalDateTime.now())
                .build();
        pointHistoryRepository.save(history);
    }

    // ── Private helper ───────────────────────────────────────────────────────

    private void upgradeTierIfEligible(Customer customer) {
        BigDecimal spent = customer.getTotalSpent();
        if (spent.compareTo(DIAMOND_THRESHOLD) >= 0) {
            customer.setMemberTier("Diamond");
        } else if (spent.compareTo(VIP_THRESHOLD) >= 0) {
            customer.setMemberTier("VIP");
        }
    }
}

