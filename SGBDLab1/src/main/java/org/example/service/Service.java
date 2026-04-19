package org.example.service;

import org.example.domain.Autor;
import org.example.domain.Carte;
import org.example.domain.Editura;
import org.example.repository.CartiHibernateRepository; // Importul nou
import org.example.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public class Service {
    private final Repository<Integer, Editura> edituraRepository;
    private final Repository<Integer, Autor> autorRepository;
    private final CartiHibernateRepository cartiRepo;

    public Service(Repository<Integer, Editura> edituraRepository,
                   Repository<Integer, Autor> autorRepository,
                   Repository<Integer, Carte> cartiRepo) {
        this.edituraRepository = edituraRepository;
        this.autorRepository = autorRepository;
        this.cartiRepo = (CartiHibernateRepository) cartiRepo;
    }

    public Iterable<Editura> getAllEdituri() {
        return edituraRepository.getAll();
    }

    public Editura getEdituraById(Integer id) {
        return edituraRepository.find(id);
    }

    /**
     * Cerința Master-Detail: Folosim metoda specifica din CartiHibernateRepository
     */
    public Iterable<Carte> getCartiByEditura(Integer idEditura) {
        if (idEditura == null) return new ArrayList<>();
        return cartiRepo.findByEditura(idEditura);
    }

    public void addCarte(String titlu, Integer an, String isbn, Editura editura) {
        if (titlu == null || titlu.isEmpty() || isbn == null || editura == null) {
            throw new IllegalArgumentException("Campurile Titlu, ISBN si Editura sunt obligatorii!");
        }

        Carte carte = new Carte(null, titlu, an, isbn, editura);
        cartiRepo.add(carte);
    }

    public void updateCarte(Integer id, String titlu, Integer an, String isbn, Editura editura) {
        if (id == null) throw new IllegalArgumentException("ID-ul cartii nu poate fi null la update!");

        Carte carte = new Carte(id, titlu, an, isbn, editura);
        cartiRepo.update(carte);
    }

    public void deleteCarte(Integer id) {
        if (id == null) throw new IllegalArgumentException("Selectati o carte pentru a o sterge!");
        cartiRepo.delete(id);
    }

    public Iterable<Carte> getAllCarti() {
        return cartiRepo.getAll();
    }
}