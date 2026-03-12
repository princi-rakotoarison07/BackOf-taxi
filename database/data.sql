-- TYPE CARBURANT (typeenergie → type_carburant)
INSERT INTO type_carburant (id_type_carburant, code, libelle) VALUES
('TC0001', 'D', 'Diesel'),
('TC0002', 'E', 'Essence'),
('TC0003', 'El', 'Electrique'),
('TC0004', 'H',  'Hybride');

-- LIEUHOTEL (hotel.distanceaeroport indique la position, on crée des lieux)
INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aéroport', 'Antananarivo'),
('LIEU002', 'Zone Hotel A', 'Antananarivo'),
('LIEU003', 'Zone Hotel B', 'Antananarivo'),
('LIEU004', 'Zone Hotel C', 'Antananarivo');

-- HOTEL (hotel → hotel, lié aux lieux)
INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT001', 'Hotel A', 'LIEU002'),
('HOT002', 'Hotel B', 'LIEU003'),
('HOT003', 'Hotel C', 'LIEU004');

-- PARAMETRE (clé/valeur → colonnes typées)
-- AttenteMinute=30, VitesseKmh=50
INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PARAM001', 50.00, 30);

-- DISTANCE (idhotelfrom/idhotelto → lieu_from/lieu_to)
-- hotel 1=LIEU002, hotel 2=LIEU003, hotel 3=LIEU004
-- + distances depuis aéroport (LIEU001) via distanceaeroport de chaque hotel
INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIST001', 'LIEU001', 'LIEU002', 6.00),  -- Aéroport → Hotel A
('DIST002', 'LIEU001', 'LIEU003', 5.00),  -- Aéroport → Hotel B
('DIST003', 'LIEU001', 'LIEU004', 8.00),  -- Aéroport → Hotel C
('DIST004', 'LIEU002', 'LIEU003', 3.00),  -- Hotel A → Hotel B
('DIST005', 'LIEU002', 'LIEU004', 6.00),  -- Hotel A → Hotel C
('DIST006', 'LIEU003', 'LIEU004', 3.00);  -- Hotel B → Hotel C

-- VEHICULE (voiture → vehicule)
-- V001=id2(Diesel/7pl), V002=id3(Diesel/8pl), V003=id4(Essence/15pl)
INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant) VALUES
('VH001', 'V001', 7,  'TC0001'),  -- Diesel
('VH002', 'V002', 8,  'TC0001'),  -- Diesel
('VH003', 'V003', 15, 'TC0002');  -- Essence

-- RESERVATION
-- dateheurearrivee → date_resa, idhotel mappé
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES001', 'C001', 5,  'HOT003', '2026-03-04 08:00:00'),
('RES002', 'C002', 10, 'HOT001', '2026-03-04 08:00:00'),
('RES003', 'C005', 2,  'HOT002', '2026-03-04 08:00:00'),
('RES004', 'C007', 4,  'HOT003', '2026-03-04 08:00:00');