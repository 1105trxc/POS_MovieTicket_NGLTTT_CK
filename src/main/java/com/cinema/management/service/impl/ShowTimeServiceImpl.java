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
 * Trien khai nghiep vu quan ly suat chieu.
 * Validate: endTime > startTime, khong xung dot lich phong.
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
                .orElseThrow(() -> new IllegalArgumentException("Phim khong ton tai: " + movieId));
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
        return showTimeRepository.save(showTime);
    }

    @Override
    public ShowTime updateShowTime(String showTimeId, String movieId, String roomId,
                                   LocalDateTime startTime, LocalDateTime endTime) {
        validateTimes(startTime, endTime);

        ShowTime existing = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suat chieu khong ton tai: " + showTimeId));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Phim khong ton tai: " + movieId));
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
        return showTimeRepository.save(existing);
    }

    @Override
    public void deleteShowTime(String showTimeId) {
        showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new IllegalArgumentException("Suat chieu khong ton tai: " + showTimeId));

        if (showTimeRepository.hasAnyBookings(showTimeId)) {
            throw new IllegalStateException("Khong the xoa suat chieu da co ve dat.");
        }
        if (showTimeRepository.hasAnySeatLocks(showTimeId)) {
            throw new IllegalStateException("Khong the xoa suat chieu dang co ghe bi khoa.");
        }

        showTimeRepository.deleteById(showTimeId);
    }

    // Validation helpers

    private void validateTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Thoi gian bat dau va ket thuc khong duoc de trong.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("Thoi gian ket thuc phai sau thoi gian bat dau.");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Khong the tao suat chieu trong qua khu.");
        }
    }
}
