package com.cinema.management.service.impl;

import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.dto.SeatStatusDto.Status;
import com.cinema.management.model.entity.*;
import com.cinema.management.repository.*;
import com.cinema.management.service.IBookingService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Triển khai nghiệp vụ Bán vé – luồng chọn ghế & khóa ghế.
 *
 * Business Rules tuân thủ:
 *   BR-03  Thời gian khóa dùng LocalDateTime.now() (giờ Server).
 *   BR-04  Khóa ghế tối đa MAX_LOCK_MINUTES = 15 phút.
 *   FR-ST-02 Race condition: hai nhân viên không thể khóa cùng 1 ghế.
 */
public class BookingServiceImpl implements IBookingService {

    /** Thời gian khóa ghế tối đa (phút) – BR-04. */
    private static final int MAX_LOCK_MINUTES = 15;

    private final SeatRepository        seatRepository;
    private final SeatLockRepository    seatLockRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowTimeRepository    showTimeRepository;
    private final UserRepository        userRepository;

    public BookingServiceImpl() {
        this.seatRepository        = new SeatRepository();
        this.seatLockRepository    = new SeatLockRepository();
        this.bookingSeatRepository = new BookingSeatRepository();
        this.showTimeRepository    = new ShowTimeRepository();
        this.userRepository        = new UserRepository();
    }

    public BookingServiceImpl(SeatRepository seatRepository,
                              SeatLockRepository seatLockRepository,
                              BookingSeatRepository bookingSeatRepository,
                              ShowTimeRepository showTimeRepository,
                              UserRepository userRepository) {
        this.seatRepository        = seatRepository;
        this.seatLockRepository    = seatLockRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.showTimeRepository    = showTimeRepository;
        this.userRepository        = userRepository;
    }

    // ── getSeatStatuses ──────────────────────────────────────────────────────

    @Override
    public List<SeatStatusDto> getSeatStatuses(String showTimeId, String currentUserId) {
        // 1. Lấy tất cả ghế của phòng thuộc suất chiếu này
        ShowTime showTime = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại: " + showTimeId));
        List<Seat> seats = seatRepository.findByRoomId(showTime.getRoom().getRoomId());

        // 2. Build lookup: seatId → SeatLock (còn hạn)
        Map<String, SeatLock> lockMap = seatLockRepository
                .findActiveLocksForShowTime(showTimeId)
                .stream()
                .collect(Collectors.toMap(sl -> sl.getId().getSeatId(), sl -> sl));

        // 3. Build lookup: seatId đã có BookingSeat
        Set<String> bookedSeatIds = bookingSeatRepository
                .findByShowTime(showTimeId)
                .stream()
                .map(bs -> bs.getId().getSeatId())
                .collect(Collectors.toSet());

        // 4. Tổng hợp trạng thái
        List<SeatStatusDto> result = new ArrayList<>();
        for (Seat seat : seats) {
            String seatId = seat.getSeatId();
            Status status;

            if (bookedSeatIds.contains(seatId)) {
                status = Status.BOOKED;
            } else if (lockMap.containsKey(seatId)) {
                SeatLock lock = lockMap.get(seatId);
                // Phân biệt ghế mình chọn vs ghế người khác khóa
                boolean isMyLock = lock.getLockedBy() != null
                        && currentUserId.equals(lock.getLockedBy().getUserId());
                status = isMyLock ? Status.SELECTED : Status.LOCKED;
            } else {
                status = Status.AVAILABLE;
            }

            result.add(SeatStatusDto.builder()
                    .seatId(seatId)
                    .rowChar(seat.getRowChar())
                    .seatNumber(seat.getSeatNumber())
                    .seatTypeName(seat.getSeatType() != null ? seat.getSeatType().getTypeName() : "")
                    .basePrice(seat.getSeatType() != null ? seat.getSeatType().getBasePrice() : java.math.BigDecimal.ZERO)
                    .status(status)
                    .build());
        }

        // Sắp xếp: hàng A→Z rồi số 1→N
        result.sort(Comparator.comparing(SeatStatusDto::getRowChar)
                .thenComparingInt(SeatStatusDto::getSeatNumber));
        return result;
    }

    // ── lockSeat ─────────────────────────────────────────────────────────────

    @Override
    public void lockSeat(String showTimeId, String seatId, String userId) {
        // Race condition check: đọc trạng thái hiện tại trước khi ghi
        BookingSeatId bsId = new BookingSeatId(showTimeId, seatId);
        if (bookingSeatRepository.existsById(bsId)) {
            throw new IllegalStateException("Ghế này đã được bán. Vui lòng chọn ghế khác.");
        }

        Optional<SeatLock> existingLock = seatLockRepository.findActiveLock(showTimeId, seatId);
        if (existingLock.isPresent()) {
            String lockedByUserId = existingLock.get().getLockedBy() != null
                    ? existingLock.get().getLockedBy().getUserId() : "";
            if (!userId.equals(lockedByUserId)) {
                throw new IllegalStateException(
                        "Ghế này đang được nhân viên khác giữ. Vui lòng chọn ghế khác.");
            }
            // Đã là lock của chính user này → bỏ qua (đã chọn rồi)
            return;
        }

        // Tạo lock mới – BR-03: dùng LocalDateTime.now() (giờ Server)
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUserId(userId);

        ShowTime showTime = new ShowTime();
        showTime.setShowTimeId(showTimeId);

        Seat seat = new Seat();
        seat.setSeatId(seatId);

        SeatLock lock = SeatLock.builder()
                .id(new SeatLockId(showTimeId, seatId))
                .showTime(showTime)
                .seat(seat)
                .lockedBy(user)
                .lockedAt(now)
                .expiresAt(now.plusMinutes(MAX_LOCK_MINUTES))   // BR-04
                .build();

        seatLockRepository.save(lock);
    }

    // ── unlockSeat ───────────────────────────────────────────────────────────

    @Override
    public void unlockSeat(String showTimeId, String seatId, String userId) {
        seatLockRepository.findActiveLock(showTimeId, seatId).ifPresent(lock -> {
            boolean isMyLock = lock.getLockedBy() != null
                    && userId.equals(lock.getLockedBy().getUserId());
            if (isMyLock) {
                seatLockRepository.deleteById(lock.getId());
            }
        });
    }

    // ── unlockAllSeatsForUser ────────────────────────────────────────────────

    @Override
    public void unlockAllSeatsForUser(String showTimeId, String userId) {
        seatLockRepository.deleteUserLocksForShowTime(showTimeId, userId);
    }

    // ── getSecondsUntilExpiry ────────────────────────────────────────────────

    @Override
    public long getSecondsUntilExpiry(String showTimeId, String userId) {
        List<SeatLock> myLocks = seatLockRepository.findActiveLocksForUser(showTimeId, userId);
        if (myLocks.isEmpty()) return -1L;
        // Trả về giây còn lại của lock sắp hết hạn nhất
        return myLocks.stream()
                .mapToLong(sl -> ChronoUnit.SECONDS.between(LocalDateTime.now(), sl.getExpiresAt()))
                .filter(s -> s > 0)
                .min()
                .orElse(-1L);
    }
}