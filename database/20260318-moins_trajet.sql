-- 1. Séquence pour l'assignation
DROP TABLE IF EXISTS assignation CASCADE;

DROP SEQUENCE IF EXISTS seq_assignation;
CREATE SEQUENCE seq_assignation;

-- 2. Table assignation
-- Cette table permet de stocker les affectations de véhicules aux réservations.
-- Elle permet de gérer la réutilisation d'un véhicule pour plusieurs trajets 
-- dans la même journée (il redevient disponible après son arrivée prévue).
CREATE TABLE assignation (
    id_assignation VARCHAR(50) PRIMARY KEY,
    id_vehicule VARCHAR(50) NOT NULL,
    id_reservation VARCHAR(50) NOT NULL,
    nbr_passager INTEGER NOT NULL CHECK (nbr_passager > 0), -- Nombre de passagers de la réservation affectés à ce véhicule
    date_assignation TIMESTAMP NOT NULL,
    heure_depart_prevue TIMESTAMP NOT NULL,
    heure_arrivee_prevue TIMESTAMP NOT NULL,
    num_trajet INTEGER NOT NULL DEFAULT 1, -- Indique s'il s'agit du 1er, 2ème... trajet du véhicule ce jour-là
    CONSTRAINT fk_assignation_vehicule FOREIGN KEY (id_vehicule) REFERENCES vehicule(id_vehicule) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_assignation_reservation FOREIGN KEY (id_reservation) REFERENCES reservation(id_reservation) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- Index pour accélérer les recherches par véhicule et par date
CREATE INDEX idx_assignation_vehicule_date ON assignation(id_vehicule, date_assignation);
CREATE INDEX idx_assignation_reservation ON assignation(id_reservation);

 DROP TABLE IF EXISTS trajet CASCADE;
 
 DROP SEQUENCE IF EXISTS seq_trajet;
 CREATE SEQUENCE seq_trajet;
 
 CREATE TABLE trajet (
     id_trajet VARCHAR(50) PRIMARY KEY,
     id_vehicule VARCHAR(50) NOT NULL,
     date_trajet TIMESTAMP NOT NULL,
     heure_depart_aeroport TIMESTAMP NOT NULL,
     heure_arrivee_aeroport TIMESTAMP NOT NULL,
     CONSTRAINT fk_trajet_vehicule FOREIGN KEY (id_vehicule) REFERENCES vehicule(id_vehicule) ON UPDATE CASCADE ON DELETE RESTRICT,
     CONSTRAINT uq_trajet_vehicule_slot UNIQUE (id_vehicule, heure_depart_aeroport, heure_arrivee_aeroport)
 );
 
 CREATE INDEX idx_trajet_vehicule_date ON trajet(id_vehicule, date_trajet);
