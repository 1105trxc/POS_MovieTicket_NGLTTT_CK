package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.AuditLog;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.time.LocalDateTime;

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
            if (tx.isActive())
                tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<AuditLog> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT a FROM AuditLog a JOIN FETCH a.changedBy ORDER BY a.changedAt DESC", AuditLog.class)
                    .setMaxResults(999)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<AuditLog> searchLogs(String keyword) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String lowerKw = "%" + keyword.toLowerCase() + "%";
            return em.createQuery(
                    "SELECT a FROM AuditLog a JOIN FETCH a.changedBy " +
                            "WHERE LOWER(a.changedBy.fullName) LIKE :kw " +
                            "OR LOWER(a.tableName) LIKE :kw " +
                            "OR LOWER(a.fieldName) LIKE :kw " +
                            "ORDER BY a.changedAt DESC",
                    AuditLog.class)
                    .setParameter("kw", lowerKw)
                    .setMaxResults(999)
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

    public List<AuditLog> findByDate(java.time.LocalDate date) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            return em.createQuery(
                    "SELECT a FROM AuditLog a JOIN FETCH a.changedBy " +
                            "WHERE a.changedAt >= :start AND a.changedAt < :end " +
                            "ORDER BY a.changedAt DESC",
                    AuditLog.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
