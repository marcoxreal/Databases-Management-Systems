package org.example.repository;

import org.example.domain.Editura;

public class EdituriHibernateRepository extends HibernateRepository<Integer, Editura> {
    public EdituriHibernateRepository() {
        super(Editura.class);
    }
}