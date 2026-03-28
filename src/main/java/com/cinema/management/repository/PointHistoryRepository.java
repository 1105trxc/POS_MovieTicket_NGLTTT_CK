package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.PointHistory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

/**
 * Repository (DAO) cho entity PointHistory.
 */
public class PointHistoryRepository {

    public PointHistory save(PointHistory pointHistory) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PointHistory saved = em.merge(pointHistory);
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

