package com.cinema.management.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Quản lý vòng đời của EntityManagerFactory (Singleton).
 * Dùng HikariCP connection pool thông qua cấu hình trong persistence.xml.
 */
public class JpaUtil {

    private static final String PERSISTENCE_UNIT_NAME = "CinemaPU";
    private static EntityManagerFactory emFactory;

    private JpaUtil() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emFactory == null || !emFactory.isOpen()) {
            emFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return emFactory;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void shutdown() {
        if (emFactory != null && emFactory.isOpen()) {
            emFactory.close();
        }
    }
}
