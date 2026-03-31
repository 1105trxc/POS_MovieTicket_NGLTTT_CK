package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Product;
import jakarta.persistence.EntityManager;

import java.util.List;

import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity Product (F&B).
 */
public class ProductRepository {
    private final EntityManager em;

    public ProductRepository() {
        this.em = JpaUtil.getEntityManager();
    }

    public List<Product> findAll() {
        try {
            return em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    public Product findById(String id) {
        return em.find(Product.class, id);
    }

    public void save(Product product) {
        try {
            em.getTransaction().begin();
            em.persist(product);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void update(Product product) {
        try {
            em.getTransaction().begin();
            em.merge(product);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void delete(String id) {
        try {
            em.getTransaction().begin();
            Product product = em.find(Product.class, id);
            if (product != null)
                em.remove(product);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
