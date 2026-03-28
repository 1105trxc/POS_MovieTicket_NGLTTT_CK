package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity User.
 * Phần xác thực (login/BCrypt) thuộc Thành viên B – Module 3.
 */
public class UserRepository {

    public Optional<User> findById(String userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(User.class, userId));
        } finally {
            em.close();
        }
    }

    public Optional<User> findByUsername(String username) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<User> result = em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :uname", User.class)
                    .setParameter("uname", username)
                    .getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    public User save(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User saved = em.merge(user);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}