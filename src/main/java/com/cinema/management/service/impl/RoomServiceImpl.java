package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Room;
import com.cinema.management.repository.RoomRepository;
import com.cinema.management.service.IAuditLogService;
import com.cinema.management.service.IRoomService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Trien khai nghiep vu quan ly phong chieu.
 * Chua business logic va validation.
 */
public class RoomServiceImpl implements IRoomService {

    private final RoomRepository roomRepository;
    private final IAuditLogService auditLogService;

    public RoomServiceImpl() {
        this.roomRepository = new RoomRepository();
        this.auditLogService = null;
    }

    public RoomServiceImpl(RoomRepository roomRepository, IAuditLogService auditLogService) {
        this.roomRepository = roomRepository;
        this.auditLogService = auditLogService;
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
            throw new IllegalArgumentException("Ten phong '" + roomName + "' da ton tai.");
        }
        Room room = Room.builder()
                .roomId(UUID.randomUUID().toString())
                .roomName(roomName.trim())
                .capacity(capacity)
                .build();
        Room saved = roomRepository.save(room);

        if (auditLogService != null) {
            auditLogService.logAction("CREATE", "Room", "RoomName",
                    "N/A", roomName.trim());
        }
        return saved;
    }

    @Override
    public Room updateRoom(String roomId, String roomName, int capacity) {
        validateRoomName(roomName);
        validateCapacity(capacity);
        Room existing = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phong chieu khong ton tai: " + roomId));

        // Lưu dữ liệu cũ trước khi cập nhật
        String oldName = existing.getRoomName();
        int oldCapacity = existing.getCapacity();

        // Chi kiem tra trung ten neu ten thay doi
        if (!existing.getRoomName().equalsIgnoreCase(roomName.trim())
                && roomRepository.existsByRoomName(roomName.trim())) {
            throw new IllegalArgumentException("Ten phong '" + roomName + "' da ton tai.");
        }
        existing.setRoomName(roomName.trim());
        existing.setCapacity(capacity);
        Room saved = roomRepository.save(existing);

        // Ghi log khi tên phòng thay đổi
        if (auditLogService != null && !oldName.equals(roomName.trim())) {
            auditLogService.logAction("UPDATE", "Room", "RoomName",
                    oldName, roomName.trim());
        }
        // Ghi log khi sức chứa thay đổi
        if (auditLogService != null && oldCapacity != capacity) {
            auditLogService.logAction("UPDATE", "Room", "Capacity",
                    String.valueOf(oldCapacity), String.valueOf(capacity));
        }

        return saved;
    }

    @Override
    public void deleteRoom(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phong chieu khong ton tai: " + roomId));

        if (roomRepository.hasActiveShowTimes(roomId)) {
            throw new IllegalStateException("Khong the xoa phong dang hoat dong.");
        }

        String roomName = room.getRoomName();
        roomRepository.deleteById(roomId);

        if (auditLogService != null) {
            auditLogService.logAction("DELETE", "Room", "RoomName",
                    roomName, "Đã xóa");
        }
    }

    // Validation helpers

    private void validateRoomName(String roomName) {
        if (roomName == null || roomName.trim().isEmpty()) {
            throw new IllegalArgumentException("Ten phong khong duoc de trong.");
        }
        if (roomName.trim().length() > 100) {
            throw new IllegalArgumentException("Ten phong khong duoc vuot qua 100 ky tu.");
        }
    }

    private void validateCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Suc chua phai lon hon 0.");
        }
        if (capacity > 1000) {
            throw new IllegalArgumentException("Suc chua khong duoc vuot qua 1000 ghe.");
        }
    }
}
