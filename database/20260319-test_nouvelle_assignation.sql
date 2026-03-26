-- Jeu de données complet pour tester la nouvelle logique d'assignation
-- Objectifs de test:
-- 1) Un véhicule en retour est considéré disponible (dispo dès la fin de dépose)
-- 2) En cas d'égalité, choisir le véhicule ayant effectué le moins de trajets (tripCount)

-- 1. Nettoyage (ordre FK)
DROP TABLE IF EXISTS trajet CASCADE;
DROP TABLE IF EXISTS assignation CASCADE;
DROP TABLE IF EXISTS reservation CASCADE;
DROP TABLE IF EXISTS vehicule CASCADE;
DROP TABLE IF EXISTS type_carburant CASCADE;
DROP TABLE IF EXISTS distance CASCADE;
DROP TABLE IF EXISTS parametre CASCADE;
DROP TABLE IF EXISTS hotel CASCADE;
DROP TABLE IF EXISTS lieuhotel CASCADE;

-- 2. Séquences
DROP SEQUENCE IF EXISTS seq_hotel;
DROP SEQUENCE IF EXISTS seq_reservation;
DROP SEQUENCE IF EXISTS seq_type_carburant;
DROP SEQUENCE IF EXISTS seq_vehicule;
DROP SEQUENCE IF EXISTS seq_assignation;
DROP SEQUENCE IF EXISTS seq_trajet;

CREATE SEQUENCE seq_hotel;
CREATE SEQUENCE seq_reservation;
CREATE SEQUENCE seq_type_carburant;
CREATE SEQUENCE seq_vehicule;
CREATE SEQUENCE seq_assignation;
CREATE SEQUENCE seq_trajet;

-- 3. Schéma (tables de base)
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

-- 4. Schéma (tables du module assignation / trajet)
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

-- 5. Données de référence
INSERT INTO type_carburant (id_type_carburant, code, libelle) VALUES
('TC0001', 'D', 'Diesel'),
('TC0002', 'E', 'Essence');

INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aéroport', 'Antananarivo'),
('LIEU010', 'Zone A', 'Antananarivo'),
('LIEU011', 'Zone B', 'Antananarivo'),
('LIEU012', 'Zone C', 'Antananarivo'),
('LIEU013', 'Zone D', 'Antananarivo');

INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT010', 'Hotel A', 'LIEU010'),
('HOT011', 'Hotel B', 'LIEU011'),
('HOT012', 'Hotel C', 'LIEU012'),
('HOT013', 'Hotel D', 'LIEU013');

-- Vitesse choisie pour avoir des durées lisibles:
-- 60 km/h -> 30 km = 30 minutes
INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PARAM001', 60.00, 0);

-- Distances (km)
-- On met surtout les distances depuis l'aéroport pour contrôler les durées.
INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIST100', 'LIEU001', 'LIEU010', 30.00),
('DIST101', 'LIEU001', 'LIEU011', 30.00),
('DIST102', 'LIEU001', 'LIEU012', 12.00),
('DIST103', 'LIEU001', 'LIEU013', 6.00),
('DIST110', 'LIEU010', 'LIEU011', 5.00),
('DIST111', 'LIEU011', 'LIEU012', 5.00),
('DIST112', 'LIEU012', 'LIEU013', 5.00),
('DIST113', 'LIEU010', 'LIEU013', 10.00);

-- Véhicules
-- VH001 et VH002: mêmes conditions (capacité & Diesel) -> tie-break par tripCount possible
INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant) VALUES
('VH001', 'V001', 7,  'TC0001'),
('VH002', 'V002', 7,  'TC0001'),
('VH003', 'V003', 15, 'TC0002');

-- 6. Réservations (cas de test)
-- Tous les horaires sont le même jour pour tester "nombre de trajets dans la journée".
-- Créneaux minute (l'algorithme groupe à la minute):
-- - 08:00 : un trajet long (A) -> occupe 30 min (sans retour)
-- - 08:20 : un trajet long (B) -> un autre véhicule doit être utilisé car le 1er n'est pas encore libre
-- - 08:35 : trajet court (D) -> à ce moment, VH001 est libre (08:30), VH002 ne l'est pas (08:50) => VH001 fait un 2e trajet
-- - 09:00 : trajet moyen (C) -> les deux VH001 et VH002 sont libres, même capacité/diésel
--           => tie-break "moins de trajets" doit choisir VH002 (1 trajet) plutôt que VH001 (2 trajets)
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES100', 'CLT100', 6, 'HOT010', '2026-03-19 08:00:00'),
('RES101', 'CLT101', 6, 'HOT011', '2026-03-19 08:20:00'),
('RES102', 'CLT102', 2, 'HOT013', '2026-03-19 08:35:00'),
('RES103', 'CLT103', 6, 'HOT012', '2026-03-19 09:00:00');

-- 7. Trajets (données de test)
-- Remarque: les valeurs ci-dessous sont cohérentes avec les distances et la vitesse (60 km/h)
-- et correspondent à un aller-retour Aéroport <-> Zone.
-- Elles servent à tester la table `trajet` (et l'affichage/contrôles DB), indépendamment du calcul à l'exécution.
INSERT INTO trajet (id_trajet, id_vehicule, date_trajet, heure_depart_aeroport, heure_arrivee_aeroport) VALUES
('TRJ0001', 'VH001', '2026-03-19 00:00:00', '2026-03-19 08:00:00', '2026-03-19 09:00:00'),
('TRJ0002', 'VH002', '2026-03-19 00:00:00', '2026-03-19 08:20:00', '2026-03-19 09:20:00'),
('TRJ0003', 'VH001', '2026-03-19 00:00:00', '2026-03-19 08:35:00', '2026-03-19 08:47:00'),
('TRJ0004', 'VH002', '2026-03-19 00:00:00', '2026-03-19 09:00:00', '2026-03-19 09:24:00');

-- 7. Notes de validation
-- - Aller sur: http://localhost:8080/BackOf-taxi/reservation/assignation-vehicule?date=2026-03-19
-- - Vérifier que RES103 est assignée à VH002 (moins de trajets) si VH001 a déjà fait 2 trajets.
