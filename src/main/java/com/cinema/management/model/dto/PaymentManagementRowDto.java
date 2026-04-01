package com.cinema.management.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentManagementRowDto {
    private final String paymentId;
    private final String invoiceId;
    private final LocalDateTime paidAt;
    private final String customerName;
    private final BigDecimal amount;
    private final String paymentMethod;
    private final String cashierId;
    private final String cashierName;
    private final String transactionCode;
    private final String status;
}
