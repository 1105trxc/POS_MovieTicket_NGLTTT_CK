package com.cinema.management.service;

import com.cinema.management.model.dto.ShiftReportSummaryDto;
import com.cinema.management.model.entity.ShiftReport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IShiftReportService {
    ShiftReportSummaryDto getCurrentShiftSummary(String staffUserId, LocalDateTime shiftStart, BigDecimal openingCash);

    ShiftReport closeShift(String staffUserId,
                           LocalDateTime shiftStart,
                           BigDecimal openingCash,
                           BigDecimal actualCash);

    List<ShiftReport> getAllShiftReports();
    List<ShiftReport> getShiftReportsByStatus(String status);

    ShiftReport approveShiftReport(String shiftReportId, String managerId, String notes);
    ShiftReport lockShiftReport(String shiftReportId, String managerId);
}
