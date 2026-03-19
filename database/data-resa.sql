BEGIN;

DELETE FROM reservation;
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
('PAR_TEST_001', 45.00, 30);

INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aeroport Ivato', 'Antananarivo'),
('LIEU010', 'Anosy', 'Antananarivo'),
('LIEU011', 'Belair', 'Antananarivo');

INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT010', 'Hotel Anosy', 'LIEU010'),
('HOT011', 'Hotel Belair', 'LIEU011');

INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIS100', 'LIEU001', 'LIEU010', 10.00),
('DIS101', 'LIEU001', 'LIEU011', 20.00),
('DIS102', 'LIEU010', 'LIEU011', 5.00);

INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant) VALUES
('VH101', 'ELEC-7', 3, 'TC0003'),
('VH102', 'DIESEL-8A', 5, 'TC0001'),
('VH103', 'ESS-6', 8, 'TC0002');

INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_GRP_001', 'CLT001', 6, 'HOT010', '2026-04-20 08:00:00'),
('RES_GRP_002', 'CLT002', 4, 'HOT011', '2026-04-20 08:15:00'),
('RES_GRP_003', 'CLT003', 7, 'HOT010', '2026-04-20 08:25:00');

COMMIT;