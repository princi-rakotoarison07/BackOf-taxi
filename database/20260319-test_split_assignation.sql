-- Jeu de données complet pour tester l'assignation fractionnée (split)
-- Page: /BackOf-taxi/reservation/assignation-vehicule-split?date=2026-03-19
-- Objectif: reproduire un cas avec portions + reliquats.
-- Exemple visé:
-- v1 = 8 places, v2 = 3 places
-- r1 = 6 places, r2 = 4 places, r3 = 3 places
-- Résultat attendu (greedy, dans l'ordre des véhicules):
-- v1 prend r1 (6)
-- v1 prend 2 de r2
-- v2 prend 2 restant de r2
-- v2 prend 1 de r3
-- reliquat r3 = 2

-- 1) Nettoyage (ordre FK)
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS vehicule CASCADE;
DROP TABLE IF EXISTS type_carburant CASCADE;
DROP TABLE IF EXISTS distance CASCADE;
DROP TABLE IF EXISTS parametre CASCADE;
DROP TABLE IF EXISTS hotel CASCADE;
DROP TABLE IF EXISTS lieuhotel CASCADE;

-- 2) Séquences (optionnel mais cohérent avec init.sql)
DROP SEQUENCE IF EXISTS seq_hotel;
DROP SEQUENCE IF EXISTS seq_reservation;
DROP SEQUENCE IF EXISTS seq_type_carburant;
DROP SEQUENCE IF EXISTS seq_vehicule;

CREATE SEQUENCE seq_hotel;
CREATE SEQUENCE seq_reservation;
CREATE SEQUENCE seq_type_carburant;
CREATE SEQUENCE seq_vehicule;

-- 3) Schéma minimal nécessaire au contrôleur
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

-- 4) Données de référence
INSERT INTO type_carburant (id_type_carburant, code, libelle) VALUES
('TC0001', 'D', 'Diesel');

INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aéroport', 'Antananarivo'),
('LIEU010', 'Zone A', 'Antananarivo');

INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT010', 'Hotel A', 'LIEU010');

-- Paramètre + distances: non utilisés par le split, mais le contrôleur lit ces tables ailleurs dans l'app
INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PARAM001', 60.00, 0);

INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIST001', 'LIEU001', 'LIEU010', 10.00);

-- 5) Véhicules pour le split
-- Important: l'ordre dans la liste `vehicules` influence le greedy (v1 puis v2).
INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant) VALUES
('VH_SPLIT_1', 'v1', 8, 'TC0001'),
('VH_SPLIT_2', 'v2', 3, 'TC0001');

-- 6) Réservations de test (même date pour filtrage)
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_SPLIT_1', 'CLT_R1', 6, 'HOT010', '2026-03-19 09:00:00'),
('RES_SPLIT_2', 'CLT_R2', 4, 'HOT010', '2026-03-19 09:00:00'),
('RES_SPLIT_3', 'CLT_R3', 3, 'HOT010', '2026-03-19 09:00:00');

-- 7) Notes
-- Ouvrir: http://localhost:8080/BackOf-taxi/reservation/assignation-vehicule-split?date=2026-03-19
-- Attendu (portions):
-- VH_SPLIT_1: RES_SPLIT_1=6, RES_SPLIT_2=2
-- VH_SPLIT_2: RES_SPLIT_2=2, RES_SPLIT_3=1
-- Reliquat: RES_SPLIT_3=2
