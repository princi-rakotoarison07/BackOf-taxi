package com.taxi.model;

import framework.annotation.Column;
import framework.annotation.Table;
import framework.utilitaire.Model;
import java.sql.Timestamp;

@Table(name = "reservation")
public class Reservation extends Model {
    @Column(name = "id_reservation")
    private String idReservation;

    @Column(name = "id_client")
    private String idClient;

    @Column(name = "nbr_passager")
    private Integer nbrPassager;

    @Column(name = "id_hotel")
    private String idHotel;

    @Column(name = "date_resa")
    private Timestamp dateResa;

    public Reservation() {}

    public String getIdReservation() { return idReservation; }
    public void setIdReservation(String idReservation) { this.idReservation = idReservation; }

    public String getIdClient() { return idClient; }
    public void setIdClient(String idClient) { this.idClient = idClient; }

    public Integer getNbrPassager() { return nbrPassager; }
    public void setNbrPassager(Integer nbrPassager) { this.nbrPassager = nbrPassager; }

    public String getIdHotel() { return idHotel; }
    public void setIdHotel(String idHotel) { this.idHotel = idHotel; }

    public Timestamp getDateResa() { return dateResa; }
    public void setDateResa(Timestamp dateResa) { this.dateResa = dateResa; }
}
