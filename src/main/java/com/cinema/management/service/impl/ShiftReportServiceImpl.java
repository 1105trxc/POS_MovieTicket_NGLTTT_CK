package com.cinema.management.service.impl;

import com.cinema.management.model.dto.ShiftReportSummaryDto;
import com.cinema.management.model.entity.Payment;
import com.cinema.management.model.entity.ShiftReport;
import com.cinema.management.model.entity.User;
import com.cinema.management.repository.PaymentRepository;
import com.cinema.management.repository.ShiftReportRepository;
import com.cinema.management.repository.UserRepository;
import com.cinema.management.service.IShiftReportService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ShiftReportServiceImpl implements IShiftReportService {

    private final PaymentRepository paymentRepository;
    private final ShiftReportRepository shiftReportRepository;
    private final UserRepository userRepository;

    public ShiftReportServiceImpl() {
        this.paymentRepository = new PaymentRepository();
        this.shiftReportRepository = new ShiftReportRepository();
        this.userRepository = new UserRepository();
    }

    @Override
    public ShiftReportSummaryDto getCurrentShiftSummary(String staffUserId, LocalDateTime shiftStart, BigDecimal openingCash) {
        LocalDateTime now = LocalDateTime.now();
        List<Payment> payments = paymentRepository.findSuccessfulByStaffAndPeriod(staffUserId, shiftStart, now);

        BigDecimal cashRevenue = BigDecimal.ZERO;
        BigDecimal transferRevenue = BigDecimal.ZERO;
        BigDecimal cardRevenue = BigDecimal.ZERO;

        for (Payment p : payments) {
            BigDecimal amount = p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO;
            String method = p.getPaymentMethod() != null ? p.getPaymentMethod().toUpperCase() : "";
            if ("CASH".equals(method)) {
                cashRevenue = cashRevenue.add(amount);
            } else if ("QR".equals(method) || "TRANSFER".equals(method)) {
                transferRevenue = transferRevenue.add(amount);
            } else if ("CARD".equals(method)) {
                cardRevenue = cardRevenue.add(amount);
            } else {
                transferRevenue = transferRevenue.add(amount);
            }
        }

        BigDecimal totalRevenue = cashRevenue.add(transferRevenue).add(cardRevenue);
        BigDecimal expectedCash = (openingCash != null ? openingCash : BigDecimal.ZERO).add(cashRevenue);

        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new IllegalArgumentException("Nhan vien khong ton tai."));

        return ShiftReportSummaryDto.builder()
                .staffId(staffUserId)
                .staffName(staff.getFullName() != null ? staff.getFullName() : staff.getUsername())
                .shiftStart(shiftStart)
                .shiftEnd(now)
                .openingCash(openingCash != null ? openingCash : BigDecimal.ZERO)
                .cashRevenue(cashRevenue)
                .transferRevenue(transferRevenue)
                .cardRevenue(cardRevenue)
                .totalRevenue(totalRevenue)
                .expectedCash(expectedCash)
                .build();
    }

    @Override
    public ShiftReport closeShift(String staffUserId, LocalDateTime shiftStart, BigDecimal openingCash, BigDecimal actualCash) {
        if (actualCash == null || actualCash.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tien mat thuc te khong hop le.");
        }

        ShiftReportSummaryDto summary = getCurrentShiftSummary(staffUserId, shiftStart, openingCash);
        BigDecimal discrepancy = actualCash.subtract(summary.getExpectedCash());
        BigDecimal standardCash = summary.getOpeningCash() != null ? summary.getOpeningCash() : BigDecimal.ZERO;
        BigDecimal remittedCash = actualCash.subtract(standardCash);
        if (remittedCash.compareTo(BigDecimal.ZERO) < 0) {
            remittedCash = BigDecimal.ZERO;
        }
        BigDecimal carryOverCash = actualCash.subtract(remittedCash);

        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new IllegalArgumentException("Nhan vien khong ton tai."));

        ShiftReport report = ShiftReport.builder()
                .shiftReportId("SR-" + UUID.randomUUID())
                .user(staff)
                .shiftStart(summary.getShiftStart())
                .shiftEnd(summary.getShiftEnd())
                .openingCash(summary.getOpeningCash())
                .cashRevenue(summary.getCashRevenue())
                .transferRevenue(summary.getTransferRevenue())
                .cardRevenue(summary.getCardRevenue())
                .totalRevenue(summary.getTotalRevenue())
                .expectedCash(summary.getExpectedCash())
                .actualCash(actualCash)
                .discrepancy(discrepancy)
                .remittedCash(remittedCash)
                .carryOverCash(carryOverCash)
                .createdAt(LocalDateTime.now())
                .status("PENDING")
                .build();

        return shiftReportRepository.save(report);
    }

    @Override
    public List<ShiftReport> getAllShiftReports() {
        return shiftReportRepository.findAll();
    }

    @Override
    public List<ShiftReport> getShiftReportsByStatus(String status) {
        return shiftReportRepository.findByStatus(status);
    }

    @Override
    public ShiftReport approveShiftReport(String shiftReportId, String managerId, String notes) {
        ShiftReport report = shiftReportRepository.findById(shiftReportId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay bao cao ca."));

        if ("LOCKED".equals(report.getStatus())) {
            throw new IllegalStateException("Bao cao da bi khoa, khong the duyet.");
        }

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Nguoi quan ly khong ton tai."));

        report.setStatus("APPROVED");
        report.setApprovedBy(manager);
        report.setApprovedAt(LocalDateTime.now());
        if (notes != null && !notes.trim().isEmpty()) {
            report.setNotes(notes);
        }

        return shiftReportRepository.save(report);
    }

    @Override
    public ShiftReport lockShiftReport(String shiftReportId, String managerId) {
        ShiftReport report = shiftReportRepository.findById(shiftReportId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay bao cao ca."));

        if (!"APPROVED".equals(report.getStatus())) {
            throw new IllegalStateException("Chi co the khoa bao cao da duyet.");
        }

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Nguoi quan ly khong ton tai."));

        report.setStatus("LOCKED");
        return shiftReportRepository.save(report);
    }
}
