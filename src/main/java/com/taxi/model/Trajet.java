package com.taxi.model;

import framework.annotation.Column;
import framework.annotation.Id;
import framework.annotation.Table;
import framework.utilitaire.Model;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

@Table(name = "trajet")
public class Trajet extends Model {
    @Id
    @Column(name = "id_trajet")
    private String idTrajet;

    @Column(name = "id_vehicule")
    private String idVehicule;

    @Column(name = "date_trajet")
    private Timestamp dateTrajet;

    @Column(name = "heure_depart_aeroport")
    private Timestamp heureDepartAeroport;

    @Column(name = "heure_arrivee_aeroport")
    private Timestamp heureArriveeAeroport;

    public Trajet() {
    }

    public String getIdTrajet() {
        return idTrajet;
    }

    public void setIdTrajet(String idTrajet) {
        this.idTrajet = idTrajet;
    }

    public String getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(String idVehicule) {
        this.idVehicule = idVehicule;
    }

    public Timestamp getDateTrajet() {
        return dateTrajet;
    }

    public void setDateTrajet(Timestamp dateTrajet) {
        this.dateTrajet = dateTrajet;
    }

    public Timestamp getHeureDepartAeroport() {
        return heureDepartAeroport;
    }

    public void setHeureDepartAeroport(Timestamp heureDepartAeroport) {
        this.heureDepartAeroport = heureDepartAeroport;
    }

    public Timestamp getHeureArriveeAeroport() {
        return heureArriveeAeroport;
    }

    public void setHeureArriveeAeroport(Timestamp heureArriveeAeroport) {
        this.heureArriveeAeroport = heureArriveeAeroport;
    }

    @Override
    public void insert(Connection conn) throws Exception {
        if (this.idTrajet == null || this.idTrajet.isEmpty()) {
            this.idTrajet = generateId(conn);
        }
        super.insert(conn);
    }

    private String generateId(Connection conn) throws Exception {
        String sql = "SELECT nextval('seq_trajet')";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                long val = rs.getLong(1);
                return String.format("TRJ%04d", val);
            }
        }
        throw new Exception("Impossible de générer l'ID Trajet");
    }
}
