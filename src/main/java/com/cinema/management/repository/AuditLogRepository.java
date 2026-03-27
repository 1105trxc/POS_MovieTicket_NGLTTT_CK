package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.AuditLog;

import jakarta.persistence.EntityManager;

public class AuditLogRepository {
    private final EntityManager em;

    public AuditLogRepository() {
        this.em = JpaUtil.getEntityManager();
    }

    public void save(AuditLog log) {
        try {
            em.getTransaction().begin();
            em.persist(log);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace(); // Không quăng lỗi chặn hệ thống, chỉ in ra console
        }
    }
}
