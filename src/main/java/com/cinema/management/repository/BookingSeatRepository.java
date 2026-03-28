package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.BookingSeat;
import com.cinema.management.model.entity.BookingSeatId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

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
}