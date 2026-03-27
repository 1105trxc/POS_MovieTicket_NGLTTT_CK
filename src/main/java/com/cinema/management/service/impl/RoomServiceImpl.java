package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Room;
import com.cinema.management.repository.RoomRepository;
import com.cinema.management.service.IRoomService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Triển khai nghiệp vụ quản lý phòng chiếu.
 * Chứa toàn bộ business logic và validation – tuân thủ MVC strict.
 */
public class RoomServiceImpl implements IRoomService {

    private final RoomRepository roomRepository;

    public RoomServiceImpl() {
        this.roomRepository = new RoomRepository();
    }

    // Constructor injection cho testability
    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public Optional<Room> getRoomById(String roomId) {
        return roomRepository.findById(roomId);
    }

    @Override
    public Room addRoom(String roomName, int capacity) {
        validateRoomName(roomName);
        validateCapacity(capacity);
        if (roomRepository.existsByRoomName(roomName.trim())) {
            throw new IllegalArgumentException("Tên phòng '" + roomName + "' đã tồn tại.");
        }
        Room room = Room.builder()
                .roomId(UUID.randomUUID().toString())
                .roomName(roomName.trim())
                .capacity(capacity)
                .build();
        return roomRepository.save(room);
    }

    @Override
    public Room updateRoom(String roomId, String roomName, int capacity) {
        validateRoomName(roomName);
        validateCapacity(capacity);
        Room existing = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng chiếu không tồn tại: " + roomId));

        // Chỉ kiểm tra trùng tên nếu tên thay đổi
        if (!existing.getRoomName().equalsIgnoreCase(roomName.trim())
                && roomRepository.existsByRoomName(roomName.trim())) {
            throw new IllegalArgumentException("Tên phòng '" + roomName + "' đã tồn tại.");
        }
        existing.setRoomName(roomName.trim());
        existing.setCapacity(capacity);
        return roomRepository.save(existing);
    }

    @Override
    public void deleteRoom(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng chiếu không tồn tại: " + roomId));
        // Kiểm tra phòng có suất chiếu không
        if (room.getShowTimes() != null && !room.getShowTimes().isEmpty()) {
            throw new IllegalStateException("Không thể xóa phòng đang có suất chiếu liên kết.");
        }
        roomRepository.deleteById(roomId);
    }

    // ── Validation helpers ──────────────────────────────────────────────────

    private void validateRoomName(String roomName) {
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phòng không được để trống.");
        }
        if (roomName.trim().length() > 100) {
            throw new IllegalArgumentException("Tên phòng không được vượt quá 100 ký tự.");
        }
    }

    private void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Sức chứa phải lớn hơn 0.");
        }
        if (capacity > 1000) {
            throw new IllegalArgumentException("Sức chứa không được vượt quá 1000 ghế.");
        }
    }
}
