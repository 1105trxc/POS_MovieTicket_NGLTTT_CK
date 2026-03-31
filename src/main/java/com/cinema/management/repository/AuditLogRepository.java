package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.AuditLog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

/**
 * Repository (DAO) cho entity AuditLog.
 * Chỉ ghi và đọc – không cập nhật, không xóa (audit log là bất biến).
 */
public class AuditLogRepository {

    public AuditLog save(AuditLog log) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(log);
            tx.commit();
            return log;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<AuditLog> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT a FROM AuditLog a ORDER BY a.changedAt DESC", AuditLog.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<AuditLog> findByTable(String tableName) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT a FROM AuditLog a WHERE a.tableName = :table ORDER BY a.changedAt DESC",
                            AuditLog.class)
                    .setParameter("table", tableName)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
