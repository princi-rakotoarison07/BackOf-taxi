package com.taxi.model;

import framework.annotation.Column;
import framework.annotation.Table;
import framework.utilitaire.Model;

@Table(name = "hotel")
public class Hotel extends Model {
    @Column(name = "id_hotel")
    private String idHotel;

    @Column(name = "nom_hotel")
    private String nomHotel;

    public Hotel() {}

    public String getIdHotel() { return idHotel; }
    public void setIdHotel(String idHotel) { this.idHotel = idHotel; }

    public String getNomHotel() { return nomHotel; }
    public void setNomHotel(String nomHotel) { this.nomHotel = nomHotel; }
}
