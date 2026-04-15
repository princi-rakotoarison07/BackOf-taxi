DELETE FROM reservation WHERE id_reservation IN ('RES_S8_1','RES_S8_2','RES_S8_3','RES_S8_4');
DELETE FROM vehicule WHERE id_vehicule IN ('VH_S8_1','VH_S8_2','VH_S8_3','VH_S8_4');
DELETE FROM distance WHERE id_distance IN ('DIST_S8_1','DIST_S8_2','DIST_S8_3');
DELETE FROM hotel WHERE id_hotel IN ('HOT_S8_1','HOT_S8_2');
DELETE FROM lieuhotel WHERE id_lieu IN ('LIEU_S8_1','LIEU_S8_2');

INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aéroport', 'Antananarivo')
ON CONFLICT (id_lieu) DO NOTHING;

INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU_S8_1', 'hotel1_zone', 'Antananarivo'),
('LIEU_S8_2', 'hotel2_zone', 'Antananarivo');

INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT_S8_1', 'hotel1', 'LIEU_S8_1'),
('HOT_S8_2', 'hotel2', 'LIEU_S8_2');

INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PARAM001', 60, 30)
ON CONFLICT (id_parametre) DO UPDATE SET vitesse_moyenne = EXCLUDED.vitesse_moyenne, temps_attente = EXCLUDED.temps_attente;

INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIST_S8_1', 'LIEU001', 'LIEU_S8_1', 90),
('DIST_S8_2', 'LIEU001', 'LIEU_S8_2', 65),
('DIST_S8_3', 'LIEU_S8_1', 'LIEU_S8_2', 10);

INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant, heure_disponible) VALUES
('VH_S8_1', 'vehicule1', 10, 'TC0001', '00:00:00'),
('VH_S8_2', 'vehicule2', 8,  'TC0001', '08:00:00'),
('VH_S8_3', 'vehicule3', 8,  'TC0002', '08:00:00'),
('VH_S8_4', 'vehicule4', 12, 'TC0002', '09:00:00');


INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_S8_1', 'Client1', 20, 'HOT_S8_1', '2026-04-02 06:00:00'),
('RES_S8_2', 'Client2', 6,  'HOT_S8_1', '2026-04-02 08:15:00'),
('RES_S8_3', 'Client3', 10, 'HOT_S8_1', '2026-04-02 09:00:00'),
('RES_S8_4', 'Client4', 6,  'HOT_S8_2', '2026-04-02 09:10:00');
