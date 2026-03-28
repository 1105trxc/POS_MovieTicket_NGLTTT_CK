package com.cinema.management.service;

import com.cinema.management.model.dto.SeatStatusDto;

import java.util.List;

/**
 * Interface nghiệp vụ cho luồng Bán vé (POS).
 * Bao gồm: hiển thị sơ đồ ghế, khóa ghế, nhả ghế, và lấy trạng thái real-time.
 * Logic thanh toán & lưu Invoice thuộc Module 3 (IInvoiceService).
 */
public interface IBookingService {

    /**
     * Tổng hợp trạng thái tất cả ghế của 1 suất chiếu.
     * Kết hợp: Seat + SeatLock (còn hạn) + BookingSeat.
     *
     * @param showTimeId  suất chiếu cần xem
     * @param currentUserId user đang thao tác (để phân biệt ghế mình chọn)
     * @return danh sách SeatStatusDto, sắp xếp theo hàng rồi số ghế
     */
    List<SeatStatusDto> getSeatStatuses(String showTimeId, String currentUserId);

    /**
     * Khóa 1 ghế trong 15 phút (BR-04, BR-03).
     * Kiểm tra race condition: nếu ghế đã bị khóa/đặt → ném exception.
     *
     * @throws IllegalStateException nếu ghế đã bị khóa hoặc đã bán
     */
    void lockSeat(String showTimeId, String seatId, String userId);

    /**
     * Nhả ghế mà user đang giữ (click bỏ chọn).
     */
    void unlockSeat(String showTimeId, String seatId, String userId);

    /**
     * Nhả toàn bộ ghế user đang giữ trong 1 suất chiếu (hủy booking).
     */
    void unlockAllSeatsForUser(String showTimeId, String userId);

    /**
     * Kiểm tra còn bao nhiêu giây trước khi lock của user hết hạn.
     * Trả về giây còn lại của lock sớm nhất sắp hết hạn.
     * -1 nếu user không giữ ghế nào.
     */
    long getSecondsUntilExpiry(String showTimeId, String userId);
}