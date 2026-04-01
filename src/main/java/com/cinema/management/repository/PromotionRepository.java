package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Promotion;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity Promotion.
 */
public class PromotionRepository {

    public Optional<Promotion> findByCode(String code) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Promotion> result = em.createQuery(
                    "SELECT p FROM Promotion p WHERE p.code = :code", Promotion.class)
                    .setParameter("code", code)
                    .getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

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

    public Optional<Promotion> findById(String promotionId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Promotion.class, promotionId));
        } finally {
            em.close();
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
     * Lấy danh sách KM đang hoạt động và áp dụng cho 1 phim cụ thể (hoặc cho mọi
     * phim nếu applyToMovie == null).
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
