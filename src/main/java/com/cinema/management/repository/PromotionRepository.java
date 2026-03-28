package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Promotion;
import jakarta.persistence.EntityManager;

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

    public Optional<Promotion> findById(String promotionId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Promotion.class, promotionId));
        } finally {
            em.close();
        }
    }
}

