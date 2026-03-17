-- ============================================================
--  SCRIPT DE DONNÉES DE TEST — BackOf-taxi
--  Objectif : tester la logique de fenêtre d'attente (trajets)
--
--  Scénarios couverts (date de test : 2026-03-15)
--  ┌──────────────────────────────────────────────────────────┐
--  │ CAS 1 — Départ immédiat (1 seule resa dans la fenêtre)   │
--  │   RES_T001  07:00  → départ à 07:00                      │
--  │   RES_T005  08:45  → départ à 08:45 (hors fenêtre T002)  │
--  ├──────────────────────────────────────────────────────────┤
--  │ CAS 2 — Regroupement (N resas dans la fenêtre 15 min)    │
--  │   RES_T002  08:00  ┐                                     │
--  │   RES_T003  08:08  ├─→ fenêtre 08:00–08:15               │
--  │   RES_T004  08:13  ┘    départ groupé à 08:13            │
--  │   RES_T006  13:00  ┐                                     │
--  │   RES_T007  13:11  ├─→ fenêtre 13:00–13:15               │
--  │   RES_T008  13:14  ┘    départ groupé à 13:14            │
--  └──────────────────────────────────────────────────────────┘
--  tempsAttente utilisé = 15 min (PARAM_TEST)
-- ============================================================


-- ────────────────────────────────────────────────────────────
-- 1. NETTOYAGE — ordre respectant les contraintes FK
-- ────────────────────────────────────────────────────────────
DELETE FROM reservation;
DELETE FROM vehicule;
DELETE FROM type_carburant;
DELETE FROM distance;
DELETE FROM hotel;
DELETE FROM parametre;
DELETE FROM lieuhotel;

-- Réinitialiser les séquences
ALTER SEQUENCE seq_hotel      RESTART WITH 1;
ALTER SEQUENCE seq_reservation RESTART WITH 1;
ALTER SEQUENCE seq_vehicule   RESTART WITH 1;
ALTER SEQUENCE seq_type_carburant RESTART WITH 1;


-- ────────────────────────────────────────────────────────────
-- 2. LIEUX
-- ────────────────────────────────────────────────────────────
INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aéroport Ivato',           'Antananarivo'),
('LIEU002', 'Analakely',                'Antananarivo'),
('LIEU003', 'Isoraka',                  'Antananarivo'),
('LIEU004', 'Faravohitra',              'Antananarivo'),
('LIEU005', 'Ambohijatovo',             'Antananarivo');


-- ────────────────────────────────────────────────────────────
-- 3. HÔTELS
-- ────────────────────────────────────────────────────────────
INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT001', 'Hotel Carlton',   'LIEU002'),
('HOT002', 'Colbert Hotel',   'LIEU003'),
('HOT003', 'Novotel Tana',    'LIEU004'),
('HOT004', 'Ibis Ambohibo',   'LIEU005');


-- ────────────────────────────────────────────────────────────
-- 4. DISTANCES (depuis/vers LIEU001 = Aéroport)
-- ────────────────────────────────────────────────────────────
INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
-- Aéroport → Hôtels
('D001', 'LIEU001', 'LIEU002', 12.00),
('D002', 'LIEU001', 'LIEU003', 15.00),
('D003', 'LIEU001', 'LIEU004', 18.00),
('D004', 'LIEU001', 'LIEU005', 20.00),
-- Hôtels → Aéroport (retour)
('D005', 'LIEU002', 'LIEU001', 12.00),
('D006', 'LIEU003', 'LIEU001', 15.00),
('D007', 'LIEU004', 'LIEU001', 18.00),
('D008', 'LIEU005', 'LIEU001', 20.00),
-- Inter-hôtels
('D009', 'LIEU002', 'LIEU003',  4.00),
('D010', 'LIEU002', 'LIEU004',  7.00),
('D011', 'LIEU002', 'LIEU005',  9.00),
('D012', 'LIEU003', 'LIEU004',  3.50),
('D013', 'LIEU003', 'LIEU005',  6.00),
('D014', 'LIEU004', 'LIEU005',  3.00);


-- ────────────────────────────────────────────────────────────
-- 5. TYPES DE CARBURANT
-- ────────────────────────────────────────────────────────────
INSERT INTO type_carburant (id_type_carburant, code, libelle) VALUES
('TC001', 'D',  'Diesel'),
('TC002', 'E',  'Essence'),
('TC003', 'El', 'Electrique'),
('TC004', 'H',  'Hybride');


