package testFramework.com.testframework.model;

import framework.annotation.Column;
import framework.annotation.Table;
import framework.utilitaire.Model;

@Table(name = "employes")
public class Employe extends Model {
    @Column(name = "nom")
    private String nom;
    @Column(name = "prenom")
    private String prenom;
    @Column(name = "age")
    private Integer age;
    @Column(name = "poste")
    private String poste;
    private Departement dept;

    public Employe() {}

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }

    public Departement getDept() { return dept; }
    public void setDept(Departement dept) { this.dept = dept; }

    
}
