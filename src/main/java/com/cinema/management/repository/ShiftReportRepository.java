package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.ShiftReport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class ShiftReportRepository {

    public ShiftReport save(ShiftReport report) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ShiftReport saved = em.merge(report);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<ShiftReport> findByUserId(String userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT s FROM ShiftReport s WHERE s.user.userId = :uid ORDER BY s.createdAt DESC",
                            ShiftReport.class)
                    .setParameter("uid", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<ShiftReport> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT s FROM ShiftReport s " +
                                    "JOIN FETCH s.user u " +
                                    "ORDER BY s.shiftStart DESC",
                            ShiftReport.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<ShiftReport> findByStatus(String status) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT s FROM ShiftReport s " +
                                    "JOIN FETCH s.user u " +
                                    "WHERE s.status = :status " +
                                    "ORDER BY s.shiftStart DESC",
                            ShiftReport.class)
                    .setParameter("status", status)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<ShiftReport> findById(String shiftReportId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<ShiftReport> result = em.createQuery(
                            "SELECT s FROM ShiftReport s " +
                                    "JOIN FETCH s.user u " +
                                    "WHERE s.shiftReportId = :sid",
                            ShiftReport.class)
                    .setParameter("sid", shiftReportId)
                    .getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }
}
