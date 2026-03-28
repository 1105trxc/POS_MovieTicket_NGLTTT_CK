package com.cinema.management.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO tóm tắt hoá đơn – dùng để hiển thị trên CheckoutPanel và in ấn.
 */
@Getter
@Builder
public class InvoiceDto {

    private final String        invoiceId;
    private final LocalDateTime createdAt;
    private final String        staffName;
    private final String        customerName;
    private final String        customerPhone;

    /** Danh sách vé (1 vé / ghế). */
    private final List<TicketDto> tickets;

    /** Danh sách F&B: "Bắp rang lớn x2 = 60.000 VNĐ". */
    private final List<String>    fbLines;

    private final BigDecimal seatTotal;
    private final BigDecimal fbTotal;
    private final BigDecimal promotionDiscount;
    private final BigDecimal pointDiscount;
    /** Tổng tiền phải trả sau giảm giá. */
    private final BigDecimal grandTotal;

    private final String  promoCode;
    private final int     earnedPoints;
    private final String  paymentMethod;
}

