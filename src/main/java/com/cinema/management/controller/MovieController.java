package com.cinema.management.controller;

import com.cinema.management.model.entity.Movie;
import com.cinema.management.repository.AuditLogRepository;
import com.cinema.management.repository.MovieRepository;
import com.cinema.management.service.IMovieService;
import com.cinema.management.service.impl.AuditLogServiceImpl;
import com.cinema.management.service.impl.MovieServiceImpl;
import java.util.List;

public class MovieController {
    private final IMovieService movieService;

    public MovieController() {
        // Init thủ công do không dùng framework Spring
        this.movieService = new MovieServiceImpl(
                new MovieRepository(),
                new AuditLogServiceImpl(new AuditLogRepository()));
    }

    public List<Movie> getAllMovies() {
        return movieService.getAllMovies();
    }

    public void addMovie(Movie movie) {
        movieService.createMovie(movie);
    }

    public void updateMovie(Movie movie) {
        movieService.updateMovie(movie);
    }

    public void deleteMovie(String id) {
        movieService.deleteMovie(id);
    }
}
