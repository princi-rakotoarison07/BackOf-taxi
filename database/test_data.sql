-- ============================================================
-- DONNEES DE TEST - COUVERTURE COMPLETE DES CAS 1 A 4
-- Projet: BackOf-taxi
-- Date de test principale: 2026-04-20
-- ============================================================
-- Cas 1: Priorite au plus petit nombre de trajets
-- Cas 2: Egalite de trajets -> priorite carburant (El > D > E)
-- Cas 3: Regroupement avec temps d'attente (08:00, 08:15, 08:25 -> depart 08:25)
-- Cas 4: Egalite reservation (heure, passagers, distance) -> quartier alphabetique
--         Exemple: Anosy prioritaire sur Belair
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- 1) Nettoyage (ordre FK)
-- ------------------------------------------------------------
DELETE FROM reservation;
DELETE FROM vehicule;
DELETE FROM type_carburant;
DELETE FROM distance;
DELETE FROM hotel;
DELETE FROM parametre;
DELETE FROM lieuhotel;

ALTER SEQUENCE seq_hotel RESTART WITH 1;
ALTER SEQUENCE seq_reservation RESTART WITH 1;
ALTER SEQUENCE seq_vehicule RESTART WITH 1;
ALTER SEQUENCE seq_type_carburant RESTART WITH 1;

-- ------------------------------------------------------------
-- 2) Lieux / quartiers
-- ------------------------------------------------------------
INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aeroport Ivato', 'Antananarivo'),
('LIEU010', 'Anosy', 'Antananarivo'),
('LIEU011', 'Belair', 'Antananarivo'),
('LIEU012', 'Ivandry', 'Antananarivo'),
('LIEU013', 'Soarano', 'Antananarivo');

-- ------------------------------------------------------------
-- 3) Hotels
-- ------------------------------------------------------------
INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT001', 'Hotel Anosy', 'LIEU010'),
('HOT002', 'Hotel Belair', 'LIEU011'),
('HOT003', 'Hotel Ivandry', 'LIEU012'),
('HOT004', 'Hotel Soarano', 'LIEU013');

-- ------------------------------------------------------------
-- 4) Distances
-- ------------------------------------------------------------
INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
-- Aeroport vers quartiers
('D001', 'LIEU001', 'LIEU010', 10.00),
('D002', 'LIEU001', 'LIEU011', 10.00),
('D003', 'LIEU001', 'LIEU012', 14.00),
('D004', 'LIEU001', 'LIEU013', 16.00),

-- Retours vers aeroport
('D005', 'LIEU010', 'LIEU001', 10.00),
('D006', 'LIEU011', 'LIEU001', 10.00),
('D007', 'LIEU012', 'LIEU001', 14.00),
('D008', 'LIEU013', 'LIEU001', 16.00),

-- Inter-quartiers
('D009',  'LIEU010', 'LIEU011', 5.00),
('D010',  'LIEU011', 'LIEU010', 5.00),
('D011',  'LIEU010', 'LIEU012', 6.00),
('D012',  'LIEU012', 'LIEU010', 6.00),
('D013',  'LIEU011', 'LIEU012', 6.00),
('D014',  'LIEU012', 'LIEU011', 6.00),
('D015',  'LIEU012', 'LIEU013', 4.00),
('D016',  'LIEU013', 'LIEU012', 4.00);

-- ------------------------------------------------------------
-- 5) Types carburant
-- ------------------------------------------------------------
INSERT INTO type_carburant (id_type_carburant, code, libelle) VALUES
('TC001', 'El', 'Electrique'),
('TC002', 'D',  'Diesel'),
('TC003', 'E',  'Essence');

-- ------------------------------------------------------------
-- 6) Vehicules
-- ------------------------------------------------------------
INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant) VALUES
('VH001', 'Bus Electrique 6', 6,  'TC001'),
('VH002', 'Van Diesel 6',     6,  'TC002'),
('VH003', 'Van Essence 6',    6,  'TC003'),
('VH004', 'MiniBus Diesel 12',12, 'TC002');

