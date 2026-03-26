-- db: taxi_transfer

-- Suppression des tables si elles existent
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS assignation_reservation CASCADE;
DROP TABLE IF EXISTS trajet_execute CASCADE;
DROP TABLE IF EXISTS vehicule_nombre_trajets CASCADE;
DROP TABLE IF EXISTS disponibilite_vehicule CASCADE;
DROP TABLE IF EXISTS hotel CASCADE;
DROP TABLE IF EXISTS lieuhotel CASCADE;
DROP TABLE IF EXISTS parametre CASCADE;
DROP TABLE IF EXISTS distance CASCADE;
DROP TABLE IF EXISTS token CASCADE;

-- Suppression des séquences si elles existent
DROP SEQUENCE IF EXISTS seq_hotel;
DROP SEQUENCE IF EXISTS seq_reservation;
DROP SEQUENCE IF EXISTS seq_token;


CREATE TABLE lieuhotel (
    id_lieu VARCHAR(50) PRIMARY KEY,
    nom_lieu VARCHAR(150) NOT NULL,
    ville VARCHAR(100) NOT NULL
);
CREATE TABLE hotel (
    id_hotel VARCHAR(50) PRIMARY KEY,
    nom_hotel VARCHAR(150) NOT NULL,
    id_lieu VARCHAR(50),
    
    CONSTRAINT fk_hotel_lieu
        FOREIGN KEY (id_lieu)
        REFERENCES lieuhotel(id_lieu)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE TABLE parametre (
    id_parametre VARCHAR(50) PRIMARY KEY,
    vitesse_moyenne DECIMAL(5,2) NOT NULL,  -- en km/h
    temps_attente INTEGER NOT NULL          -- en minutes
);

CREATE TABLE distance (
    id_distance VARCHAR(50) PRIMARY KEY,
    lieu_from VARCHAR(50) NOT NULL,
    lieu_to VARCHAR(50) NOT NULL,
    kilometre DECIMAL(8,2) NOT NULL CHECK (kilometre > 0),
    
    CONSTRAINT fk_distance_lieu_from
        FOREIGN KEY (lieu_from)
        REFERENCES lieuhotel(id_lieu)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
        
    CONSTRAINT fk_distance_lieu_to
        FOREIGN KEY (lieu_to)
        REFERENCES lieuhotel(id_lieu)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE reservation (
    id_reservation VARCHAR(50) PRIMARY KEY,
    id_client VARCHAR(50) NOT NULL,       
    nbr_passager INTEGER NOT NULL CHECK (nbr_passager > 0),
    id_hotel VARCHAR(50) NOT NULL,
    date_resa TIMESTAMP NOT NULL,         

    CONSTRAINT fk_reservation_hotel
        FOREIGN KEY (id_hotel)
        REFERENCES hotel(id_hotel)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- Séquences pour la génération d'IDs personnalisés
CREATE SEQUENCE seq_hotel;
CREATE SEQUENCE seq_reservation;
CREATE SEQUENCE seq_type_carburant;
CREATE SEQUENCE seq_vehicule;

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
    
    CONSTRAINT fk_vehicule_type_carburant
        FOREIGN KEY (id_type_carburant)
        REFERENCES type_carburant(id_type_carburant)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE disponibilite_vehicule (
    id_vehicule VARCHAR(50) PRIMARY KEY,
    heure_debut TIME NOT NULL DEFAULT TIME '00:00:00',
    heure_fin TIME NOT NULL DEFAULT TIME '23:59:59',
    date_maj TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_disponibilite_vehicule
        FOREIGN KEY (id_vehicule)
        REFERENCES vehicule(id_vehicule)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE assignation_reservation (
    id_assignation SERIAL PRIMARY KEY,
    id_reservation VARCHAR(50) NOT NULL,
    id_vehicule VARCHAR(50),
    passagers_assignes INTEGER NOT NULL DEFAULT 0 CHECK (passagers_assignes >= 0),
    passagers_non_assignes INTEGER NOT NULL DEFAULT 0 CHECK (passagers_non_assignes >= 0),
    heure_depart TIMESTAMP,
    heure_retour TIMESTAMP,
    date_calcul TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_assignation_reservation
        FOREIGN KEY (id_reservation)
        REFERENCES reservation(id_reservation)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_assignation_vehicule
        FOREIGN KEY (id_vehicule)
        REFERENCES vehicule(id_vehicule)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE TABLE trajet_execute (
    id_trajet SERIAL PRIMARY KEY,
    date_jour DATE NOT NULL,
    id_vehicule VARCHAR(50) NOT NULL,
    heure_depart TIMESTAMP NOT NULL,
    heure_retour TIMESTAMP NOT NULL,
    nb_reservations INTEGER NOT NULL DEFAULT 0 CHECK (nb_reservations >= 0),
    total_passagers INTEGER NOT NULL DEFAULT 0 CHECK (total_passagers >= 0),
    reservations_detail TEXT,
    date_calcul TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trajet_vehicule
        FOREIGN KEY (id_vehicule)
        REFERENCES vehicule(id_vehicule)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE vehicule_nombre_trajets (
    date_jour DATE NOT NULL,
    id_vehicule VARCHAR(50) NOT NULL,
    nombre_trajets INTEGER NOT NULL DEFAULT 0 CHECK (nombre_trajets >= 0),
    date_calcul TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (date_jour, id_vehicule),

    CONSTRAINT fk_nombre_trajets_vehicule
        FOREIGN KEY (id_vehicule)
        REFERENCES vehicule(id_vehicule)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- Insertion des types de carburant par défaut
INSERT INTO type_carburant (id_type_carburant, code, libelle) VALUES 
('TC0001', 'D', 'Diesel'),
('TC0002', 'E', 'Essence'),
('TC0003', 'El', 'Electrique'),
('TC0004', 'H', 'Hybride');

CREATE SEQUENCE seq_token;

CREATE TABLE token (
    id_token VARCHAR(50) PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    date_heure_expiration TIMESTAMP NOT NULL
);


