package com.taxi.model;

import framework.annotation.Column;
import framework.annotation.Id;
import framework.annotation.Table;
import framework.utilitaire.Model;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Table(name = "vehicule")
public class Vehicule extends Model {
    @Id
    @Column(name = "id_vehicule")
    private String idVehicule;

    @Column(name = "reference")
    private String reference;

    @Column(name = "nbr_place")
    private Integer nbrPlace;

    @Column(name = "id_type_carburant")
    private String idTypeCarburant;

    @Column(name = "heure_disponible")
    private java.sql.Time heureDisponible;

    public Vehicule() {
    }

    public String getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(String idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Integer getNbrPlace() {
        return nbrPlace;
    }

    public void setNbrPlace(Integer nbrPlace) {
        this.nbrPlace = nbrPlace;
    }

    public String getIdTypeCarburant() {
        return idTypeCarburant;
    }

    public void setIdTypeCarburant(String idTypeCarburant) {
        this.idTypeCarburant = idTypeCarburant;
    }

    public java.sql.Time getHeureDisponible() {
        return heureDisponible;
    }

    public void setHeureDisponible(java.sql.Time heureDisponible) {
        this.heureDisponible = heureDisponible;
    }

    @Override
    public void insert(Connection conn) throws Exception {
        if (this.idVehicule == null || this.idVehicule.isEmpty()) {
            this.idVehicule = generateId(conn);
        }
        super.insert(conn);
    }

    private String generateId(Connection conn) throws Exception {
        String sql = "SELECT nextval('seq_vehicule')";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                long val = rs.getLong(1);
                return String.format("VEH%04d", val);
            }
        }
        throw new Exception("Impossible de générer l'ID Vehicule");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicule vehicule = (Vehicule) o;
        return idVehicule != null ? idVehicule.equals(vehicule.idVehicule) : vehicule.idVehicule == null;
    }

    @Override
    public int hashCode() {
        return idVehicule != null ? idVehicule.hashCode() : 0;
    }
}
