DELETE FROM reservation WHERE id_reservation IN ('RES_S7_1','RES_S7_2','RES_S7_3','RES_S7_4','RES_S7_5','RES_S7_6');
DELETE FROM vehicule WHERE id_vehicule IN ('VH_S7_1','VH_S7_2','VH_S7_3','VH_S7_4','VH_S7_5');
DELETE FROM distance WHERE id_distance IN ('DIST_S7_1','DIST_S7_2','DIST_S7_3','DIST_S7_4');
DELETE FROM hotel WHERE id_hotel IN ('HOT_S7_1','HOT_S7_2');
DELETE FROM lieuhotel WHERE id_lieu IN ('LIEU_S7_1','LIEU_S7_2');
INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aéroport', 'Antananarivo')
ON CONFLICT (id_lieu) DO NOTHING;
INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU_S7_1', 'hotel1_zone', 'Antananarivo'),
('LIEU_S7_2', 'hotel2_zone', 'Antananarivo');
INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT_S7_1', 'hotel1', 'LIEU_S7_1'),
('HOT_S7_2', 'hotel2', 'LIEU_S7_2');
INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PARAM001', 50, 30)
ON CONFLICT (id_parametre) DO UPDATE SET vitesse_moyenne = EXCLUDED.vitesse_moyenne, temps_attente = EXCLUDED.temps_attente;
INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIST_S7_1', 'LIEU001', 'LIEU_S7_1', 90),
('DIST_S7_2', 'LIEU001', 'LIEU_S7_2', 35),
('DIST_S7_3', 'LIEU_S7_1', 'LIEU_S7_2', 60);
INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant, heure_disponible) VALUES
('VH_S7_1', 'vehicule1', 5,  'TC0001', '2026-03-19 09:00:00'),
('VH_S7_2', 'vehicule2', 5,  'TC0002', '2026-03-19 09:00:00'),
('VH_S7_3', 'vehicule3', 12, 'TC0001', '2026-03-19 00:00:00'),
('VH_S7_4', 'vehicule4', 9,  'TC0001', '2026-03-19 09:00:00'),
('VH_S7_5', 'vehicule5', 12, 'TC0002', '2026-03-19 13:00:00');


INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_S7_1', 'Client1', 7,  'HOT_S7_1', '2026-03-19 09:00:00'),
('RES_S7_2', 'Client2', 20, 'HOT_S7_2', '2026-03-19 08:00:00'),
('RES_S7_3', 'Client3', 3,  'HOT_S7_1', '2026-03-19 09:10:00'),
('RES_S7_4', 'Client4', 10, 'HOT_S7_1', '2026-03-19 09:15:00'),
('RES_S7_5', 'Client5', 5,  'HOT_S7_1', '2026-03-19 09:20:00'),
('RES_S7_6', 'Client6', 12, 'HOT_S7_1', '2026-03-19 13:30:00');
