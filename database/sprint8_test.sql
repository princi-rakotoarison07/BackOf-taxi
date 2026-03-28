-- Nettoyage des données existantes
DELETE FROM assignation;
DELETE FROM trajet;
DELETE FROM reservation WHERE id_reservation IN ('RES_S8_1','RES_S8_2','RES_S8_3','RES_S8_4','RES_S8_0');
DELETE FROM vehicule WHERE id_vehicule IN ('VH_S8_1');
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

-- Configuration du véhicule (12 places, dispo à 09h50)
INSERT INTO vehicule (id_vehicule, reference, nbr_place, id_type_carburant, heure_disponible) VALUES
('VH_S8_1', 'vehicule1', 12,  'TC0001', '09:50:00');

-- Création des réservations selon l'exemple
-- Non Assigne : 7 places res (on va créer une réservation initiale de 7 places qui sera "non assignée" ou un reliquat)
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_S8_0', 'Client_NonAssigne', 7, 'HOT_S8_1', '2026-03-20 09:40:00'); -- Arrive avant, donc c'est le reliquat/non assigné

-- Les autres réservations
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES_S8_1', 'Client_RES1', 10, 'HOT_S8_1', '2026-03-20 10:00:00'),
('RES_S8_2', 'Client_RES2', 7,  'HOT_S8_1', '2026-03-20 10:10:00'),
('RES_S8_3', 'Client_RES3', 3,  'HOT_S8_1', '2026-03-20 10:15:00'),
('RES_S8_4', 'Client_RES4', 5,  'HOT_S8_1', '2026-03-20 10:30:00');

-- Explication de ce qui doit se passer :
-- 1. VH_S8_1 (12 places) arrive à 09:50.
-- 2. Il trouve RES_S8_0 (7 places) en attente.
-- 3. Il charge les 7 places. Il lui reste 5 places libres.
-- 4. Comme il n'est pas plein, il attend 30 minutes (jusqu'à 10:20).
-- 5. Pendant son attente (09:50 - 10:20), RES_S8_1 (10p), RES_S8_2 (7p) et RES_S8_3 (3p) arrivent.
-- 6. A 10:20, il doit choisir comment combler ses 5 places.
-- 7. RES_S8_2 (7 places) est choisie car 5/7 est le plus proche de la capacité restante (meilleur match).
-- 8. Il prend 5 personnes de RES_S8_2.
-- 9. Il reste 2 personnes de RES_S8_2 non assignées, qui deviendront prioritaires pour le prochain véhicule.
-- 10. RES_S8_4 arrive à 10:30, donc après le départ du véhicule à 10:20, il ne sera pas pris en compte pour ce départ.
