package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Genre;

import jakarta.persistence.EntityManager;
import java.util.List;

public class GenreRepository {
    private final EntityManager em = JpaUtil.getEntityManager();

    public List<Genre> findAll() {
        return em.createQuery("SELECT g FROM Genre g", Genre.class).getResultList();
    }

    public void save(Genre g) {
        try {
            em.getTransaction().begin();
            em.persist(g);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(String id) {
        try {
            em.getTransaction().begin();
            Genre g = em.find(Genre.class, id);
            if (g != null)
                em.remove(g);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
