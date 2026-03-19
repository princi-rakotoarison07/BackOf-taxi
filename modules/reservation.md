
# Réservation & Assignation — Logique de l’algorithme

Ce document décrit la logique métier implémentée dans `ReservationController` pour :

- créer des réservations,
- calculer automatiquement une **assignation** de véhicules,
- estimer les **horaires** (départ aéroport, arrivée, retour aéroport),
- enregistrer en base les **assignations** et les **trajets**.

## 1) Vocabulaire / objets

- **Réservation (`reservation`)** : demande d’un client (id client, nombre de passagers, hôtel, date/heure `date_resa`).
- **Véhicule (`vehicule`)** : capacité `nbr_place` + type carburant.
- **Paramètre (`parametre`)** : vitesse moyenne (km/h) utilisée pour convertir km → durée.
- **Distance (`distance`)** : matrice de distances (km) entre lieux.
- **Hôtel (`hotel`)** : associé à un lieu (`id_lieu`).
- **Assignation (`assignation`)** : enregistrement *par réservation* : quel véhicule, quels horaires, n° de trajet.
- **Trajet (`trajet`)** : enregistrement *par tournée (créneau d’un véhicule)* : départ aéroport / retour aéroport.

### Invariant important

- L’aéroport est représenté par le lieu **`LIEU001`**.

## 2) Endpoints / pages principales

### Création

- `GET /BackOf-taxi/reservation/form`
  - Affiche le formulaire de réservation.
- `POST /BackOf-taxi/reservation/save`
  - Enregistre 1 réservation.
  - Si `dateResa` est vide, elle est définie à `now()`.

### Insertion multiple

- `POST /BackOf-taxi/reservation/save-multiple`
  - Reçoit une chaîne `reservationsData` au format :
    - `idRes|idClient|nbrPax|idHotel|date;idRes2|...`
  - Insère toutes les réservations en transaction.
  - La date peut arriver au format HTML `yyyy-MM-ddTHH:mm`.

### Calcul & affichage de l’assignation

- `GET /BackOf-taxi/reservation/assignation`
  - Page “assignation” (vue `assignation.jsp`).
- `GET /BackOf-taxi/reservation/assignation-vehicule`
  - Page “assignation par véhicule” (vue `assignationVehicule.jsp`).

Ces 2 pages utilisent la même préparation de données via :

- `prepareAssignationData(mv, date)`

## 3) Préparation globale (`prepareAssignationData`)

### 3.1 Chargement des données

Depuis la base :

- toutes les réservations
- tous les véhicules
- les types carburant
- les hôtels + lieux
- les distances
- le paramètre (vitesse moyenne)

### 3.2 Filtrage par date

Si un paramètre `date=YYYY-MM-DD` est fourni :

- fenêtre `[date 00:00:00, date 23:59:59]`
- on garde les réservations dont `date_resa` est dans cette fenêtre.

### 3.3 Tri des réservations

Les réservations filtrées sont triées :

1. par `date_resa` décroissante (plus récente d’abord)
2. puis par `nbr_passager` décroissant (gros groupe d’abord)

Objectif : prioriser les demandes récentes et remplir les véhicules efficacement.

## 4) Assignation des véhicules (`assignerVehicules`)

### 4.1 Groupement par créneau minute

L’algorithme regroupe les réservations par **timestamp tronqué à la minute** :

- `truncated = floor(date_resa / 60000) * 60000`

Chaque groupe représente un créneau d’assignation.

### 4.2 Capacité restante et disponibilité du véhicule

Pour chaque groupe (créneau) :

- on initialise `remainingCapacity[v] = v.nbr_place`
- on maintient `nextFreeTime[v]` : quand le véhicule sera à nouveau libre.
  - initialisé à l’epoch (0) pour tous les véhicules
  - mis à jour après chaque tournée du véhicule

Un véhicule est éligible si :

- `remainingCapacity[v] >= nbr_passager(reservation)`
- `nextFreeTime[v] <= currentTime(du groupe)`

### 4.3 Sélection du “meilleur” véhicule (`trouverMeilleurVehiculePourGroupe`)

Critères, dans l’ordre :

1. **Maximiser le taux de remplissage final** après ajout de la réservation
   - `fillRateAfter = occupiedAfter / totalCapacity`
