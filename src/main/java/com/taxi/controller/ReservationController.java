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
import com.taxi.model.Assignation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.math.RoundingMode;
import java.math.BigDecimal;
import com.taxi.model.Trajet;

@Controller
@RestController
public class ReservationController {

    public static class ReservationPortion {
        private Reservation reservation;
        private int placesAssignees;

        public ReservationPortion(Reservation reservation, int placesAssignees) {
            this.reservation = reservation;
            this.placesAssignees = placesAssignees;
        }

        public Reservation getReservation() {
            return reservation;
        }

        public int getPlacesAssignees() {
            return placesAssignees;
        }
    }

    public static class SplitAssignationResult {
        private Map<String, List<ReservationPortion>> portionsParVehicule;
        private Map<String, Integer> reliquatsParReservation;
        private Map<String, Timestamp> departureTimesParVehicule;
        private Map<String, Timestamp> returnTimesParVehicule;

        public SplitAssignationResult(Map<String, List<ReservationPortion>> portionsParVehicule,
                Map<String, Integer> reliquatsParReservation,
                Map<String, Timestamp> departureTimesParVehicule,
                Map<String, Timestamp> returnTimesParVehicule) {
            this.portionsParVehicule = portionsParVehicule;
            this.reliquatsParReservation = reliquatsParReservation;
            this.departureTimesParVehicule = departureTimesParVehicule;
            this.returnTimesParVehicule = returnTimesParVehicule;
        }

        public Map<String, List<ReservationPortion>> getPortionsParVehicule() {
            return portionsParVehicule;
        }

        public Map<String, Integer> getReliquatsParReservation() {
            return reliquatsParReservation;
        }

        public Map<String, Timestamp> getDepartureTimesParVehicule() {
            return departureTimesParVehicule;
        }

        public Map<String, Timestamp> getReturnTimesParVehicule() {
            return returnTimesParVehicule;
        }
    }

    public static class AssignationView {
        private List<Reservation> reservations;
        private Map<String, Vehicule> assignments;
        public AssignationView(List<Reservation> reservations, Map<String, Vehicule> assignments) {
            this.reservations = reservations;
            this.assignments = assignments;
        }
        public List<Reservation> getReservations() {
            return reservations;
        }
        public Map<String, Vehicule> getAssignments() {
            return assignments;
        }
    }

    private static class PendingReservation {
        private Reservation reservation;
        private int remaining;
        private boolean remainder;
        private PendingReservation(Reservation reservation) {
            this.reservation = reservation;
            this.remaining = reservation.getNbrPassager() != null ? reservation.getNbrPassager() : 0;
            this.remainder = false;
        }
    }

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

    @GetMapping("/BackOf-taxi/reservation/assignation-vehicule-split")
    public ModelAndView assignationVehiculeSplit(@Param("date") String date) throws Exception {
        ModelAndView mv = new ModelAndView("/views/reservation/assignationVehiculeSplit.jsp");
        mv.addObject("pageTitle", "Assignation fractionnée par Véhicule");

        try (Connection conn = DBConnection.getConnection()) {
            List<Reservation> allReservations = Reservation.getAll(Reservation.class, conn);
            List<Vehicule> vehicules = Vehicule.getAll(Vehicule.class, conn);
            List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
            List<Hotel> hotels = Hotel.getAll(Hotel.class, conn);
            List<Distance> distances = Distance.getAll(Distance.class, conn);
            List<Parametre> parametres = Parametre.getAll(Parametre.class, conn);
            Parametre currentParam = (parametres != null && !parametres.isEmpty()) ? parametres.get(0) : null;

            Map<String, TypeCarburant> typeById = construireMapType(types);
            Map<String, Hotel> hotelMap = construireMapHotel(hotels);
            Map<String, Map<String, Distance>> distanceMatrix = construireMatriceDistance(distances);

            Map<String, Integer> tripCountByVehicule = compterTrajetsDuJour(conn, date);

            List<Reservation> filtered = filtrerReservations(allReservations, date);

            // Pour être cohérent avec l'ancien algo: trier comme assignationVehicule (plus récent, puis plus grand)
            filtered.sort((a, b) -> {
                if (a.getDateResa() == null)
                    return 1;
                if (b.getDateResa() == null)
                    return -1;
                int dateCompare = b.getDateResa().compareTo(a.getDateResa());
                if (dateCompare != 0) {
                    return dateCompare;
                }
                return b.getNbrPassager().compareTo(a.getNbrPassager());
            });

            SplitAssignationResult split = assignerVehiculesFractionneAvecDisponibilite(filtered, vehicules, typeById,
                    hotelMap, distanceMatrix, currentParam, tripCountByVehicule);

            mv.addObject("reservations", filtered);
            mv.addObject("vehicules", vehicules);
            mv.addObject("types", types);
            mv.addObject("typeById", typeById);
            mv.addObject("hotelMap", hotelMap);
            mv.addObject("selectedDate", date);
            mv.addObject("splitPortions", split.getPortionsParVehicule());
            mv.addObject("splitReliquats", split.getReliquatsParReservation());
            mv.addObject("splitDepartureVehicule", split.getDepartureTimesParVehicule());
            mv.addObject("splitReturnVehicule", split.getReturnTimesParVehicule());
        }

        return mv;
    }

    private Map<String, Integer> compterTrajetsDuJour(Connection conn, String date) {
        Map<String, Integer> count = new HashMap<>();
        if (date == null || date.isEmpty()) {
            return count;
        }
        try {
            Timestamp start = Timestamp.valueOf(date + " 00:00:00");
            Timestamp end = Timestamp.valueOf(date + " 23:59:59");
            List<Assignation> assignations = Assignation.getAll(Assignation.class, conn);
            for (Assignation a : assignations) {
                if (a.getDateAssignation() == null)
                    continue;
                if (a.getDateAssignation().before(start) || a.getDateAssignation().after(end))
                    continue;
                if (a.getIdVehicule() == null)
                    continue;
                count.put(a.getIdVehicule(), count.getOrDefault(a.getIdVehicule(), 0) + 1);
            }
        } catch (Exception e) {
            // On laisse le compteur vide si une erreur survient, pour ne pas casser l'affichage split.
            e.printStackTrace();
        }
        return count;
    }

