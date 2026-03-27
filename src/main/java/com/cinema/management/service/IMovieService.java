package com.cinema.management.service;

import com.cinema.management.model.entity.Movie;
import java.util.List;

public interface IMovieService {
    List<Movie> getAllMovies();

    Movie getMovieById(String id);

    void createMovie(Movie movie);

    void updateMovie(Movie movie);

    void deleteMovie(String id);
}
