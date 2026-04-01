package com.cinema.management.service;

import com.cinema.management.model.dto.ShiftReportSummaryDto;
import com.cinema.management.model.entity.ShiftReport;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IShiftReportService {
    ShiftReportSummaryDto getCurrentShiftSummary(String staffUserId, LocalDateTime shiftStart, BigDecimal openingCash);

    ShiftReport closeShift(String staffUserId,
                           LocalDateTime shiftStart,
                           BigDecimal openingCash,
                           BigDecimal actualCash);
}
