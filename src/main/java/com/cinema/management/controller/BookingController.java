package com.cinema.management.controller;

import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.entity.Product;
import com.cinema.management.model.entity.ShowTime;
import com.cinema.management.repository.ProductRepository;
import com.cinema.management.service.IBookingService;
import com.cinema.management.service.IShowTimeService;
import com.cinema.management.service.impl.BookingServiceImpl;
import com.cinema.management.service.impl.ShowTimeServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller cho luong Ban ve (POS).
 */
public class BookingController {

    private static final int POS_KEEP_PAST_MINUTES = 60;

    private final IBookingService bookingService;
    private final IShowTimeService showTimeService;
    private final ProductRepository productRepository;

    public BookingController() {
        this.bookingService = new BookingServiceImpl();
        this.showTimeService = new ShowTimeServiceImpl();
        this.productRepository = new ProductRepository();
    }

    public BookingController(IBookingService bookingService,
                             IShowTimeService showTimeService,
                             ProductRepository productRepository) {
        this.bookingService = bookingService;
        this.showTimeService = showTimeService;
        this.productRepository = productRepository;
    }

    /**
     * Danh sach suat chieu hien tren POS:
     * - Giu toan bo suat trong ngay hien tai.
     * - Loai bo suat qua cu, chi giu cua so grace 60 phut gan nhat.
     */
    public List<ShowTime> getAllShowTimes() {
        LocalDateTime now = LocalDateTime.now();
        return showTimeService.getAllShowTimes()
                .stream()
                .filter(st -> isVisibleInPos(st, now))
                .sorted(Comparator.comparing(ShowTime::getStartTime))
                .collect(Collectors.toList());
    }

    private boolean isVisibleInPos(ShowTime showTime, LocalDateTime now) {
        if (showTime == null || showTime.getStartTime() == null) {
            return false;
        }
        LocalDateTime startTime = showTime.getStartTime();
        if (startTime.toLocalDate().isEqual(LocalDate.now())) {
            return true;
        }
        return startTime.isAfter(now.minusMinutes(POS_KEEP_PAST_MINUTES));
    }

    public List<SeatStatusDto> getSeatStatuses(String showTimeId, String currentUserId) {
        return bookingService.getSeatStatuses(showTimeId, currentUserId);
    }

    public void lockSeat(String showTimeId, String seatId, String userId) {
        bookingService.lockSeat(showTimeId, seatId, userId);
    }

    public void unlockSeat(String showTimeId, String seatId, String userId) {
        bookingService.unlockSeat(showTimeId, seatId, userId);
    }

    public void unlockAllSeats(String showTimeId, String userId) {
        bookingService.unlockAllSeatsForUser(showTimeId, userId);
    }

    public long getSecondsUntilExpiry(String showTimeId, String userId) {
        return bookingService.getSecondsUntilExpiry(showTimeId, userId);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
