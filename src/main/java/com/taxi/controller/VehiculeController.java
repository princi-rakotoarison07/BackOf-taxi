package com.taxi.controller;

import com.taxi.model.Vehicule;
import com.taxi.model.TypeCarburant;
import com.taxi.model.Reservation;
import com.taxi.util.DBConnection;
import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.annotation.PostMapping;
import framework.annotation.Param;
import framework.annotation.ModelAttribute;
import framework.annotation.RestController;
import framework.utilitaire.ModelAndView;
import java.sql.Connection;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.taxi.model.Hotel;
import com.taxi.model.Distance;
import com.taxi.model.Parametre;

@Controller
@RestController
public class VehiculeController {

    @GetMapping("/api/type-carburants")
    public List<TypeCarburant> listTypeCarburants() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            return TypeCarburant.getAll(TypeCarburant.class, conn);
        }
    }

    @GetMapping("/vehicule/list")
    public ModelAndView list() throws Exception {
        ModelAndView mv = new ModelAndView("/views/vehicule/list.jsp");
        try (Connection conn = DBConnection.getConnection()) {
            List<Vehicule> vehicules = Vehicule.getAll(Vehicule.class, conn);
            List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
            mv.addObject("vehicules", vehicules);
            mv.addObject("types", types);
        }
        return mv;
    }

    @GetMapping("/vehicule/form")
    public ModelAndView showForm() throws Exception {
        ModelAndView mv = new ModelAndView("/views/vehicule/form.jsp");
        try (Connection conn = DBConnection.getConnection()) {
            List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
            mv.addObject("types", types);
        }
        return mv;
    }

    @GetMapping("/vehicule/edit")
    public ModelAndView edit(@Param("id") String id) throws Exception {
        ModelAndView mv = new ModelAndView("/views/vehicule/form.jsp");
        try (Connection conn = DBConnection.getConnection()) {
            Vehicule v = Vehicule.getById(Vehicule.class, id, conn);
            List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
            mv.addObject("vehicule", v);
            mv.addObject("types", types);
        }
        return mv;
    }

    @GetMapping("/vehicule/disponible")
    public ModelAndView disponible(@Param("date") String date, @Param("time") String time) throws Exception {
        ModelAndView mv = new ModelAndView("/views/vehicule/disponible.jsp");
        mv.addObject("pageTitle", "Véhicules Disponibles");
        try (Connection conn = DBConnection.getConnection()) {
            List<Vehicule> vehicules = Vehicule.getAll(Vehicule.class, conn);
            List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
            List<Reservation> reservations = Reservation.getAll(Reservation.class, conn);
            List<Hotel> hotels = Hotel.getAll(Hotel.class, conn);
            List<Distance> distances = Distance.getAll(Distance.class, conn);
            List<Parametre> parametres = Parametre.getAll(Parametre.class, conn);
            Parametre currentParam = (parametres != null && !parametres.isEmpty()) ? parametres.get(0) : null;

            List<Reservation> filtered = filtrerReservations(reservations, date);

            Map<String, TypeCarburant> typeById = construireMapType(types);
            Map<String, Hotel> hotelMap = construireMapHotel(hotels);
            Map<String, Map<String, Distance>> distanceMatrix = construireMatriceDistance(distances);

            // Calculer les assignations pour toute la journée pour connaître les périodes
            // d'occupation
            Map<Vehicule, List<Tournee>> planning = calculerPlanningJournée(filtered, vehicules, typeById, hotelMap,
                    distanceMatrix, currentParam);

            Timestamp targetTime = null;
            if (date != null && !date.isEmpty() && time != null && !time.isEmpty()) {
                targetTime = Timestamp.valueOf(date + " " + time + ":00");
            }

            List<Vehicule> disponibles = new ArrayList<>();
            for (Vehicule v : vehicules) {
                boolean isBusy = false;
                List<Tournee> tournees = planning.get(v);
                if (tournees != null) {
                    if (targetTime != null) {
                        // Vérifier si le véhicule est en tournée à targetTime
                        for (Tournee t : tournees) {
                            if (!targetTime.before(t.start) && targetTime.before(t.end)) {
                                isBusy = true;
                                break;
                            }
                        }
                    } else {
                        // Si pas d'heure, on considère occupé s'il a AU MOINS une tournée ce jour
                        isBusy = !tournees.isEmpty();
                    }
                }
                if (!isBusy) {
                    disponibles.add(v);
                }
            }

            mv.addObject("disponibles", disponibles);
            mv.addObject("types", types);
            mv.addObject("selectedDate", date);
            mv.addObject("selectedTime", time);
        }
        return mv;
    }

    private static class Tournee {
        Timestamp start;
        Timestamp end;

        Tournee(Timestamp s, Timestamp e) {
            this.start = s;
            this.end = e;
        }
    }

    private Map<Vehicule, List<Tournee>> calculerPlanningJournée(List<Reservation> reservations,
            List<Vehicule> vehicules, Map<String, TypeCarburant> typeById, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix, Parametre param) {
        Map<Vehicule, List<Tournee>> planning = new HashMap<>();

        // Durée de la fenêtre d'attente en ms
        long waitWindowMs = (param != null && param.getTempsAttente() != null && param.getTempsAttente() > 0)
                ? param.getTempsAttente() * 60_000L : 0L;

        // Trier les réservations par heure croissante
        List<Reservation> sorted = new ArrayList<>(reservations);
        sorted.sort((a, b) -> {
            if (a.getDateResa() == null) return -1;
            if (b.getDateResa() == null) return 1;
            return a.getDateResa().compareTo(b.getDateResa());
        });

        Map<Vehicule, Timestamp> nextFreeTime = new HashMap<>();
        Map<Vehicule, Integer> trajetCount = new HashMap<>();
        Timestamp dayStart = sorted.isEmpty() || sorted.get(0).getDateResa() == null
                ? new Timestamp(0)
                : startOfDay(sorted.get(0).getDateResa());
        for (Vehicule v : vehicules) {
            nextFreeTime.put(v, dayStart);
            trajetCount.put(v, 0);
        }

        Set<String> processed = new HashSet<>();

        for (Reservation anchor : sorted) {
            if (processed.contains(anchor.getIdReservation())) continue;
            if (anchor.getDateResa() == null) continue;

            Timestamp t0        = anchor.getDateResa();
            Timestamp windowEnd = new Timestamp(t0.getTime() + waitWindowMs);

            // Fenêtre d'attente
            List<Reservation> group = new ArrayList<>();
            for (Reservation r : sorted) {
                if (processed.contains(r.getIdReservation())) continue;
                if (r.getDateResa() == null) continue;
                if (!r.getDateResa().before(t0) && !r.getDateResa().after(windowEnd)) {
                    group.add(r);
                }
            }

            // Heure de départ réelle du groupe
            Timestamp departureTime = t0;
            if (group.size() > 1) {
                Timestamp latest = t0;
                for (Reservation r : group) {
                    if (r.getDateResa().after(latest)) latest = r.getDateResa();
                }
                departureTime = latest;
            }

            group.sort((a, b) -> {
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
                return pb.compareTo(pa);
            });

            Map<Vehicule, Integer> remainingCapacity = new HashMap<>();
            for (Vehicule v : vehicules) {
                remainingCapacity.put(v, v.getNbrPlace() != null ? v.getNbrPlace() : 0);
            }

            Map<Vehicule, List<Reservation>> assignedToVehicule = new HashMap<>();
            final Timestamp dTime = departureTime;
            for (Reservation r : group) {
                Vehicule best = trouverMeilleurVehicule(r, vehicules, remainingCapacity, nextFreeTime,
                        trajetCount, dTime, typeById);
                if (best != null) {
                    remainingCapacity.put(best, remainingCapacity.get(best) - r.getNbrPassager());
                    assignedToVehicule.computeIfAbsent(best, k -> new ArrayList<>()).add(r);
                }
                processed.add(r.getIdReservation());
            }

            for (Map.Entry<Vehicule, List<Reservation>> entry : assignedToVehicule.entrySet()) {
                Vehicule v = entry.getKey();
                List<Reservation> tourResas = entry.getValue();
                long durationMs = calculerDureeTournee(tourResas, hotelMap, distanceMatrix, param);
                Timestamp endTime = new Timestamp(dTime.getTime() + durationMs);
                nextFreeTime.put(v, endTime);
                trajetCount.put(v, trajetCount.getOrDefault(v, 0) + 1);
                planning.computeIfAbsent(v, k -> new ArrayList<>()).add(new Tournee(dTime, endTime));
            }
        }
        return planning;
    }

    private Vehicule trouverMeilleurVehicule(Reservation r, List<Vehicule> available,
            Map<Vehicule, Integer> remainingCapacity, Map<Vehicule, Timestamp> nextFreeTime,
            Map<Vehicule, Integer> trajetCount, Timestamp currentTime, Map<String, TypeCarburant> typeById) {
        List<Vehicule> candidates = new ArrayList<>();
        for (Vehicule v : available) {
            int cap = remainingCapacity.getOrDefault(v, 0);
            Timestamp freeTime = nextFreeTime.getOrDefault(v, new Timestamp(0));
            int pax = r.getNbrPassager() != null ? r.getNbrPassager() : 0;
            if (cap >= pax && !freeTime.after(currentTime)) {
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

        Vehicule best = null;
        int bestFuelPriority = Integer.MIN_VALUE;
        int bestDiff = Integer.MAX_VALUE;
        String bestId = null;

        for (Vehicule v : candidates) {
            if (trajetCount.getOrDefault(v, 0) != minTrajets) {
                continue;
            }
            int pax = r.getNbrPassager() != null ? r.getNbrPassager() : 0;
            int cap = remainingCapacity.getOrDefault(v, 0);
            int diff = cap - pax;
            TypeCarburant t = typeById.get(v.getIdTypeCarburant());
            int fuelPriority = getFuelPriority(t != null ? t.getCode() : null);
            String currentId = v.getIdVehicule() != null ? v.getIdVehicule() : "";

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

    private long calculerDureeTournee(List<Reservation> tour, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix, Parametre param) {
        if (param == null || param.getVitesseMoyenne() == null
                || param.getVitesseMoyenne().compareTo(BigDecimal.ZERO) <= 0)
            return 0;
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
            } else
                break;
        }
        Distance retour = getDistance(distanceMatrix, currentLieu, "LIEU001");
        if (retour != null)
            totalDistance = totalDistance.add(retour.getKilometre());
        BigDecimal travelTimeHours = totalDistance.divide(param.getVitesseMoyenne(), 4, RoundingMode.HALF_UP);
        return (long) (travelTimeHours.doubleValue() * 3600000L);
    }

    private Distance getDistance(Map<String, Map<String, Distance>> matrix, String from, String to) {
        if (matrix.containsKey(from)) {
            Distance d = matrix.get(from).get(to);
            if (d != null)
                return d;
        }
        if (matrix.containsKey(to))
            return matrix.get(to).get(from);
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

    private List<Reservation> filtrerReservations(List<Reservation> reservations, String date) {
        if (date == null || date.isEmpty())
            return new ArrayList<>();
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

    @PostMapping("/vehicule/save")
    public ModelAndView save(@ModelAttribute Vehicule vehicule) {
        ModelAndView mv = new ModelAndView("/views/vehicule/form.jsp");
        try (Connection conn = DBConnection.getConnection()) {
            vehicule.insert(conn);
            mv.addObject("successMessage", "Véhicule enregistré avec succès ! ID: " + vehicule.getIdVehicule());

            // Re-charger les types pour le formulaire
            List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
            mv.addObject("types", types);
        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("errorMessage", "Erreur lors de l'enregistrement du véhicule : " + e.getMessage());
            try (Connection conn = DBConnection.getConnection()) {
                List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
                mv.addObject("types", types);
            } catch (Exception ignored) {
            }
        }
        return mv;
    }

    @PostMapping("/vehicule/update")
    public ModelAndView update(@ModelAttribute Vehicule vehicule) {
        ModelAndView mv = new ModelAndView("/views/vehicule/form.jsp");
        try (Connection conn = DBConnection.getConnection()) {
            vehicule.update(conn);
            mv.addObject("successMessage", "Véhicule mis à jour avec succès !");
            mv.addObject("vehicule", vehicule);

            // Re-charger les types pour le formulaire
            List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
            mv.addObject("types", types);
        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("errorMessage", "Erreur lors de la mise à jour du véhicule : " + e.getMessage());
            mv.addObject("vehicule", vehicule);
            try (Connection conn = DBConnection.getConnection()) {
                List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
                mv.addObject("types", types);
            } catch (Exception ignored) {
            }
        }
        return mv;
    }

    @GetMapping("/vehicule/delete")
    public ModelAndView delete(@Param("id") String id) {
        ModelAndView mv = new ModelAndView("/vehicule/list");
        mv.setRedirect(true);
        try (Connection conn = DBConnection.getConnection()) {
            Vehicule.deleteById(Vehicule.class, id, conn);
        } catch (Exception e) {
            e.printStackTrace();
            // On pourrait gérer l'erreur via un paramètre flash ou session si le framework
            // le supporte
        }
        return mv;
    }
}
