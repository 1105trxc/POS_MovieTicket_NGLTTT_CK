package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Movie;

import jakarta.persistence.EntityManager;

import java.util.List;

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

    public Movie findById(String id) {
        return em.find(Movie.class, id);
    }

    public void save(Movie movie) {
        try {
            em.getTransaction().begin();
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
