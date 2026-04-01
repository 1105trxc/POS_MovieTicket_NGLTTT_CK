package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.PointHistory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Collections;
import java.util.List;

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

    /**
     * Lấy lịch sử tích/dùng điểm của 1 khách hàng, sắp xếp mới nhất trước.
     */
    public List<PointHistory> findByCustomerId(String customerId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT ph FROM PointHistory ph WHERE ph.customer.customerId = :custId ORDER BY ph.createdAt DESC",
                            PointHistory.class)
                    .setParameter("custId", customerId)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}

