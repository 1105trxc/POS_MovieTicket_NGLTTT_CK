package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.PointHistory;
import jakarta.persistence.EntityManager;

import java.util.Collections;
import java.util.List;

public class PointHistoryRepository {
    private final EntityManager em;

    public PointHistoryRepository() {
        this.em = JpaUtil.getEntityManager();
    }

    public void save(PointHistory pointHistory) {
        try {
            em.getTransaction().begin();
            em.persist(pointHistory);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    /**
     * Lấy lịch sử tích/dùng điểm của 1 khách hàng, sắp xếp mới nhất trước.
     */
    public List<PointHistory> findByCustomerId(String customerId) {
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
