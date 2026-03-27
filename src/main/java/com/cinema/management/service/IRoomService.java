package com.cinema.management.service;

import com.cinema.management.model.entity.Room;

import java.util.List;
import java.util.Optional;

/**
 * Interface định nghĩa các nghiệp vụ quản lý phòng chiếu.
 * Tuân theo nguyên lý Dependency Inversion (SOLID).
 */
public interface IRoomService {

    List<Room> getAllRooms();

    Optional<Room> getRoomById(String roomId);

    /**
     * Thêm phòng mới. Validate tên phòng không trùng và capacity > 0.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    Room addRoom(String roomName, int capacity);

    /**
     * Cập nhật thông tin phòng.
     * @throws IllegalArgumentException nếu phòng không tồn tại hoặc dữ liệu không hợp lệ
     */
    Room updateRoom(String roomId, String roomName, int capacity);

    /**
     * Xóa phòng. Chỉ xóa được nếu phòng chưa có suất chiếu liên kết.
     * @throws IllegalStateException nếu phòng đang được sử dụng
     */
    void deleteRoom(String roomId);
}
