package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity Payment.
 */
public class PaymentRepository {

    public Payment save(Payment payment) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Payment saved = em.merge(payment);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Optional<Payment> findById(String paymentId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Payment.class, paymentId));
        } finally {
            em.close();
        }
    }

    public Optional<Payment> findByIdWithInvoice(String paymentId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Payment p " +
                                    "JOIN FETCH p.invoice i " +
                                    "JOIN FETCH i.user u " +
                                    "LEFT JOIN FETCH i.customer c " +
                                    "LEFT JOIN FETCH i.promotion pr " +
                                    "WHERE p.paymentId = :paymentId",
                            Payment.class)
                    .setParameter("paymentId", paymentId)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }

    public boolean updateStatus(String paymentId, String status, String transactionCode) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Payment payment = em.find(Payment.class, paymentId);
            if (payment == null) {
                tx.rollback();
                return false;
            }
            payment.setStatus(status);
            if (transactionCode != null && !transactionCode.isBlank()) {
                payment.setTransactionCode(transactionCode);
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Payment> findSuccessfulByStaffAndPeriod(String staffUserId, LocalDateTime from, LocalDateTime to) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM Payment p " +
                                    "JOIN FETCH p.invoice i " +
                                    "JOIN FETCH i.user u " +
                                    "WHERE u.userId = :uid " +
                                    "AND p.status = :status " +
                                    "AND p.createdAt >= :fromTime " +
                                    "AND p.createdAt <= :toTime",
                            Payment.class)
                    .setParameter("uid", staffUserId)
                    .setParameter("status", "SUCCESS")
                    .setParameter("fromTime", from)
                    .setParameter("toTime", to)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}

