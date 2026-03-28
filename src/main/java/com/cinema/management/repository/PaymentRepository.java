package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

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
}

