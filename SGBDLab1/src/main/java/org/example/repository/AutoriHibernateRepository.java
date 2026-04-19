package org.example.repository;

import org.example.domain.Autor;

public class AutoriHibernateRepository extends HibernateRepository<Integer, Autor> {
    public AutoriHibernateRepository() {
        super(Autor.class);
    }
}