
INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aéroport', 'Antananarivo'),
('LIEU002', 'Zone Hotel A', 'Antananarivo'),
('LIEU003', 'Zone Hotel B', 'Antananarivo'),
('LIEU004', 'Zone Hotel C', 'Antananarivo');
INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT001', 'Hotel A', 'LIEU002'),
('HOT002', 'Hotel B', 'LIEU003'),
('HOT003', 'Hotel C', 'LIEU004');
INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PARAM001', 50.00, 30);
INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIST001', 'LIEU001', 'LIEU002', 6.00),
('DIST002', 'LIEU001', 'LIEU003', 5.00),
('DIST003', 'LIEU001', 'LIEU004', 8.00),
('DIST004', 'LIEU002', 'LIEU003', 3.00),
('DIST005', 'LIEU002', 'LIEU004', 6.00),
('DIST006', 'LIEU003', 'LIEU004', 3.00);
INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant) VALUES
('VH001', 'V001', 7,  'TC0001'),
('VH002', 'V002', 8,  'TC0001'),
('VH003', 'V003', 15, 'TC0002');
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES001', 'CLT001', 4, 'HOT001', '2026-03-19 09:00:00'),
('RES002', 'CLT002', 2, 'HOT002', '2026-03-19 09:00:00'),
('RES003', 'CLT003', 10, 'HOT003', '2026-03-19 09:00:00'),
('RES004', 'CLT004', 5, 'HOT001', '2026-03-19 11:00:00');
