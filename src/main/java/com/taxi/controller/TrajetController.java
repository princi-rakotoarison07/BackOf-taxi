package com.taxi.controller;

import com.taxi.model.Distance;
import com.taxi.model.Hotel;
import com.taxi.model.LieuHotel;
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
            List<LieuHotel>       lieux            = LieuHotel.getAll(LieuHotel.class, conn);
            List<Parametre>       parametres       = Parametre.getAll(Parametre.class, conn);
            Parametre currentParam = (parametres != null && !parametres.isEmpty()) ? parametres.get(0) : null;

            // Filtrer par date si fournie
            List<Reservation> filtered = filtrerParDate(allReservations, date);

            // Construire les maps utilitaires
            Map<String, TypeCarburant>              typeById       = buildTypeMap(types);
            Map<String, Hotel>                      hotelMap       = buildHotelMap(hotels);
            Map<String, Map<String, Distance>>      distanceMatrix = buildDistanceMatrix(distances);
                Map<String, String>                     quartierByHotel = buildQuartierByHotelMap(hotels, lieux);

            // Calculer et retourner la liste des trajets
            List<Trajet> trajets = calculerTrajets(
                    filtered, vehicules, typeById, hotelMap, quartierByHotel, distanceMatrix, currentParam);

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
            Map<String, String>                  quartierByHotel,
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
        Map<Vehicule, Integer> trajetCount = new HashMap<>();

        Timestamp dayStart = sorted.isEmpty() || sorted.get(0).getDateResa() == null
            ? new Timestamp(0)
            : startOfDay(sorted.get(0).getDateResa());

        // Chaque début de journée, tous les véhicules sont disponibles à 00:00
        // et leur compteur de trajets est remis à 0.
        for (Vehicule v : available) {
            nextFreeTime.put(v, dayStart);
            trajetCount.put(v, 0);
        }

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

            // Ordre de traitement des réservations regroupées:
            // priorité à la réservation la plus ancienne, puis au plus grand groupe.
            window.sort((a, b) -> {
                if (!isReservationConfirmed(a) && isReservationConfirmed(b)) {
                    return 1;
                }
                if (isReservationConfirmed(a) && !isReservationConfirmed(b)) {
                    return -1;
                }

                Timestamp da = a.getDateResa();
                Timestamp db = b.getDateResa();
                int byDate;
                if (da == null && db == null) {
                    byDate = 0;
                } else if (da == null) {
                    byDate = 1;
                } else if (db == null) {
                    byDate = -1;
                } else {
                    byDate = da.compareTo(db);
                }
                if (byDate != 0) {
                    return byDate;
                }

                Integer pa = a.getNbrPassager() != null ? a.getNbrPassager() : 0;
                Integer pb = b.getNbrPassager() != null ? b.getNbrPassager() : 0;
                int byPax = pb.compareTo(pa);
                if (byPax != 0) {
                    return byPax;
                }

                BigDecimal dair = distanceFromAirport(a, hotelMap, distanceMatrix);
                BigDecimal dbir = distanceFromAirport(b, hotelMap, distanceMatrix);
                int byDistance = dair.compareTo(dbir);
                if (byDistance != 0) {
                    return byDistance;
                }

                String qa = quartierByHotel.getOrDefault(a.getIdHotel(), "");
                String qb = quartierByHotel.getOrDefault(b.getIdHotel(), "");
                int byQuartier = qa.compareToIgnoreCase(qb);
                if (byQuartier != 0) {
                    return byQuartier;
                }

                String ida = a.getIdReservation() != null ? a.getIdReservation() : "";
                String idb = b.getIdReservation() != null ? b.getIdReservation() : "";
                return ida.compareToIgnoreCase(idb);
            });

            Map<Vehicule, List<Reservation>> vehiculeGroup = new LinkedHashMap<>();
            Map<Vehicule, Integer> remainingCap = new HashMap<>();
            for (Vehicule v : available) remainingCap.put(v, v.getNbrPlace() != null ? v.getNbrPlace() : 0);

            for (Reservation r : window) {
                if (!isReservationConfirmed(r)) {
                    assigned.add(r.getIdReservation());
                    continue;
                }
                Vehicule best = choisirVehicule(r, available, remainingCap, nextFreeTime, trajetCount,
                        heureDepart, typeById);
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
                trajetCount.put(v, trajetCount.getOrDefault(v, 0) + 1);

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
            Map<Vehicule, Integer> trajetCount,
            Timestamp departureTime,
            Map<String, TypeCarburant> typeById) {

        List<Vehicule> candidates = new ArrayList<>();
        for (Vehicule v : available) {
            int cap      = remainingCap.getOrDefault(v, 0);
            Timestamp ft = nextFreeTime.getOrDefault(v, new Timestamp(0));
            Integer pax = r.getNbrPassager() != null ? r.getNbrPassager() : 0;

            if (cap >= pax && !ft.after(departureTime)) {
                candidates.add(v);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        int minTrajets = Integer.MAX_VALUE;
        for (Vehicule v : candidates) {
            int cnt = trajetCount.getOrDefault(v, 0);
            if (cnt < minTrajets) {
                minTrajets = cnt;
            }
        }

        // Priorité 1: prendre les véhicules avec le plus petit nombre de trajets,
        // donc prioritairement ceux à 0 trajet en début de journée.
        List<Vehicule> byUsage = new ArrayList<>();
        for (Vehicule v : candidates) {
            if (trajetCount.getOrDefault(v, 0) == minTrajets) {
                byUsage.add(v);
            }
        }

        Vehicule best = null;
        int bestFuelPriority = Integer.MIN_VALUE;
        int bestDiff = Integer.MAX_VALUE;
        String bestId = null;

        for (Vehicule v : byUsage) {
            Integer pax = r.getNbrPassager() != null ? r.getNbrPassager() : 0;
            int cap = remainingCap.getOrDefault(v, 0);
            int diff = cap - pax;

            TypeCarburant tc = typeById.get(v.getIdTypeCarburant());
            int fuelPriority = getFuelPriority(tc != null ? tc.getCode() : null);
            String currentId = v.getIdVehicule() != null ? v.getIdVehicule() : "";

            // Priorité 2: à usage égal, prioriser le carburant
            // Electrique > Diesel > Essence > autres.
            if (fuelPriority > bestFuelPriority
                    || (fuelPriority == bestFuelPriority && diff < bestDiff)
                    || (fuelPriority == bestFuelPriority && diff == bestDiff
                            && (bestId == null || currentId.compareTo(bestId) < 0))) {
                best = v;
                bestFuelPriority = fuelPriority;
                bestDiff = diff;
                bestId = currentId;
            }
        }

        return best;
    }

    private int getFuelPriority(String fuelCode) {
        if (fuelCode == null) {
            return 0;
        }
        if ("EL".equalsIgnoreCase(fuelCode)) {
            return 3;
        }
        if ("D".equalsIgnoreCase(fuelCode)) {
            return 2;
        }
        if ("E".equalsIgnoreCase(fuelCode)) {
            return 1;
        }
        return 0;
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

    private Timestamp startOfDay(Timestamp ts) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(ts.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Timestamp(cal.getTimeInMillis());
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

    private Map<String, String> buildQuartierByHotelMap(List<Hotel> hotels, List<LieuHotel> lieux) {
        Map<String, String> lieuNames = new HashMap<>();
        for (LieuHotel l : lieux) {
            lieuNames.put(l.getIdLieu(), l.getNomLieu());
        }

        Map<String, String> quartierByHotel = new HashMap<>();
        for (Hotel h : hotels) {
            quartierByHotel.put(h.getIdHotel(), lieuNames.getOrDefault(h.getIdLieu(), ""));
        }
        return quartierByHotel;
    }

    private BigDecimal distanceFromAirport(Reservation r, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix) {
        if (r == null || r.getIdHotel() == null) {
            return BigDecimal.valueOf(Double.MAX_VALUE);
        }
        Hotel h = hotelMap.get(r.getIdHotel());
        if (h == null || h.getIdLieu() == null) {
            return BigDecimal.valueOf(Double.MAX_VALUE);
        }
        Distance d = getDistance(distanceMatrix, "LIEU001", h.getIdLieu());
        return d != null ? d.getKilometre() : BigDecimal.valueOf(Double.MAX_VALUE);
    }

    // En l'absence d'un champ de statut dédié en base, une réservation complète
    // (id/client/hotel/date/passagers valides) est considérée confirmée.
    private boolean isReservationConfirmed(Reservation r) {
        return r != null
                && r.getIdReservation() != null
                && r.getIdClient() != null
                && r.getIdHotel() != null
                && r.getDateResa() != null
                && r.getNbrPassager() != null
                && r.getNbrPassager() > 0;
    }

    private Map<String, Map<String, Distance>> buildDistanceMatrix(List<Distance> distances) {
        Map<String, Map<String, Distance>> m = new HashMap<>();
        for (Distance d : distances) {
            m.computeIfAbsent(d.getLieuFrom(), k -> new HashMap<>()).put(d.getLieuTo(), d);
        }
        return m;
    }
}
