package com.taxi.model;

import framework.annotation.Column;
import framework.annotation.Table;
import framework.utilitaire.Model;

@Table(name = "lieuhotel")
public class LieuHotel extends Model {
    @Column(name = "id_lieu")
    private String idLieu;

    @Column(name = "nom_lieu")
    private String nomLieu;

    @Column(name = "ville")
    private String ville;

    public LieuHotel() {
    }

    public String getIdLieu() {
        return idLieu;
    }

    public void setIdLieu(String idLieu) {
        this.idLieu = idLieu;
    }

    public String getNomLieu() {
        return nomLieu;
    }

    public void setNomLieu(String nomLieu) {
        this.nomLieu = nomLieu;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }
}
