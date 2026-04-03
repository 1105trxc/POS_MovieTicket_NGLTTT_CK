package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.User;
import jakarta.persistence.EntityManager;
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
            // Sử dụng JOIN FETCH u.role để tải Role ngay lập tức, tránh LazyInitializationException khi session đóng [cite: 131]
            List<User> result = em.createQuery(
                    "SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :uname", User.class)
                    .setParameter("uname", username)
                    .getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    public Optional<User> findByCccd(String cccd) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<User> result = em.createQuery(
                    "SELECT u FROM User u WHERE u.cccd = :cccd", User.class)
                    .setParameter("cccd", cccd)
                    .getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
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
            if (user.getRole() != null && user.getRole().getRoleId() != null) {
                com.cinema.management.model.entity.Role attachedRole = em.find(com.cinema.management.model.entity.Role.class, user.getRole().getRoleId());
                user.setRole(attachedRole);
            }
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
            // Re-attach Role entity to avoid wiping roleName during merge
            if (user.getRole() != null && user.getRole().getRoleId() != null) {
                com.cinema.management.model.entity.Role attachedRole = em.find(
                        com.cinema.management.model.entity.Role.class, user.getRole().getRoleId());
                user.setRole(attachedRole);
            }
            em.merge(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
