package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Movie;
import com.cinema.management.model.entity.Room;
import com.cinema.management.model.entity.ShowTime;
import com.cinema.management.repository.MovieRepository;
import com.cinema.management.repository.RoomRepository;
import com.cinema.management.repository.ShowTimeRepository;
import com.cinema.management.service.IShowTimeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Triển khai nghiệp vụ quản lý suất chiếu.
 * Validate: endTime > startTime, không xung đột lịch phòng.
 */
public class ShowTimeServiceImpl implements IShowTimeService {

    private final ShowTimeRepository showTimeRepository;
    private final RoomRepository roomRepository;
    private final MovieRepository movieRepository;

    public ShowTimeServiceImpl() {
        this.showTimeRepository = new ShowTimeRepository();
        this.roomRepository = new RoomRepository();
        this.movieRepository = new MovieRepository();
    }

    public ShowTimeServiceImpl(ShowTimeRepository showTimeRepository,
                                RoomRepository roomRepository,
                                MovieRepository movieRepository) {
        this.showTimeRepository = showTimeRepository;
        this.roomRepository = roomRepository;
        this.movieRepository = movieRepository;
    }

    @Override
    public List<ShowTime> getAllShowTimes() {
        return showTimeRepository.findAll();
    }

    @Override
    public List<ShowTime> getShowTimesByMovie(String movieId) {
        return showTimeRepository.findByMovieId(movieId);
    }

    @Override
    public Optional<ShowTime> getShowTimeById(String showTimeId) {
        return showTimeRepository.findById(showTimeId);
    }

    @Override
    public ShowTime addShowTime(String movieId, String roomId,
                                LocalDateTime startTime, LocalDateTime endTime) {
        validateTimes(startTime, endTime);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Phim không tồn tại: " + movieId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng chiếu không tồn tại: " + roomId));

        if (showTimeRepository.hasConflict(roomId, startTime, endTime, null)) {
            throw new IllegalArgumentException(
                    "Phòng '" + room.getRoomName() + "' đã có suất chiếu trong khung giờ này. Vui lòng chọn thời gian khác.");
        }

        ShowTime showTime = ShowTime.builder()
                .showTimeId(UUID.randomUUID().toString())
                .movie(movie)
                .room(room)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        return showTimeRepository.save(showTime);
    }

    @Override
    public ShowTime updateShowTime(String showTimeId, String movieId, String roomId,
                                    LocalDateTime startTime, LocalDateTime endTime) {
        validateTimes(startTime, endTime);

        ShowTime existing = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại: " + showTimeId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Phim không tồn tại: " + movieId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng chiếu không tồn tại: " + roomId));

        // Loại trừ chính suất chiếu đang cập nhật khi kiểm tra xung đột
        if (showTimeRepository.hasConflict(roomId, startTime, endTime, showTimeId)) {
            throw new IllegalArgumentException(
                    "Phòng '" + room.getRoomName() + "' đã có suất chiếu trong khung giờ này.");
        }

        existing.setMovie(movie);
        existing.setRoom(room);
        existing.setStartTime(startTime);
        existing.setEndTime(endTime);
        return showTimeRepository.save(existing);
    }

    @Override
    public void deleteShowTime(String showTimeId) {
        ShowTime st = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại: " + showTimeId));
        if (st.getBookingSeats() != null && !st.getBookingSeats().isEmpty()) {
            throw new IllegalStateException("Không thể xóa suất chiếu đã có vé đặt.");
        }
        showTimeRepository.deleteById(showTimeId);
    }

    // ── Validation helpers ──────────────────────────────────────────────────

    private void validateTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu và kết thúc không được để trống.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Không thể tạo suất chiếu trong quá khứ.");
        }
    }
}
