package com.taxi.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * Représente un trajet : un véhicule assigné à un groupe de réservations
 * avec son heure de départ réelle, la liste des réservations/hôtels desservis,
 * la distance parcourue et l'heure de retour.
 */
public class Trajet {

    private Vehicule vehicule;
    private Timestamp heureDepart;
    private Timestamp heureRetour;
    private BigDecimal kmParcouru;
    private List<Reservation> reservations;

    public Trajet() {}

    public Trajet(Vehicule vehicule, Timestamp heureDepart, Timestamp heureRetour,
                  BigDecimal kmParcouru, List<Reservation> reservations) {
        this.vehicule = vehicule;
        this.heureDepart = heureDepart;
        this.heureRetour = heureRetour;
        this.kmParcouru = kmParcouru;
        this.reservations = reservations;
    }

    public Vehicule getVehicule() { return vehicule; }
    public void setVehicule(Vehicule vehicule) { this.vehicule = vehicule; }

    public Timestamp getHeureDepart() { return heureDepart; }
    public void setHeureDepart(Timestamp heureDepart) { this.heureDepart = heureDepart; }

    public Timestamp getHeureRetour() { return heureRetour; }
    public void setHeureRetour(Timestamp heureRetour) { this.heureRetour = heureRetour; }

    public BigDecimal getKmParcouru() { return kmParcouru; }
    public void setKmParcouru(BigDecimal kmParcouru) { this.kmParcouru = kmParcouru; }

    public List<Reservation> getReservations() { return reservations; }
    public void setReservations(List<Reservation> reservations) { this.reservations = reservations; }
}
