package com.cinema.management.controller;

import com.cinema.management.model.dto.SeatStatusDto;
import com.cinema.management.model.entity.Product;
import com.cinema.management.model.entity.ShowTime;
import com.cinema.management.repository.ProductRepository;
import com.cinema.management.service.IBookingService;
import com.cinema.management.service.IShowTimeService;
import com.cinema.management.service.impl.BookingServiceImpl;
import com.cinema.management.service.impl.ShowTimeServiceImpl;

import java.util.List;

/**
 * Controller cho luồng Bán vé (POS) – Module 2.
 * Cầu nối giữa BookingPanel/SeatMapPanel và BookingService.
 * Không chứa business logic.
 */
public class BookingController {

    private final IBookingService  bookingService;
    private final IShowTimeService showTimeService;
    private final ProductRepository productRepository;

    public BookingController() {
        this.bookingService     = new BookingServiceImpl();
        this.showTimeService    = new ShowTimeServiceImpl();
        this.productRepository  = new ProductRepository();
    }

    public BookingController(IBookingService bookingService,
                             IShowTimeService showTimeService,
                             ProductRepository productRepository) {
        this.bookingService    = bookingService;
        this.showTimeService   = showTimeService;
        this.productRepository = productRepository;
    }

    // ── ShowTime ─────────────────────────────────────────────────────────────

    /** Lấy danh sách suất chiếu để điền ComboBox chọn suất. */
    public List<ShowTime> getAllShowTimes() {
        return showTimeService.getAllShowTimes();
    }

    // ── Seat map ─────────────────────────────────────────────────────────────

    /**
     * Trả về trạng thái tất cả ghế để vẽ sơ đồ ghế.
     * @param showTimeId  suất chiếu
     * @param currentUserId user đang thao tác (phân biệt SELECTED vs LOCKED)
     */
    public List<SeatStatusDto> getSeatStatuses(String showTimeId, String currentUserId) {
        return bookingService.getSeatStatuses(showTimeId, currentUserId);
    }

    // ── Lock / Unlock ────────────────────────────────────────────────────────

    /**
     * Khóa ghế 15 phút khi user click chọn.
     * @throws IllegalStateException nếu ghế đã bị người khác khóa hoặc đã bán
     */
    public void lockSeat(String showTimeId, String seatId, String userId) {
        bookingService.lockSeat(showTimeId, seatId, userId);
    }

    /** Nhả 1 ghế khi user click bỏ chọn. */
    public void unlockSeat(String showTimeId, String seatId, String userId) {
        bookingService.unlockSeat(showTimeId, seatId, userId);
    }

    /** Nhả toàn bộ ghế user đang giữ (hủy booking / reset). */
    public void unlockAllSeats(String showTimeId, String userId) {
        bookingService.unlockAllSeatsForUser(showTimeId, userId);
    }

    /**
     * Giây còn lại trước khi lock hết hạn.
     * @return -1 nếu user không giữ ghế nào
     */
    public long getSecondsUntilExpiry(String showTimeId, String userId) {
        return bookingService.getSecondsUntilExpiry(showTimeId, userId);
    }

    // ── F&B Products ─────────────────────────────────────────────────────────

    /** Lấy danh sách sản phẩm F&B để hiển thị trên ProductPanel. */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}