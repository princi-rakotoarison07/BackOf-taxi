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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.taxi.model.Hotel;
import com.taxi.model.Distance;
import com.taxi.model.Parametre;

@Controller
@RestController
public class VehiculeController {

    @GetMapping("/BackOf-taxi/api/type-carburants")
    public List<TypeCarburant> listTypeCarburants() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            return TypeCarburant.getAll(TypeCarburant.class, conn);
        }
    }

    @GetMapping("/BackOf-taxi/vehicule/list")
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

    @GetMapping("/BackOf-taxi/vehicule/form")
    public ModelAndView showForm() throws Exception {
        ModelAndView mv = new ModelAndView("/views/vehicule/form.jsp");
        try (Connection conn = DBConnection.getConnection()) {
            List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
            mv.addObject("types", types);
        }
        return mv;
    }

    @GetMapping("/BackOf-taxi/vehicule/edit")
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

    @GetMapping("/BackOf-taxi/vehicule/disponible")
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
            Parametre currentParam = (parametres != null && !parametres.isEmpty()) ? parametres.get(parametres.size() - 1) : null;

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
        int waitMinutes = getTempsAttenteMinutes(param);
        List<List<Reservation>> waitWindows = construireFenetresAttente(reservations, waitMinutes);
        // Le planning est calcule par jour: au debut, tous les vehicules ont 0 trajet.
        Map<Vehicule, Integer> dailyTripCount = initialiserCompteurTrajets(vehicules);

        Map<Vehicule, Timestamp> nextFreeTime = new HashMap<>();
        for (Vehicule v : vehicules) {
            nextFreeTime.put(v, new Timestamp(0));
        }

        for (List<Reservation> group : waitWindows) {
            Timestamp departureTime = resoudreHeureDepart(group, waitMinutes);
            if (departureTime == null)
                continue;

            group.sort((a, b) -> comparerReservationsIntraGroupe(a, b, hotelMap, distanceMatrix));

            Map<Vehicule, Integer> remainingCapacity = new HashMap<>();
            for (Vehicule v : vehicules) {
                remainingCapacity.put(v, v.getNbrPlace() != null ? v.getNbrPlace() : 0);
            }

            Map<Vehicule, List<Reservation>> assignedToVehicule = new HashMap<>();
            for (Reservation r : group) {
                List<AffectationVehicule> chunks = repartirReservationParCapacite(r, vehicules, remainingCapacity,
                        nextFreeTime, departureTime, typeById, dailyTripCount);
                for (AffectationVehicule chunk : chunks) {
                    remainingCapacity.put(chunk.vehicule,
                            remainingCapacity.getOrDefault(chunk.vehicule, 0) - chunk.passagers);
                    List<Reservation> tour = assignedToVehicule.computeIfAbsent(chunk.vehicule, k -> new ArrayList<>());
                    if (!tour.contains(r)) {
                        tour.add(r);
                    }
                }
            }

            for (Map.Entry<Vehicule, List<Reservation>> entry : assignedToVehicule.entrySet()) {
                Vehicule v = entry.getKey();
                List<Reservation> tourResas = entry.getValue();
                long durationMs = calculerDureeTournee(tourResas, hotelMap, distanceMatrix, param);
                Timestamp endTime = new Timestamp(departureTime.getTime() + durationMs);
                nextFreeTime.put(v, endTime);
                dailyTripCount.put(v, dailyTripCount.getOrDefault(v, 0) + 1);
                planning.computeIfAbsent(v, k -> new ArrayList<>()).add(new Tournee(departureTime, endTime));
            }
        }
        return planning;
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
        Map<Vehicule, Integer> simulatedCapacity = new HashMap<>(remainingCapacity);

        while (toAssign > 0) {
            Vehicule fit = trouverMeilleurVehicule(reservation, available, simulatedCapacity, nextFreeTime,
                    currentTime, typeById, dailyTripCount, toAssign);
            if (fit != null) {
                chunks.add(new AffectationVehicule(fit, toAssign));
                simulatedCapacity.put(fit, simulatedCapacity.getOrDefault(fit, 0) - toAssign);
                toAssign = 0;
                continue;
            }

            Vehicule partial = trouverVehiculePourAllocationPartielle(available, simulatedCapacity, nextFreeTime,
                    currentTime, typeById, dailyTripCount);
            if (partial == null) {
                break;
            }

            int cap = simulatedCapacity.getOrDefault(partial, 0);
            if (cap <= 0) {
                break;
            }

            int affectes = Math.min(cap, toAssign);
            chunks.add(new AffectationVehicule(partial, affectes));
            simulatedCapacity.put(partial, cap - affectes);
            toAssign -= affectes;
        }
        return chunks;
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

    private int comparerReservationsIntraGroupe(Reservation a, Reservation b,
            Map<String, Hotel> hotelMap, Map<String, Map<String, Distance>> distanceMatrix) {
        if (a == null && b == null)
            return 0;
        if (a == null)
            return 1;
        if (b == null)
            return -1;

        // Traitement collectif dans une meme fenetre: pas de priorite a la premiere arrivee.
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

    private Map<Vehicule, Integer> initialiserCompteurTrajets(List<Vehicule> vehicules) {
        Map<Vehicule, Integer> compteur = new HashMap<>();
        for (Vehicule v : vehicules) {
            compteur.put(v, 0);
        }
        return compteur;
    }

    private Vehicule trouverMeilleurVehicule(Reservation r, List<Vehicule> available,
            Map<Vehicule, Integer> remainingCapacity, Map<Vehicule, Timestamp> nextFreeTime, Timestamp currentTime,
            Map<String, TypeCarburant> typeById, Map<Vehicule, Integer> dailyTripCount) {
        return trouverMeilleurVehicule(r, available, remainingCapacity, nextFreeTime, currentTime,
                typeById, dailyTripCount, r.getNbrPassager());
    }

    private Vehicule trouverMeilleurVehicule(Reservation r, List<Vehicule> available,
            Map<Vehicule, Integer> remainingCapacity, Map<Vehicule, Timestamp> nextFreeTime, Timestamp currentTime,
            Map<String, TypeCarburant> typeById, Map<Vehicule, Integer> dailyTripCount,
            int passagersAPlacer) {
        Vehicule best = null;
        int bestCapacityDelta = Integer.MAX_VALUE;
        int bestTripCount = Integer.MAX_VALUE;
        int bestFuelPriority = Integer.MAX_VALUE;
        int bestTotalCapacity = Integer.MAX_VALUE;

        for (Vehicule v : available) {
            int cap = remainingCapacity.getOrDefault(v, 0);
            Timestamp freeTime = nextFreeTime.getOrDefault(v, new Timestamp(0));

            if (cap >= passagersAPlacer && !freeTime.after(currentTime)) {
                int capacityDelta = cap - passagersAPlacer;
                int tripCount = dailyTripCount.getOrDefault(v, 0);
                TypeCarburant t = typeById.get(v.getIdTypeCarburant());
                int fuelPriority = getPrioriteCarburant(t);

                // Priorite 1: capacite la plus proche >= passagers restants
                // Priorite 2: nombre de trajets le plus faible
                // Priorite 3: type carburant (Electrique, Diesel, Essence)
                // Priorite 4: plus petite capacite totale
                if (capacityDelta < bestCapacityDelta ||
                    (capacityDelta == bestCapacityDelta && tripCount < bestTripCount) ||
                    (capacityDelta == bestCapacityDelta && tripCount == bestTripCount && fuelPriority < bestFuelPriority) ||
                    (capacityDelta == bestCapacityDelta && tripCount == bestTripCount && fuelPriority == bestFuelPriority
                            && v.getNbrPlace() < bestTotalCapacity)) {
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

        if (code.equals("d") || libelle.contains("diesel")) {
            return 1;
        }
        if (code.equals("el") || code.equals("elec") || code.equals("ev")
                || libelle.contains("elect") || libelle.contains("lectri")) {
            return 2;
        }
        if (code.equals("e") || libelle.contains("essence")) {
            return 3;
        }
        return 4;
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

    private List<Reservation> filtrerReservations(List<Reservation> reservations, String date) {
        if (date == null || date.isEmpty())
            return new ArrayList<>();
        List<Reservation> filtered = new ArrayList<>();
        Timestamp start = Timestamp.valueOf(date + " 00:00:00");
        Timestamp end = Timestamp.valueOf(date + " 23:59:59");
        for (Reservation r : reservations) {
            if (estReservationValide(r) && !r.getDateResa().before(start) && !r.getDateResa().after(end)) {
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

    @PostMapping("/BackOf-taxi/vehicule/save")
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

    @PostMapping("/BackOf-taxi/vehicule/update")
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

    @GetMapping("/BackOf-taxi/vehicule/delete")
    public ModelAndView delete(@Param("id") String id) {
        ModelAndView mv = new ModelAndView("/BackOf-taxi/vehicule/list");
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
