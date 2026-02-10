INSERT INTO hotel (id_hotel, nom_hotel) VALUES
('HOT001', 'Colbert'),
('HOT002', 'Novotel'),
('HOT003', 'Ibis'),
('HOT004', 'Lokanga');



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
