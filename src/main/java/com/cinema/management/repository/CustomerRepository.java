package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Repository (DAO) cho entity Customer.
 */
public class CustomerRepository {

    public Optional<Customer> findById(String customerId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Customer.class, customerId));
        } finally {
            em.close();
        }
    }

    public Optional<Customer> findByPhone(String phone) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Customer> result = em.createQuery(
                            "SELECT c FROM Customer c WHERE c.phone = :phone", Customer.class)
                    .setParameter("phone", phone)
                    .getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    public Customer save(Customer customer) {
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Customer saved = em.merge(customer);
            tx.commit();
            return saved;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}

