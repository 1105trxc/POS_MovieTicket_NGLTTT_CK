package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity Room.
 * Chỉ xử lý truy vấn DB bằng HQL – không chứa business logic.
 */
public class RoomRepository {

    public List<Room> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT r FROM Room r ORDER BY r.roomName", Room.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Room> findById(String roomId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Room.class, roomId));
        } finally {
            em.close();
        }
    }

    public boolean existsByRoomName(String roomName) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(r) FROM Room r WHERE r.roomName = :name", Long.class)
                    .setParameter("name", roomName)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public Room save(Room room) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Room saved = em.merge(room);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteById(String roomId) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Room room = em.find(Room.class, roomId);
            if (room != null) em.remove(room);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
