-- Nettoyage préalable pour le test
DELETE FROM reservation WHERE id_reservation IN ('RES001', 'RES002', 'RES003', 'RES004');

-- Insertion des données de test
INSERT INTO reservation (id_reservation, id_client, nbr_passager, id_hotel, date_resa) VALUES
('RES001', 'CLT001', 4, 'HOT001', '2026-03-11 09:00:00'), -- Hotel Colbert (18.5 km)
('RES002', 'CLT002', 2, 'HOT002', '2026-03-11 09:00:00'), -- Novotel (15.2 km)
('RES003', 'CLT003', 10, 'HOT003', '2026-03-11 09:00:00'), -- Ibis (17.8 km)
('RES004', 'CLT004', 5, 'HOT004', '2026-03-11 11:00:00'); -- Hotel Lokanga (20.1 km)