package org.example.repository.factory;

import org.example.domain.Autor;
import org.example.domain.Carte;
import org.example.domain.Editura;
import org.example.repository.CartiHibernateRepository;
import org.example.repository.HibernateRepository;
import org.example.repository.Repository;

public class RepositoryFactory {
    private static RepositoryFactory instance = new RepositoryFactory();

    public Repository createRepository(RepositoryEntity repositoryEntity) {
        switch (repositoryEntity) {
            case AUTORI:
                return new HibernateRepository<>(Autor.class);
            case EDITURI:
                return new HibernateRepository<>(Editura.class);
            case CARTI:
                return new CartiHibernateRepository();
            default:
                throw new IllegalArgumentException("Entitate necunoscuta!");
        }
    }

    public static RepositoryFactory getInstance() {
        return instance;
    }
}