    private int comparerVehiculesPourSplit(Vehicule v1, Vehicule v2, Map<String, TypeCarburant> typeById,
            Map<String, Integer> tripCountByVehicule) {
        if (v1 == null && v2 == null)
            return 0;
        if (v1 == null)
            return 1;
        if (v2 == null)
            return -1;

        TypeCarburant t1 = typeById != null ? typeById.get(v1.getIdTypeCarburant()) : null;
        TypeCarburant t2 = typeById != null ? typeById.get(v2.getIdTypeCarburant()) : null;

        int diesel1 = (t1 != null && t1.getCode() != null && t1.getCode().equalsIgnoreCase("D")) ? 1 : 0;
        int diesel2 = (t2 != null && t2.getCode() != null && t2.getCode().equalsIgnoreCase("D")) ? 1 : 0;

        // 1) Diesel d'abord
        if (diesel1 != diesel2)
            return Integer.compare(diesel2, diesel1);

        // 2) Moins de trajets effectués dans la journée
        int trips1 = tripCountByVehicule != null ? tripCountByVehicule.getOrDefault(v1.getIdVehicule(), 0) : 0;
        int trips2 = tripCountByVehicule != null ? tripCountByVehicule.getOrDefault(v2.getIdVehicule(), 0) : 0;
        if (trips1 != trips2)
            return Integer.compare(trips1, trips2);

        // 3) Plus petite capacité (économiser les gros véhicules)
        int cap1 = v1.getNbrPlace() != null ? v1.getNbrPlace() : Integer.MAX_VALUE;
        int cap2 = v2.getNbrPlace() != null ? v2.getNbrPlace() : Integer.MAX_VALUE;
        if (cap1 != cap2)
            return Integer.compare(cap1, cap2);

        // 4) Stable: idVehicule
        return safe(v1.getIdVehicule()).compareTo(safe(v2.getIdVehicule()));
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private SplitAssignationResult assignerVehiculesFractionneAvecDisponibilite(List<Reservation> reservations,
            List<Vehicule> vehicules,
            Map<String, TypeCarburant> typeById,
            Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix,
            Parametre param,
            Map<String, Integer> tripCountByVehicule) {

        Map<String, List<ReservationPortion>> portionsParVehicule = new HashMap<>();
        Map<String, Integer> reliquatsParReservation = new HashMap<>();
        Map<String, Timestamp> departureTimesParVehicule = new HashMap<>();
        Map<String, Timestamp> returnTimesParVehicule = new HashMap<>();

        // Etat véhicule: capacité restante + prochaine disponibilité
        Map<Vehicule, Integer> remainingCapacity = new HashMap<>();
        Map<Vehicule, Timestamp> nextFreeTime = new HashMap<>();
        for (Vehicule v : vehicules) {
            remainingCapacity.put(v, v.getNbrPlace() != null ? v.getNbrPlace() : 0);
            nextFreeTime.put(v, new Timestamp(0));
        }

        // Grouper les réservations par minute, comme l'ancien algo
        Map<Timestamp, List<Reservation>> groupedByTime = new HashMap<>();
        for (Reservation r : reservations) {
            if (r.getDateResa() == null)
                continue;
            long timeMs = r.getDateResa().getTime();
            Timestamp truncated = new Timestamp((timeMs / 60000L) * 60000L);
            groupedByTime.computeIfAbsent(truncated, k -> new ArrayList<>()).add(r);
        }
        List<Timestamp> sortedTimes = new ArrayList<>(groupedByTime.keySet());
        Collections.sort(sortedTimes);

        for (Timestamp t : sortedTimes) {
            List<Reservation> group = groupedByTime.get(t);
            if (group == null)
                continue;

            // Pour être cohérent avec l'ancien algo à l'intérieur du groupe
            group.sort((a, b) -> {
                if (a.getDateResa() == null)
                    return 1;
                if (b.getDateResa() == null)
                    return -1;
                int dateCompare = b.getDateResa().compareTo(a.getDateResa());
                if (dateCompare != 0) {
                    return dateCompare;
                }
                return b.getNbrPassager().compareTo(a.getNbrPassager());
            });

            // Trace pour calculer les tournées par véhicule sur ce créneau
            Map<Vehicule, List<Reservation>> tourByVehicule = new HashMap<>();
            List<Vehicule> usedVehiculeOrder = new ArrayList<>();

            for (Reservation r : group) {
                int reste = r.getNbrPassager() != null ? r.getNbrPassager() : 0;
                if (reste <= 0)
                    continue;

                // 1) Essayer d'assigner toute la réservation à un seul véhicule (comme l'ancien)
                Vehicule bestFull = trouverMeilleurVehiculePourGroupe(r, vehicules, remainingCapacity, nextFreeTime, t,
                        typeById, convertirTrips(vehicules, tripCountByVehicule));
                if (bestFull != null) {
                    int pris = reste;
                    remainingCapacity.put(bestFull, remainingCapacity.getOrDefault(bestFull, 0) - pris);
                    portionsParVehicule.computeIfAbsent(bestFull.getIdVehicule(), k -> new ArrayList<>())
                            .add(new ReservationPortion(r, pris));
                    if (!tourByVehicule.containsKey(bestFull)) {
                        usedVehiculeOrder.add(bestFull);
                    }
                    tourByVehicule.computeIfAbsent(bestFull, k -> new ArrayList<>()).add(r);
                    reste = 0;
                }

                // 2) Si impossible de la mettre entière, on fractionne sur plusieurs véhicules disponibles
                while (reste > 0) {
                    // 2.a) Priorité: remplir d'abord les véhicules déjà engagés dans la tournée du créneau
                    Vehicule bestPartial = trouverVehiculeEngageDisponible(usedVehiculeOrder, remainingCapacity,
                            nextFreeTime, t);
                    if (bestPartial == null) {
                        // 2.b) Sinon, choisir un nouveau véhicule selon des critères proches de l'ancien
                        bestPartial = trouverMeilleurVehiculePartiel(r, vehicules, remainingCapacity, nextFreeTime,
                                t, typeById, tripCountByVehicule);
                    }
                    if (bestPartial == null)
                        break;

                    int cap = remainingCapacity.getOrDefault(bestPartial, 0);
                    if (cap <= 0)
                        break;

                    int pris = Math.min(cap, reste);
                    remainingCapacity.put(bestPartial, cap - pris);
                    reste -= pris;

                    portionsParVehicule.computeIfAbsent(bestPartial.getIdVehicule(), k -> new ArrayList<>())
                            .add(new ReservationPortion(r, pris));
                    if (!tourByVehicule.containsKey(bestPartial)) {
                        usedVehiculeOrder.add(bestPartial);
                    }
                    tourByVehicule.computeIfAbsent(bestPartial, k -> new ArrayList<>()).add(r);
                }

                if (reste > 0) {
                    reliquatsParReservation.put(r.getIdReservation(), reste);
                }
            }

            // Mettre à jour nextFreeTime comme l'ancien: durée tournée incluant retour
            for (Map.Entry<Vehicule, List<Reservation>> e : tourByVehicule.entrySet()) {
                Vehicule v = e.getKey();
                List<Reservation> tour = e.getValue();
                if (tour == null || tour.isEmpty())
                    continue;

                // Départ affiché = heure du créneau
                departureTimesParVehicule.put(v.getIdVehicule(), t);

                long durationMs = calculerDureeTournee(tour, hotelMap, distanceMatrix, param);
                Timestamp returnTime = new Timestamp(t.getTime() + durationMs);
                returnTimesParVehicule.put(v.getIdVehicule(), returnTime);
                nextFreeTime.put(v, returnTime);

                // incrémenter le compteur de trajets du jour
                if (tripCountByVehicule != null && v.getIdVehicule() != null) {
                    tripCountByVehicule.put(v.getIdVehicule(), tripCountByVehicule.getOrDefault(v.getIdVehicule(), 0) + 1);
                }
            }
        }

        return new SplitAssignationResult(portionsParVehicule, reliquatsParReservation, departureTimesParVehicule,
                returnTimesParVehicule);
    }

    private Vehicule trouverVehiculeEngageDisponible(List<Vehicule> usedVehiculeOrder,
            Map<Vehicule, Integer> remainingCapacity,
            Map<Vehicule, Timestamp> nextFreeTime,
            Timestamp currentTime) {
        if (usedVehiculeOrder == null)
            return null;
        for (Vehicule v : usedVehiculeOrder) {
            int cap = remainingCapacity != null ? remainingCapacity.getOrDefault(v, 0) : 0;
            Timestamp freeTime = nextFreeTime != null ? nextFreeTime.getOrDefault(v, new Timestamp(0)) : new Timestamp(0);
            if (cap > 0 && !freeTime.after(currentTime)) {
                return v;
            }
        }
        return null;
    }

    private Map<Vehicule, Integer> convertirTrips(List<Vehicule> vehicules, Map<String, Integer> tripCountByVehicule) {
        Map<Vehicule, Integer> trips = new HashMap<>();
        if (vehicules == null)
            return trips;
        for (Vehicule v : vehicules) {
            int c = (tripCountByVehicule != null && v != null) ? tripCountByVehicule.getOrDefault(v.getIdVehicule(), 0) : 0;
            trips.put(v, c);
        }
        return trips;
    }

    private Vehicule trouverMeilleurVehiculePartiel(Reservation r,
            List<Vehicule> available,
            Map<Vehicule, Integer> remainingCapacity,
            Map<Vehicule, Timestamp> nextFreeTime,
            Timestamp currentTime,
            Map<String, TypeCarburant> typeById,
            Map<String, Integer> tripCountByVehicule) {

        Vehicule best = null;
        double bestFillRate = -1.0;
        int bestTotalCapacity = Integer.MAX_VALUE;
        int bestTripCount = Integer.MAX_VALUE;
        int bestScoreDiesel = -1;

        for (Vehicule v : available) {
            int cap = remainingCapacity.getOrDefault(v, 0);
            Timestamp freeTime = nextFreeTime.getOrDefault(v, new Timestamp(0));
            if (cap <= 0)
                continue;
            if (freeTime.after(currentTime))
                continue;

            int pris = Math.min(cap, r != null && r.getNbrPassager() != null ? r.getNbrPassager() : cap);
            if (pris <= 0)
                continue;

            // Calculer un "fillRate" similaire à l'ancien algo avec une prise partielle
            int occupiedAfter = v.getNbrPlace() - (cap - pris);
            double fillRateAfter = (v.getNbrPlace() != null && v.getNbrPlace() > 0) ? (double) occupiedAfter / v.getNbrPlace() : 0.0;

            TypeCarburant t = typeById != null ? typeById.get(v.getIdTypeCarburant()) : null;
            int dieselScore = (t != null && t.getCode() != null && t.getCode().equalsIgnoreCase("D")) ? 1 : 0;
            int trips = (tripCountByVehicule != null && v.getIdVehicule() != null)
                    ? tripCountByVehicule.getOrDefault(v.getIdVehicule(), 0)
                    : 0;

            boolean better = false;
            if (fillRateAfter > bestFillRate + 0.0001) {
                better = true;
            } else if (Math.abs(fillRateAfter - bestFillRate) < 0.0001) {
                if (v.getNbrPlace() < bestTotalCapacity) {
                    better = true;
                } else if (v.getNbrPlace() == bestTotalCapacity) {
                    if (trips < bestTripCount) {
                        better = true;
                    } else if (trips == bestTripCount) {
                        if (dieselScore > bestScoreDiesel) {
                            better = true;
                        }
                    }
                }
            }

            if (better) {
                best = v;
                bestFillRate = fillRateAfter;
                bestTotalCapacity = v.getNbrPlace() != null ? v.getNbrPlace() : Integer.MAX_VALUE;
                bestTripCount = trips;
                bestScoreDiesel = dieselScore;
            }
        }
        return best;
    }

    private String resolveDatePrefix(String selectedDate, List<Reservation> reservations) {
        if (selectedDate != null && !selectedDate.isEmpty()) {
            return selectedDate;
        }
        if (reservations != null && !reservations.isEmpty()) {
            Timestamp min = null;
            for (Reservation r : reservations) {
                if (r == null || r.getDateResa() == null) continue;
                if (min == null || r.getDateResa().before(min)) {
                    min = r.getDateResa();
                }
            }
            if (min != null) {
                return new java.sql.Date(min.getTime()).toString();
            }
        }
        return new java.sql.Date(System.currentTimeMillis()).toString();
    }

    @PostMapping("/BackOf-taxi/reservation/save-assignation")
    public Map<String, Object> saveAssignation(@ModelAttribute Assignation assignation) {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (assignation.getIdAssignation() == null || assignation.getIdAssignation().isEmpty()) {
                assignation.setIdAssignation("ASS" + System.currentTimeMillis());
            }
            
            // Sécurité : Si dateAssignation est nulle, utiliser la date de départ prévue
            if (assignation.getDateAssignation() == null && assignation.getHeureDepartPrevue() != null) {
                assignation.setDateAssignation(assignation.getHeureDepartPrevue());
            }
            
            assignation.insert(conn);
            result.put("status", "success");
            result.put("message", "Assignation enregistrée !");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/BackOf-taxi/reservation/save-trajet")
    public Map<String, Object> saveTrajet(@ModelAttribute Trajet trajet) {
        Map<String, Object> result = new HashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            if (trajet.getDateTrajet() == null && trajet.getHeureDepartAeroport() != null) {
                trajet.setDateTrajet(trajet.getHeureDepartAeroport());
            }

            trajet.insert(conn);
            result.put("status", "success");
            result.put("message", "Trajet enregistré !");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "error");
            result.put("message", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/BackOf-taxi/reservation/assignations")
    public ModelAndView listAssignations() throws Exception {
        ModelAndView mv = new ModelAndView("/views/reservation/listAssignation.jsp");
        mv.addObject("pageTitle", "Liste des Assignations");
        try (Connection conn = DBConnection.getConnection()) {
            List<Assignation> assignations = Assignation.getAll(Assignation.class, conn);
            List<Vehicule> vehicules = Vehicule.getAll(Vehicule.class, conn);
            List<Reservation> reservations = Reservation.getAll(Reservation.class, conn);
            List<Hotel> hotels = Hotel.getAll(Hotel.class, conn);
            
            Map<String, Vehicule> vehiculeMap = new HashMap<>();
            for (Vehicule v : vehicules) vehiculeMap.put(v.getIdVehicule(), v);
            
            Map<String, Reservation> reservationMap = new HashMap<>();
            for (Reservation r : reservations) reservationMap.put(r.getIdReservation(), r);
            
            Map<String, Hotel> hotelMap = construireMapHotel(hotels);
            
            mv.addObject("assignations", assignations);
            mv.addObject("vehiculeMap", vehiculeMap);
            mv.addObject("reservationMap", reservationMap);
            mv.addObject("hotelMap", hotelMap);
        }
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
            Parametre currentParam = (parametres != null && !parametres.isEmpty()) ? parametres.get(0) : null;

            List<Reservation> filtered = filtrerReservations(allReservations, date);
            // Trier d'abord par date de réservation (plus récent en premier), puis par nombre de passagers (plus grand en premier)
            filtered.sort((a, b) -> {
                if (a.getDateResa() == null)
                    return 1;
                if (b.getDateResa() == null)
                    return -1;
                int dateCompare = b.getDateResa().compareTo(a.getDateResa());
                if (dateCompare != 0) {
                    return dateCompare;
                }
                return b.getNbrPassager().compareTo(a.getNbrPassager());
            });

            Map<String, TypeCarburant> typeById = construireMapType(types);
            Map<String, Hotel> hotelMap = construireMapHotel(hotels);
            Map<String, Map<String, Distance>> distanceMatrix = construireMatriceDistance(distances);

        AssignationView view = assignerVehiculesGenererPortions(filtered, vehicules, typeById, hotelMap,
                distanceMatrix, currentParam, date);
        Map<String, Vehicule> assignments = view.getAssignments();
        List<Reservation> viewReservations = view.getReservations();
        List<Reservation> unassigned = new ArrayList<>();
        java.util.Set<String> assignedBaseIds = new java.util.HashSet<>();
        for (Reservation r : viewReservations) {
            String id = r.getIdReservation();
            if (id != null) {
                int idx = id.indexOf("#");
                assignedBaseIds.add(idx >= 0 ? id.substring(0, idx) : id);
            }
        }
        for (Reservation r : filtered) {
            if (r.getIdReservation() != null && !assignedBaseIds.contains(r.getIdReservation())) {
                unassigned.add(r);
            }
        }
            Map<String, Timestamp> departureTimes = new HashMap<>();
            Map<String, Timestamp> arrivalTimes = new HashMap<>();

        calculerHoraires(viewReservations, currentParam, hotelMap, distanceMatrix, assignments, departureTimes,
                    arrivalTimes);

        mv.addObject("reservations", viewReservations);
            mv.addObject("vehicules", vehicules);
            mv.addObject("types", types);
            mv.addObject("assignments", assignments);
            mv.addObject("departureTimes", departureTimes);
            mv.addObject("arrivalTimes", arrivalTimes);
            mv.addObject("selectedDate", date);
            mv.addObject("hotels", hotels);
            mv.addObject("hotelMap", hotelMap);
            mv.addObject("unassignedReservations", unassigned);
            
            // Calculer l'ordre des tournées et les heures détaillées pour l'affichage
        Map<String, List<Reservation>> tourOrders = calculerOrdreTournées(viewReservations, assignments, hotelMap, distanceMatrix, departureTimes, arrivalTimes);
        Map<String, Map<String, java.sql.Timestamp>> detailedTimes = calculerHorairesDetailles(viewReservations, assignments, hotelMap, distanceMatrix, currentParam, departureTimes, arrivalTimes);
            mv.addObject("tourOrders", tourOrders);
            mv.addObject("detailedTimes", detailedTimes);
        }
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
            Map<String, TypeCarburant> typeById, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix, Parametre param) {
        Map<String, Vehicule> assignments = new HashMap<>();
        List<Vehicule> available = new ArrayList<>(vehicules);

        Map<Vehicule, Integer> tripCount = new HashMap<>();
        for (Vehicule v : available) {
            tripCount.put(v, 0);
        }

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

        // Trier les groupes par temps (chronologique)
        List<Timestamp> sortedTimes = new ArrayList<>(groupedByTime.keySet());
        Collections.sort(sortedTimes);

        Map<Vehicule, Timestamp> nextFreeTime = new HashMap<>();
        for (Vehicule v : available) {
            Timestamp init = new Timestamp(0);
            if (v.getHeureDisponible() != null) {
                String datePrefix = new java.sql.Date(System.currentTimeMillis()).toString(); // Fallback
                if (!reservations.isEmpty() && reservations.get(0).getDateResa() != null) {
                    datePrefix = new java.sql.Date(reservations.get(0).getDateResa().getTime()).toString();
                }
                init = Timestamp.valueOf(datePrefix + " " + v.getHeureDisponible().toString());
            }
            nextFreeTime.put(v, init);
        }

        for (Timestamp t : sortedTimes) {
            List<Reservation> group = groupedByTime.get(t);
            // Règle 1: priorité à l'heure de réservation (chronologique), puis par nombre de passagers décroissant
            group.sort((a, b) -> {
                if (a.getDateResa() == null)
                    return 1;
                if (b.getDateResa() == null)
                    return -1;
                int dateCompare = a.getDateResa().compareTo(b.getDateResa());
                if (dateCompare != 0) {
                    return dateCompare;
                }
                return b.getNbrPassager().compareTo(a.getNbrPassager());
            });

            Map<Vehicule, Integer> remainingCapacity = new HashMap<>();
            for (Vehicule v : available) {
                remainingCapacity.put(v, v.getNbrPlace() != null ? v.getNbrPlace() : 0);
            }

            // On garde trace des réservations assignées à chaque véhicule pour ce groupe
            Map<Vehicule, List<Reservation>> assignedToVehicule = new HashMap<>();

            for (Reservation r : group) {
                Vehicule best = trouverMeilleurVehiculePourGroupe(r, available, remainingCapacity, nextFreeTime, t,
                        typeById, tripCount);
                if (best != null) {
                    assignments.put(r.getIdReservation(), best);
                    remainingCapacity.put(best, remainingCapacity.get(best) - r.getNbrPassager());
                    assignedToVehicule.computeIfAbsent(best, k -> new ArrayList<>()).add(r);
                } else {
                    // Règle 4: découpage des grands groupes si aucun véhicule unique ne peut prendre tout le groupe
                    // Essayer d'ajouter sur des véhicules déjà engagés à ce créneau (regroupement même hôtel en priorité)
                    int reste = r.getNbrPassager() != null ? r.getNbrPassager() : 0;
                    // Priorité même hôtel
                    for (Map.Entry<Vehicule, List<Reservation>> e : assignedToVehicule.entrySet()) {
                        if (reste <= 0) break;
                        Vehicule v = e.getKey();
                        int cap = remainingCapacity.getOrDefault(v, 0);
                        Timestamp freeTime = nextFreeTime.getOrDefault(v, new Timestamp(0));
                        boolean sameHotel = false;
                        for (Reservation ar : e.getValue()) {
                            if (safe(ar.getIdHotel()).equals(safe(r.getIdHotel()))) {
                                sameHotel = true;
                                break;
                            }
                        }
                        if (cap > 0 && !freeTime.after(t) && sameHotel) {
                            int pris = Math.min(cap, reste);
                            remainingCapacity.put(v, cap - pris);
                            reste -= pris;
                            assignments.put(r.getIdReservation(), v);
                            assignedToVehicule.computeIfAbsent(v, k -> new ArrayList<>()).add(r);
                        }
                    }
                    // Si toujours des reliquats, répartir sur nouveaux véhicules disponibles (Diesel prioritaire)
                    if (reste > 0) {
                        List<Vehicule> candidates = new ArrayList<>();
                        for (Vehicule v : available) {
                            int cap = remainingCapacity.getOrDefault(v, 0);
                            Timestamp freeTime = nextFreeTime.getOrDefault(v, new Timestamp(0));
                            if (cap > 0 && !freeTime.after(t)) {
                                candidates.add(v);
                            }
                        }
                        Map<String, Integer> idTripCount = new HashMap<>();
                        for (Vehicule vv : available) {
                            idTripCount.put(vv.getIdVehicule(), tripCount.getOrDefault(vv, 0));
                        }
                        candidates.sort((v1, v2) -> comparerVehiculesPourSplit(v1, v2, typeById, idTripCount));
                        for (Vehicule v : candidates) {
                            if (reste <= 0) break;
                            int cap = remainingCapacity.getOrDefault(v, 0);
                            if (cap <= 0) continue;
                            int pris = Math.min(cap, reste);
                            remainingCapacity.put(v, cap - pris);
                            reste -= pris;
                            assignments.put(r.getIdReservation(), v);
                            assignedToVehicule.computeIfAbsent(v, k -> new ArrayList<>()).add(r);
                        }
                    }
                }
            }

            // Après avoir assigné le groupe, on met à jour nextFreeTime pour les véhicules
            // utilisés
            for (Map.Entry<Vehicule, List<Reservation>> entry : assignedToVehicule.entrySet()) {
                Vehicule v = entry.getKey();
                List<Reservation> tour = entry.getValue();
                long durationMs = calculerDureeTournee(tour, hotelMap, distanceMatrix, param);
                nextFreeTime.put(v, new Timestamp(t.getTime() + durationMs));
                tripCount.put(v, tripCount.getOrDefault(v, 0) + 1);
            }
        }

        return assignments;
    }

    class VehicleState {
        Vehicule v;
        Timestamp availableTime;
        boolean isFirstCourse = true;
        boolean isWaiting = false;
        int remainingCap = 0;
        List<ReservationPortion> loadedPortions = new ArrayList<>();
        Timestamp departureTime = null;
        int trips = 0;
    }

    private PendingReservation choisirMeilleureReservation(List<PendingReservation> pending, int remainingCap,
            Timestamp maxAcceptedDate) {
        if (pending == null || pending.isEmpty() || remainingCap <= 0) {
            return null;
        }

        PendingReservation best = null;
        boolean hasPriority = false;
        for (PendingReservation p : pending) {
            if (p == null) continue;
            if (maxAcceptedDate != null && (p.reservation == null || p.reservation.getDateResa() == null
                    || p.reservation.getDateResa().after(maxAcceptedDate))) {
                continue;
            }
            if (p.remainder) {
                hasPriority = true;
                break;
            }
        }

        int bestEmptySeats = Integer.MAX_VALUE;
        int bestLeftover = Integer.MAX_VALUE;
        Timestamp bestDate = null;

        for (PendingReservation pr : pending) {
            if (pr == null || pr.reservation == null) continue;
            if (pr.remaining <= 0) continue;
            if (maxAcceptedDate != null && (pr.reservation.getDateResa() == null
                    || pr.reservation.getDateResa().after(maxAcceptedDate))) {
                continue;
            }

            if (hasPriority && !pr.remainder) {
                continue;
            }

            int taken = Math.min(remainingCap, pr.remaining);
            int emptySeats = remainingCap - taken;
            int leftover = pr.remaining - taken;
            Timestamp d = pr.reservation.getDateResa();

            boolean better = false;
            if (emptySeats < bestEmptySeats) {
                better = true;
            } else if (emptySeats == bestEmptySeats) {
                if (leftover < bestLeftover) {
                    better = true;
                } else if (leftover == bestLeftover) {
                    if (bestDate == null) {
                        better = true;
                    } else if (d != null && d.before(bestDate)) {
                        better = true;
                    }
                }
            }

            if (better) {
                best = pr;
                bestEmptySeats = emptySeats;
                bestLeftover = leftover;
                bestDate = d;
            }
        }

        return best;
    }

    private AssignationView assignerVehiculesGenererPortions(List<Reservation> reservations, List<Vehicule> vehicules,
            Map<String, TypeCarburant> typeById, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix, Parametre param, String selectedDate) {
        
        Map<String, Vehicule> assignments = new HashMap<>();
        List<Reservation> outReservations = new ArrayList<>();
        
        List<PendingReservation> future = new ArrayList<>();
        for (Reservation r : reservations) {
            if (r.getDateResa() != null) future.add(new PendingReservation(r));
        }
        future.sort((a, b) -> a.reservation.getDateResa().compareTo(b.reservation.getDateResa()));
        
        List<PendingReservation> pending = new ArrayList<>();

        String datePrefix = resolveDatePrefix(selectedDate, reservations);
        
        List<VehicleState> states = new ArrayList<>();
        for (Vehicule v : vehicules) {
            VehicleState vs = new VehicleState();
            vs.v = v;
            if (v.getHeureDisponible() != null) {
                String timeStr = v.getHeureDisponible().toString();
                vs.availableTime = Timestamp.valueOf(datePrefix + " " + timeStr);
            } else {
                vs.availableTime = Timestamp.valueOf(datePrefix + " 00:00:00");
            }
            vs.remainingCap = v.getNbrPlace() != null ? v.getNbrPlace() : 0;
            states.add(vs);
        }
        
        int seq = 1;
        int tempsAttente = param != null && param.getTempsAttente() != null ? param.getTempsAttente() : 30;
        
        while (true) {
            Timestamp nextTime = null;
            if (!future.isEmpty()) {
                nextTime = future.get(0).reservation.getDateResa();
            }
            for (VehicleState vs : states) {
                if (vs.availableTime != null) {
                    if (nextTime == null || vs.availableTime.before(nextTime)) {
                        nextTime = vs.availableTime;
                    }
                }
            }
            
            if (nextTime == null) break;
            if (nextTime.getTime() > Timestamp.valueOf("2099-01-01 00:00:00").getTime()) break;
            
            Timestamp currentTime = nextTime;
            
            while (!future.isEmpty() && !future.get(0).reservation.getDateResa().after(currentTime)) {
                pending.add(future.remove(0));
            }
            
            pending.sort((a, b) -> {
                if (a.remainder != b.remainder) return a.remainder ? -1 : 1;
                return a.reservation.getDateResa().compareTo(b.reservation.getDateResa());
            });
            
            List<VehicleState> readyVehicles = new ArrayList<>();
            for (VehicleState vs : states) {
                if (vs.availableTime != null && !vs.availableTime.after(currentTime)) {
                    readyVehicles.add(vs);
                }
            }
            
            if (readyVehicles.isEmpty()) continue;
            
            readyVehicles.sort((vs1, vs2) -> {
                Vehicule v1 = vs1.v; Vehicule v2 = vs2.v;
                int cap1 = v1.getNbrPlace() != null ? v1.getNbrPlace() : 0;
                int cap2 = v2.getNbrPlace() != null ? v2.getNbrPlace() : 0;
                boolean big1 = cap1 >= 10; boolean big2 = cap2 >= 10;
                if (big1 != big2) return Boolean.compare(big2, big1);
                if (cap1 != cap2) return Integer.compare(cap2, cap1);
                if (vs1.trips != vs2.trips) return Integer.compare(vs1.trips, vs2.trips);
                TypeCarburant tCarb1 = typeById != null ? typeById.get(v1.getIdTypeCarburant()) : null;
                TypeCarburant tCarb2 = typeById != null ? typeById.get(v2.getIdTypeCarburant()) : null;
                int diesel1 = (tCarb1 != null && tCarb1.getCode() != null && tCarb1.getCode().equalsIgnoreCase("D")) ? 1 : 0;
                int diesel2 = (tCarb2 != null && tCarb2.getCode() != null && tCarb2.getCode().equalsIgnoreCase("D")) ? 1 : 0;
                if (diesel1 != diesel2) return Integer.compare(diesel2, diesel1);
                return safe(v1.getIdVehicule()).compareTo(safe(v2.getIdVehicule()));
            });
            
            for (VehicleState vs : readyVehicles) {
                if (vs.isWaiting) {
                    while (vs.remainingCap > 0 && !pending.isEmpty()) {
                        PendingReservation best = choisirMeilleureReservation(pending, vs.remainingCap, vs.departureTime);
                        if (best != null) {
                            int pris = Math.min(vs.remainingCap, best.remaining);
                            vs.loadedPortions.add(new ReservationPortion(best.reservation, pris));
                            vs.remainingCap -= pris;
                            best.remaining -= pris;
                            if (best.remaining <= 0) pending.remove(best);
                            else best.remainder = true;
                        } else {
                            break;
                        }
                    }
                    
                    vs.isWaiting = false;
                    vs.isFirstCourse = false;
                    List<Reservation> tour = new ArrayList<>();
                    Timestamp depTime = vs.departureTime != null ? vs.departureTime : currentTime;
                    for (ReservationPortion rp : vs.loadedPortions) {
                        Reservation clone = new Reservation();
                        clone.setIdReservation(rp.reservation.getIdReservation() + "#" + (seq++));
                        clone.setIdClient(rp.reservation.getIdClient());
                        clone.setNbrPassager(rp.placesAssignees);
                        clone.setIdHotel(rp.reservation.getIdHotel());
                        clone.setDateResa(depTime);
                        outReservations.add(clone);
                        assignments.put(clone.getIdReservation(), vs.v);
                        tour.add(clone);
                    }
                    long durationMs = calculerDureeTournee(tour, hotelMap, distanceMatrix, param);
                    if (durationMs <= 0) durationMs = 60000L;
                    vs.availableTime = new Timestamp(depTime.getTime() + durationMs);
                    vs.loadedPortions.clear();
                    vs.departureTime = null;
                    vs.trips++;
                } else {
                    if (pending.isEmpty()) {
                        if (!vs.isFirstCourse) {
                            vs.availableTime = null;
                        } else {
                            if (!future.isEmpty()) vs.availableTime = future.get(0).reservation.getDateResa();
                            else vs.availableTime = null;
                        }
                    } else {
                        int totalPending = pending.stream().mapToInt(p -> p.remaining).sum();
                        int cap = vs.v.getNbrPlace() != null ? vs.v.getNbrPlace() : 0;
                        vs.remainingCap = cap;
                        
                        if (totalPending >= cap) {
                            while (vs.remainingCap > 0 && !pending.isEmpty()) {
                                PendingReservation best = choisirMeilleureReservation(pending, vs.remainingCap, null);
                                if (best != null) {
                                    int pris = Math.min(vs.remainingCap, best.remaining);
                                    vs.loadedPortions.add(new ReservationPortion(best.reservation, pris));
                                    vs.remainingCap -= pris;
                                    best.remaining -= pris;
                                    if (best.remaining <= 0) pending.remove(best);
                                    else best.remainder = true;
                                } else {
                                    break;
                                }
                            }
                            
                            vs.isWaiting = false;
                            vs.isFirstCourse = false;
                            List<Reservation> tour = new ArrayList<>();
                            Timestamp depTime = currentTime;
                            for (ReservationPortion rp : vs.loadedPortions) {
                                Reservation clone = new Reservation();
                                clone.setIdReservation(rp.reservation.getIdReservation() + "#" + (seq++));
                                clone.setIdClient(rp.reservation.getIdClient());
                                clone.setNbrPassager(rp.placesAssignees);
                                clone.setIdHotel(rp.reservation.getIdHotel());
                                clone.setDateResa(depTime);
                                outReservations.add(clone);
                                assignments.put(clone.getIdReservation(), vs.v);
                                tour.add(clone);
                            }
                            long durationMs = calculerDureeTournee(tour, hotelMap, distanceMatrix, param);
                            if (durationMs <= 0) durationMs = 60000L;
                            vs.availableTime = new Timestamp(depTime.getTime() + durationMs);
                            vs.loadedPortions.clear();
                            vs.departureTime = null;
                            vs.trips++;
                        } else {
                            // Vehicule not full, initiate waiting
                            while (vs.remainingCap > 0 && !pending.isEmpty()) {
                                PendingReservation best = choisirMeilleureReservation(pending, vs.remainingCap, null);
                                if (best != null) {
                                    int pris = Math.min(vs.remainingCap, best.remaining);
                                    vs.loadedPortions.add(new ReservationPortion(best.reservation, pris));
                                    vs.remainingCap -= pris;
                                    best.remaining -= pris;
                                    if (best.remaining <= 0) pending.remove(best);
                                    else best.remainder = true;
                                } else {
                                    break;
                                }
                            }
                            
                            vs.isWaiting = true;
                            vs.departureTime = new Timestamp(currentTime.getTime() + tempsAttente * 60000L);
                            vs.availableTime = vs.departureTime;
                        }
                    }
                }
            }
        }
        
        return new AssignationView(outReservations, assignments);
    }

    private long calculerDureeTourneeSansRetour(List<Reservation> tour, Map<String, Hotel> hotelMap,
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

        BigDecimal travelTimeHours = totalDistance.divide(param.getVitesseMoyenne(), 4, RoundingMode.HALF_UP);
        return (long) (travelTimeHours.doubleValue() * 3600000L);
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
            Map<String, TypeCarburant> typeById, Map<Vehicule, Integer> tripCount) {
        Vehicule best = null;
        double bestFillRate = -1.0;
        int bestTotalCapacity = Integer.MAX_VALUE;
        int bestTripCount = Integer.MAX_VALUE;
        int bestScoreDiesel = -1;

        for (Vehicule v : available) {
            int currentCap = remainingCapacity.getOrDefault(v, 0);
            Timestamp freeTime = nextFreeTime.getOrDefault(v, new Timestamp(0));

            // Si le véhicule peut accueillir la réservation et est libre
            if (currentCap >= r.getNbrPassager() && !freeTime.after(currentTime)) {
                // Calculer le taux de remplissage SI on ajoute cette réservation
                int occupiedAfter = v.getNbrPlace() - (currentCap - r.getNbrPassager());
                double fillRateAfter = (double) occupiedAfter / v.getNbrPlace();
                
                TypeCarburant t = typeById.get(v.getIdTypeCarburant());
                int dieselScore = (t != null && t.getCode() != null && t.getCode().equalsIgnoreCase("D")) ? 1 : 0;

                // Critères de choix (par ordre de priorité) :
                // 1. Diesel prioritaire
                // 2. Moins de trajets effectués dans la journée
                // 3. Plus petite capacité totale (économiser les gros véhicules)
                // 4. Maximiser le taux de remplissage final (favorise le groupement)
                
                boolean isBetter = false;
                int vTrips = tripCount != null ? tripCount.getOrDefault(v, 0) : 0;
                if (dieselScore > bestScoreDiesel) {
                    isBetter = true;
                } else if (dieselScore == bestScoreDiesel) {
                    if (vTrips < bestTripCount) {
                        isBetter = true;
                    } else if (vTrips == bestTripCount) {
                        if (v.getNbrPlace() < bestTotalCapacity) {
                            isBetter = true;
                        } else if (v.getNbrPlace() == bestTotalCapacity) {
                            if (fillRateAfter > bestFillRate + 0.0001) {
                                isBetter = true;
                            }
                        }
                    }
                } else if (fillRateAfter > bestFillRate + 0.0001) {
                    isBetter = true;
                }

                if (isBetter) {
                    best = v;
                    bestFillRate = fillRateAfter;
                    bestTotalCapacity = v.getNbrPlace();
                    bestTripCount = vTrips;
                    bestScoreDiesel = dieselScore;
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
                // Tronquer à la minute pour le groupement, comme dans assignerVehicules
                long timeMs = r.getDateResa().getTime();
                Timestamp truncated = new Timestamp((timeMs / 60000L) * 60000L);
                tournées.computeIfAbsent(v, k -> new HashMap<>())
                        .computeIfAbsent(truncated, k -> new ArrayList<>())
                        .add(r);
            }
        }

        Map<Vehicule, Timestamp> nextFreeTime = new HashMap<>();
        for (Vehicule v : tournées.keySet()) {
            Timestamp init = new Timestamp(0);
            if (v.getHeureDisponible() != null) {
                String datePrefix = new java.sql.Date(System.currentTimeMillis()).toString(); // Fallback
                if (!reservations.isEmpty() && reservations.get(0).getDateResa() != null) {
                    datePrefix = new java.sql.Date(reservations.get(0).getDateResa().getTime()).toString();
                }
                init = Timestamp.valueOf(datePrefix + " " + v.getHeureDisponible().toString());
            }
            nextFreeTime.put(v, init);
        }

        for (Map.Entry<Vehicule, Map<Timestamp, List<Reservation>>> vEntry : tournées.entrySet()) {
            List<Timestamp> times = new ArrayList<>(vEntry.getValue().keySet());
            Collections.sort(times);
            for (Timestamp time : times) {
                List<Reservation> tour = vEntry.getValue().get(time);

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

                Timestamp dep = time;
                Timestamp vFree = nextFreeTime.getOrDefault(vEntry.getKey(), new Timestamp(0));
                if (vFree != null && vFree.after(dep)) {
                    dep = vFree;
                }
                // Appliquer les horaires à toutes les réservations de la tournée
                for (Reservation r : tour) {
                    departureTimes.put(r.getIdReservation(), dep);
                    arrivalTimes.put(r.getIdReservation(), new Timestamp(dep.getTime() + travelTimeMs));
                }
                nextFreeTime.put(vEntry.getKey(), new Timestamp(dep.getTime() + travelTimeMs));
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
