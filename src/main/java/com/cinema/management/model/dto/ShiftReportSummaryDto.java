package com.cinema.management.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ShiftReportSummaryDto {
    private final String staffId;
    private final String staffName;
    private final LocalDateTime shiftStart;
    private final LocalDateTime shiftEnd;
    private final BigDecimal openingCash;
    private final BigDecimal cashRevenue;
    private final BigDecimal transferRevenue;
    private final BigDecimal cardRevenue;
    private final BigDecimal totalRevenue;
    private final BigDecimal expectedCash;
}
