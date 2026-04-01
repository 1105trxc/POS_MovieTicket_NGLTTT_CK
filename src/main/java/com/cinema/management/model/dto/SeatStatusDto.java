package com.cinema.management.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO chứa trạng thái hiển thị của 1 ghế trên sơ đồ ghế.
 * Được tổng hợp từ Seat + SeatLock + BookingSeat.
 */
@Getter
@Builder
@AllArgsConstructor
public class SeatStatusDto {

    public enum Status {
        AVAILABLE,   // Trống – có thể chọn
        SELECTED,    // Đang được chính user này chọn (highlight)
        LOCKED,      // Đang bị user khác khóa (15 phút)
        BOOKED       // Đã bán
    }

    private final String     seatId;
    private final String     rowChar;
    private final int        seatNumber;
    private final String     seatTypeName;
    /** Giá hiện tại của loại ghế – dùng để tính tổng trước khi thanh toán. */
    private final BigDecimal basePrice;
    private final Status     status;

    public String getLabel() {
        return rowChar + seatNumber;
    }
}