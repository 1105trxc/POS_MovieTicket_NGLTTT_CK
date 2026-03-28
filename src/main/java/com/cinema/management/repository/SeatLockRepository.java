package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.SeatLock;
import com.cinema.management.model.entity.SeatLockId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity SeatLock.
 * Tuân thủ BR-03: mọi so sánh thời gian dùng LocalDateTime.now() (giờ Server).
 * Tuân thủ BR-04: ghế bị khóa tối đa 15 phút.
 */
public class SeatLockRepository {

    /** Tìm lock hiện tại (chưa hết hạn) của 1 ghế trong 1 suất chiếu. */
    public Optional<SeatLock> findActiveLock(String showTimeId, String seatId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<SeatLock> result = em.createQuery(
                            "SELECT sl FROM SeatLock sl " +
                                    "WHERE sl.id.showTimeId = :stId AND sl.id.seatId = :sId " +
                                    "AND sl.expiresAt > :now",
                            SeatLock.class)
                    .setParameter("stId", showTimeId)
                    .setParameter("sId", seatId)
                    .setParameter("now", LocalDateTime.now())
                    .getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    /** Lấy tất cả lock còn hạn của 1 suất chiếu (để vẽ sơ đồ ghế). */
    public List<SeatLock> findActiveLocksForShowTime(String showTimeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT sl FROM SeatLock sl " +
                                    "WHERE sl.id.showTimeId = :stId AND sl.expiresAt > :now",
                            SeatLock.class)
                    .setParameter("stId", showTimeId)
                    .setParameter("now", LocalDateTime.now())
                    .getResultList();
        } finally {
            em.close();
        }
    }

    /** Lấy tất cả lock còn hạn do 1 user đang giữ trong 1 suất chiếu. */
    public List<SeatLock> findActiveLocksForUser(String showTimeId, String userId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT sl FROM SeatLock sl " +
                                    "WHERE sl.id.showTimeId = :stId " +
                                    "AND sl.lockedBy.userId = :uid " +
                                    "AND sl.expiresAt > :now",
                            SeatLock.class)
                    .setParameter("stId", showTimeId)
                    .setParameter("uid", userId)
                    .setParameter("now", LocalDateTime.now())
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public SeatLock save(SeatLock seatLock) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SeatLock saved = em.merge(seatLock);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Xóa lock (nhả ghế) theo composite key. */
    public void deleteById(SeatLockId id) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            SeatLock sl = em.find(SeatLock.class, id);
            if (sl != null) em.remove(sl);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Dọn dẹp tất cả lock đã hết hạn (có thể gọi định kỳ). */
    public int deleteExpiredLocks() {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            int deleted = em.createQuery(
                            "DELETE FROM SeatLock sl WHERE sl.expiresAt <= :now")
                    .setParameter("now", LocalDateTime.now())
                    .executeUpdate();
            tx.commit();
            return deleted;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Xóa toàn bộ lock của user trong 1 suất chiếu (hủy chọn ghế). */
    public void deleteUserLocksForShowTime(String showTimeId, String userId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery(
                            "DELETE FROM SeatLock sl " +
                                    "WHERE sl.id.showTimeId = :stId AND sl.lockedBy.userId = :uid")
                    .setParameter("stId", showTimeId)
                    .setParameter("uid", userId)
                    .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}