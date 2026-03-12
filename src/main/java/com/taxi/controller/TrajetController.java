package com.taxi.controller;

import com.taxi.model.Distance;
import com.taxi.model.Hotel;
import com.taxi.model.Parametre;
import com.taxi.model.Reservation;
import com.taxi.model.Trajet;
import com.taxi.model.TypeCarburant;
import com.taxi.model.Vehicule;
import com.taxi.util.DBConnection;
import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.annotation.Param;
import framework.utilitaire.ModelAndView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.*;

@Controller
public class TrajetController {

    // ─────────────────────────────────────────────────────────────────────────
    // Route principale : liste des trajets pour une date donnée
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/trajet/list")
    public ModelAndView list(@Param("date") String date) throws Exception {
        ModelAndView mv = new ModelAndView("/views/trajet/list.jsp");
        mv.addObject("pageTitle", "Liste des Trajets");
        mv.addObject("selectedDate", date);

        try (Connection conn = DBConnection.getConnection()) {

            List<Reservation>     allReservations = Reservation.getAll(Reservation.class, conn);
            List<Vehicule>        vehicules        = Vehicule.getAll(Vehicule.class, conn);
            List<TypeCarburant>   types            = TypeCarburant.getAll(TypeCarburant.class, conn);
            List<Hotel>           hotels           = Hotel.getAll(Hotel.class, conn);
            List<Distance>        distances        = Distance.getAll(Distance.class, conn);
            List<Parametre>       parametres       = Parametre.getAll(Parametre.class, conn);
            Parametre currentParam = (parametres != null && !parametres.isEmpty()) ? parametres.get(0) : null;

            // Filtrer par date si fournie
            List<Reservation> filtered = filtrerParDate(allReservations, date);

            // Construire les maps utilitaires
            Map<String, TypeCarburant>              typeById       = buildTypeMap(types);
            Map<String, Hotel>                      hotelMap       = buildHotelMap(hotels);
            Map<String, Map<String, Distance>>      distanceMatrix = buildDistanceMatrix(distances);

            // Calculer et retourner la liste des trajets
            List<Trajet> trajets = calculerTrajets(
                    filtered, vehicules, typeById, hotelMap, distanceMatrix, currentParam);

            mv.addObject("trajets", trajets);
            mv.addObject("hotelMap", hotelMap);
            mv.addObject("typeById", typeById);
        }

        return mv;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Logique principale : fenêtre d'attente + regroupement + calcul horaires
    // ─────────────────────────────────────────────────────────────────────────
    /**
     * Pour chaque "slot de départ" (heure de la première résa encore non assignée),
     * on ouvre une fenêtre [t0, t0 + tempsAttente] et on regroupe toutes les
     * réservations dont l'heure tombe dans cette fenêtre.
     *
     * Règle :
     *  - Si une seule réservation dans la fenêtre → départ immédiat à t0.
     *  - Si plusieurs réservations → départ à la dernière heure de résa dans la
     *    fenêtre (heure la plus proche ≤ t0 + tempsAttente).
     */
    private List<Trajet> calculerTrajets(
            List<Reservation> reservations,
            List<Vehicule>    vehicules,
            Map<String, TypeCarburant>           typeById,
            Map<String, Hotel>                   hotelMap,
            Map<String, Map<String, Distance>>   distanceMatrix,
            Parametre param) {

        // Trier les réservations par heure croissante
        List<Reservation> sorted = new ArrayList<>(reservations);
        sorted.sort(Comparator.comparing(r -> r.getDateResa() != null ? r.getDateResa() : new Timestamp(0)));

        long waitWindowMs = 0L;
        if (param != null && param.getTempsAttente() != null && param.getTempsAttente() > 0) {
            waitWindowMs = param.getTempsAttente() * 60_000L;
        }

        List<Trajet>   trajets   = new ArrayList<>();
        Set<String>    assigned  = new HashSet<>();          // ids réservations déjà traitées
        List<Vehicule> available = new ArrayList<>(vehicules);
        Map<Vehicule, Timestamp> nextFreeTime = new HashMap<>();
        for (Vehicule v : available) nextFreeTime.put(v, new Timestamp(0));

        for (int i = 0; i < sorted.size(); i++) {
            Reservation anchor = sorted.get(i);
            if (assigned.contains(anchor.getIdReservation())) continue;
            if (anchor.getDateResa() == null) continue;

            Timestamp t0       = anchor.getDateResa();
            Timestamp windowEnd = new Timestamp(t0.getTime() + waitWindowMs);

            // Collecter toutes les réservations dans la fenêtre [t0, t0+waitWindow]
            List<Reservation> window = new ArrayList<>();
            for (Reservation r : sorted) {
                if (assigned.contains(r.getIdReservation())) continue;
                if (r.getDateResa() == null) continue;
                if (!r.getDateResa().before(t0) && !r.getDateResa().after(windowEnd)) {
                    window.add(r);
                }
            }

            // Heure de départ réelle :
            //  - 1 réservation → départ immédiat à t0
            //  - plusieurs      → heure de la dernière résa dans la fenêtre
            Timestamp heureDepart = t0;
            if (window.size() > 1) {
                Timestamp latest = t0;
                for (Reservation r : window) {
                    if (r.getDateResa().after(latest)) latest = r.getDateResa();
                }
                heureDepart = latest;
            }

            // Assigner les véhicules au groupe
            window.sort((a, b) -> b.getNbrPassager().compareTo(a.getNbrPassager()));
            Map<Vehicule, List<Reservation>> vehiculeGroup = new LinkedHashMap<>();
            Map<Vehicule, Integer> remainingCap = new HashMap<>();
            for (Vehicule v : available) remainingCap.put(v, v.getNbrPlace() != null ? v.getNbrPlace() : 0);

            for (Reservation r : window) {
                Vehicule best = choisirVehicule(r, available, remainingCap, nextFreeTime, heureDepart, typeById);
                if (best != null) {
                    remainingCap.put(best, remainingCap.get(best) - r.getNbrPassager());
                    vehiculeGroup.computeIfAbsent(best, k -> new ArrayList<>()).add(r);
                }
                assigned.add(r.getIdReservation());
            }

            // Créer un Trajet par véhicule utilisé dans ce groupe
            for (Map.Entry<Vehicule, List<Reservation>> entry : vehiculeGroup.entrySet()) {
                Vehicule v    = entry.getKey();
                List<Reservation> tour = entry.getValue();

                BigDecimal km = calculerDistanceTournee(tour, hotelMap, distanceMatrix);
                long travelMs = calculerDureeMs(km, param);

                Timestamp heureRetour = new Timestamp(heureDepart.getTime() + travelMs);
                nextFreeTime.put(v, heureRetour);

                trajets.add(new Trajet(v, heureDepart, heureRetour, km, tour));
            }
        }

        // Trier les trajets par heure de départ
        trajets.sort(Comparator.comparing(Trajet::getHeureDepart));
        return trajets;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Choisir le meilleur véhicule disponible pour une réservation
    // Priorité : diesel, capacité la plus ajustée (éviter le gaspillage de places)
    // ─────────────────────────────────────────────────────────────────────────
    private Vehicule choisirVehicule(
            Reservation r,
            List<Vehicule> available,
            Map<Vehicule, Integer> remainingCap,
            Map<Vehicule, Timestamp> nextFreeTime,
            Timestamp departureTime,
            Map<String, TypeCarburant> typeById) {

        Vehicule best = null;
        int bestDiff  = Integer.MAX_VALUE;
        int bestDiesel = -1;

        for (Vehicule v : available) {
            int cap      = remainingCap.getOrDefault(v, 0);
            Timestamp ft = nextFreeTime.getOrDefault(v, new Timestamp(0));

            if (cap >= r.getNbrPassager() && !ft.after(departureTime)) {
                TypeCarburant tc   = typeById.get(v.getIdTypeCarburant());
                int dieselScore    = (tc != null && "D".equalsIgnoreCase(tc.getCode())) ? 1 : 0;
                int diff           = cap - r.getNbrPassager();

                if (diff < bestDiff || (diff == bestDiff && dieselScore > bestDiesel)) {
                    best       = v;
                    bestDiff   = diff;
                    bestDiesel = dieselScore;
                }
            }
        }
        return best;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Calcul de la distance totale d'un trajet (greedy TSP depuis LIEU001)
    // ─────────────────────────────────────────────────────────────────────────
    private BigDecimal calculerDistanceTournee(
            List<Reservation> tour,
            Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix) {

        BigDecimal total = BigDecimal.ZERO;
        String currentLieu = "LIEU001";
        List<Reservation> remaining = new ArrayList<>(tour);

        while (!remaining.isEmpty()) {
            Reservation next   = null;
            BigDecimal minDist = BigDecimal.valueOf(Double.MAX_VALUE);

            for (Reservation r : remaining) {
                Hotel h = hotelMap.get(r.getIdHotel());
                if (h != null) {
                    Distance d = getDistance(distanceMatrix, currentLieu, h.getIdLieu());
                    if (d != null && d.getKilometre().compareTo(minDist) < 0) {
                        minDist = d.getKilometre();
                        next    = r;
                    }
                }
            }

            if (next != null) {
                total = total.add(minDist);
                currentLieu = hotelMap.get(next.getIdHotel()).getIdLieu();
                remaining.remove(next);
            } else {
                break;
            }
        }

        // Retour à l'aéroport
        Distance retour = getDistance(distanceMatrix, currentLieu, "LIEU001");
        if (retour != null) total = total.add(retour.getKilometre());

        return total;
    }

    private long calculerDureeMs(BigDecimal km, Parametre param) {
        if (param == null || param.getVitesseMoyenne() == null
                || param.getVitesseMoyenne().compareTo(BigDecimal.ZERO) <= 0) return 0L;
        BigDecimal heures = km.divide(param.getVitesseMoyenne(), 4, RoundingMode.HALF_UP);
        return (long) (heures.doubleValue() * 3_600_000L);
    }

    private Distance getDistance(Map<String, Map<String, Distance>> matrix, String from, String to) {
        if (matrix.containsKey(from)) {
            Distance d = matrix.get(from).get(to);
            if (d != null) return d;
        }
        if (matrix.containsKey(to)) return matrix.get(to).get(from);
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitaires
    // ─────────────────────────────────────────────────────────────────────────
    private List<Reservation> filtrerParDate(List<Reservation> all, String date) {
        if (date == null || date.isEmpty()) return all;
        List<Reservation> result = new ArrayList<>();
        Timestamp start = Timestamp.valueOf(date + " 00:00:00");
        Timestamp end   = Timestamp.valueOf(date + " 23:59:59");
        for (Reservation r : all) {
            if (r.getDateResa() != null
                    && !r.getDateResa().before(start)
                    && !r.getDateResa().after(end)) {
                result.add(r);
            }
        }
        return result;
    }

    private Map<String, TypeCarburant> buildTypeMap(List<TypeCarburant> types) {
        Map<String, TypeCarburant> m = new HashMap<>();
        for (TypeCarburant t : types) m.put(t.getIdTypeCarburant(), t);
        return m;
    }

    private Map<String, Hotel> buildHotelMap(List<Hotel> hotels) {
        Map<String, Hotel> m = new HashMap<>();
        for (Hotel h : hotels) m.put(h.getIdHotel(), h);
        return m;
    }

    private Map<String, Map<String, Distance>> buildDistanceMatrix(List<Distance> distances) {
        Map<String, Map<String, Distance>> m = new HashMap<>();
        for (Distance d : distances) {
            m.computeIfAbsent(d.getLieuFrom(), k -> new HashMap<>()).put(d.getLieuTo(), d);
        }
        return m;
    }
}
