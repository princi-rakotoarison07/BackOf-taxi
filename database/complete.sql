DROP TABLE IF EXISTS trajet CASCADE;
DROP TABLE IF EXISTS assignation CASCADE;
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS vehicule CASCADE;
DROP TABLE IF EXISTS type_carburant CASCADE;
DROP TABLE IF EXISTS distance CASCADE;
DROP TABLE IF EXISTS parametre CASCADE;
DROP TABLE IF EXISTS hotel CASCADE;
DROP TABLE IF EXISTS lieuhotel CASCADE;
DROP TABLE IF EXISTS token CASCADE;
DROP SEQUENCE IF EXISTS seq_hotel;
DROP SEQUENCE IF EXISTS seq_reservation;
DROP SEQUENCE IF EXISTS seq_type_carburant;
DROP SEQUENCE IF EXISTS seq_vehicule;
DROP SEQUENCE IF EXISTS seq_assignation;
DROP SEQUENCE IF EXISTS seq_trajet;
DROP SEQUENCE IF EXISTS seq_token;
CREATE SEQUENCE seq_hotel;
CREATE SEQUENCE seq_reservation;
CREATE SEQUENCE seq_type_carburant;
CREATE SEQUENCE seq_vehicule;
CREATE SEQUENCE seq_assignation;
CREATE SEQUENCE seq_trajet;
CREATE SEQUENCE seq_token;
CREATE TABLE lieuhotel (
    id_lieu VARCHAR(50) PRIMARY KEY,
    nom_lieu VARCHAR(150) NOT NULL,
    ville VARCHAR(100) NOT NULL
);
CREATE TABLE hotel (
    id_hotel VARCHAR(50) PRIMARY KEY,
    nom_hotel VARCHAR(150) NOT NULL,
    id_lieu VARCHAR(50),
    CONSTRAINT fk_hotel_lieu FOREIGN KEY (id_lieu) REFERENCES lieuhotel(id_lieu) ON UPDATE CASCADE ON DELETE SET NULL
);
CREATE TABLE parametre (
    id_parametre VARCHAR(50) PRIMARY KEY,
    vitesse_moyenne DECIMAL(5,2) NOT NULL,
    temps_attente INTEGER NOT NULL
);
CREATE TABLE distance (
    id_distance VARCHAR(50) PRIMARY KEY,
    lieu_from VARCHAR(50) NOT NULL,
    lieu_to VARCHAR(50) NOT NULL,
    kilometre DECIMAL(8,2) NOT NULL CHECK (kilometre > 0),
    CONSTRAINT fk_distance_lieu_from FOREIGN KEY (lieu_from) REFERENCES lieuhotel(id_lieu) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_distance_lieu_to FOREIGN KEY (lieu_to) REFERENCES lieuhotel(id_lieu) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE TABLE type_carburant (
    id_type_carburant VARCHAR(50) PRIMARY KEY,
    code VARCHAR(5) NOT NULL UNIQUE,
    libelle VARCHAR(50) NOT NULL
);
CREATE TABLE vehicule (
    id_vehicule VARCHAR(50) PRIMARY KEY,
    reference VARCHAR(50) NOT NULL,
    nbr_place INTEGER NOT NULL CHECK (nbr_place > 0),
    id_type_carburant VARCHAR(50) NOT NULL,
    heure_disponible TIME NULL,
    CONSTRAINT fk_vehicule_type_carburant FOREIGN KEY (id_type_carburant) REFERENCES type_carburant(id_type_carburant) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE TABLE reservation (
    id_reservation VARCHAR(50) PRIMARY KEY,
    id_client VARCHAR(50) NOT NULL,
    nbr_passager INTEGER NOT NULL CHECK (nbr_passager > 0),
    id_hotel VARCHAR(50) NOT NULL,
    date_resa TIMESTAMP NOT NULL,
    CONSTRAINT fk_reservation_hotel FOREIGN KEY (id_hotel) REFERENCES hotel(id_hotel) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE TABLE assignation (
    id_assignation VARCHAR(50) PRIMARY KEY,
    id_vehicule VARCHAR(50) NOT NULL,
    id_reservation VARCHAR(50) NOT NULL,
    nbr_passager INTEGER NOT NULL CHECK (nbr_passager > 0),
    date_assignation TIMESTAMP NOT NULL,
    heure_depart_prevue TIMESTAMP NOT NULL,
    heure_arrivee_prevue TIMESTAMP NOT NULL,
    num_trajet INTEGER NOT NULL DEFAULT 1,
    CONSTRAINT fk_assignation_vehicule FOREIGN KEY (id_vehicule) REFERENCES vehicule(id_vehicule) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_assignation_reservation FOREIGN KEY (id_reservation) REFERENCES reservation(id_reservation) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX idx_assignation_vehicule_date ON assignation(id_vehicule, date_assignation);
CREATE INDEX idx_assignation_reservation ON assignation(id_reservation);
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
CREATE TABLE token (
    id_token VARCHAR(50) PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    date_heure_expiration TIMESTAMP NOT NULL
);
INSERT INTO type_carburant (id_type_carburant, code, libelle) VALUES
('TC0001', 'D', 'Diesel'),
('TC0002', 'E', 'Essence'),
('TC0003', 'El', 'Electrique'),
('TC0004', 'H', 'Hybride');
