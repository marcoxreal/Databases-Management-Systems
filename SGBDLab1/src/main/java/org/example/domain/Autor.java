package org.example.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "autori")
public class Autor extends BaseEntity<Integer> {

    @Column(name = "nume_autor")
    private String nume;

    private String nationalitate;

    @ManyToMany(mappedBy = "autori")
    private Set<Carte> carti = new HashSet<>();

    public Autor() {}

    public Autor(Integer id, String nume, String nationalitate) {
        this.setId(id);
        this.nume = nume;
        this.nationalitate = nationalitate;
    }

    public String getNume() {
        return nume;
    }
    public void setNume(String nume) {
        this.nume = nume;
    }
    public String getNationalitate() {
        return nationalitate;
    }
    public void setNationalitate(String nationalitate) {
        this.nationalitate = nationalitate;
    }
}
