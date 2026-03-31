package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.Collections;
import java.util.List;

public class RoleRepository {
    private final EntityManager em;

    public RoleRepository() {
        this.em = JpaUtil.getEntityManager();
    }

    public List<Role> findAll() {
        try {
            return em.createQuery("SELECT r FROM Role r", Role.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Role findByName(String roleName) {
        try {
            return em.createQuery("SELECT r FROM Role r WHERE r.roleName = :name", Role.class)
                    .setParameter("name", roleName)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void save(Role role) {
        try {
            em.getTransaction().begin();
            em.persist(role);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(String id) {
        try {
            em.getTransaction().begin();
            Role g = em.find(Role.class, id);
            if (g != null) em.remove(g);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
