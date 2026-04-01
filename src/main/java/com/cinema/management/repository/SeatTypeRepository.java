package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.SeatType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity SeatType.
 */
public class SeatTypeRepository {

    public List<SeatType> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT st FROM SeatType st ORDER BY st.typeName", SeatType.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<SeatType> findById(String seatTypeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(SeatType.class, seatTypeId));
        } finally {
            em.close();
        }
    }

    public boolean existsByTypeName(String typeName) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(s) FROM SeatType s WHERE s.typeName = :name", Long.class)
                    .setParameter("name", typeName)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public SeatType save(SeatType seatType) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SeatType saved = em.merge(seatType);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteById(String seatTypeId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SeatType st = em.find(SeatType.class, seatTypeId);
            if (st != null) em.remove(st);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
