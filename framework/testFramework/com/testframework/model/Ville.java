package testFramework.com.testframework.model;

import framework.annotation.Column;
import framework.annotation.Table;
import framework.utilitaire.Model;

@Table(name = "villes")
public class Ville extends Model {
    @Column(name = "id_ville")
    private Integer id;
    @Column(name = "nom_ville")
    private String nom;
    private String codePostal;

    public Ville() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCodePostal() { return codePostal; }
    public void setCodePostal(String codePostal) { this.codePostal = codePostal; }
}
