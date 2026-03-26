-- TYPE CARBURANT (typeenergie → type_carburant)
INSERT INTO type_carburant (id_type_carburant, code, libelle) VALUES
('TC0001', 'D', 'Diesel'),
('TC0002', 'E', 'Essence'),
('TC0003', 'El', 'Electrique'),
('TC0004', 'H',  'Hybride');

-- LIEUHOTEL (hotel.distanceaeroport indique la position, on crée des lieux)
INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aéroport', 'Antananarivo'),
('LIEU002', 'Zone Hotel 1', 'Antananarivo');

-- HOTEL (hotel → hotel, lié aux lieux)
INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT001', 'Hotel 1', 'LIEU002');
-- PARAMETRE (clé/valeur → colonnes typées)
-- AttenteMinute=30, VitesseKmh=50
INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PARAM001', 50.00, 30);

-- DISTANCE (idhotelfrom/idhotelto → lieu_from/lieu_to)
-- hotel 1=LIEU002, hotel 2=LIEU003, hotel 3=LIEU004
-- + distances depuis aéroport (LIEU001) via distanceaeroport de chaque hotel
INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIST001', 'LIEU001', 'LIEU002', 50.00);  -- Aéroport → Hotel 1


-- VEHICULE (voiture → vehicule)
-- V001=id2(Diesel/7pl), V002=id3(Diesel/8pl), V003=id4(Essence/15pl)
INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant) VALUES
('VH001', 'V001', 12,  'TC0001'),  -- Diesel
('VH002', 'V002', 5,  'TC0002'),  -- Diesel
('VH003', 'V003', 5, 'TC0001'),
('VH004', 'V004', 12, 'TC0002');  -- Essence

INSERT INTO disponibilite_vehicule (id_vehicule, heure_debut, heure_fin)
SELECT id_vehicule, TIME '00:00:00', TIME '23:59:59'
FROM vehicule;

-- RESERVATION
-- dateheurearrivee → date_resa, idhotel mappé
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES001', 'C001', 7,  'HOT001', '2026-03-12 09:00:00'),
('RES002', 'C002', 11,  'HOT001', '2026-03-12 09:00:00'),
('RES003', 'C003', 3,  'HOT001', '2026-03-12 09:00:00'),
('RES004', 'C004', 1,  'HOT001', '2026-03-12 09:00:00'),
('RES005', 'C005', 2,  'HOT001', '2026-03-12 09:00:00'),
('RES006', 'C006', 20,  'HOT001', '2026-03-12 09:00:00');