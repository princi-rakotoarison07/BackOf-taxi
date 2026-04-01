-- Nettoyage des données existantes
DELETE FROM assignation;
DELETE FROM trajet;
DELETE FROM reservation WHERE id_reservation IN ('RES_S8_1','RES_S8_2','RES_S8_3','RES_S8_4','RES_S8_0');
DELETE FROM vehicule WHERE id_vehicule IN ('VH_S8_0', 'VH_S8_1');
DELETE FROM distance WHERE id_distance IN ('DIST_S8_1');
DELETE FROM hotel WHERE id_hotel IN ('HOT_S8_1');
DELETE FROM lieuhotel WHERE id_lieu IN ('LIEU_S8_1');

-- Configuration des lieux et hôtels
INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU001', 'Aéroport', 'Antananarivo')
ON CONFLICT (id_lieu) DO NOTHING;

INSERT INTO lieuhotel (id_lieu, nom_lieu, ville) VALUES
('LIEU_S8_1', 'Zone_Test_Sprint8', 'Antananarivo');

INSERT INTO hotel (id_hotel, nom_hotel, id_lieu) VALUES
('HOT_S8_1', 'Hotel_Sprint8', 'LIEU_S8_1');

-- Configuration du temps d'attente à 30 min (comme dans l'exemple)
INSERT INTO parametre (id_parametre, vitesse_moyenne, temps_attente) VALUES
('PARAM001', 50, 30)
ON CONFLICT (id_parametre) DO UPDATE SET vitesse_moyenne = EXCLUDED.vitesse_moyenne, temps_attente = EXCLUDED.temps_attente;

INSERT INTO distance (id_distance, lieu_from, lieu_to, kilometre) VALUES
('DIST_S8_1', 'LIEU001', 'LIEU_S8_1', 25); -- Trajet de 30 min environ à 50km/h

-- Configuration du véhicule VH_0 (5 places) qui va prendre 5 pers de RES_S8_0 (12 pers)
-- ce qui laissera 7 personnes non assignées pour le VH_1
INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant, heure_disponible) VALUES
('VH_S8_0', 'vehicule0', 15,  'TC0001', '09:40:00');

-- Les autres réservations
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_S8_1', 'Client_RES1', 10, 'HOT_S8_1', '2026-03-20 10:00:00'),
('RES_S8_2', 'Client_RES2', 10, 'HOT_S8_1', '2026-03-20 10:05:00')
;
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_S8_3', 'Client_RES3', 10, 'HOT_S8_1', '2026-03-20 11:30:00');