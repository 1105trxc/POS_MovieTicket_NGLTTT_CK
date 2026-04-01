package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.OrderDetail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

/**
 * Repository (DAO) cho entity OrderDetail (F&B trong hoá đơn).
 */
public class OrderDetailRepository {

    public void saveAll(List<OrderDetail> details) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            for (OrderDetail od : details) {
                em.merge(od);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<OrderDetail> findByInvoiceIdWithProduct(String invoiceId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT od FROM OrderDetail od " +
                                    "JOIN FETCH od.product p " +
                                    "WHERE od.invoice.invoiceId = :invoiceId",
                            OrderDetail.class)
                    .setParameter("invoiceId", invoiceId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}

