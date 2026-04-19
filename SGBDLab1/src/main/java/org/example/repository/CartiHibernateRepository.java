package org.example.repository;

import jakarta.persistence.EntityManager;
import org.example.domain.Carte;
import org.example.util.HibernateUtil;
import java.util.List;

public class CartiHibernateRepository extends HibernateRepository<Integer, Carte> {

    public CartiHibernateRepository() {
        super(Carte.class);
    }

    /**
     * Hibernate face JOIN-ul automat prin obiectul Editura din entitate.
     */
    public Iterable<Carte> findByEditura(Integer idEditura) {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            return em.createQuery(
                            "SELECT c FROM Carte c WHERE c.editura.id = :idEd", Carte.class)
                    .setParameter("idEd", idEditura)
                    .getResultList();
        }
    }
}