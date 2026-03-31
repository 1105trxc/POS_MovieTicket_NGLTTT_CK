package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.User;


import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;


import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity User.
 * Phần xác thực (login/BCrypt) thuộc Thành viên B – Module 3.
 */
public class UserRepository {

    private final EntityManager em;

    public UserRepository() {
        this.em = JpaUtil.getEntityManager();
    }

    public User findByUsername(String username) {
        try {
            String jpql = "SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :username";
            TypedQuery<User> query = em.createQuery(jpql, User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public User findById(String userId) {
        return em.find(User.class, userId);
    }

    public List<User> findAll() {
        try {
            // Nhớ Join với Role để lấy được RoleName hiển thị lên bảng
            String jpql = "SELECT u FROM User u JOIN FETCH u.role";
            return em.createQuery(jpql, User.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public void save(User user) {
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void update(User user) {
        try {
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
