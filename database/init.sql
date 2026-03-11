-- 1. Suppression des tables (Ordre respectant les contraintes FK)
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS vehicule CASCADE;
DROP TABLE IF EXISTS type_carburant CASCADE;
DROP TABLE IF EXISTS distance CASCADE;
DROP TABLE IF EXISTS parametre CASCADE;
DROP TABLE IF EXISTS hotel CASCADE;
DROP TABLE IF EXISTS lieuhotel CASCADE;

-- 2. Suppression et recréation des séquences
DROP SEQUENCE IF EXISTS seq_hotel;
DROP SEQUENCE IF EXISTS seq_reservation;
DROP SEQUENCE IF EXISTS seq_type_carburant;
DROP SEQUENCE IF EXISTS seq_vehicule;

CREATE SEQUENCE seq_hotel;
CREATE SEQUENCE seq_reservation;
CREATE SEQUENCE seq_type_carburant;
CREATE SEQUENCE seq_vehicule;

-- 3. Création des tables
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

CREATE TABLE reservation (
    id_reservation VARCHAR(50) PRIMARY KEY,
    id_client VARCHAR(50) NOT NULL,
    nbr_passager INTEGER NOT NULL CHECK (nbr_passager > 0),
    id_hotel VARCHAR(50) NOT NULL,
    date_resa TIMESTAMP NOT NULL,
    CONSTRAINT fk_reservation_hotel FOREIGN KEY (id_hotel) REFERENCES hotel(id_hotel) ON UPDATE CASCADE ON DELETE RESTRICT
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
    CONSTRAINT fk_vehicule_type_carburant FOREIGN KEY (id_type_carburant) REFERENCES type_carburant(id_type_carburant) ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 4. Insertion des données initiales
INSERT INTO type_carburant (id_type_carburant, code, libelle) VALUES 
('TC0001', 'D', 'Diesel'),
('TC0002', 'E', 'Essence'),
('TC0003', 'El', 'Electrique'),
('TC0004', 'H', 'Hybride');

INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aéroport International d''Antananarivo', 'Antananarivo'),
('LIEU002', 'Gare Routière de Soarano', 'Antananarivo'),
('LIEU003', 'Stade Municipal de Mahamasina', 'Antananarivo'),
('LIEU004', 'Jardin Botanique de Tsimbazaza', 'Antananarivo'),
('LIEU005', 'Palais de la Reine', 'Antananarivo'),
('LIEU006', 'Marché Analakely', 'Antananarivo'),
('LIEU007', 'Lac Anosy', 'Antananarivo'),
('LIEU008', 'Rova d''Antananarivo', 'Antananarivo'),
('LIEU009', 'Parc Zoologique de Tsimbazaza', 'Antananarivo'),
('LIEU010', 'Centre Commercial Acacia', 'Antananarivo');

INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT001', 'Hotel Colbert Antananarivo', 'LIEU005'),
('HOT002', 'Novotel Antananarivo', 'LIEU010'),
('HOT003', 'Ibis Antananarivo', 'LIEU006'),
('HOT004', 'Hotel Lokanga', 'LIEU008');

INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PARAM001', 40.00, 15);

INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIST001', 'LIEU001', 'LIEU005', 18.50), -- Aéroport -> Hotel Colbert (Lieu 005)
('DIST002', 'LIEU001', 'LIEU010', 15.20), -- Aéroport -> Novotel (Lieu 010)
('DIST003', 'LIEU001', 'LIEU006', 17.80), -- Aéroport -> Ibis (Lieu 006)
('DIST004', 'LIEU001', 'LIEU008', 20.10), -- Aéroport -> Hotel Lokanga (Lieu 008)

-- Distances Inter-hôtels (Exemples)
('DIST005', 'LIEU010', 'LIEU005', 4.50);

INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant) VALUES
('VH001', 'Toyota Hiace D1', 12, 'TC0001'),
('VH002', 'Toyota Hiace D2', 15, 'TC0001'),
('VH003', 'Toyota Coaster E1', 25, 'TC0002'),
('VH004', 'Mercedes Sprinter D3', 8, 'TC0001'),
('VH005', 'Nissan Urvan E2', 6, 'TC0002'),
('VH006', 'Toyota Hiace H1', 10, 'TC0004'),
('VH007', 'Tesla Bus El1', 20, 'TC0003');