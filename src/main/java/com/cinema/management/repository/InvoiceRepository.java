package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Invoice;
import jakarta.persistence.EntityManager;

import java.util.Collections;
import java.util.List;

public class InvoiceRepository {
    private final EntityManager em;

    public InvoiceRepository() {
        this.em = JpaUtil.getEntityManager();
    }

    public List<Invoice> findAll() {
        try {
            return em.createQuery("SELECT i FROM Invoice i ORDER BY i.createdAt DESC", Invoice.class)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Invoice findById(String id) {
        return em.find(Invoice.class, id);
    }

    public void save(Invoice invoice) {
        try {
            em.getTransaction().begin();
            em.persist(invoice);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void update(Invoice invoice) {
        try {
            em.getTransaction().begin();
            em.merge(invoice);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
