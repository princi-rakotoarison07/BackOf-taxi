package testFramework.com.testframework.model;

import framework.annotation.Column;
import framework.annotation.Table;
import framework.utilitaire.Model;

@Table(name = "departements")
public class Departement extends Model {
    @Column(name = "id_dept")
    private int id;
    @Column(name = "nom_dept")
    private String nom;
    private String description;
    private Ville ville;

    public Departement() {}

    public Departement(int id, String nom, String description) {
        this.id = id;
        this.nom = nom;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Ville getVille() {
        return ville;
    }

    public void setVille(Ville ville) {
        this.ville = ville;
    }
}
