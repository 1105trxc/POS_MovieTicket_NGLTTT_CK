package com.cinema.management.service.impl;


import com.cinema.management.model.dto.PaymentDashboardDto;
import com.cinema.management.model.dto.PaymentManagementRowDto;
import com.cinema.management.model.entity.Payment;
import com.cinema.management.model.entity.ShiftReport;
import com.cinema.management.model.entity.User;
import com.cinema.management.repository.PaymentRepository;
import com.cinema.management.repository.ShiftReportRepository;
import com.cinema.management.repository.UserRepository;
import com.cinema.management.service.IPaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final ShiftReportRepository shiftReportRepository;
    private final UserRepository userRepository;

    public PaymentServiceImpl() {
        this.paymentRepository = new PaymentRepository();
        this.shiftReportRepository = new ShiftReportRepository();
        this.userRepository = new UserRepository();
    }

    @Override
    public List<PaymentManagementRowDto> searchPayments(LocalDate fromDate,
                                                        LocalDate toDate,
                                                        String shiftReportId,
                                                        String staffUserId,
                                                        String paymentMethod) {
        LocalDateTime fromTime;
        LocalDateTime toTime;
        String resolvedStaffId = staffUserId;

        if (shiftReportId != null && !shiftReportId.isBlank() && !"ALL".equalsIgnoreCase(shiftReportId)) {
            ShiftReport shift = shiftReportRepository.findById(shiftReportId)
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay ca lam viec."));
            fromTime = shift.getShiftStart();
            toTime = shift.getShiftEnd() != null ? shift.getShiftEnd() : LocalDateTime.now();
            resolvedStaffId = shift.getUser() != null ? shift.getUser().getUserId() : resolvedStaffId;
        } else {
            LocalDate from = fromDate != null ? fromDate : LocalDate.now();
            LocalDate to = toDate != null ? toDate : from;
            fromTime = from.atStartOfDay();
            toTime = LocalDateTime.of(to, LocalTime.MAX);
        }

        List<Payment> payments = paymentRepository.searchPayments(fromTime, toTime, resolvedStaffId, paymentMethod);

        return payments.stream().map(p -> {
            String cashierId = p.getInvoice() != null && p.getInvoice().getUser() != null
                    ? p.getInvoice().getUser().getUserId() : "";
            String cashierName = p.getInvoice() != null && p.getInvoice().getUser() != null
                    ? nonNull(p.getInvoice().getUser().getFullName(), p.getInvoice().getUser().getUsername()) : "";
            String customerName = p.getInvoice() != null && p.getInvoice().getCustomer() != null
                    ? nonNull(p.getInvoice().getCustomer().getFullName(), "Khach le") : "Khach le";
            String invoiceId = p.getInvoice() != null ? p.getInvoice().getInvoiceId() : "";

            return PaymentManagementRowDto.builder()
                    .paymentId(p.getPaymentId())
                    .invoiceId(invoiceId)
                    .paidAt(p.getCreatedAt())
                    .customerName(customerName)
                    .amount(p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                    .paymentMethod(normalizeMethod(p.getPaymentMethod()))
                    .cashierId(cashierId)
                    .cashierName(cashierName)
                    .transactionCode(p.getTransactionCode())
                    .status(p.getStatus())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public PaymentDashboardDto summarize(List<PaymentManagementRowDto> rows) {
        BigDecimal cashAmount = BigDecimal.ZERO;
        BigDecimal qrAmount = BigDecimal.ZERO;
        BigDecimal cardAmount = BigDecimal.ZERO;
        int cashCount = 0;
        int qrCount = 0;
        int cardCount = 0;

        for (PaymentManagementRowDto row : rows) {
            BigDecimal amount = row.getAmount() != null ? row.getAmount() : BigDecimal.ZERO;
            String method = row.getPaymentMethod() != null ? row.getPaymentMethod().toUpperCase(Locale.ROOT) : "";
            if ("CASH".equals(method)) {
                cashAmount = cashAmount.add(amount);
                cashCount++;
            } else if ("QR".equals(method) || "TRANSFER".equals(method)) {
                qrAmount = qrAmount.add(amount);
                qrCount++;
            } else if ("CARD".equals(method)) {
                cardAmount = cardAmount.add(amount);
                cardCount++;
            } else {
                qrAmount = qrAmount.add(amount);
                qrCount++;
            }
        }
        BigDecimal totalAmount = cashAmount.add(qrAmount).add(cardAmount);

        return PaymentDashboardDto.builder()
                .totalAmount(totalAmount)
                .cashAmount(cashAmount)
                .qrAmount(qrAmount)
                .cardAmount(cardAmount)
                .totalCount(rows.size())
                .cashCount(cashCount)
                .qrCount(qrCount)
                .cardCount(cardCount)
                .build();
    }

    @Override
    public List<ShiftReport> getAllShifts() {
        return shiftReportRepository.findAll();
    }

    @Override
    public List<User> getAllCashiers() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && !"R01".equalsIgnoreCase(u.getRole().getRoleId()))
                .sorted(Comparator.comparing(u -> nonNull(u.getFullName(), "")))
                .collect(Collectors.toList());
    }

    private String normalizeMethod(String method) {
        if (method == null) return "UNKNOWN";
        String m = method.toUpperCase(Locale.ROOT);
        if ("TRANSFER".equals(m)) return "QR";
        return m;
    }

    private String nonNull(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

}
