package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Invoice;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Collections;
import java.util.List;

import java.util.List;
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
}

