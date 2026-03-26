INSERT INTO disponibilite_vehicule (id_vehicule, heure_debut, heure_fin, date_maj)
VALUES ('VH105', TIME '13:00:00', TIME '23:59:59', CURRENT_TIMESTAMP)
ON CONFLICT (id_vehicule) DO UPDATE
SET
    heure_debut = EXCLUDED.heure_debut,
    heure_fin = EXCLUDED.heure_fin,
    date_maj = CURRENT_TIMESTAMP;