package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Room;
import com.cinema.management.repository.RoomRepository;
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
            throw new IllegalArgumentException("Ten phong '" + roomName + "' da ton tai.");
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
                .orElseThrow(() -> new IllegalArgumentException("Phong chieu khong ton tai: " + roomId));

        // Chi kiem tra trung ten neu ten thay doi
        if (!existing.getRoomName().equalsIgnoreCase(roomName.trim())
                && roomRepository.existsByRoomName(roomName.trim())) {
            throw new IllegalArgumentException("Ten phong '" + roomName + "' da ton tai.");
        }
        existing.setRoomName(roomName.trim());
        existing.setCapacity(capacity);
        return roomRepository.save(existing);
    }

    @Override
    public void deleteRoom(String roomId) {
        roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phong chieu khong ton tai: " + roomId));

        if (roomRepository.hasActiveShowTimes(roomId)) {
            throw new IllegalStateException("Khong the xoa phong dang hoat dong.");
        }

        roomRepository.deleteById(roomId);
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
