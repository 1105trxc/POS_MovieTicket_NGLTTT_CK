package com.cinema.management.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PaymentDashboardDto {
    private final BigDecimal totalAmount;
    private final BigDecimal cashAmount;
    private final BigDecimal qrAmount;
    private final BigDecimal cardAmount;
    private final int totalCount;
    private final int cashCount;
    private final int qrCount;
    private final int cardCount;
}
