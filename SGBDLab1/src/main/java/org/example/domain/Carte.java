package org.example.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "carti")
public class Carte extends BaseEntity<Integer> {

    private String titlu;

    @Column(name = "an_aparitie")
    private Integer anAparitie;

    private String isbn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_editura")
    private Editura editura;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "carti_autori",
            joinColumns = @JoinColumn(name = "id_carte"),
            inverseJoinColumns = @JoinColumn(name = "id_autor")
    )
    private Set<Autor> autori = new HashSet<>();

    public Carte() {}

    public Carte(Integer id, String titlu, Integer anAparitie, String isbn, Editura editura) {
        super(id);
        this.titlu = titlu;
        this.anAparitie = anAparitie;
        this.isbn = isbn;
        this.editura = editura;
    }

    public String getTitlu() {
        return titlu;
    }
    public void setTitlu(String titlu) {
        this.titlu = titlu;
    }
    public Integer getAnAparitite() {
        return anAparitie;
    }

    public void setAnAparitite(Integer anAparitite) {
        this.anAparitie = anAparitite;
    }

    public String getIsbn() {
        return isbn;
    }
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    public Editura getEditura() {
        return editura;
    }

    public void setEditura(Editura editura) {
        this.editura = editura;
    }
}
