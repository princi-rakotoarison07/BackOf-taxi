package com.taxi.model;

import framework.annotation.Column;
import framework.annotation.Id;
import framework.annotation.Table;
import framework.utilitaire.Model;
import java.math.BigDecimal;

@Table(name = "parametre")
public class Parametre extends Model {
    
    @Id
    @Column(name = "id_parametre")
    private String idParametre;
    
    @Column(name = "vitesse_moyenne")
    private BigDecimal vitesseMoyenne;
    
    @Column(name = "temps_attente")
    private Integer tempsAttente;
    
    public Parametre() {}
    
    public Parametre(String idParametre, BigDecimal vitesseMoyenne, Integer tempsAttente) {
        this.idParametre = idParametre;
        this.vitesseMoyenne = vitesseMoyenne;
        this.tempsAttente = tempsAttente;
    }
    
    public String getIdParametre() {
        return idParametre;
    }
    
    public void setIdParametre(String idParametre) {
        this.idParametre = idParametre;
    }
    
    public BigDecimal getVitesseMoyenne() {
        return vitesseMoyenne;
    }
    
    public void setVitesseMoyenne(String vitesseMoyenne) {
        if (vitesseMoyenne == null || vitesseMoyenne.trim().isEmpty()) {
            this.vitesseMoyenne = null;
            return;
        }
        this.vitesseMoyenne = new BigDecimal(vitesseMoyenne.trim());
    }
    
    public Integer getTempsAttente() {
        return tempsAttente;
    }
    
    public void setTempsAttente(Integer tempsAttente) {
        this.tempsAttente = tempsAttente;
    }
    
    @Override
    public String toString() {
        return "Parametre{" +
                "idParametre='" + idParametre + '\'' +
                ", vitesseMoyenne=" + vitesseMoyenne +
                ", tempsAttente=" + tempsAttente +
                '}';
    }
}
