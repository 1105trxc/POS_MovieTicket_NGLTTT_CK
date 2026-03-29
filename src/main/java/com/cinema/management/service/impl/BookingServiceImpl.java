package com.cinema.management.service.impl;

import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.dto.SeatStatusDto.Status;
import com.cinema.management.model.entity.BookingSeatId;
import com.cinema.management.model.entity.Seat;
import com.cinema.management.model.entity.SeatLock;
import com.cinema.management.model.entity.SeatLockId;
import com.cinema.management.model.entity.ShowTime;
import com.cinema.management.model.entity.User;
import com.cinema.management.repository.BookingSeatRepository;
import com.cinema.management.repository.SeatLockRepository;
import com.cinema.management.repository.SeatRepository;
import com.cinema.management.repository.ShowTimeRepository;
import com.cinema.management.repository.UserRepository;
import com.cinema.management.service.IBookingService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Trien khai nghiep vu Ban ve - luong chon ghe va khoa ghe.
 */
public class BookingServiceImpl implements IBookingService {

    private static final int MAX_LOCK_MINUTES = 15;

    private final SeatRepository seatRepository;
    private final SeatLockRepository seatLockRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final ShowTimeRepository showTimeRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl() {
        this.seatRepository = new SeatRepository();
        this.seatLockRepository = new SeatLockRepository();
        this.bookingSeatRepository = new BookingSeatRepository();
        this.showTimeRepository = new ShowTimeRepository();
        this.userRepository = new UserRepository();
    }

    public BookingServiceImpl(SeatRepository seatRepository,
                              SeatLockRepository seatLockRepository,
                              BookingSeatRepository bookingSeatRepository,
                              ShowTimeRepository showTimeRepository,
                              UserRepository userRepository) {
        this.seatRepository = seatRepository;
        this.seatLockRepository = seatLockRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.showTimeRepository = showTimeRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<SeatStatusDto> getSeatStatuses(String showTimeId, String currentUserId) {
        ShowTime showTime = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suat chieu khong ton tai: " + showTimeId));
        List<Seat> seats = seatRepository.findByRoomId(showTime.getRoom().getRoomId());

        Map<String, SeatLock> lockMap = seatLockRepository.findActiveLocksForShowTime(showTimeId)
                .stream()
                .collect(Collectors.toMap(sl -> sl.getId().getSeatId(), sl -> sl));

        Set<String> bookedSeatIds = bookingSeatRepository.findByShowTime(showTimeId)
                .stream()
                .map(bs -> bs.getId().getSeatId())
                .collect(Collectors.toSet());

        List<SeatStatusDto> result = new ArrayList<>();
        for (Seat seat : seats) {
            String seatId = seat.getSeatId();
            Status status;

            if (bookedSeatIds.contains(seatId)) {
                status = Status.BOOKED;
            } else if (lockMap.containsKey(seatId)) {
                SeatLock lock = lockMap.get(seatId);
                boolean isMyLock = lock.getLockedBy() != null
                        && Objects.equals(currentUserId, lock.getLockedBy().getUserId());
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

        result.sort(Comparator.comparing(SeatStatusDto::getRowChar)
                .thenComparingInt(SeatStatusDto::getSeatNumber));
        return result;
    }

    @Override
    public void lockSeat(String showTimeId, String seatId, String userId) {
        User user = resolveUserForLock(userId);
        ShowTime showTimeEntity = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suat chieu khong ton tai: " + showTimeId));
        validateShowTimeIsSellable(showTimeEntity);

        BookingSeatId bsId = new BookingSeatId(showTimeId, seatId);
        if (bookingSeatRepository.existsById(bsId)) {
            throw new IllegalStateException("Ghe nay da duoc ban. Vui long chon ghe khac.");
        }

        Optional<SeatLock> existingLock = seatLockRepository.findActiveLock(showTimeId, seatId);
        if (existingLock.isPresent()) {
            String lockedByUserId = existingLock.get().getLockedBy() != null
                    ? existingLock.get().getLockedBy().getUserId() : "";
            if (!Objects.equals(user.getUserId(), lockedByUserId)) {
                throw new IllegalStateException("Ghe nay dang duoc nhan vien khac giu. Vui long chon ghe khac.");
            }
            return;
        }

        LocalDateTime now = LocalDateTime.now();
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
                .expiresAt(now.plusMinutes(MAX_LOCK_MINUTES))
                .build();

        seatLockRepository.save(lock);
    }

    private User resolveUserForLock(String userIdOrUsername) {
        if (userIdOrUsername == null || userIdOrUsername.trim().isEmpty()) {
            throw new IllegalStateException("Khong xac dinh duoc nhan vien dang thao tac.");
        }
        String input = userIdOrUsername.trim();
        return userRepository.findById(input)
                .or(() -> userRepository.findByUsername(input))
                .orElseThrow(() -> new IllegalStateException(
                        "Tai khoan '" + input + "' khong ton tai hoac da bi xoa."));
    }

    private void validateShowTimeIsSellable(ShowTime showTime) {
        if (showTime.getStartTime() == null) {
            throw new IllegalStateException("Suat chieu khong hop le de ban ve.");
        }
        if (!showTime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Khong the ban ve cho suat chieu da bat dau hoac da ket thuc.");
        }
    }

    @Override
    public void unlockSeat(String showTimeId, String seatId, String userId) {
        seatLockRepository.findActiveLock(showTimeId, seatId).ifPresent(lock -> {
            boolean isMyLock = lock.getLockedBy() != null
                    && Objects.equals(userId, lock.getLockedBy().getUserId());
            if (isMyLock) {
                seatLockRepository.deleteById(lock.getId());
            }
        });
    }

    @Override
    public void unlockAllSeatsForUser(String showTimeId, String userId) {
        seatLockRepository.deleteUserLocksForShowTime(showTimeId, userId);
    }

    @Override
    public long getSecondsUntilExpiry(String showTimeId, String userId) {
        List<SeatLock> myLocks = seatLockRepository.findActiveLocksForUser(showTimeId, userId);
        if (myLocks.isEmpty()) return -1L;
        return myLocks.stream()
                .mapToLong(sl -> ChronoUnit.SECONDS.between(LocalDateTime.now(), sl.getExpiresAt()))
                .filter(s -> s > 0)
                .min()
                .orElse(-1L);
    }
}