2. si égalité, choisir la **plus petite capacité totale** (évite d’utiliser un grand véhicule)
3. si égalité, favoriser le **Diesel** (`TypeCarburant.code == 'D'`)

Quand une réservation est affectée :

- `assignments[idReservation] = vehicule`
- `remainingCapacity[vehicule] -= nbr_passager`

### 4.4 Réutilisation du véhicule dans la journée

Après avoir affecté tout un groupe, pour chaque véhicule utilisé dans ce groupe :

- on calcule une durée de tournée `calculerDureeTournee(tour, ...)`
- `nextFreeTime[v] = groupTime + duration`

Ainsi, un même véhicule peut faire plusieurs trajets dans la journée, mais seulement s’il est redevenu libre.

## 5) Calcul des horaires (`calculerHoraires`)

### 5.1 Groupement par véhicule + créneau minute

Après assignation, on regroupe les réservations :

- par véhicule
- par créneau minute (même logique de troncature)

Chaque couple (véhicule, créneau) = **une tournée**.

### 5.2 Calcul de la distance totale

Pour une tournée :

1. départ du lieu `LIEU001` (aéroport)
2. visite des hôtels via un heuristique “plus proche voisin” (Greedy) :
   - à chaque étape on choisit l’hôtel restant le plus proche du lieu courant
3. retour à `LIEU001`

Les distances sont obtenues via `getDistance(matrix, from, to)` :

- on tente (from → to)
- sinon on tente l’inverse (to → from)

### 5.3 Conversion distance → temps

- `travelTimeHours = totalDistance / vitesse_moyenne`
- `travelTimeMs = travelTimeHours * 3600000`

Puis :

- `departureTimes[idReservation] = startTime(du créneau)`
- `arrivalTimes[idReservation] = startTime + travelTimeMs`

Remarque : dans cette étape, l’horaire “arrivée” appliqué à chaque réservation est celui de **fin de tournée** (estimation globale), pas l’heure d’arrivée à chaque hôtel.

## 6) Ordre de tournée + horaires détaillés (affichage)

Pour l’affichage (surtout sur `assignationVehicule.jsp`), le contrôleur calcule en plus :

- `tourOrders` : ordre des réservations/hôtels par tournée
- `detailedTimes` : horaires détaillés par segment

### 6.1 Clé de tournée

Une tournée est identifiée par une clé texte :

- `vehiculeId|depStr|arrStr`
  - où `depStr` et `arrStr` sont au format `dd/MM/yyyy HH:mm`

### 6.2 Horaires détaillés (`calculerHorairesDetailles`)

Pour chaque tournée :

- `calculerOrdreOptimal` (Greedy plus proche voisin)
- `calculerHeuresSegments` :
  - stocke `idReservation_departure` et `idReservation_arrival`
  - calcule les durées segment par segment
- `calculerHeureRetour` :
  - calcule `return_departure` et `return_arrival` (retour final à l’aéroport)

## 7) Enregistrement en base

### 7.1 Sauvegarde d’une assignation (par réservation)

- `POST /BackOf-taxi/reservation/save-assignation`
- Reçoit un objet `Assignation` via `@ModelAttribute` :
  - `idVehicule`, `idReservation`, `nbrPassager`, `dateAssignation`, `heureDepartPrevue`, `heureArriveePrevue`, `numTrajet`
- Si `dateAssignation` est null, elle est remplacée par `heureDepartPrevue`.

### 7.2 Sauvegarde d’un trajet (par tournée / véhicule)

- `POST /BackOf-taxi/reservation/save-trajet`
- Reçoit un objet `Trajet` via `@ModelAttribute` :
  - `idVehicule`, `dateTrajet`, `heureDepartAeroport`, `heureArriveeAeroport`
- Si `dateTrajet` est null, elle est remplacée par `heureDepartAeroport`.

## 8) Limites / hypothèses actuelles

- La tournée est un heuristique Greedy (pas optimal TSP).
- `calculerHoraires` met une “heure d’arrivée” identique pour toutes les réservations de la tournée (fin de tournée), tandis que l’affichage détaillé calcule des heures par segment.
- Tout part de `LIEU001` (aéroport). Si le code du lieu aéroport change, il faut modifier le contrôleur.
- La matrice de distance est supposée quasi-symétrique (fallback inverse).

