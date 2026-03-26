-- Jeu de donnees complet pour tester:
-- 1) Regroupement par fenetre de temps d'attente
-- 2) Priorites vehicules (trajets journaliers + carburant)
-- 3) Selection par capacite la plus proche >= passagers
-- 4) Depassement de capacite avec repartition multi-vehicules
-- 5) Egalites de tri reservations (heure, passagers, distance, alphabetique)
-- 6) Cas de passagers restants non assignes (capacite totale insuffisante)

BEGIN;

-- Nettoyage (ordre respectant les FK)
DELETE FROM reservation;
DELETE FROM disponibilite_vehicule;
DELETE FROM vehicule;
DELETE FROM type_carburant;
DELETE FROM distance;
DELETE FROM parametre;
DELETE FROM hotel;
DELETE FROM lieuhotel;

-- Types carburant
INSERT INTO type_carburant (id_type_carburant, code, libelle) VALUES
('TC0001', 'D',  'Diesel'),
('TC0002', 'E',  'Essence'),
('TC0003', 'El', 'Electrique');

-- Parametre actif (attente 30 min)
INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PAR_TEST_001', 50.00, 30);

-- Lieux
INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aeroport Ivato', 'Antananarivo'),
('LIEU010', 'Anosy', 'Antananarivo'),
('LIEU011', 'Belair', 'Antananarivo'),
('LIEU012', 'Ivandry', 'Antananarivo'),
('LIEU013', 'Talatamaty', 'Antananarivo'),
('LIEU014', 'Ambatobe', 'Antananarivo'),
('LIEU015', 'Andraharo', 'Antananarivo');

-- Hotels
INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT010', 'Hotel Anosy', 'LIEU010'),
('HOT011', 'Hotel Belair', 'LIEU011'),
('HOT012', 'Hotel Ivandry', 'LIEU012'),
('HOT013', 'Hotel Talatamaty', 'LIEU013'),
('HOT014', 'Hotel Ambatobe', 'LIEU014'),
('HOT015', 'Hotel Andraharo', 'LIEU015');

-- Distances aeroport -> lieux
-- HOT010 et HOT011 sont volontairement a distance egale (5 km)
-- pour tester le tie-break alphabetique (Anosy avant Belair).
INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIS100', 'LIEU001', 'LIEU010', 5.00),
('DIS101', 'LIEU001', 'LIEU011', 5.00),
('DIS102', 'LIEU001', 'LIEU012', 7.00),
('DIS103', 'LIEU001', 'LIEU013', 9.00),
('DIS104', 'LIEU001', 'LIEU014', 12.00),
('DIS105', 'LIEU001', 'LIEU015', 6.00);

-- Distances inter-lieux (suffisantes pour calcul de tournee)
INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIS110', 'LIEU010', 'LIEU011', 2.00),
('DIS111', 'LIEU010', 'LIEU012', 3.00),
('DIS112', 'LIEU010', 'LIEU013', 5.00),
('DIS113', 'LIEU010', 'LIEU014', 8.00),
('DIS114', 'LIEU010', 'LIEU015', 4.00),
('DIS115', 'LIEU011', 'LIEU012', 3.00),
('DIS116', 'LIEU011', 'LIEU013', 5.00),
('DIS117', 'LIEU011', 'LIEU014', 7.00),
('DIS118', 'LIEU011', 'LIEU015', 4.00),
('DIS119', 'LIEU012', 'LIEU013', 4.00),
('DIS120', 'LIEU012', 'LIEU014', 6.00),
('DIS121', 'LIEU012', 'LIEU015', 2.00),
('DIS122', 'LIEU013', 'LIEU014', 4.00),
('DIS123', 'LIEU013', 'LIEU015', 5.00),
('DIS124', 'LIEU014', 'LIEU015', 6.00);

-- Flotte de test
-- Objectif: couvrir capacite proche, split et priorites carburant.
INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant) VALUES
('VH101', 'ELEC-7',    7, 'TC0003'),
('VH102', 'DIESEL-8A', 8, 'TC0001'),
('VH103', 'ESS-6',     6, 'TC0002'),
('VH104', 'DIESEL-6',  6, 'TC0001'),
('VH105', 'ELEC-8',    8, 'TC0003'),
('VH106', 'ESS-4',     4, 'TC0002');

INSERT INTO disponibilite_vehicule (id_vehicule, heure_debut, heure_fin)
SELECT id_vehicule, TIME '00:00:00', TIME '23:59:59'
FROM vehicule;

-- ================================================
-- JOURNEE DE TEST: 2026-04-20
-- ================================================

-- [CAS 1] REGROUPEMENT 08:00 -> 08:30
-- Attendu: depart du groupe a 08:25 (derniere reservation dans la fenetre)
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_GRP_001', 'CLT001', 2, 'HOT012', '2026-04-20 08:00:00'),
('RES_GRP_002', 'CLT002', 2, 'HOT013', '2026-04-20 08:15:00'),
('RES_GRP_003', 'CLT003', 1, 'HOT014', '2026-04-20 08:25:00');

-- Reservation hors fenetre precedente (nouveau groupe)
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_GRP_004', 'CLT004', 2, 'HOT015', '2026-04-20 09:10:00');

-- [CAS 2] EGALITE TRI RESERVATIONS
-- Meme heure + meme nb passagers + meme distance aeroport (HOT010/HOT011 a 5 km)
-- Tie-break attendu: ordre alphabetique quartier/hotel => Anosy avant Belair.
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_EQ_001', 'CLT005', 2, 'HOT010', '2026-04-20 09:00:00'),
('RES_EQ_002', 'CLT006', 2, 'HOT011', '2026-04-20 09:00:00');

-- [CAS 3] CAPACITE PROCHE + SPLIT
-- Fenetre 10:00 -> 10:30
-- 10:00/10:01/10:02 occupent d'abord les 8 places et 7 places.
-- 10:05 avec 10 passagers doit etre decoupe (ex: 6 + 4).
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_CAP_001', 'CLT007', 8,  'HOT014', '2026-04-20 10:00:00'),
('RES_CAP_002', 'CLT008', 8,  'HOT013', '2026-04-20 10:01:00'),
('RES_CAP_003', 'CLT009', 7,  'HOT015', '2026-04-20 10:02:00'),
('RES_CAP_004', 'CLT010', 10, 'HOT012', '2026-04-20 10:05:00');

-- [CAS 4] CAS AVANCE RESTANT + PRIORITE CARBURANT
-- Fenetre 14:00 -> 14:30
-- Sequence pour creer deux vehicules avec capacite restante egale,
-- puis une reservation testant le tie-break carburant.
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_ADV_000', 'CLT011', 7, 'HOT015', '2026-04-20 13:59:00'),
('RES_ADV_001', 'CLT012', 5, 'HOT013', '2026-04-20 14:00:00'),
('RES_ADV_002', 'CLT013', 5, 'HOT014', '2026-04-20 14:01:00'),
('RES_ADV_003', 'CLT014', 5, 'HOT010', '2026-04-20 14:02:00'),
('RES_ADV_004', 'CLT015', 5, 'HOT011', '2026-04-20 14:03:00'),
('RES_ADV_005', 'CLT016', 3, 'HOT012', '2026-04-20 14:04:00');

-- [CAS 5] CAPACITE TOTALE INSUFFISANTE
-- Permet de verifier les passagers non assignes remontes par le backend.
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_OVR_001', 'CLT017', 50, 'HOT013', '2026-04-20 18:00:00');

COMMIT;
