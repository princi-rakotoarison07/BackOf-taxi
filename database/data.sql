INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aéroport International d''Antananarivo', 'Antananarivo'),
('LIEU002', 'Gare Routière de Soarano', 'Antananarivo'),
('LIEU003', 'Stade Municipal de Mahamasina', 'Antananarivo'),
('LIEU004', 'Jardin Botanique de Tsimbazaza', 'Antananarivo'),
('LIEU005', 'Palais de la Reine', 'Antananarivo'),
('LIEU006', 'Marché Analakely', 'Antananarivo'),
('LIEU007', 'Lac Anosy', 'Antananarivo'),
('LIEU008', 'Rova d''Antananarivo', 'Antananarivo'),
('LIEU009', 'Parc Zoologique de Tsimbazaza', 'Antananarivo'),
('LIEU010', 'Centre Commercial Acacia', 'Antananarivo');


INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT001', 'Hotel Colbert Antananarivo', 'LIEU005'),
('HOT002', 'Novotel Antananarivo', 'LIEU010'),
('HOT003', 'Ibis Antananarivo', 'LIEU006'),
('HOT004', 'Hotel Lokanga', 'LIEU008');



INSERT INTO reservation (
    id_reservation,
    id_client,
    nbr_passager,
    id_hotel,
    date_resa
) VALUES
('RES001', '4631', 11, 'HOT003', '2026-02-05 00:01'),
('RES002', '4394', 1,  'HOT003', '2026-02-05 23:55'),
('RES003', '8054', 2,  'HOT001', '2026-02-09 10:17'),
('RES004', '1432', 4,  'HOT002', '2026-02-01 15:25'),
('RES005', '7861', 4,  'HOT001', '2026-01-28 07:11'),
('RES006', '3308', 5,  'HOT001', '2026-01-28 07:45'),
('RES007', '4484', 13, 'HOT002', '2026-02-28 08:25'),
('RES008', '9687', 8,  'HOT002', '2026-02-28 13:00'),
('RES009', '6302', 7,  'HOT001', '2026-02-15 13:00'),
('RES010', '8640', 1,  'HOT004', '2026-02-18 22:55');

-- Données pour la table paramètre
INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PARAM001', 40.00, 15),
('PARAM002', 50.00, 10);

-- Données pour la table vehicule
INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant) VALUES
('VH001', 'Toyota Hiace D1', 12, 'TC0001'),  -- Diesel 12 places
('VH002', 'Toyota Hiace D2', 15, 'TC0001'),  -- Diesel 15 places
('VH003', 'Toyota Coaster E1', 25, 'TC0002'), -- Essence 25 places
('VH004', 'Mercedes Sprinter D3', 8, 'TC0001'), -- Diesel 8 places
('VH005', 'Nissan Urvan E2', 6, 'TC0002'),   -- Essence 6 places
('VH006', 'Toyota Hiace H1', 10, 'TC0004'),  -- Hybride 10 places
('VH007', 'Tesla Bus El1', 20, 'TC0003');    -- Électrique 20 places

