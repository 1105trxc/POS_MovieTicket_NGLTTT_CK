package com.cinema.management.repository;

import com.cinema.management.config.JpaUtil;
import com.cinema.management.model.entity.Customer;
import jakarta.persistence.EntityManager;

import java.util.Collections;
import java.util.List;

public class CustomerRepository {
    private final EntityManager em;

    public CustomerRepository() {
        this.em = JpaUtil.getEntityManager();
    }

    public List<Customer> findAll() {
        try {
            return em.createQuery("SELECT c FROM Customer c", Customer.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Customer findById(String id) {
        return em.find(Customer.class, id);
    }

    /**
     * Tìm khách hàng theo số điện thoại.
     */
    public Customer findByPhone(String phone) {
        try {
            return em.createQuery(
                            "SELECT c FROM Customer c WHERE c.phone = :phone", Customer.class)
                    .setParameter("phone", phone)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public void save(Customer customer) {
        try {
            em.getTransaction().begin();
            em.persist(customer);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }

    public void update(Customer customer) {
        try {
            em.getTransaction().begin();
            em.merge(customer);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        }
    }
}
