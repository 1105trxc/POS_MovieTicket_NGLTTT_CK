package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Promotion;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class PromotionRepository {
    private final EntityManager em;

    public PromotionRepository() {
        this.em = JpaUtil.getEntityManager();
    }

    public List<Promotion> findAll() {
        try {
            return em.createQuery("SELECT p FROM Promotion p", Promotion.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Promotion findById(String id) {
        return em.find(Promotion.class, id);
    }

    /**
     * Tìm khuyến mãi theo mã code.
     */
    public Promotion findByCode(String code) {
        try {
            return em.createQuery(
                            "SELECT p FROM Promotion p WHERE p.code = :code", Promotion.class)
                    .setParameter("code", code)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lấy danh sách khuyến mãi đang hoạt động tại thời điểm hiện tại.
     * Điều kiện: startDate <= today <= expiryDate
     */
    public List<Promotion> findActivePromotions(LocalDate today) {
        try {
            return em.createQuery(
                            "SELECT p FROM Promotion p WHERE p.startDate <= :today AND p.expiryDate >= :today",
                            Promotion.class)
                    .setParameter("today", today)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Lấy danh sách KM đang hoạt động và áp dụng cho 1 phim cụ thể (hoặc cho mọi phim nếu applyToMovie == null).
     */
    public List<Promotion> findActiveForMovie(String movieId, LocalDate today) {
        try {
            return em.createQuery(
                            "SELECT p FROM Promotion p WHERE p.startDate <= :today AND p.expiryDate >= :today " +
                                    "AND (p.applyToMovie IS NULL OR p.applyToMovie.movieId = :movieId)",
                            Promotion.class)
                    .setParameter("today", today)
                    .setParameter("movieId", movieId)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public void save(Promotion promotion) {
        try {
            em.getTransaction().begin();
            em.persist(promotion);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void update(Promotion promotion) {
        try {
            em.getTransaction().begin();
            em.merge(promotion);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(String id) {
        try {
            em.getTransaction().begin();
            Promotion promotion = em.find(Promotion.class, id);
            if (promotion != null)
                em.remove(promotion);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
