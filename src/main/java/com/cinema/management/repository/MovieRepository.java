package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Movie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity Movie.
 * Thuộc phạm vi Thành viên B – khai báo đủ để Thành viên A dùng được
 * theo "Interface First" pattern (Skill_agent – Git Workflow Rule 4).
 */
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
