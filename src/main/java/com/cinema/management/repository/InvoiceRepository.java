package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Invoice;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository (DAO) cho entity Invoice.
 */
public class InvoiceRepository {

    public Optional<Invoice> findById(String invoiceId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Invoice.class, invoiceId));
        } finally {
            em.close();
        }
    }

    public Optional<Invoice> findByIdWithDetails(String invoiceId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Invoice> result = em.createQuery(
                            "SELECT i FROM Invoice i " +
                                    "JOIN FETCH i.user u " +
                                    "LEFT JOIN FETCH i.customer c " +
                                    "LEFT JOIN FETCH i.promotion p " +
                                    "WHERE i.invoiceId = :invoiceId",
                            Invoice.class)
                    .setParameter("invoiceId", invoiceId)
                    .getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    public List<Invoice> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT i FROM Invoice i ORDER BY i.createdAt DESC", Invoice.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Lưu Invoice mới (persist) – không dùng merge để tránh overwrite snapshot.
     */
    public Invoice save(Invoice invoice) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Invoice saved = em.merge(invoice);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<String> findExpiredPendingQrInvoiceIdsByShowTime(String showTimeId, LocalDateTime expiredBefore) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT DISTINCT i.invoiceId " +
                                    "FROM Invoice i " +
                                    "JOIN i.bookingSeats bs " +
                                    "JOIN i.payments p " +
                                    "WHERE bs.id.showTimeId = :stId " +
                                    "AND p.paymentMethod = :method " +
                                    "AND p.status = :status " +
                                    "AND p.createdAt <= :expiredBefore",
                            String.class)
                    .setParameter("stId", showTimeId)
                    .setParameter("method", "QR")
                    .setParameter("status", "PENDING")
                    .setParameter("expiredBefore", expiredBefore)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void deleteById(String invoiceId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Invoice invoice = em.find(Invoice.class, invoiceId);
            if (invoice != null) {
                em.remove(invoice);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}

