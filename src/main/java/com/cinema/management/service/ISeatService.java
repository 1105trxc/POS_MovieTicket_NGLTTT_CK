package com.cinema.management.service;

import com.cinema.management.model.entity.Seat;

import java.util.List;
import java.util.Optional;

/**
 * Interface định nghĩa các nghiệp vụ quản lý ghế.
 */
public interface ISeatService {

    List<Seat> getSeatsByRoom(String roomId);

    Optional<Seat> getSeatById(String seatId);

    /**
     * Thêm ghế vào phòng. Validate vị trí (row, number) không trùng trong phòng.
     *
     * @throws IllegalArgumentException nếu vị trí đã tồn tại hoặc loại ghế không hợp lệ
     */
    Seat addSeat(String roomId, String seatTypeId, String rowChar, int seatNumber);

    /**
     * Cập nhật loại ghế. Chỉ cho phép đổi SeatType, không đổi vị trí.
     *
     * @throws IllegalArgumentException nếu ghế hoặc loại ghế không tồn tại
     */
    Seat updateSeatType(String seatId, String seatTypeId);

    /**
     * Xóa ghế. Chỉ xóa được nếu ghế chưa có booking hoặc lock.
     *
     * @throws IllegalStateException nếu ghế đang được sử dụng
     */
    void deleteSeat(String seatId);

    void addSeatsByPattern(String roomId, String seatTypeId, String rowChar, String pattern);

    void cloneRoomLayout(String sourceRoomId, String targetRoomId);
}
