package com.cinema.management.service.impl;

import com.cinema.management.model.entity.Movie;
import com.cinema.management.repository.MovieRepository;
import com.cinema.management.service.IAuditLogService;
import com.cinema.management.service.IMovieService;

import java.util.List;

public class MovieServiceImpl implements IMovieService {
    private final MovieRepository movieRepo;
    private final IAuditLogService auditLogService; // Chèn module Log vào đây

    public MovieServiceImpl(MovieRepository movieRepo, IAuditLogService auditLogService) {
        this.movieRepo = movieRepo;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<Movie> getAllMovies() {
        return movieRepo.findAll();
    }

    @Override
    public Movie getMovieById(String id) {
        return movieRepo.findById(id).orElse(null);
    }

    @Override
    public void createMovie(Movie movie) {
        movieRepo.save(movie);
        // Ghi Log ngay sau khi tạo
        auditLogService.logAction("CREATE", "Movie", "MovieID", "None", movie.getMovieId());
    }

    @Override
    public void updateMovie(Movie movie) {
        movieRepo.update(movie);
        auditLogService.logAction("UPDATE", "Movie", "Title", "Old", movie.getTitle());
    }

    @Override
    public void deleteMovie(String id) {
        movieRepo.delete(id);
        auditLogService.logAction("DELETE", "Movie", "MovieID", id, "None");
    }
}
