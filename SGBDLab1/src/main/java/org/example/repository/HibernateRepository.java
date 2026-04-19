package org.example.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.util.HibernateUtil;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HibernateRepository<ID extends Serializable, T extends org.example.domain.BaseEntity<ID>>
        implements Repository<ID, T> {

    protected final Class<T> type;

    public HibernateRepository(Class<T> type) {
        this.type = type;
    }

    @Override
    public T find(ID id) {
        if (id == null) throw new IllegalArgumentException("ID-ul nu poate fi null!");
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            return em.find(type, id);
        }
    }

    public void add(T entity) {
        executeTransaction(em -> em.persist(entity));
    }

    public void update(T entity) {
        executeTransaction(em -> em.merge(entity));
    }

    public void delete(ID id) {
        executeTransaction(em -> {
            T entity = em.find(type, id);
            if (entity != null) {
                em.remove(entity);
            }
        });
    }

    @Override
    public Iterable<T> getAll() {
        try (EntityManager em = HibernateUtil.getEntityManager()) {
            return em.createQuery("from " + type.getSimpleName(), type).getResultList();
        }
    }

    @Override
    public Map<ID, T> getMap() {
        Map<ID, T> map = new HashMap<>();
        for (T entity : getAll()) {
            map.put(entity.getId(), entity);
        }
        return map;
    }

    @Override
    public void clear() {
        executeTransaction(em -> em.createQuery("delete from " + type.getSimpleName()).executeUpdate());
    }

    private void executeTransaction(Consumer<EntityManager> action) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            action.accept(em);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}