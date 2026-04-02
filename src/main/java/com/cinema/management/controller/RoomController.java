package com.cinema.management.controller;

import com.cinema.management.model.entity.Room;
import com.cinema.management.repository.AuditLogRepository;
import com.cinema.management.repository.RoomRepository;
import com.cinema.management.service.IRoomService;
import com.cinema.management.service.impl.AuditLogServiceImpl;
import com.cinema.management.service.impl.RoomServiceImpl;

import java.util.List;
import java.util.Optional;

/**
 * Controller cho Quản lý Phòng chiếu.
 * Nhận input từ View, gọi Service, trả kết quả về View.
 * Không chứa business logic hay DB calls.
 */
public class RoomController {

    private final IRoomService roomService;

    public RoomController() {
        this.roomService = new RoomServiceImpl(
                new RoomRepository(),
                new AuditLogServiceImpl(new AuditLogRepository()));
    }

    public RoomController(IRoomService roomService) {
        this.roomService = roomService;
    }

    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    public Optional<Room> getRoomById(String roomId) {
        return roomService.getRoomById(roomId);
    }

    /**
     * @return Room vừa tạo, hoặc ném IllegalArgumentException nếu validation thất bại.
     */
    public Room addRoom(String roomName, int capacity) {
        return roomService.addRoom(roomName, capacity);
    }

    /**
     * @return Room sau khi cập nhật.
     */
    public Room updateRoom(String roomId, String roomName, int capacity) {
        return roomService.updateRoom(roomId, roomName, capacity);
    }

    /**
     * @throws IllegalStateException nếu phòng đang có suất chiếu liên kết.
     */
    public void deleteRoom(String roomId) {
        roomService.deleteRoom(roomId);
    }
}