-- ------------------------------------------------------------
-- 7) Parametre (attente 30 min)
-- ------------------------------------------------------------
INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PARAM_CASES', 40.00, 30);

-- ------------------------------------------------------------
-- 8) Reservations tests - JOUR PRINCIPAL: 2026-04-20
-- ------------------------------------------------------------

-- CAS 2 (egalite trajets=0): tous les vehicules a 0 trajet au debut,
-- priorite carburant -> Electrique d'abord (VH001 attendu)
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES001', 'CLI001', 4, 'HOT004', '2026-04-20 06:00:00');

-- CAS 1 (priorite au plus petit nb trajets):
-- apres RES001, VH001 a 1 trajet. Les autres sont a 0 -> choisir un vehicule a 0 trajet.
-- Si egalite entre 0 trajet, CAS 2 s'applique (Diesel avant Essence) -> VH002 attendu.
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES002', 'CLI002', 5, 'HOT003', '2026-04-20 06:40:00');

-- CAS 3 regroupement fenetre [08:00, 08:30]
-- depart groupe attendu a 08:25 (derniere reservation valide dans la fenetre)
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES003', 'CLI003', 2, 'HOT001', '2026-04-20 08:00:00'),
('RES004', 'CLI004', 3, 'HOT003', '2026-04-20 08:15:00'),
('RES005', 'CLI005', 4, 'HOT002', '2026-04-20 08:25:00');
-- Note metier: un "vol" a 08:26 sans reservation n'est pas pris en compte.

-- CAS 4 egalite forte entre reservations:
-- meme heure, meme passagers, meme distance (Aeroport->Anosy=10, Aeroport->Belair=10)
-- departage par quartier alphabetique -> Anosy prioritaire sur Belair
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES006', 'CLI006', 2, 'HOT001', '2026-04-20 09:00:00'), -- Anosy
('RES007', 'CLI007', 2, 'HOT002', '2026-04-20 09:00:00'); -- Belair

-- Reservations supplementaires pour charge multi-plages
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES008', 'CLI008', 6, 'HOT003', '2026-04-20 11:00:00'),
('RES009', 'CLI009', 1, 'HOT004', '2026-04-20 11:20:00'),
('RES010', 'CLI010', 3, 'HOT001', '2026-04-20 17:30:00');

-- Jour secondaire pour test reset journalier des compteurs
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES011', 'CLI011', 4, 'HOT004', '2026-04-21 06:00:00'),
('RES012', 'CLI012', 4, 'HOT003', '2026-04-21 06:20:00');

COMMIT;

-- ============================================================
-- Verifications rapides
-- ============================================================

-- A) Nombre de reservations le 20/04 (attendu: 10)
SELECT COUNT(*) AS nb_resa_2026_04_20
FROM reservation
WHERE date_resa >= '2026-04-20 00:00:00'
  AND date_resa <= '2026-04-20 23:59:59';

-- B) Points temporels existants (controle regroupement)
SELECT to_char(date_trunc('minute', date_resa), 'YYYY-MM-DD HH24:MI') AS minute_resa,
       COUNT(*) AS nb
FROM reservation
WHERE date_resa >= '2026-04-20 00:00:00'
  AND date_resa <= '2026-04-20 23:59:59'
GROUP BY date_trunc('minute', date_resa)
ORDER BY minute_resa;

-- C) Controle cas 4 (Anosy/Belair, meme heure/meme pax)
SELECT r.id_reservation, r.date_resa, r.nbr_passager, h.nom_hotel, l.nom_lieu AS quartier
FROM reservation r
JOIN hotel h ON h.id_hotel = r.id_hotel
JOIN lieuhotel l ON l.id_lieu = h.id_lieu
WHERE r.date_resa = '2026-04-20 09:00:00'
ORDER BY l.nom_lieu ASC, r.id_reservation ASC;
