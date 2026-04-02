package com.cinema.management.controller;

import com.cinema.management.model.dto.ShiftReportSummaryDto;
import com.cinema.management.model.entity.ShiftReport;
import com.cinema.management.repository.AuditLogRepository;
import com.cinema.management.service.IShiftReportService;
import com.cinema.management.service.impl.AuditLogServiceImpl;
import com.cinema.management.service.impl.ShiftReportServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ShiftReportController {

    private final IShiftReportService shiftReportService;

    public ShiftReportController() {
        this.shiftReportService = new ShiftReportServiceImpl(
                new AuditLogServiceImpl(new AuditLogRepository()));
    }

    public ShiftReportSummaryDto getCurrentShiftSummary(String staffUserId, LocalDateTime shiftStart, BigDecimal openingCash) {
        return shiftReportService.getCurrentShiftSummary(staffUserId, shiftStart, openingCash);
    }

    public ShiftReport closeShift(String staffUserId, LocalDateTime shiftStart, BigDecimal openingCash, BigDecimal actualCash) {
        return shiftReportService.closeShift(staffUserId, shiftStart, openingCash, actualCash);
    }
    
    public List<ShiftReport> getAllShiftReports() {
        return shiftReportService.getAllShiftReports();
    }

    public ShiftReport approveShiftReport(String shiftReportId, String managerId, String notes) {
        return shiftReportService.approveShiftReport(shiftReportId, managerId, notes);
    }

    public ShiftReport lockShiftReport(String shiftReportId, String managerId) {
        return shiftReportService.lockShiftReport(shiftReportId, managerId);
    }
}