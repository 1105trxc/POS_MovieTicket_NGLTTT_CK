package com.cinema.management.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO for seat status on the seat map.
 */
@Getter
@Builder
@AllArgsConstructor
public class SeatStatusDto {

    public enum Status {
        AVAILABLE,
        SELECTED,
        LOCKED,
        PROCESSING,
        BOOKED
    }

    private final String seatId;
    private final String rowChar;
    private final int seatNumber;
    private final String seatTypeName;
    private final BigDecimal basePrice;
    private final Status status;
    private final String pendingPaymentId;

    public String getLabel() {
        return rowChar + seatNumber;
    }
}