-- ────────────────────────────────────────────────────────────
-- 6. VÉHICULES
-- ────────────────────────────────────────────────────────────
INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant) VALUES
('VH001', 'Toyota Hiace Diesel A',    12, 'TC001'),  -- Diesel  12 places
('VH002', 'Toyota Hiace Diesel B',    15, 'TC001'),  -- Diesel  15 places
('VH003', 'Toyota Coaster Essence',   25, 'TC002'),  -- Essence 25 places
('VH004', 'Mercedes Sprinter Diesel',  8, 'TC001'),  -- Diesel   8 places
('VH005', 'Nissan Urvan Essence',      6, 'TC002');  -- Essence  6 places


-- ────────────────────────────────────────────────────────────
-- 7. PARAMÈTRE — temps_attente = 15 minutes
-- ────────────────────────────────────────────────────────────
INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PARAM_TEST', 40.00, 15);


-- ────────────────────────────────────────────────────────────
-- 8. RÉSERVATIONS DE TEST
--
--  URL à tester : /trajet/list?date=2026-03-15
-- ────────────────────────────────────────────────────────────

-- ── CAS 1 : 07:00 — une seule réservation → départ immédiat ──
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_T001', 'CLI_001', 4, 'HOT001', '2026-03-15 07:00:00');

-- ── CAS 2a : 08:00–08:15 → 3 resas regroupées, départ à 08:13 ──
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_T002', 'CLI_002',  3, 'HOT002', '2026-03-15 08:00:00'),
('RES_T003', 'CLI_003',  5, 'HOT003', '2026-03-15 08:08:00'),
('RES_T004', 'CLI_004',  2, 'HOT001', '2026-03-15 08:13:00');

-- ── CAS 1 : 08:45 — hors fenêtre du groupe 08:00 → départ seul ──
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_T005', 'CLI_005',  6, 'HOT004', '2026-03-15 08:45:00');

-- ── CAS 2b : 13:00–13:15 → 3 resas regroupées, départ à 13:14 ──
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_T006', 'CLI_006',  4, 'HOT002', '2026-03-15 13:00:00'),
('RES_T007', 'CLI_007',  7, 'HOT003', '2026-03-15 13:11:00'),
('RES_T008', 'CLI_008',  3, 'HOT004', '2026-03-15 13:14:00');

-- ── CAS 1 : 17:30 — une seule réservation le soir ──
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_T009', 'CLI_009',  9, 'HOT003', '2026-03-15 17:30:00');

-- ── AUTRE JOUR (2026-03-16) pour tester le filtre par date ──
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_T010', 'CLI_010',  2, 'HOT001', '2026-03-16 09:00:00'),
('RES_T011', 'CLI_011', 10, 'HOT002', '2026-03-16 09:05:00'),
('RES_T012', 'CLI_012',  4, 'HOT004', '2026-03-16 14:00:00');


-- ────────────────────────────────────────────────────────────
-- 9. Vérifications rapides après insertion
-- ────────────────────────────────────────────────────────────
-- Doit retourner 9 pour le 15/03/2026
SELECT COUNT(*) AS nb_reservations_2026_03_15
FROM reservation
WHERE date_resa >= '2026-03-15 00:00:00'
	AND date_resa <= '2026-03-15 23:59:59';

-- Doit afficher 5 groupes attendus: 07:00, 08:00, 08:45, 13:00, 17:30
SELECT to_char(date_trunc('minute', date_resa), 'YYYY-MM-DD HH24:MI') AS minute_resa,
			 COUNT(*) AS nb_resa
FROM reservation
WHERE date_resa >= '2026-03-15 00:00:00'
	AND date_resa <= '2026-03-15 23:59:59'
GROUP BY date_trunc('minute', date_resa)
ORDER BY minute_resa;


-- ────────────────────────────────────────────────────────────
-- RÉSULTATS ATTENDUS pour /trajet/list?date=2026-03-15
-- ────────────────────────────────────────────────────────────
--
-- Trajet 1  | 07:00 | VH00x | RES_T001 (4 pax → Carlton)      | ~24 km
-- Trajet 2  | 08:13 | VH00x | RES_T002 (3 pax → Colbert)      | groupe
--           |       |       | RES_T003 (5 pax → Novotel)       |
--           |       |       | RES_T004 (2 pax → Carlton)       |
-- Trajet 3  | 08:45 | VH00x | RES_T005 (6 pax → Ibis)         | ~40 km
-- Trajet 4  | 13:14 | VH00x | RES_T006 (4 pax → Colbert)      | groupe
--           |       |       | RES_T007 (7 pax → Novotel)       |
--           |       |       | RES_T008 (3 pax → Ibis)          |
-- Trajet 5  | 17:30 | VH00x | RES_T009 (9 pax → Novotel)      | ~36 km
-- ────────────────────────────────────────────────────────────
