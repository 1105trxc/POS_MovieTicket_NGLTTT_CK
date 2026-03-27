package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Movie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity Movie.
 * Thuộc phạm vi Thành viên B – khai báo đủ để Thành viên A dùng được
 * theo "Interface First" pattern (Skill_agent – Git Workflow Rule 4).
 */
public class MovieRepository {

    public List<Movie> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT m FROM Movie m ORDER BY m.title", Movie.class)
                    .getResultList();
        } finally {
            em.close();
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

    public Movie save(Movie movie) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Movie saved = em.merge(movie);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteById(String movieId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Movie movie = em.find(Movie.class, movieId);
            if (movie != null) em.remove(movie);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
