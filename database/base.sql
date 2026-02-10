-- db: taxi_transfer

-- Suppression des tables si elles existent
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS hotel CASCADE;

-- Suppression des séquences si elles existent
DROP SEQUENCE IF EXISTS seq_hotel;
DROP SEQUENCE IF EXISTS seq_reservation;

CREATE TABLE hotel (
    id_hotel VARCHAR(50) PRIMARY KEY,
    nom_hotel VARCHAR(150) NOT NULL
);

CREATE TABLE reservation (
    id_reservation VARCHAR(50) PRIMARY KEY,
    id_client VARCHAR(50) NOT NULL,       -- peut contenir des lettres
    nbr_passager INTEGER NOT NULL CHECK (nbr_passager > 0),
    id_hotel VARCHAR(50) NOT NULL,
    date_resa TIMESTAMP NOT NULL,         -- Changé en TIMESTAMP pour date et heure

    CONSTRAINT fk_reservation_hotel
        FOREIGN KEY (id_hotel)
        REFERENCES hotel(id_hotel)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- Séquences pour la génération d'IDs personnalisés
CREATE SEQUENCE seq_hotel;
CREATE SEQUENCE seq_reservation;


