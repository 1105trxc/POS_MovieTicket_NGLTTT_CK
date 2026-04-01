package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.ShowTime;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity ShowTime.
 */
public class ShowTimeRepository {

    public List<ShowTime> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT st FROM ShowTime st " +
                            "JOIN FETCH st.movie " +
                            "JOIN FETCH st.room " +
                            "ORDER BY st.startTime DESC", ShowTime.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<ShowTime> findByMovieId(String movieId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT st FROM ShowTime st " +
                            "JOIN FETCH st.movie " +
                            "JOIN FETCH st.room " +
                            "WHERE st.movie.movieId = :movieId " +
                            "ORDER BY st.startTime",
                            ShowTime.class)
                    .setParameter("movieId", movieId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<ShowTime> findById(String showTimeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<ShowTime> result = em.createQuery(
                            "SELECT st FROM ShowTime st " +
                            "JOIN FETCH st.movie " +
                            "JOIN FETCH st.room " +
                            "WHERE st.showTimeId = :showTimeId",
                            ShowTime.class)
                    .setParameter("showTimeId", showTimeId)
                    .getResultList();
            return result.stream().findFirst();
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra xung đột lịch chiếu: phòng đã có suất chiếu khác trong khoảng thời gian đó.
     */
    public boolean hasConflict(String roomId, LocalDateTime startTime, LocalDateTime endTime, String excludeShowTimeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(st) FROM ShowTime st " +
                            "WHERE st.room.roomId = :roomId " +
                            "AND st.showTimeId <> :excludeId " +
                            "AND st.startTime < :endTime AND st.endTime > :startTime",
                            Long.class)
                    .setParameter("roomId", roomId)
                    .setParameter("excludeId", excludeShowTimeId != null ? excludeShowTimeId : "")
                    .setParameter("startTime", startTime)
                    .setParameter("endTime", endTime)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public boolean hasAnyBookings(String showTimeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(bs) FROM BookingSeat bs WHERE bs.id.showTimeId = :showTimeId",
                            Long.class)
                    .setParameter("showTimeId", showTimeId)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public boolean hasAnySeatLocks(String showTimeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(sl) FROM SeatLock sl WHERE sl.id.showTimeId = :showTimeId",
                            Long.class)
                    .setParameter("showTimeId", showTimeId)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public ShowTime save(ShowTime showTime) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ShowTime saved = em.merge(showTime);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteById(String showTimeId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ShowTime st = em.find(ShowTime.class, showTimeId);
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
