package com.cinema.management.controller;

import com.cinema.management.model.entity.Movie;
import com.cinema.management.model.entity.Room;
import com.cinema.management.model.entity.ShowTime;
import com.cinema.management.repository.MovieRepository;
import com.cinema.management.service.IShowTimeService;
import com.cinema.management.service.impl.ShowTimeServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller cho Quản lý Suất chiếu.
 */
public class ShowTimeController {

    private final IShowTimeService showTimeService;
    private final MovieRepository movieRepository;

    public ShowTimeController() {
        this.showTimeService = new ShowTimeServiceImpl();
        this.movieRepository = new MovieRepository();
    }

    public ShowTimeController(IShowTimeService showTimeService, MovieRepository movieRepository) {
        this.showTimeService = showTimeService;
        this.movieRepository = movieRepository;
    }

    public List<ShowTime> getAllShowTimes() {
        return showTimeService.getAllShowTimes();
    }

    public List<ShowTime> getShowTimesByMovie(String movieId) {
        return showTimeService.getShowTimesByMovie(movieId);
    }

    /** Lấy danh sách phim để điền vào ComboBox trên View. */
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public ShowTime addShowTime(String movieId, String roomId,
                                LocalDateTime startTime, LocalDateTime endTime) {
        return showTimeService.addShowTime(movieId, roomId, startTime, endTime);
    }

    public ShowTime updateShowTime(String showTimeId, String movieId, String roomId,
                                    LocalDateTime startTime, LocalDateTime endTime) {
        return showTimeService.updateShowTime(showTimeId, movieId, roomId, startTime, endTime);
    }

    public void deleteShowTime(String showTimeId) {
        showTimeService.deleteShowTime(showTimeId);
    }
}
