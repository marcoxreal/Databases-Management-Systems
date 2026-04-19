package org.example.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "edituri")
public class Editura extends BaseEntity<Integer> {

    private String nume;
    private String adresa;
    private String website;

    @OneToMany(mappedBy = "editura", cascade = CascadeType.ALL)
    private List<Carte> carti = new ArrayList<>();

    public Editura() {}

    public Editura(Integer id, String nume, String adresa, String website) {
        super(id);
        this.nume = nume;
        this.adresa = adresa;
        this.website = website;
    }

    public String getNume() { return nume; }
    public void setNume(String nume) { this.nume = nume; }

    public String getAdresa() { return adresa; }
    public void setAdresa(String adresa) { this.adresa = adresa; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public List<Carte> getCarti() { return carti; }
    public void setCarti(List<Carte> carti) { this.carti = carti; }
}
