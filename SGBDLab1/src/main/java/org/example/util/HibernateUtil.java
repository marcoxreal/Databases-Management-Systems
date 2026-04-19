package org.example.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;

public class HibernateUtil {
    private static final EntityManagerFactory emf;
    private static final HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:postgresql://localhost:5432/biblioteca");
            config.setUsername("postgres");
            config.setPassword("123skem2");
            config.setMaximumPoolSize(10);

            dataSource = new HikariDataSource(config);

            Map<String, Object> props = new HashMap<>();
            props.put("jakarta.persistence.nonJtaDataSource", dataSource);

            emf = Persistence.createEntityManagerFactory("myLibraryPU", props);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError("Initializarea a esuat: " + e.getMessage());
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }
}