# S5/S6 INFO PROJET TOUR OPERATOR FEV 26

---

## SCÉNARIO

### Réservations clients

| reservation | nb | date    | heure    | hotel  |
| ----------- | -- | ------- | -------- | ------ |
| Client1     | 7  | 19/3/26 | 09:00:00 | hotel1 |
| Client2     | 20 | 19/3/26 | 08:00:00 | hotel2 |
| Client3     | 3  | 19/3/26 | 09:10:00 | hotel1 |
| Client4     | 10 | 19/3/26 | 09:15:00 | hotel1 |
| Client5     | 5  | 19/3/26 | 09:20:00 | hotel1 |
| Client6     | 12 | 19/3/26 | 13:30:00 | hotel1 |

### Véhicules disponibles

| vehicule  | Place | type    | heure disponible |
| --------- | ----- | ------- | ---------------- |
| vehicule1 | 5     | diesel  | 09:00:00         |
| vehicule2 | 5     | essence | 09:00:00         |
| vehicule3 | 12    | diesel  | 00:00:00         |
| vehicule4 | 9     | diesel  | 09:00:00         |
| vehicule5 | 12    | essence | 13:00:00         |

---

## RÉSULTAT — du 19/03/26

| vehicule  | client  | nb pers | heure départ | heure retour | min durée |
| --------- | ------- | ------- | ------------- | ------------ | ---------- |
| vehicule3 | Client2 | 12      | 08:00:00      | 09:24:00     | 84         |
| vehicule3 | Client4 | 10      | 09:24:00      | 13:00:00     | 216        |
| vehicule3 | Client3 | 2       | 09:24:00      | 13:00:00     | 216        |
| vehicule4 | Client2 | 8       | 09:24:00      | 13:06:00     | 222        |
| vehicule4 | Client3 | 1       | 09:24:00      | 13:05:00     | 222        |
| vehicule1 | Client1 | 5       | 09:24:00      | 13:00:00     | 216        |
| vehicule2 | Client1 | 2       | 09:24:00      | 13:00:00     | 216        |
| vehicule2 | Client5 | 3       | 09:24:00      | 13:00:00     | 216        |
| vehicule5 | Client6 | 12      | 13:30:00      | 17:06:00     | 216        |
| vehicule1 | Client5 | 2       | 13:30:00      | 17:06:00     | 216        |

---

## Analyse

- Le système affecte automatiquement les clients aux véhicules selon leur capacité et disponibilité.
- **vehicule3** (12 places, diesel) prend Client2 en premier dès 08h00, puis enchaîne avec d'autres clients à 09h24.
- Les **grands groupes sont divisés** : Client2 (20 pers) est réparti sur vehicule3 (12) + vehicule4 (8).
- **vehicule5** (disponible seulement à 13h00) prend en charge Client6 dont la réservation est à 13h30.
- La durée standard d'une tournée est de **216 minutes (3h36)**, sauf la première course de vehicule3 (84 min).
- La formule `=D62+TEMPS(0;F68;0)` calcule les heures de retour dynamiquement..
