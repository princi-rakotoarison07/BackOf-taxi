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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.math.RoundingMode;
import java.math.BigDecimal;

@Controller
@RestController
public class ReservationController {

    @GetMapping("/BackOf-taxi/api/reservations")
    public List<Reservation> listReservations() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            return Reservation.getAll(Reservation.class, conn);
        }
    }

    @GetMapping("/BackOf-taxi/reservation/form")
    public ModelAndView showForm() throws Exception {
        ModelAndView mv = new ModelAndView("/views/reservation/form.jsp");
        try (Connection conn = DBConnection.getConnection()) {
            List<Hotel> hotels = Hotel.getAll(Hotel.class, conn);
            mv.addObject("hotels", hotels);
        }
        return mv;
    }

    @PostMapping("/BackOf-taxi/reservation/save")
    public ModelAndView save(@ModelAttribute Reservation reservation) {
        ModelAndView mv = new ModelAndView("/views/result.jsp");
        try (Connection conn = DBConnection.getConnection()) {
            if (reservation.getDateResa() == null) {
                reservation.setDateResa(new Timestamp(System.currentTimeMillis()));
            }
            reservation.insert(conn);
            mv.addObject("message", "Réservation enregistrée !");
        } catch (Exception e) {
            mv.addObject("error", "Erreur : " + e.getMessage());
        }
        return mv;
    }

    @PostMapping("/BackOf-taxi/reservation/save-multiple")
    public ModelAndView saveMultiple(@Param("reservationsData") String data) {
        ModelAndView mv = new ModelAndView("/views/result.jsp");
        if (data == null || data.trim().isEmpty()) {
            mv.addObject("error", "Aucune donnée reçue.");
            return mv;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int count = 0;
                // Format: idRes|idClient|nbrPax|idHotel|date;idRes2|...
                String[] rows = data.split(";");
                for (String row : rows) {
                    if (row.trim().isEmpty()) continue;
                    String[] fields = row.split("\\|");
                    if (fields.length < 4) continue;

                    Reservation r = new Reservation();
                    r.setIdReservation(fields[0]);
                    r.setIdClient(fields[1]);
                    r.setNbrPassager(Integer.parseInt(fields[2]));
                    r.setIdHotel(fields[3]);

                    if (fields.length > 4 && !fields[4].isEmpty()) {
                        String dateStr = fields[4].replace("T", " ");
                        if (dateStr.length() == 16) dateStr += ":00";
                        r.setDateResa(Timestamp.valueOf(dateStr));
                    } else {
                        r.setDateResa(new Timestamp(System.currentTimeMillis()));
                    }

                    r.insert(conn);
                    count++;
                }
                conn.commit();
                mv.addObject("message", count + " réservations enregistrées !");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("error", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
        return mv;
    }

    @GetMapping("/BackOf-taxi/reservation/assignation")
    public ModelAndView assignation(@Param("date") String date) throws Exception {
        ModelAndView mv = new ModelAndView("/views/reservation/assignation.jsp");
        mv.addObject("pageTitle", "Assignation des Réservations");
        prepareAssignationData(mv, date);
        return mv;
    }

    @GetMapping("/BackOf-taxi/reservation/assignation-vehicule")
    public ModelAndView assignationVehicule(@Param("date") String date) throws Exception {
        ModelAndView mv = new ModelAndView("/views/reservation/assignationVehicule.jsp");
        mv.addObject("pageTitle", "Assignation par Véhicule");
        prepareAssignationData(mv, date);
        return mv;
    }

    private void prepareAssignationData(ModelAndView mv, String date) throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            List<Reservation> allReservations = Reservation.getAll(Reservation.class, conn);
            List<Vehicule> vehicules = Vehicule.getAll(Vehicule.class, conn);
            List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
            List<Hotel> hotels = Hotel.getAll(Hotel.class, conn);
            List<Distance> distances = Distance.getAll(Distance.class, conn);
            List<Parametre> parametres = Parametre.getAll(Parametre.class, conn);
            Parametre currentParam = (parametres != null && !parametres.isEmpty()) ? parametres.get(parametres.size() - 1) : null;

            Map<String, TypeCarburant> typeById = construireMapType(types);
            Map<String, Hotel> hotelMap = construireMapHotel(hotels);
            Map<String, Map<String, Distance>> distanceMatrix = construireMatriceDistance(distances);

            List<Reservation> filtered = filtrerReservations(allReservations, date);
            filtered.sort((a, b) -> comparerReservationsPourAssignation(a, b, hotelMap, distanceMatrix));

            Map<String, Timestamp> plannedDepartureTimes = new HashMap<>();
            Map<String, Integer> unassignedPassengers = new HashMap<>();
            Map<String, Vehicule> assignments = assignerVehicules(filtered, vehicules, typeById, hotelMap,
                    distanceMatrix, currentParam, plannedDepartureTimes, unassignedPassengers);
            Map<String, Timestamp> departureTimes = new HashMap<>();
            Map<String, Timestamp> arrivalTimes = new HashMap<>();

            calculerHoraires(filtered, currentParam, hotelMap, distanceMatrix, assignments, plannedDepartureTimes, departureTimes,
                    arrivalTimes);

            mv.addObject("reservations", filtered);
            mv.addObject("vehicules", vehicules);
            mv.addObject("types", types);
            mv.addObject("assignments", assignments);
            mv.addObject("departureTimes", departureTimes);
            mv.addObject("arrivalTimes", arrivalTimes);
            mv.addObject("unassignedPassengers", unassignedPassengers);
            mv.addObject("selectedDate", date);
            mv.addObject("hotels", hotels);
            mv.addObject("hotelMap", hotelMap);
            
            // Calculer l'ordre des tournées et les heures détaillées pour l'affichage
            Map<String, List<Reservation>> tourOrders = calculerOrdreTournées(filtered, assignments, hotelMap, distanceMatrix, departureTimes, arrivalTimes);
            Map<String, Map<String, java.sql.Timestamp>> detailedTimes = calculerHorairesDetailles(filtered, assignments, hotelMap, distanceMatrix, currentParam, departureTimes, arrivalTimes);
            Map<String, BigDecimal> tourDistancesKm = calculerDistancesTournees(filtered, assignments, hotelMap, distanceMatrix, departureTimes, arrivalTimes, tourOrders);
            mv.addObject("tourOrders", tourOrders);
            mv.addObject("detailedTimes", detailedTimes);
            mv.addObject("tourDistancesKm", tourDistancesKm);
        }
    }

    private List<Reservation> filtrerReservations(List<Reservation> reservations, String date) {
        List<Reservation> filtered = new ArrayList<>();
        if (reservations == null) {
            return filtered;
        }

        boolean hasDateFilter = date != null && !date.isEmpty();
        Timestamp start = hasDateFilter ? Timestamp.valueOf(date + " 00:00:00") : null;
        Timestamp end = hasDateFilter ? Timestamp.valueOf(date + " 23:59:59") : null;

        for (Reservation r : reservations) {
            if (!estReservationValide(r)) {
                continue;
            }
            if (!hasDateFilter || (!r.getDateResa().before(start) && !r.getDateResa().after(end))) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    private boolean estReservationValide(Reservation reservation) {
        return reservation != null
                && reservation.getDateResa() != null
                && reservation.getNbrPassager() != null
                && reservation.getNbrPassager() > 0
                && reservation.getIdHotel() != null
                && !reservation.getIdHotel().trim().isEmpty();
    }

    private int comparerReservationsPourAssignation(Reservation a, Reservation b,
            Map<String, Hotel> hotelMap, Map<String, Map<String, Distance>> distanceMatrix) {
        if (a == null && b == null)
            return 0;
        if (a == null)
            return 1;
        if (b == null)
            return -1;

        int byDate = a.getDateResa().compareTo(b.getDateResa());
        if (byDate != 0) {
            return byDate;
        }

        int byPax = b.getNbrPassager().compareTo(a.getNbrPassager());
        if (byPax != 0) {
            return byPax;
        }

        BigDecimal distA = distanceDepuisBase(a, hotelMap, distanceMatrix);
        BigDecimal distB = distanceDepuisBase(b, hotelMap, distanceMatrix);
        int byDistance = distA.compareTo(distB);
        if (byDistance != 0) {
            return byDistance;
        }

        String lieuA = cleLieuAlphabetique(a, hotelMap);
        String lieuB = cleLieuAlphabetique(b, hotelMap);
        int byLieu = lieuA.compareToIgnoreCase(lieuB);
        if (byLieu != 0) {
            return byLieu;
        }

        String idA = a.getIdReservation() == null ? "" : a.getIdReservation();
        String idB = b.getIdReservation() == null ? "" : b.getIdReservation();
        return idA.compareToIgnoreCase(idB);
    }

    private BigDecimal distanceDepuisBase(Reservation reservation, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix) {
        Hotel hotel = reservation != null ? hotelMap.get(reservation.getIdHotel()) : null;
        if (hotel == null || hotel.getIdLieu() == null) {
            return BigDecimal.valueOf(Double.MAX_VALUE);
        }
        Distance d = getDistance(distanceMatrix, "LIEU001", hotel.getIdLieu());
        if (d == null || d.getKilometre() == null) {
            return BigDecimal.valueOf(Double.MAX_VALUE);
        }
        return d.getKilometre();
    }

    private String cleLieuAlphabetique(Reservation reservation, Map<String, Hotel> hotelMap) {
        Hotel hotel = reservation != null ? hotelMap.get(reservation.getIdHotel()) : null;
        if (hotel == null) {
            return "";
        }
        if (hotel.getNomHotel() != null && !hotel.getNomHotel().trim().isEmpty()) {
            return hotel.getNomHotel().trim();
        }
        return hotel.getIdLieu() != null ? hotel.getIdLieu() : "";
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
            Map<String, TypeCarburant> typeById, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix, Parametre param,
            Map<String, Timestamp> plannedDepartureTimes) {
        return assignerVehicules(reservations, vehicules, typeById, hotelMap, distanceMatrix, param,
                plannedDepartureTimes, null);
    }

    private Map<String, Vehicule> assignerVehicules(List<Reservation> reservations, List<Vehicule> vehicules,
            Map<String, TypeCarburant> typeById, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix, Parametre param,
            Map<String, Timestamp> plannedDepartureTimes, Map<String, Integer> unassignedPassengers) {
        Map<String, Vehicule> assignments = new HashMap<>();
        List<Vehicule> available = new ArrayList<>(vehicules);
        int waitMinutes = getTempsAttenteMinutes(param);
        List<List<Reservation>> waitWindows = construireFenetresAttente(reservations, waitMinutes);
        // Le calcul est journalier: au debut de la journee, tous les compteurs repartent a 0.
        Map<Vehicule, Integer> dailyTripCount = initialiserCompteurTrajets(available);

        Map<Vehicule, Timestamp> nextFreeTime = new HashMap<>();
        for (Vehicule v : available) {
            nextFreeTime.put(v, new Timestamp(0));
        }

        for (List<Reservation> group : waitWindows) {
            Timestamp departureTime = resoudreHeureDepart(group, waitMinutes);
            if (departureTime == null)
                continue;

            group.sort((a, b) -> comparerReservationsPourAssignation(a, b, hotelMap, distanceMatrix));

            Map<Vehicule, Integer> remainingCapacity = new HashMap<>();
            for (Vehicule v : available) {
                remainingCapacity.put(v, v.getNbrPlace() != null ? v.getNbrPlace() : 0);
            }

            // On garde trace des réservations assignées à chaque véhicule pour ce groupe
            Map<Vehicule, List<Reservation>> assignedToVehicule = new HashMap<>();

            for (Reservation r : group) {
                List<AffectationVehicule> chunks = repartirReservationParCapacite(r, available, remainingCapacity,
                        nextFreeTime, departureTime, typeById, dailyTripCount);
                if (!chunks.isEmpty()) {
                    Vehicule principal = choisirVehiculePrincipal(chunks, dailyTripCount, typeById);
                    if (principal != null) {
                        assignments.put(r.getIdReservation(), principal);
                        plannedDepartureTimes.put(r.getIdReservation(), departureTime);
                    }

                    int totalAssigned = 0;
                    for (AffectationVehicule chunk : chunks) {
                        totalAssigned += chunk.passagers;
                        remainingCapacity.put(chunk.vehicule,
                                remainingCapacity.getOrDefault(chunk.vehicule, 0) - chunk.passagers);
                        List<Reservation> tour = assignedToVehicule.computeIfAbsent(chunk.vehicule, k -> new ArrayList<>());
                        if (!tour.contains(r)) {
                            tour.add(r);
                        }
                    }

                    int remaining = r.getNbrPassager() - totalAssigned;
                    if (remaining > 0 && unassignedPassengers != null) {
                        unassignedPassengers.put(r.getIdReservation(), remaining);
                    }
                }
            }

            // Après avoir assigné le groupe, on met à jour nextFreeTime pour les véhicules
            // utilisés
            for (Map.Entry<Vehicule, List<Reservation>> entry : assignedToVehicule.entrySet()) {
                Vehicule v = entry.getKey();
                List<Reservation> tour = entry.getValue();
                long durationMs = calculerDureeTournee(tour, hotelMap, distanceMatrix, param);
                nextFreeTime.put(v, new Timestamp(departureTime.getTime() + durationMs));
                dailyTripCount.put(v, dailyTripCount.getOrDefault(v, 0) + 1);
            }
        }

        return assignments;
    }

    private static class AffectationVehicule {
        private final Vehicule vehicule;
        private final int passagers;

        private AffectationVehicule(Vehicule vehicule, int passagers) {
            this.vehicule = vehicule;
            this.passagers = passagers;
        }
    }

    private List<AffectationVehicule> repartirReservationParCapacite(Reservation reservation,
            List<Vehicule> available, Map<Vehicule, Integer> remainingCapacity,
            Map<Vehicule, Timestamp> nextFreeTime, Timestamp currentTime,
            Map<String, TypeCarburant> typeById, Map<Vehicule, Integer> dailyTripCount) {
        List<AffectationVehicule> chunks = new ArrayList<>();
        int toAssign = reservation.getNbrPassager();

        while (toAssign > 0) {
            Vehicule fit = trouverMeilleurVehiculePourGroupe(reservation, available, remainingCapacity, nextFreeTime,
                    currentTime, typeById, dailyTripCount, toAssign);
            if (fit != null) {
                chunks.add(new AffectationVehicule(fit, toAssign));
                toAssign = 0;
                continue;
            }

            Vehicule partial = trouverVehiculePourAllocationPartielle(available, remainingCapacity, nextFreeTime,
                    currentTime, typeById, dailyTripCount);
            if (partial == null) {
                break;
            }

            int cap = remainingCapacity.getOrDefault(partial, 0);
            if (cap <= 0) {
                break;
            }

            int affectes = Math.min(cap, toAssign);
            chunks.add(new AffectationVehicule(partial, affectes));
            remainingCapacity.put(partial, cap - affectes);
            toAssign -= affectes;
        }

        // Restaurer les capacites temporairement modifiees pendant la simulation partielle.
        for (AffectationVehicule chunk : chunks) {
            int cap = remainingCapacity.getOrDefault(chunk.vehicule, 0);
            remainingCapacity.put(chunk.vehicule, cap + chunk.passagers);
        }
        return chunks;
    }

    private Vehicule choisirVehiculePrincipal(List<AffectationVehicule> chunks,
            Map<Vehicule, Integer> dailyTripCount, Map<String, TypeCarburant> typeById) {
        Vehicule principal = null;
        int maxPassengers = -1;
        int bestTrips = Integer.MAX_VALUE;
        int bestFuel = Integer.MAX_VALUE;

        for (AffectationVehicule chunk : chunks) {
            int trips = dailyTripCount.getOrDefault(chunk.vehicule, 0);
            TypeCarburant t = typeById.get(chunk.vehicule.getIdTypeCarburant());
            int fuel = getPrioriteCarburant(t);
            if (chunk.passagers > maxPassengers
                    || (chunk.passagers == maxPassengers && trips < bestTrips)
                    || (chunk.passagers == maxPassengers && trips == bestTrips && fuel < bestFuel)) {
                principal = chunk.vehicule;
                maxPassengers = chunk.passagers;
                bestTrips = trips;
                bestFuel = fuel;
            }
        }
        return principal;
    }

    private int getTempsAttenteMinutes(Parametre param) {
        if (param == null || param.getTempsAttente() == null || param.getTempsAttente() < 0) {
            return 0;
        }
        return param.getTempsAttente();
    }

    private List<List<Reservation>> construireFenetresAttente(List<Reservation> reservations, int waitMinutes) {
        List<Reservation> datedReservations = new ArrayList<>();
        for (Reservation r : reservations) {
            if (estReservationValide(r)) {
                datedReservations.add(r);
            }
        }

        datedReservations.sort((a, b) -> a.getDateResa().compareTo(b.getDateResa()));

        List<List<Reservation>> windows = new ArrayList<>();
        int index = 0;
        while (index < datedReservations.size()) {
            Reservation first = datedReservations.get(index);
            long firstTime = tronquerMinute(first.getDateResa()).getTime();
            long windowEnd = firstTime + (waitMinutes * 60000L);

            List<Reservation> group = new ArrayList<>();
            int cursor = index;
            while (cursor < datedReservations.size()) {
                Reservation current = datedReservations.get(cursor);
                long currentTime = tronquerMinute(current.getDateResa()).getTime();
                if (currentTime <= windowEnd) {
                    group.add(current);
                    cursor++;
                } else {
                    break;
                }
            }

            windows.add(group);
            index = cursor;
        }

        return windows;
    }

    private Timestamp resoudreHeureDepart(List<Reservation> group, int waitMinutes) {
        if (group == null || group.isEmpty()) {
            return null;
        }

        Timestamp first = tronquerMinute(group.get(0).getDateResa());
        if (group.size() == 1 || waitMinutes <= 0) {
            return first;
        }

        Timestamp latestInWindow = first;
        long windowEnd = first.getTime() + (waitMinutes * 60000L);
        for (Reservation r : group) {
            Timestamp current = tronquerMinute(r.getDateResa());
            if (current != null && current.getTime() <= windowEnd && current.after(latestInWindow)) {
                latestInWindow = current;
            }
        }
        return latestInWindow;
    }

    private Timestamp tronquerMinute(Timestamp ts) {
        if (ts == null)
            return null;
        return new Timestamp((ts.getTime() / 60000L) * 60000L);
    }

    private Map<Vehicule, Integer> initialiserCompteurTrajets(List<Vehicule> vehicules) {
        Map<Vehicule, Integer> compteur = new HashMap<>();
        for (Vehicule v : vehicules) {
            compteur.put(v, 0);
        }
        return compteur;
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
            } else {
                break;
            }
        }

        Distance retour = getDistance(distanceMatrix, currentLieu, "LIEU001");
        if (retour != null) {
            totalDistance = totalDistance.add(retour.getKilometre());
        }

        BigDecimal travelTimeHours = totalDistance.divide(param.getVitesseMoyenne(), 4, RoundingMode.HALF_UP);
        return (long) (travelTimeHours.doubleValue() * 3600000L);
    }

    private Vehicule trouverMeilleurVehiculePourGroupe(Reservation r, List<Vehicule> available,
            Map<Vehicule, Integer> remainingCapacity, Map<Vehicule, Timestamp> nextFreeTime, Timestamp currentTime,
            Map<String, TypeCarburant> typeById, Map<Vehicule, Integer> dailyTripCount) {
        return trouverMeilleurVehiculePourGroupe(r, available, remainingCapacity, nextFreeTime, currentTime,
                typeById, dailyTripCount, r.getNbrPassager());
    }

    private Vehicule trouverMeilleurVehiculePourGroupe(Reservation r, List<Vehicule> available,
            Map<Vehicule, Integer> remainingCapacity, Map<Vehicule, Timestamp> nextFreeTime, Timestamp currentTime,
            Map<String, TypeCarburant> typeById, Map<Vehicule, Integer> dailyTripCount,
            int passagersAPlacer) {
        Vehicule best = null;
        int bestCapacityDelta = Integer.MAX_VALUE;
        int bestTripCount = Integer.MAX_VALUE;
        int bestFuelPriority = Integer.MAX_VALUE;
        int bestTotalCapacity = Integer.MAX_VALUE;

        for (Vehicule v : available) {
            int currentCap = remainingCapacity.getOrDefault(v, 0);
            Timestamp freeTime = nextFreeTime.getOrDefault(v, new Timestamp(0));

            // Si le véhicule peut accueillir la réservation et est libre
            if (currentCap >= passagersAPlacer && !freeTime.after(currentTime)) {
                int capacityDelta = currentCap - passagersAPlacer;
                int tripCount = dailyTripCount.getOrDefault(v, 0);
                TypeCarburant t = typeById.get(v.getIdTypeCarburant());
                int fuelPriority = getPrioriteCarburant(t);

                // Criteres de choix (ordre de priorite):
                // 1. Capacite la plus proche (delta minimal) pour les passagers restants
                // 2. Minimiser le nombre de trajets journaliers (priorite aux vehicules a 0 trajet)
                // 3. Carburant: Electrique, puis Diesel, puis Essence
                // 4. Si egalite, favoriser la plus petite capacite totale
                boolean isBetter = false;
                if (capacityDelta < bestCapacityDelta) {
                    isBetter = true;
                } else if (capacityDelta == bestCapacityDelta && tripCount < bestTripCount) {
                    isBetter = true;
                } else if (capacityDelta == bestCapacityDelta && tripCount == bestTripCount
                        && fuelPriority < bestFuelPriority) {
                    isBetter = true;
                } else if (capacityDelta == bestCapacityDelta && tripCount == bestTripCount
                        && fuelPriority == bestFuelPriority
                        && v.getNbrPlace() < bestTotalCapacity) {
                    isBetter = true;
                }

                if (isBetter) {
                    best = v;
                    bestCapacityDelta = capacityDelta;
                    bestTripCount = tripCount;
                    bestFuelPriority = fuelPriority;
                    bestTotalCapacity = v.getNbrPlace();
                }
            }
        }
        return best;
    }

    private Vehicule trouverVehiculePourAllocationPartielle(List<Vehicule> available,
            Map<Vehicule, Integer> remainingCapacity, Map<Vehicule, Timestamp> nextFreeTime, Timestamp currentTime,
            Map<String, TypeCarburant> typeById, Map<Vehicule, Integer> dailyTripCount) {
        Vehicule best = null;
        int bestCapacity = -1;
        int bestTripCount = Integer.MAX_VALUE;
        int bestFuelPriority = Integer.MAX_VALUE;

        for (Vehicule v : available) {
            int cap = remainingCapacity.getOrDefault(v, 0);
            Timestamp freeTime = nextFreeTime.getOrDefault(v, new Timestamp(0));
            if (cap <= 0 || freeTime.after(currentTime)) {
                continue;
            }

            int tripCount = dailyTripCount.getOrDefault(v, 0);
            int fuelPriority = getPrioriteCarburant(typeById.get(v.getIdTypeCarburant()));
            if (cap > bestCapacity
                    || (cap == bestCapacity && tripCount < bestTripCount)
                    || (cap == bestCapacity && tripCount == bestTripCount && fuelPriority < bestFuelPriority)) {
                best = v;
                bestCapacity = cap;
                bestTripCount = tripCount;
                bestFuelPriority = fuelPriority;
            }
        }

        return best;
    }

    private int getPrioriteCarburant(TypeCarburant type) {
        if (type == null) {
            return 4;
        }

        String code = type.getCode() != null ? type.getCode().trim().toLowerCase() : "";
        String libelle = type.getLibelle() != null ? type.getLibelle().trim().toLowerCase() : "";

        if (code.equals("el") || code.equals("elec") || code.equals("ev")
                || libelle.contains("elect") || libelle.contains("lectri")) {
            return 1;
        }
        if (code.equals("d") || libelle.contains("diesel")) {
            return 2;
        }
        if (code.equals("e") || libelle.contains("essence")) {
            return 3;
        }
        return 4;
    }

    private void calculerHoraires(List<Reservation> reservations, Parametre param, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix, Map<String, Vehicule> assignments,
            Map<String, Timestamp> plannedDepartureTimes,
            Map<String, Timestamp> departureTimes, Map<String, Timestamp> arrivalTimes) {
        if (param == null || param.getVitesseMoyenne() == null
                || param.getVitesseMoyenne().compareTo(BigDecimal.ZERO) <= 0)
            return;

        // Grouper par véhicule et heure pour calculer les tournées
        Map<Vehicule, Map<Timestamp, List<Reservation>>> tournées = new HashMap<>();
        for (Reservation r : reservations) {
            Vehicule v = assignments.get(r.getIdReservation());
            Timestamp plannedDeparture = plannedDepartureTimes.get(r.getIdReservation());
            if (v != null && plannedDeparture != null) {
                tournées.computeIfAbsent(v, k -> new HashMap<>())
                        .computeIfAbsent(plannedDeparture, k -> new ArrayList<>())
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

    private Map<String, List<Reservation>> calculerOrdreTournées(List<Reservation> reservations, 
            Map<String, Vehicule> assignments, Map<String, Hotel> hotelMap, 
            Map<String, Map<String, Distance>> distanceMatrix,
            Map<String, Timestamp> departureTimes, Map<String, Timestamp> arrivalTimes) {
        
        Map<String, List<Reservation>> tourOrders = new HashMap<>();
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        // Grouper par véhicule et par créneau horaire (en utilisant les heures de départ/arrivée calculées)
        Map<String, List<Reservation>> vehicleTours = new HashMap<>();
        for (Reservation r : reservations) {
            Vehicule v = assignments.get(r.getIdReservation());
            Timestamp dep = departureTimes.get(r.getIdReservation());
            Timestamp arr = arrivalTimes.get(r.getIdReservation());
            
            if (v != null && dep != null && arr != null) {
                String depStr = df.format(dep);
                String arrStr = df.format(arr);
                String key = v.getIdVehicule() + "|" + depStr + "|" + arrStr;
                vehicleTours.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }
        }
        
        // Calculer l'ordre pour chaque tournée
        for (Map.Entry<String, List<Reservation>> entry : vehicleTours.entrySet()) {
            String key = entry.getKey();
            List<Reservation> tour = entry.getValue();
            
            List<Reservation> orderedTour = calculerOrdreOptimal(tour, hotelMap, distanceMatrix);
            tourOrders.put(key, orderedTour);
        }
        
        return tourOrders;
    }

    private Map<String, Map<String, java.sql.Timestamp>> calculerHorairesDetailles(List<Reservation> reservations, 
            Map<String, Vehicule> assignments, Map<String, Hotel> hotelMap, 
            Map<String, Map<String, Distance>> distanceMatrix, Parametre param,
            Map<String, Timestamp> departureTimes, Map<String, Timestamp> arrivalTimes) {
        
        Map<String, Map<String, java.sql.Timestamp>> detailedTimes = new HashMap<>();
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        // Grouper les réservations par véhicule et créneau horaire
        Map<String, List<Reservation>> vehicleTours = new HashMap<>();
        for (Reservation r : reservations) {
            Vehicule v = assignments.get(r.getIdReservation());
            Timestamp dep = departureTimes.get(r.getIdReservation());
            Timestamp arr = arrivalTimes.get(r.getIdReservation());
            
            if (v != null && dep != null && arr != null) {
                String depStr = df.format(dep);
                String arrStr = df.format(arr);
                String key = v.getIdVehicule() + "|" + depStr + "|" + arrStr;
                vehicleTours.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }
        }
        
        // Calculer les heures détaillées pour chaque tournée
        for (Map.Entry<String, List<Reservation>> entry : vehicleTours.entrySet()) {
            String key = entry.getKey();
            List<Reservation> tour = entry.getValue();
            
            if (!tour.isEmpty()) {
                // Utiliser l'heure de départ calculée pour cette tournée (depTime)
                Timestamp depTime = departureTimes.get(tour.get(0).getIdReservation());
                Map<String, java.sql.Timestamp> times = calculerHeuresTournée(tour, hotelMap, distanceMatrix, param, depTime);
                detailedTimes.put(key, times);
            }
        }
        
        return detailedTimes;
    }

    private Map<String, java.sql.Timestamp> calculerHeuresTournée(List<Reservation> tour, 
            Map<String, Hotel> hotelMap, Map<String, Map<String, Distance>> distanceMatrix, Parametre param, Timestamp startTime) {
        
        Map<String, java.sql.Timestamp> times = new HashMap<>();
        
        // Calculer l'ordre de la tournée et les heures
        List<Reservation> orderedTour = calculerOrdreOptimal(tour, hotelMap, distanceMatrix);
        calculerHeuresSegments(orderedTour, hotelMap, distanceMatrix, param, startTime, times);
        calculerHeureRetour(orderedTour, hotelMap, distanceMatrix, param, times);
        
        return times;
    }

    private Map<String, BigDecimal> calculerDistancesTournees(List<Reservation> reservations,
            Map<String, Vehicule> assignments, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix,
            Map<String, Timestamp> departureTimes, Map<String, Timestamp> arrivalTimes,
            Map<String, List<Reservation>> tourOrders) {
        Map<String, BigDecimal> distances = new LinkedHashMap<>();
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        Map<String, List<Reservation>> vehicleTours = new LinkedHashMap<>();

        for (Reservation r : reservations) {
            Vehicule v = assignments.get(r.getIdReservation());
            Timestamp dep = departureTimes.get(r.getIdReservation());
            Timestamp arr = arrivalTimes.get(r.getIdReservation());
            if (v != null && dep != null && arr != null) {
                String key = v.getIdVehicule() + "|" + df.format(dep) + "|" + df.format(arr);
                vehicleTours.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }
        }

        for (Map.Entry<String, List<Reservation>> entry : vehicleTours.entrySet()) {
            String key = entry.getKey();
            List<Reservation> orderedTour = tourOrders != null && tourOrders.containsKey(key)
                    ? tourOrders.get(key)
                    : calculerOrdreOptimal(entry.getValue(), hotelMap, distanceMatrix);
            distances.put(key, calculerDistanceParcours(orderedTour, hotelMap, distanceMatrix));
        }

        return distances;
    }

    private BigDecimal calculerDistanceParcours(List<Reservation> orderedTour,
            Map<String, Hotel> hotelMap, Map<String, Map<String, Distance>> distanceMatrix) {
        BigDecimal totalDistance = BigDecimal.ZERO;
        if (orderedTour == null || orderedTour.isEmpty()) {
            return totalDistance;
        }

        String currentLieu = "LIEU001";
        for (Reservation r : orderedTour) {
            Hotel h = hotelMap.get(r.getIdHotel());
            if (h == null)
                continue;

            Distance d = getDistance(distanceMatrix, currentLieu, h.getIdLieu());
            if (d != null && d.getKilometre() != null) {
                totalDistance = totalDistance.add(d.getKilometre());
            }
            currentLieu = h.getIdLieu();
        }

        Distance retour = getDistance(distanceMatrix, currentLieu, "LIEU001");
        if (retour != null && retour.getKilometre() != null) {
            totalDistance = totalDistance.add(retour.getKilometre());
        }

        return totalDistance;
    }

    private List<Reservation> calculerOrdreOptimal(List<Reservation> tour, 
            Map<String, Hotel> hotelMap, Map<String, Map<String, Distance>> distanceMatrix) {
        
        List<Reservation> orderedTour = new ArrayList<>();
        String currentLieu = "LIEU001";
        List<Reservation> remainingResa = new ArrayList<>(tour);
        
        while (!remainingResa.isEmpty()) {
            Reservation nextResa = trouverProchainHotel(remainingResa, currentLieu, hotelMap, distanceMatrix);
            
            if (nextResa != null) {
                orderedTour.add(nextResa);
                Hotel h = hotelMap.get(nextResa.getIdHotel());
                currentLieu = h.getIdLieu();
                remainingResa.remove(nextResa);
            } else {
                break;
            }
        }
        
        return orderedTour;
    }

    private Reservation trouverProchainHotel(List<Reservation> remainingResa, String currentLieu,
            Map<String, Hotel> hotelMap, Map<String, Map<String, Distance>> distanceMatrix) {
        
        Reservation nextResa = null;
        BigDecimal minDistance = BigDecimal.valueOf(Double.MAX_VALUE);
        
        for (Reservation r : remainingResa) {
            Hotel h = hotelMap.get(r.getIdHotel());
            if (h != null) {
                BigDecimal currentDist = null;
                // Cas où le départ et l'arrivée sont identiques (ex: Hotel 1 -> Hotel 1)
                if (currentLieu.equals(h.getIdLieu())) {
                    currentDist = BigDecimal.ZERO;
                } else {
                    Distance d = getDistance(distanceMatrix, currentLieu, h.getIdLieu());
                    if (d != null) {
                        currentDist = d.getKilometre();
                    }
                }
                
                if (currentDist != null && currentDist.compareTo(minDistance) < 0) {
                    minDistance = currentDist;
                    nextResa = r;
                }
            }
        }
        
        return nextResa;
    }

    private void calculerHeuresSegments(List<Reservation> orderedTour, 
            Map<String, Hotel> hotelMap, Map<String, Map<String, Distance>> distanceMatrix, 
            Parametre param, Timestamp currentTime, Map<String, java.sql.Timestamp> times) {
        
        String currentLieu = "LIEU001";
        
        for (Reservation nextResa : orderedTour) {
            // Heure de départ vers cet hôtel
            times.put(nextResa.getIdReservation() + "_departure", currentTime);
            
            // Calculer le temps de trajet jusqu'à cet hôtel
            Hotel h = hotelMap.get(nextResa.getIdHotel());
            if (h != null && param != null && param.getVitesseMoyenne() != null) {
                Distance d = getDistance(distanceMatrix, currentLieu, h.getIdLieu());
                if (d != null) {
                    BigDecimal travelTimeHours = d.getKilometre().divide(param.getVitesseMoyenne(), 4, RoundingMode.HALF_UP);
                    long travelTimeMs = (long) (travelTimeHours.doubleValue() * 3600000L);
                    currentTime = new Timestamp(currentTime.getTime() + travelTimeMs);
                }
            }
            
            // Heure d'arrivée à cet hôtel
            times.put(nextResa.getIdReservation() + "_arrival", currentTime);
            
            currentLieu = h.getIdLieu();
        }
    }

    private void calculerHeureRetour(List<Reservation> orderedTour, 
            Map<String, Hotel> hotelMap, Map<String, Map<String, Distance>> distanceMatrix, 
            Parametre param, Map<String, java.sql.Timestamp> times) {
        
        if (orderedTour.isEmpty()) return;
        
        Reservation lastResa = orderedTour.get(orderedTour.size() - 1);
        Hotel lastHotel = hotelMap.get(lastResa.getIdHotel());
        
        if (lastHotel == null) return;
        
        // Heure de départ du dernier hôtel
        Timestamp currentTime = times.get(lastResa.getIdReservation() + "_arrival");
        times.put("return_departure", currentTime);
        
        // Calculer le temps de retour à l'aéroport
        if (param != null && param.getVitesseMoyenne() != null) {
            Distance d = getDistance(distanceMatrix, lastHotel.getIdLieu(), "LIEU001");
            if (d != null) {
                BigDecimal travelTimeHours = d.getKilometre().divide(param.getVitesseMoyenne(), 4, RoundingMode.HALF_UP);
                long travelTimeMs = (long) (travelTimeHours.doubleValue() * 3600000L);
                currentTime = new Timestamp(currentTime.getTime() + travelTimeMs);
            }
        }
        
        times.put("return_arrival", currentTime);
    }
}
