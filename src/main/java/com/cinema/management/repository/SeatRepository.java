package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Seat;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDateTime;
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
            List<Seat> results = em.createQuery(
                    "SELECT s FROM Seat s " +
                            "JOIN FETCH s.seatType " +
                            "JOIN FETCH s.room " +
                            "WHERE s.seatId = :seatId",
                    Seat.class)
                    .setParameter("seatId", seatId)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
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

    public boolean hasAnyBookings(String seatId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(bs) FROM BookingSeat bs WHERE bs.id.seatId = :seatId",
                            Long.class)
                    .setParameter("seatId", seatId)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public boolean hasActiveLocks(String seatId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(sl) FROM SeatLock sl " +
                                    "WHERE sl.id.seatId = :seatId " +
                                    "AND sl.expiresAt > :now",
                            Long.class)
                    .setParameter("seatId", seatId)
                    .setParameter("now", LocalDateTime.now())
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
