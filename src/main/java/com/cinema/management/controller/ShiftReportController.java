package com.cinema.management.controller;

import com.cinema.management.model.dto.ShiftReportSummaryDto;
import com.cinema.management.model.entity.ShiftReport;
import com.cinema.management.service.IShiftReportService;
import com.cinema.management.service.impl.ShiftReportServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShiftReportController {

    private final IShiftReportService shiftReportService;

    public ShiftReportController() {
        this.shiftReportService = new ShiftReportServiceImpl();
    }

    public ShiftReportSummaryDto getCurrentShiftSummary(String staffUserId, LocalDateTime shiftStart, BigDecimal openingCash) {
        return shiftReportService.getCurrentShiftSummary(staffUserId, shiftStart, openingCash);
    }

    public ShiftReport closeShift(String staffUserId, LocalDateTime shiftStart, BigDecimal openingCash, BigDecimal actualCash) {
        return shiftReportService.closeShift(staffUserId, shiftStart, openingCash, actualCash);
    }
}
