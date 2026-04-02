package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Movie;
import com.cinema.management.model.entity.Room;
import com.cinema.management.model.entity.ShowTime;
import com.cinema.management.repository.MovieRepository;
import com.cinema.management.repository.RoomRepository;
import com.cinema.management.repository.ShowTimeRepository;
import com.cinema.management.service.IAuditLogService;
import com.cinema.management.service.IShowTimeService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Trien khai nghiep vu quan ly suat chieu.
 * Validate: endTime > startTime, khong xung dot lich phong.
 */
public class ShowTimeServiceImpl implements IShowTimeService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final ShowTimeRepository showTimeRepository;
    private final RoomRepository roomRepository;
    private final MovieRepository movieRepository;
    private final IAuditLogService auditLogService;

    public ShowTimeServiceImpl() {
        this.showTimeRepository = new ShowTimeRepository();
        this.roomRepository = new RoomRepository();
        this.movieRepository = new MovieRepository();
        this.auditLogService = null;
    }

    public ShowTimeServiceImpl(ShowTimeRepository showTimeRepository,
                               RoomRepository roomRepository,
                               MovieRepository movieRepository,
                               IAuditLogService auditLogService) {
        this.showTimeRepository = showTimeRepository;
        this.roomRepository = roomRepository;
        this.movieRepository = movieRepository;
        this.auditLogService = auditLogService;
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
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Phim khong ton tai: " + movieId));
        validateTimes(startTime, endTime, movie.getDuration());
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phong chieu khong ton tai: " + roomId));

        if (showTimeRepository.hasConflict(roomId, startTime, endTime, null)) {
            throw new IllegalArgumentException(
                    "Phong '" + room.getRoomName() + "' da co suat chieu trong khung gio nay. Vui long chon thoi gian khac.");
        }

        ShowTime showTime = ShowTime.builder()
                .showTimeId(UUID.randomUUID().toString())
                .movie(movie)
                .room(room)
                .startTime(startTime)
                .endTime(endTime)
                .build();
        ShowTime saved = showTimeRepository.save(showTime);

        if (auditLogService != null) {
            auditLogService.logAction("CREATE", "ShowTime", "Suất chiếu",
                    "N/A", movie.getTitle() + " | " + room.getRoomName()
                            + " | " + startTime.format(DT_FMT) + "-" + endTime.format(DT_FMT));
        }

        return saved;
    }

    @Override
    public ShowTime updateShowTime(String showTimeId, String movieId, String roomId,
                                   LocalDateTime startTime, LocalDateTime endTime) {
        ShowTime existing = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suat chieu khong ton tai: " + showTimeId));

        // Lưu dữ liệu cũ
        String oldInfo = (existing.getMovie() != null ? existing.getMovie().getTitle() : "")
                + " | " + (existing.getRoom() != null ? existing.getRoom().getRoomName() : "")
                + " | " + (existing.getStartTime() != null ? existing.getStartTime().format(DT_FMT) : "");

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Phim khong ton tai: " + movieId));
        validateTimes(startTime, endTime, movie.getDuration());
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phong chieu khong ton tai: " + roomId));

        // Loai tru chinh suat chieu dang cap nhat khi kiem tra xung dot
        if (showTimeRepository.hasConflict(roomId, startTime, endTime, showTimeId)) {
            throw new IllegalArgumentException(
                    "Phong '" + room.getRoomName() + "' da co suat chieu trong khung gio nay.");
        }

        existing.setMovie(movie);
        existing.setRoom(room);
        existing.setStartTime(startTime);
        existing.setEndTime(endTime);
        ShowTime saved = showTimeRepository.save(existing);

        if (auditLogService != null) {
            String newInfo = movie.getTitle() + " | " + room.getRoomName()
                    + " | " + startTime.format(DT_FMT) + "-" + endTime.format(DT_FMT);
            auditLogService.logAction("UPDATE", "ShowTime", "Suất chiếu",
                    oldInfo, newInfo);
        }

        return saved;
    }

    @Override
    public void deleteShowTime(String showTimeId) {
        ShowTime st = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suat chieu khong ton tai: " + showTimeId));

        if (showTimeRepository.hasAnyBookings(showTimeId)) {
            throw new IllegalStateException("Khong the xoa suat chieu da co ve dat.");
        }
        if (showTimeRepository.hasAnySeatLocks(showTimeId)) {
            throw new IllegalStateException("Khong the xoa suat chieu dang co ghe bi khoa.");
        }

        String info = (st.getMovie() != null ? st.getMovie().getTitle() : "")
                + " | " + (st.getRoom() != null ? st.getRoom().getRoomName() : "")
                + " | " + (st.getStartTime() != null ? st.getStartTime().format(DT_FMT) : "");

        showTimeRepository.deleteById(showTimeId);

        if (auditLogService != null) {
            auditLogService.logAction("DELETE", "ShowTime", "Suất chiếu",
                    info, "Đã xóa");
        }
    }

    // Validation helpers

    private void validateTimes(LocalDateTime startTime, LocalDateTime endTime, Integer movieDurationMinutes) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Thoi gian bat dau va ket thuc khong duoc de trong.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Thoi gian ket thuc phai sau thoi gian bat dau.");
        }
        if (movieDurationMinutes != null && movieDurationMinutes > 0) {
            LocalDateTime minEndTime = startTime.plusMinutes(movieDurationMinutes);
            if (endTime.isBefore(minEndTime)) {
                throw new IllegalArgumentException(
                        "Thoi gian ket thuc khong hop le. End time phai lon hon hoac bang Start time + thoi luong phim.");
            }
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Khong the tao suat chieu trong qua khu.");
        }
    }
}
