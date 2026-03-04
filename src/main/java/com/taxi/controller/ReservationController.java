package com.taxi.controller;

import com.taxi.model.Reservation;
import com.taxi.util.DBConnection;
import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.annotation.PostMapping;
import framework.annotation.Param;
import framework.annotation.ModelAttribute;
import framework.utilitaire.ModelAndView;
import framework.annotation.RestController;
import java.sql.Connection;
import java.util.List;
import java.sql.Timestamp;
import com.taxi.model.Vehicule;
import com.taxi.model.TypeCarburant;
import com.taxi.model.Hotel;
import com.taxi.model.Distance;
import com.taxi.model.Parametre;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.math.RoundingMode;
import java.math.BigDecimal;

@Controller
@RestController
public class ReservationController {

    @GetMapping("/api/reservations")
    public List<Reservation> listReservations() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            return Reservation.getAll(Reservation.class, conn);
        }
    }

    @GetMapping("/reservation/form")
    public ModelAndView showForm() {
        return new ModelAndView("/views/reservationForm.jsp");
    }

    @PostMapping("/reservation/save")
    public ModelAndView save(@ModelAttribute Reservation reservation) {
        ModelAndView mv = new ModelAndView("/views/result.jsp");
        try (Connection conn = DBConnection.getConnection()) {
            // Si la date est nulle, on met la date actuelle
            if (reservation.getDateResa() == null) {
                reservation.setDateResa(new Timestamp(System.currentTimeMillis()));
            }

            reservation.insert(conn);
            mv.addObject("message", "Réservation enregistrée avec succès !");
        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("error", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
        return mv;
    }

    @GetMapping("/reservation/assignation")
    public ModelAndView assignation(@Param("date") String date) throws Exception {
        ModelAndView mv = new ModelAndView("/views/reservationAssignation.jsp");
        mv.addObject("pageTitle", "Assignation des Réservations");
        try (Connection conn = DBConnection.getConnection()) {
            List<Reservation> allReservations = Reservation.getAll(Reservation.class, conn);
            List<Vehicule> vehicules = Vehicule.getAll(Vehicule.class, conn);
            List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
            List<Hotel> hotels = Hotel.getAll(Hotel.class, conn);
            List<Distance> distances = Distance.getAll(Distance.class, conn);
            List<Parametre> parametres = Parametre.getAll(Parametre.class, conn);
            Parametre currentParam = (parametres != null && !parametres.isEmpty()) ? parametres.get(0) : null;

            List<Reservation> filtered = filtrerReservations(allReservations, date);

            Map<String, TypeCarburant> typeById = construireMapType(types);
            Map<String, Hotel> hotelMap = construireMapHotel(hotels);
            Map<String, Map<String, Distance>> distanceMatrix = construireMatriceDistance(distances);

            Map<String, Vehicule> assignments = assignerVehicules(filtered, vehicules, typeById);
            Map<String, Timestamp> departureTimes = new HashMap<>();
            Map<String, Timestamp> arrivalTimes = new HashMap<>();

            calculerHoraires(filtered, currentParam, hotelMap, distanceMatrix, assignments, departureTimes,
                    arrivalTimes);

            mv.addObject("reservations", filtered);
            mv.addObject("types", types);
            mv.addObject("assignments", assignments);
            mv.addObject("departureTimes", departureTimes);
            mv.addObject("arrivalTimes", arrivalTimes);
            mv.addObject("selectedDate", date);
        }
        return mv;
    }

    private List<Reservation> filtrerReservations(List<Reservation> reservations, String date) {
        if (date == null || date.isEmpty())
            return reservations;
        List<Reservation> filtered = new ArrayList<>();
        Timestamp start = Timestamp.valueOf(date + " 00:00:00");
        Timestamp end = Timestamp.valueOf(date + " 23:59:59");
        for (Reservation r : reservations) {
            if (r.getDateResa() != null && !r.getDateResa().before(start) && !r.getDateResa().after(end)) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    private Map<String, TypeCarburant> construireMapType(List<TypeCarburant> types) {
        Map<String, TypeCarburant> map = new HashMap<>();
        for (TypeCarburant t : types)
            map.put(t.getIdTypeCarburant(), t);
        return map;
    }

    private Map<String, Hotel> construireMapHotel(List<Hotel> hotels) {
        Map<String, Hotel> map = new HashMap<>();
        for (Hotel h : hotels)
            map.put(h.getIdHotel(), h);
        return map;
    }

    private Map<String, Map<String, Distance>> construireMatriceDistance(List<Distance> distances) {
        Map<String, Map<String, Distance>> matrix = new HashMap<>();
        for (Distance d : distances) {
            matrix.computeIfAbsent(d.getLieuFrom(), k -> new HashMap<>()).put(d.getLieuTo(), d);
        }
        return matrix;
    }

    private Map<String, Vehicule> assignerVehicules(List<Reservation> reservations, List<Vehicule> vehicules,
            Map<String, TypeCarburant> typeById) {
        Map<String, Vehicule> assignments = new HashMap<>();
        List<Vehicule> available = new ArrayList<>(vehicules);

        // Grouper les réservations par date/heure exacte (tronqué à la minute)
        Map<Timestamp, List<Reservation>> groupedByTime = new HashMap<>();
        for (Reservation r : reservations) {
            if (r.getDateResa() == null)
                continue;
            // Tronquer à la minute pour le groupement
            long timeMs = r.getDateResa().getTime();
            Timestamp truncated = new Timestamp((timeMs / 60000L) * 60000L);
            groupedByTime.computeIfAbsent(truncated, k -> new ArrayList<>()).add(r);
        }

        for (Map.Entry<Timestamp, List<Reservation>> entry : groupedByTime.entrySet()) {
            List<Reservation> group = entry.getValue();
            group.sort((a, b) -> b.getNbrPassager().compareTo(a.getNbrPassager()));

            Map<Vehicule, Integer> remainingCapacity = new HashMap<>();
            for (Vehicule v : available) {
                remainingCapacity.put(v, v.getNbrPlace() != null ? v.getNbrPlace() : 0);
            }

            for (Reservation r : group) {
                Vehicule best = trouverMeilleurVehiculePourGroupe(r, available, remainingCapacity, typeById);
                if (best != null) {
                    assignments.put(r.getIdReservation(), best);
                    remainingCapacity.put(best, remainingCapacity.get(best) - r.getNbrPassager());
                }
            }

        }

        return assignments;
    }

    private Vehicule trouverMeilleurVehiculePourGroupe(Reservation r, List<Vehicule> available,
            Map<Vehicule, Integer> remainingCapacity, Map<String, TypeCarburant> typeById) {
        Vehicule best = null;
        int bestScoreDiesel = -1;
        int bestDiff = Integer.MAX_VALUE;

        for (Vehicule v : available) {
            int cap = remainingCapacity.getOrDefault(v, 0);
            if (cap >= r.getNbrPassager()) {
                TypeCarburant t = typeById.get(v.getIdTypeCarburant());
                int dieselScore = (t != null && t.getCode() != null && t.getCode().equalsIgnoreCase("D")) ? 1 : 0;
                // La différence est par rapport à la capacité INITIALE pour le "best fit" du
                // premier passager,
                // ou par rapport à la capacité restante ? L'énoncé dit "proche du capacite du
                // vehicule".
                // On va garder la logique de proximité à la capacité totale pour le choix
                // initial.
                int diff = v.getNbrPlace() - r.getNbrPassager();

                if (diff < bestDiff || (diff == bestDiff && dieselScore > bestScoreDiesel)) {
                    best = v;
                    bestScoreDiesel = dieselScore;
                    bestDiff = diff;
                }
            }
        }
        return best;
    }

    private void calculerHoraires(List<Reservation> reservations, Parametre param, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix, Map<String, Vehicule> assignments,
            Map<String, Timestamp> departureTimes, Map<String, Timestamp> arrivalTimes) {
        if (param == null || param.getVitesseMoyenne() == null
                || param.getVitesseMoyenne().compareTo(BigDecimal.ZERO) <= 0)
            return;

        // Grouper par véhicule et heure pour calculer les tournées
        Map<Vehicule, Map<Timestamp, List<Reservation>>> tournées = new HashMap<>();
        for (Reservation r : reservations) {
            Vehicule v = assignments.get(r.getIdReservation());
            if (v != null && r.getDateResa() != null) {
                tournées.computeIfAbsent(v, k -> new HashMap<>())
                        .computeIfAbsent(r.getDateResa(), k -> new ArrayList<>())
                        .add(r);
            }
        }

        for (Map.Entry<Vehicule, Map<Timestamp, List<Reservation>>> vEntry : tournées.entrySet()) {
            for (Map.Entry<Timestamp, List<Reservation>> tEntry : vEntry.getValue().entrySet()) {
                List<Reservation> tour = tEntry.getValue();
                Timestamp time = tEntry.getKey();

                // Calcul de la tournée optimisée (Greedy TSP à partir de l'aéroport LIEU001)
                BigDecimal totalDistance = BigDecimal.ZERO;
                String currentLieu = "LIEU001";
                List<Reservation> remainingResa = new ArrayList<>(tour);

                while (!remainingResa.isEmpty()) {
                    Reservation nextResa = null;
                    BigDecimal minDistance = BigDecimal.valueOf(Double.MAX_VALUE);

                    for (Reservation r : remainingResa) {
                        Hotel h = hotelMap.get(r.getIdHotel());
                        if (h != null) {
                            Distance d = getDistance(distanceMatrix, currentLieu, h.getIdLieu());
                            if (d != null && d.getKilometre().compareTo(minDistance) < 0) {
                                minDistance = d.getKilometre();
                                nextResa = r;
                            }
                        }
                    }

                    if (nextResa != null) {
                        totalDistance = totalDistance.add(minDistance);
                        Hotel h = hotelMap.get(nextResa.getIdHotel());
                        currentLieu = h.getIdLieu();
                        remainingResa.remove(nextResa);
                    } else {
                        // Pas de distance trouvée pour le reste, on sort
                        break;
                    }
                }

                // Retour à l'aéroport
                Distance retour = getDistance(distanceMatrix, currentLieu, "LIEU001");
                if (retour != null) {
                    totalDistance = totalDistance.add(retour.getKilometre());
                }

                // Temps total de trajet en ms
                BigDecimal travelTimeHours = totalDistance.divide(param.getVitesseMoyenne(), 4, RoundingMode.HALF_UP);
                long travelTimeMs = (long) (travelTimeHours.doubleValue() * 3600000L);

                // Appliquer les horaires à toutes les réservations de la tournée
                for (Reservation r : tour) {
                    departureTimes.put(r.getIdReservation(), time);
                    arrivalTimes.put(r.getIdReservation(), new Timestamp(time.getTime() + travelTimeMs));
                }
            }
        }
    }

    private Distance getDistance(Map<String, Map<String, Distance>> matrix, String from, String to) {
        if (matrix.containsKey(from)) {
            Distance d = matrix.get(from).get(to);
            if (d != null)
                return d;
        }
        // Tenter l'inverse si non trouvé
        if (matrix.containsKey(to)) {
            return matrix.get(to).get(from);
        }
        return null;
    }
}
