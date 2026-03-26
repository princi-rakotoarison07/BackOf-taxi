BEGIN;

DELETE FROM assignation_reservation;
DELETE FROM trajet_execute; 
DELETE FROM vehicule_nombre_trajets;
DELETE FROM reservation;
DELETE FROM disponibilite_vehicule;
DELETE FROM vehicule;
DELETE FROM type_carburant;
DELETE FROM distance;
DELETE FROM parametre;
DELETE FROM hotel;
DELETE FROM lieuhotel;

INSERT INTO type_carburant (id_type_carburant, code, libelle) VALUES
('TC0001', 'D',  'Diesel'),
('TC0002', 'E',  'Essence'),
('TC0003', 'El', 'Electrique');

INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PAR_TEST_001', 50.00, 30);

INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aeroport Ivato', 'Antananarivo'),
('LIEU010', 'Anosy', 'Antananarivo'),
('LIEU011', 'Lieu', 'Antananarivo');

INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT010', 'Hotel Anosy', 'LIEU010'),
('HOT011', 'Hote', 'LIEU011');

INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIS100', 'LIEU001', 'LIEU010', 90.00),
('DIS101', 'LIEU001', 'LIEU011', 35.00),
('DIS102', 'LIEU010', 'LIEU011', 60.00);

INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant) VALUES
('VH101', '-1', 5, 'TC0001'),
('VH102', '-2A', 5, 'TC0002'),
('VH103', '-3', 12, 'TC0001'),
('VH104', '-4A', 9, 'TC0001'),
('VH105', '-5', 12, 'TC0002');

INSERT INTO disponibilite_vehicule (id_vehicule, heure_debut, heure_fin) VALUES
('VH101', TIME '00:00:00', TIME '23:59:59'),
('VH102', TIME '00:00:00', TIME '23:59:59'),
('VH103', TIME '00:00:00', TIME '23:59:59'),
('VH104', TIME '00:00:00', TIME '23:59:59'),
('VH105', TIME '13:30:00', TIME '23:59:59');

INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_GRP_001', 'CLT001', 7, 'HOT010', '2026-03-19 09:00:00'),
('RES_GRP_002', 'CLT002', 20, 'HOT011', '2026-03-19 08:00:00'),
('RES_GRP_003', 'CLT003', 3, 'HOT010', '2026-03-19 09:10:00'),
('RES_GRP_004', 'CLT004', 10, 'HOT010', '2026-03-19 09:15:00'),
('RES_GRP_005', 'CLT005', 5, 'HOT010', '2026-03-19 09:20:00'),
('RES_GRP_006', 'CLT006', 12, 'HOT010', '2026-03-19 13:30:00');

INSERT INTO disponibilite_vehicule (id_vehicule, heure_debut, heure_fin, date_maj)
VALUES ('VH105', TIME '13:00:00', TIME '23:59:59', CURRENT_TIMESTAMP)
ON CONFLICT (id_vehicule) DO UPDATE
SET
    heure_debut = EXCLUDED.heure_debut,
    heure_fin = EXCLUDED.heure_fin,
    date_maj = CURRENT_TIMESTAMP;
    
COMMIT;