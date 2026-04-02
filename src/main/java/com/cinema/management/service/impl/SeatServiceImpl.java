package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Room;
import com.cinema.management.model.entity.Seat;
import com.cinema.management.model.entity.SeatType;
import com.cinema.management.repository.RoomRepository;
import com.cinema.management.repository.SeatRepository;
import com.cinema.management.repository.SeatTypeRepository;
import com.cinema.management.service.IAuditLogService;
import com.cinema.management.service.ISeatService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Trien khai nghiep vu quan ly ghe ngoi.
 */
public class SeatServiceImpl implements ISeatService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final IAuditLogService auditLogService;

    public SeatServiceImpl() {
        this.seatRepository = new SeatRepository();
        this.roomRepository = new RoomRepository();
        this.seatTypeRepository = new SeatTypeRepository();
        this.auditLogService = null;
    }

    public SeatServiceImpl(SeatRepository seatRepository, RoomRepository roomRepository,
                           SeatTypeRepository seatTypeRepository, IAuditLogService auditLogService) {
        this.seatRepository = seatRepository;
        this.roomRepository = roomRepository;
        this.seatTypeRepository = seatTypeRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<Seat> getSeatsByRoom(String roomId) {
        return seatRepository.findByRoomId(roomId);
    }

    @Override
    public Optional<Seat> getSeatById(String seatId) {
        return seatRepository.findById(seatId);
    }

    @Override
    public Seat addSeat(String roomId, String seatTypeId, String rowChar, int seatNumber) {
        validateRowChar(rowChar);
        validateSeatNumber(seatNumber);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phong chieu khong ton tai: " + roomId));
        SeatType seatType = seatTypeRepository.findById(seatTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Loai ghe khong ton tai: " + seatTypeId));

        if (seatRepository.existsByRoomAndPosition(roomId, rowChar.toUpperCase(), seatNumber)) {
            throw new IllegalArgumentException(
                    "Vi tri ghe " + rowChar.toUpperCase() + seatNumber + " da ton tai trong phong nay.");
        }

        // Kiem tra tong so ghe khong vuot suc chua phong
        long currentCount = seatRepository.findByRoomId(roomId).size();
        if (currentCount >= room.getCapacity()) {
            throw new IllegalStateException(
                    "Phong da du suc chua (" + room.getCapacity() + " ghe). Khong the them ghe moi.");
        }

        Seat seat = Seat.builder()
                .seatId(UUID.randomUUID().toString())
                .room(room)
                .seatType(seatType)
                .rowChar(rowChar.toUpperCase())
                .seatNumber(seatNumber)
                .build();
        Seat saved = seatRepository.save(seat);

        if (auditLogService != null) {
            auditLogService.logAction("CREATE", "Seat", "Position",
                    "N/A", rowChar.toUpperCase() + seatNumber + " (" + seatType.getTypeName() + ")");
        }

        return saved;
    }

    @Override
    public Seat updateSeatType(String seatId, String seatTypeId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Ghe khong ton tai: " + seatId));
        SeatType oldType = seat.getSeatType();
        String oldTypeName = oldType != null ? oldType.getTypeName() : "N/A";

        SeatType seatType = seatTypeRepository.findById(seatTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Loai ghe khong ton tai: " + seatTypeId));
        seat.setSeatType(seatType);
        Seat saved = seatRepository.save(seat);

        if (auditLogService != null) {
            auditLogService.logAction("UPDATE", "Seat", "SeatType",
                    oldTypeName, seatType.getTypeName());
        }

        return saved;
    }

    @Override
    public void deleteSeat(String seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Ghe khong ton tai: " + seatId));

        if (seatRepository.hasAnyBookings(seatId)) {
            throw new IllegalStateException("Khong the xoa ghe da co lich su dat cho.");
        }
        if (seatRepository.hasActiveLocks(seatId)) {
            throw new IllegalStateException("Khong the xoa ghe dang bi khoa.");
        }

        String seatInfo = seat.getRowChar() + seat.getSeatNumber();
        seatRepository.deleteById(seatId);

        if (auditLogService != null) {
            auditLogService.logAction("DELETE", "Seat", "Position",
                    seatInfo, "Đã xóa");
        }
    }

    @Override
    public void addSeatsByPattern(String roomId, String seatTypeId, String rowChar, String pattern) {
        // 1. Tách chuỗi theo dấu phẩy: "1-6, 11-16, 20" -> ["1-6", "11-16", "20"]
        String[] parts = pattern.split(",");

        // Yêu cầu EntityTransaction ở đây để rollback nếu 1 ghế bị lỗi
        for (String part : parts) {
            part = part.trim();
            if (part.contains("-")) {
                // Xử lý dải ghế
                String[] bounds = part.split("-");
                int start = Integer.parseInt(bounds[0].trim());
                int end = Integer.parseInt(bounds[1].trim());
                for (int i = start; i <= end; i++) {
                    // Tái sử dụng hàm addSeat() đã viết sẵn
                    this.addSeat(roomId, seatTypeId, rowChar, i);
                }
            } else {
                // Xử lý ghế đơn
                int seatNum = Integer.parseInt(part);
                this.addSeat(roomId, seatTypeId, rowChar, seatNum);
            }
        }
    }

    @Override
    public void cloneRoomLayout(String sourceRoomId, String targetRoomId) {
        // Lấy toàn bộ danh sách ghế của sourceRoomId từ Database
        List<Seat> sourceSeats = getSeatsByRoom(sourceRoomId);

        for (Seat s : sourceSeats) {
            // Tái sử dụng hàm addSeat để tạo từng ghế sang phòng targetRoomId
            this.addSeat(targetRoomId, s.getSeatType().getSeatTypeId(), s.getRowChar(), s.getSeatNumber());
        }
    }

    // Validation helpers

    private void validateRowChar(String rowChar) {
        if (rowChar == null || rowChar.trim().isEmpty()) {
            throw new IllegalArgumentException("Ky tu hang ghe khong duoc de trong.");
        }
        if (!rowChar.trim().matches("[A-Za-z]+")) {
            throw new IllegalArgumentException("Ky tu hang ghe chi duoc chua chu cai (A-Z).");
        }
    }

    private void validateSeatNumber(int seatNumber) {
        if (seatNumber <= 0) {
            throw new IllegalArgumentException("So thu tu ghe phai lon hon 0.");
        }
    }

}
