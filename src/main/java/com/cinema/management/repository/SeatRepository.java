package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Seat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity Seat.
 */
public class SeatRepository {

    public List<Seat> findByRoomId(String roomId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT s FROM Seat s " +
                                    "JOIN FETCH s.seatType " +
                                    "JOIN FETCH s.room " +
                                    "WHERE s.room.roomId = :roomId " +
                                    "ORDER BY s.rowChar, s.seatNumber",
                            Seat.class)
                    .setParameter("roomId", roomId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Seat> findById(String seatId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Seat.class, seatId));
        } finally {
            em.close();
        }
    }

    public boolean existsByRoomAndPosition(String roomId, String rowChar, int seatNumber) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(s) FROM Seat s WHERE s.room.roomId = :roomId AND s.rowChar = :row AND s.seatNumber = :num",
                            Long.class)
                    .setParameter("roomId", roomId)
                    .setParameter("row", rowChar)
                    .setParameter("num", seatNumber)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public Seat save(Seat seat) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Seat saved = em.merge(seat);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteById(String seatId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Seat seat = em.find(Seat.class, seatId);
            if (seat != null) em.remove(seat);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
