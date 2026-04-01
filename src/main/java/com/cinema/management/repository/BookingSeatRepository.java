package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.BookingSeat;
import com.cinema.management.model.entity.BookingSeatId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository (DAO) cho entity BookingSeat.
 */
public class BookingSeatRepository {

    /** Lấy tất cả ghế đã được đặt (có invoice) trong 1 suất chiếu. */
    public List<BookingSeat> findByShowTime(String showTimeId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT bs FROM BookingSeat bs WHERE bs.id.showTimeId = :stId",
                            BookingSeat.class)
                    .setParameter("stId", showTimeId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean existsById(BookingSeatId id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(BookingSeat.class, id) != null;
        } finally {
            em.close();
        }
    }

    public BookingSeat save(BookingSeat bookingSeat) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            BookingSeat saved = em.merge(bookingSeat);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void saveAll(List<BookingSeat> bookingSeats) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            for (BookingSeat bs : bookingSeats) {
                em.merge(bs);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Map<String, String> findProcessingSeatPaymentMap(String showTimeId, LocalDateTime validFrom) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Object[]> rows = em.createQuery(
                            "SELECT bs.id.seatId, p.paymentId " +
                                    "FROM BookingSeat bs " +
                                    "JOIN bs.invoice i " +
                                    "JOIN i.payments p " +
                                    "WHERE bs.id.showTimeId = :stId " +
                                    "AND p.paymentMethod = :method " +
                                    "AND p.status = :status " +
                                    "AND p.createdAt >= :validFrom",
                            Object[].class)
                    .setParameter("stId", showTimeId)
                    .setParameter("method", "QR")
                    .setParameter("status", "PENDING")
                    .setParameter("validFrom", validFrom)
                    .getResultList();
            Map<String, String> map = new LinkedHashMap<>();
            for (Object[] row : rows) {
                map.put(String.valueOf(row[0]), String.valueOf(row[1]));
            }
            return map;
        } finally {
            em.close();
        }
    }

    public List<BookingSeat> findByInvoiceIdWithDetails(String invoiceId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT bs FROM BookingSeat bs " +
                                    "JOIN FETCH bs.showTime st " +
                                    "LEFT JOIN FETCH st.movie " +
                                    "LEFT JOIN FETCH st.room " +
                                    "JOIN FETCH bs.seat s " +
                                    "LEFT JOIN FETCH s.seatType " +
                                    "WHERE bs.invoice.invoiceId = :invoiceId",
                            BookingSeat.class)
                    .setParameter("invoiceId", invoiceId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
