package com.taxi.model;

import framework.annotation.Column;
import framework.annotation.Table;
import framework.utilitaire.Model;

@Table(name = "type_carburant")
public class TypeCarburant extends Model {
    @Column(name = "id_type_carburant")
    private String idTypeCarburant;

    @Column(name = "code")
    private String code;

    @Column(name = "libelle")
    private String libelle;

    public TypeCarburant() {}

    public String getIdTypeCarburant() { return idTypeCarburant; }
    public void setIdTypeCarburant(String idTypeCarburant) { this.idTypeCarburant = idTypeCarburant; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
