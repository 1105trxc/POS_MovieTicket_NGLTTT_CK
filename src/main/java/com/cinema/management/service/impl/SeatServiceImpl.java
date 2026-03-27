package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Room;
import com.cinema.management.model.entity.Seat;
import com.cinema.management.model.entity.SeatType;
import com.cinema.management.repository.RoomRepository;
import com.cinema.management.repository.SeatRepository;
import com.cinema.management.repository.SeatTypeRepository;
import com.cinema.management.service.ISeatService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Triển khai nghiệp vụ quản lý ghế ngồi.
 */
public class SeatServiceImpl implements ISeatService {

    private final SeatRepository seatRepository;
    private final RoomRepository roomRepository;
    private final SeatTypeRepository seatTypeRepository;

    public SeatServiceImpl() {
        this.seatRepository = new SeatRepository();
        this.roomRepository = new RoomRepository();
        this.seatTypeRepository = new SeatTypeRepository();
    }

    public SeatServiceImpl(SeatRepository seatRepository, RoomRepository roomRepository,
                            SeatTypeRepository seatTypeRepository) {
        this.seatRepository = seatRepository;
        this.roomRepository = roomRepository;
        this.seatTypeRepository = seatTypeRepository;
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
                .orElseThrow(() -> new IllegalArgumentException("Phòng chiếu không tồn tại: " + roomId));
        SeatType seatType = seatTypeRepository.findById(seatTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Loại ghế không tồn tại: " + seatTypeId));

        if (seatRepository.existsByRoomAndPosition(roomId, rowChar.toUpperCase(), seatNumber)) {
            throw new IllegalArgumentException(
                    "Vị trí ghế " + rowChar.toUpperCase() + seatNumber + " đã tồn tại trong phòng này.");
        }

        // Kiểm tra tổng số ghế không vượt sức chứa phòng
        long currentCount = seatRepository.findByRoomId(roomId).size();
        if (currentCount >= room.getCapacity()) {
            throw new IllegalStateException(
                    "Phòng đã đủ sức chứa (" + room.getCapacity() + " ghế). Không thể thêm ghế mới.");
        }

        Seat seat = Seat.builder()
                .seatId(UUID.randomUUID().toString())
                .room(room)
                .seatType(seatType)
                .rowChar(rowChar.toUpperCase())
                .seatNumber(seatNumber)
                .build();
        return seatRepository.save(seat);
    }

    @Override
    public Seat updateSeatType(String seatId, String seatTypeId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại: " + seatId));
        SeatType seatType = seatTypeRepository.findById(seatTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Loại ghế không tồn tại: " + seatTypeId));
        seat.setSeatType(seatType);
        return seatRepository.save(seat);
    }

    @Override
    public void deleteSeat(String seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("Ghế không tồn tại: " + seatId));
        if (seat.getBookingSeats() != null && !seat.getBookingSeats().isEmpty()) {
            throw new IllegalStateException("Không thể xóa ghế đã có lịch sử đặt chỗ.");
        }
        if (seat.getSeatLocks() != null && !seat.getSeatLocks().isEmpty()) {
            throw new IllegalStateException("Không thể xóa ghế đang bị khóa.");
        }
        seatRepository.deleteById(seatId);
    }

    // ── Validation helpers ──────────────────────────────────────────────────

    private void validateRowChar(String rowChar) {
        if (rowChar == null || rowChar.trim().isEmpty()) {
            throw new IllegalArgumentException("Ký tự hàng ghế không được để trống.");
        }
        if (!rowChar.trim().matches("[A-Za-z]+")) {
            throw new IllegalArgumentException("Ký tự hàng ghế chỉ được chứa chữ cái (A-Z).");
        }
    }

    private void validateSeatNumber(int seatNumber) {
        if (seatNumber <= 0) {
            throw new IllegalArgumentException("Số thứ tự ghế phải lớn hơn 0.");
        }
    }
}
