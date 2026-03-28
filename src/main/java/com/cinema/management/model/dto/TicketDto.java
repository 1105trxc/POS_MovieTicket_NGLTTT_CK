package com.cinema.management.model.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO đại diện cho 1 vé xem phim – dùng để in vé (FR-ST-04).
 */
@Getter
@Builder
public class TicketDto {

    private final String        invoiceId;
    private final String        movieTitle;
    private final String        roomName;
    private final LocalDateTime showTime;
    private final String        seatLabel;    // VD: "A3"
    private final String        seatTypeName; // VD: "VIP"
    /** Giá snapshot đã lưu vào BookingSeat – không thay đổi sau khi in. */
    private final BigDecimal    price;
    private final String        customerName;
}

