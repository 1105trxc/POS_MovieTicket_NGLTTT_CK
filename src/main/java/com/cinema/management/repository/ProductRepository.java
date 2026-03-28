package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Product;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity Product (F&B).
 */
public class ProductRepository {

    public List<Product> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Product p ORDER BY p.productName", Product.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Optional<Product> findById(String productId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Product.class, productId));
        } finally {
            em.close();
        }
    }
}