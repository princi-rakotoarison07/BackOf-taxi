package com.taxi.model;

import framework.annotation.Column;
import framework.annotation.Table;
import framework.utilitaire.Model;
import java.sql.Timestamp;

@Table(name = "assignation")
public class Assignation extends Model {
    @Column(name = "id_assignation")
    private String idAssignation;

    @Column(name = "id_vehicule")
    private String idVehicule;

    @Column(name = "id_reservation")
    private String idReservation;

    @Column(name = "nbr_passager")
    private Integer nbrPassager;

    @Column(name = "date_assignation")
    private Timestamp dateAssignation;

    @Column(name = "heure_depart_prevue")
    private Timestamp heureDepartPrevue;

    @Column(name = "heure_arrivee_prevue")
    private Timestamp heureArriveePrevue;

    @Column(name = "num_trajet")
    private Integer numTrajet;

    public Assignation() {}

    public String getIdAssignation() { return idAssignation; }
    public void setIdAssignation(String idAssignation) { this.idAssignation = idAssignation; }

    public String getIdVehicule() { return idVehicule; }
    public void setIdVehicule(String idVehicule) { this.idVehicule = idVehicule; }

    public String getIdReservation() { return idReservation; }
    public void setIdReservation(String idReservation) { this.idReservation = idReservation; }

    public Integer getNbrPassager() { return nbrPassager; }
    public void setNbrPassager(Integer nbrPassager) { this.nbrPassager = nbrPassager; }

    public Timestamp getDateAssignation() { return dateAssignation; }
    public void setDateAssignation(Timestamp dateAssignation) { this.dateAssignation = dateAssignation; }

    public Timestamp getHeureDepartPrevue() { return heureDepartPrevue; }
    public void setHeureDepartPrevue(Timestamp heureDepartPrevue) { this.heureDepartPrevue = heureDepartPrevue; }

    public Timestamp getHeureArriveePrevue() { return heureArriveePrevue; }
    public void setHeureArriveePrevue(Timestamp heureArriveePrevue) { this.heureArriveePrevue = heureArriveePrevue; }

    public Integer getNumTrajet() { return numTrajet; }
    public void setNumTrajet(Integer numTrajet) { this.numTrajet = numTrajet; }
}
