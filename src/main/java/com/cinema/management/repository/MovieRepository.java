package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Movie;
import jakarta.persistence.EntityManager;
import com.cinema.management.model.entity.Genre;
import java.util.List;

import java.util.Optional;

public class MovieRepository {

    private final EntityManager em;

    public MovieRepository() {
        this.em = JpaUtil.getEntityManager();
    }

    public List<Movie> findAll() {
        try {
            return em.createQuery("SELECT m FROM Movie m", Movie.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public Optional<Movie> findById(String movieId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Movie.class, movieId));
        } finally {
            em.close();
        }
    }

    public void save(Movie movie) {
        try {
            em.getTransaction().begin();
            if (movie.getMovieGenres() != null) {
                for (com.cinema.management.model.entity.MovieGenre mg : movie.getMovieGenres()) {
                    if (mg.getGenre() != null) {
                        mg.setGenre(em.getReference(Genre.class,
                                mg.getGenre().getGenreId()));
                    }
                }
            }
            em.persist(movie);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void update(Movie movie) {
        try {
            em.getTransaction().begin();
            if (movie.getMovieGenres() != null) {
                for (com.cinema.management.model.entity.MovieGenre mg : movie.getMovieGenres()) {
                    if (mg.getGenre() != null) {
                        mg.setGenre(em.getReference(Genre.class, mg.getGenre().getGenreId()));
                    }
                }
            }
            em.merge(movie);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(String id) {
        try {
            em.getTransaction().begin();
            Movie movie = em.find(Movie.class, id);
            if (movie != null)
                em.remove(movie);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
