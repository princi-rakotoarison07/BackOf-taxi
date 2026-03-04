package com.taxi.model;

import framework.annotation.Column;
import framework.annotation.Table;
import framework.utilitaire.Model;
import java.math.BigDecimal;

@Table(name = "distance")
public class Distance extends Model {
    @Column(name = "id_distance")
    private String idDistance;

    @Column(name = "lieu_from")
    private String lieuFrom;

    @Column(name = "lieu_to")
    private String lieuTo;

    @Column(name = "kilometre")
    private BigDecimal kilometre;

    public Distance() {}

    public String getIdDistance() { return idDistance; }
    public void setIdDistance(String idDistance) { this.idDistance = idDistance; }

    public String getLieuFrom() { return lieuFrom; }
    public void setLieuFrom(String lieuFrom) { this.lieuFrom = lieuFrom; }

    public String getLieuTo() { return lieuTo; }
    public void setLieuTo(String lieuTo) { this.lieuTo = lieuTo; }

    public BigDecimal getKilometre() { return kilometre; }
    public void setKilometre(BigDecimal kilometre) { this.kilometre = kilometre; }
}
