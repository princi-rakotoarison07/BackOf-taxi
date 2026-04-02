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
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashSet;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RestController
public class ReservationController {

    private static class PlageDisponibilite {
        private final LocalTime heureDebut;
        private final LocalTime heureFin;

        private PlageDisponibilite(LocalTime heureDebut, LocalTime heureFin) {
            this.heureDebut = heureDebut;
            this.heureFin = heureFin;
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
            mv.addObject("message", "Reservation enregistree !");
        } catch (Exception e) {
            mv.addObject("error", "Erreur : " + e.getMessage());
        }
        return mv;
    }

    @PostMapping("/BackOf-taxi/reservation/save-multiple")
    public ModelAndView saveMultiple(@Param("reservationsData") String data) {
        ModelAndView mv = new ModelAndView("/views/result.jsp");
        if (data == null || data.trim().isEmpty()) {
            mv.addObject("error", "Aucune donnee ree.");
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
                mv.addObject("message", count + " reservations enregistrees !");
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
        mv.addObject("pageTitle", "Assignation des Reservations");
        prepareAssignationData(mv, date);
        return mv;
    }

    @GetMapping("/BackOf-taxi/reservation/assignation-vehicule")
    public ModelAndView assignationVehicule(@Param("date") String date) throws Exception {
        ModelAndView mv = new ModelAndView("/views/reservation/assignationVehicule.jsp");
        mv.addObject("pageTitle", "Assignation par Vehicule");
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
            Map<String, PlageDisponibilite> disponibilitesVehicules = chargerDisponibilites(conn, vehicules);

            List<Reservation> filtered = filtrerReservations(allReservations, date);
            filtered.sort((a, b) -> comparerReservationsPourAssignation(a, b, hotelMap, distanceMatrix));

            Map<String, Timestamp> plannedDepartureTimes = new HashMap<>();
            Map<String, Integer> unassignedPassengers = new HashMap<>();
                Map<String, List<String>> splitDetails = new HashMap<>();
                Map<String, List<AffectationVehicule>> assignmentChunksByReservation = new HashMap<>();
            Map<String, Vehicule> assignments = assignerVehicules(filtered, vehicules, typeById, hotelMap,
                    distanceMatrix, currentParam, plannedDepartureTimes, unassignedPassengers, splitDetails,
                    assignmentChunksByReservation, disponibilitesVehicules);
            Map<String, Timestamp> departureTimes = new HashMap<>();
            Map<String, Timestamp> arrivalTimes = new HashMap<>();

            calculerHoraires(filtered, currentParam, hotelMap, distanceMatrix, assignments, plannedDepartureTimes, departureTimes,
                    arrivalTimes);

            // Garantit des horaires coherents avec les chunks reels (splits multi-vagues inclus).
            synchroniserHorairesDepuisChunks(assignmentChunksByReservation, plannedDepartureTimes, departureTimes, arrivalTimes);

                sauvegarderAssignations(conn, filtered, assignmentChunksByReservation, departureTimes, arrivalTimes,
                    unassignedPassengers);

            mv.addObject("reservations", filtered);
            mv.addObject("vehicules", vehicules);
            mv.addObject("types", types);
            mv.addObject("assignments", assignments);
            mv.addObject("departureTimes", departureTimes);
            mv.addObject("arrivalTimes", arrivalTimes);
            mv.addObject("unassignedPassengers", unassignedPassengers);
            mv.addObject("splitDetails", splitDetails);
            mv.addObject("selectedDate", date);
            mv.addObject("hotels", hotels);
            mv.addObject("hotelMap", hotelMap);

                SlotVehiculeData slotData = construireSlotsVehicule(filtered, assignmentChunksByReservation);

                // Calculer l'ordre des tournees et les heures detaillees pour l'affichage
                Map<String, List<Reservation>> tourOrders = calculerOrdreTournees(slotData.slotToResa, hotelMap, distanceMatrix);
                Map<String, Map<String, java.sql.Timestamp>> detailedTimes = calculerHorairesDetailles(tourOrders, hotelMap,
                    distanceMatrix, currentParam);
                Map<String, BigDecimal> tourDistancesKm = calculerDistancesTournees(slotData.slotToResa, hotelMap,
                    distanceMatrix, tourOrders);

            sauvegarderTrajetsEtCompteurs(conn, filtered, assignmentChunksByReservation, departureTimes, arrivalTimes);

                mv.addObject("slotToResa", slotData.slotToResa);
                mv.addObject("slotToAssignedPax", slotData.slotToAssignedPax);
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
            plannedDepartureTimes, null, null, null, null);
    }

    private Map<String, Vehicule> assignerVehicules(List<Reservation> reservations, List<Vehicule> vehicules,
            Map<String, TypeCarburant> typeById, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix, Parametre param,
            Map<String, Timestamp> plannedDepartureTimes, Map<String, Integer> unassignedPassengers,
            Map<String, List<String>> splitDetails,
            Map<String, List<AffectationVehicule>> assignmentChunksByReservation,
            Map<String, PlageDisponibilite> disponibilitesVehicules) {
        Map<String, Vehicule> assignments = new HashMap<>();
        List<Vehicule> available = new ArrayList<>(vehicules);
        int waitMinutes = getTempsAttenteMinutes(param);
        Map<Vehicule, Integer> dailyTripCount = initialiserCompteurTrajets(available);

        List<Reservation> orderedReservations = new ArrayList<>();
        if (reservations != null) {
            for (Reservation r : reservations) {
                if (estReservationValide(r)) {
                    orderedReservations.add(r);
                }
            }
        }
        orderedReservations.sort((a, b) -> a.getDateResa().compareTo(b.getDateResa()));

        if (orderedReservations.isEmpty()) {
            return assignments;
        }

        LocalDateTime referenceDateTime = reservations != null && !reservations.isEmpty() && reservations.get(0).getDateResa() != null
            ? reservations.get(0).getDateResa().toLocalDateTime()
            : LocalDateTime.now();
        Map<Vehicule, Timestamp> nextFreeTime = initialiserProchaineDisponibilite(available, disponibilitesVehicules,
            referenceDateTime.toLocalDate());

        Map<Reservation, Integer> remainingDemand = new LinkedHashMap<>();
        for (Reservation r : orderedReservations) {
            remainingDemand.put(r, r.getNbrPassager() != null ? r.getNbrPassager() : 0);
            if (assignmentChunksByReservation != null && r.getIdReservation() != null) {
                assignmentChunksByReservation.putIfAbsent(r.getIdReservation(), new ArrayList<>());
            }
        }

        Map<Vehicule, List<AffectationVehicule>> vehiculesEnAttente = new HashMap<>();
        Map<Vehicule, Integer> placesRestantes = new HashMap<>();

        Timestamp currentWaveTime = orderedReservations.get(0).getDateResa();
        while (aEncoreDesPassagers(remainingDemand) || !vehiculesEnAttente.isEmpty()) {
            List<Reservation> pendingNow = construireReservationsEnAttente(orderedReservations, remainingDemand, currentWaveTime);

            if (pendingNow.isEmpty() && !vehiculesEnAttente.isEmpty()) {
                if (!aDemandesFutures(orderedReservations, remainingDemand, currentWaveTime)) {
                    // Aucune demande future, les vehicules en attente partent!
                } else {
                    Timestamp nextArrival = trouverProchaineArrivee(orderedReservations, remainingDemand, currentWaveTime);
                    Timestamp nextVehicule = trouverProchaineDisponibilite(nextFreeTime, currentWaveTime);
                    Timestamp nextTime = minTimestamp(nextVehicule, nextArrival);
                    if (nextTime == null) {
                        break;
                    }
                    currentWaveTime = nextTime;
                    continue;
                }
            }

            Timestamp departureWaveTime = resoudreHeureDepartGlobal(orderedReservations, remainingDemand, currentWaveTime,
                    waitMinutes);
            List<Reservation> pending = construireReservationsEnAttente(orderedReservations, remainingDemand, departureWaveTime);
            pending.sort((a, b) -> comparerReservationsIntraGroupe(a, b, hotelMap, distanceMatrix));

            List<Vehicule> vehiculesDuGroupe = new ArrayList<>();
            // Toujours proposer les vehicules partiels en priorite (ceux e'aeroport)
            for (Vehicule v : vehiculesEnAttente.keySet()) {
                if (!vehiculesDuGroupe.contains(v)) {
                    vehiculesDuGroupe.add(v);
                }
            }

            List<Vehicule> currentAvailable = trierVehiculesDisponiblesPourGroupe(available, nextFreeTime,
                    departureWaveTime, dailyTripCount, typeById, disponibilitesVehicules);

            for (Vehicule v : currentAvailable) {
                if (!vehiculesDuGroupe.contains(v)) {
                    vehiculesDuGroupe.add(v);
                    placesRestantes.put(v, v.getNbrPlace() != null ? v.getNbrPlace() : 0);
                }
            }

            if (vehiculesDuGroupe.isEmpty()) {
                Timestamp nextVehicule = trouverProchaineDisponibilite(nextFreeTime, departureWaveTime);
                Timestamp nextArrival = trouverProchaineArrivee(orderedReservations, remainingDemand, departureWaveTime);
                Timestamp nextTime = minTimestamp(nextVehicule, nextArrival);
                if (nextTime == null) {
                    break;
                }
                currentWaveTime = nextTime;
                continue;
            }

            Map<Reservation, Set<String>> vehiculesParReservation = new HashMap<>();

            while (true) {
                AffectationCandidate best = choisirMeilleureAffectation(pending, vehiculesDuGroupe, remainingDemand,
                        placesRestantes, dailyTripCount, typeById, vehiculesParReservation);
                if (best == null) {
                    break;
                }

                remainingDemand.put(best.reservation,
                        remainingDemand.getOrDefault(best.reservation, 0) - best.passagers);
                placesRestantes.put(best.vehicule,
                        placesRestantes.getOrDefault(best.vehicule, 0) - best.passagers);

                AffectationVehicule chunkCree = new AffectationVehicule(best.vehicule, best.reservation,
                        best.passagers, null, null);
                
                vehiculesEnAttente.computeIfAbsent(best.vehicule, k -> new ArrayList<>()).add(chunkCree);
                vehiculesParReservation.computeIfAbsent(best.reservation, k -> new HashSet<>())
                        .add(best.vehicule.getIdVehicule());
            }

            boolean aFutures = aDemandesFutures(orderedReservations, remainingDemand, departureWaveTime);
            List<Vehicule> vehiculesPartants = new ArrayList<>();
            for (Map.Entry<Vehicule, List<AffectationVehicule>> entry : vehiculesEnAttente.entrySet()) {
                Vehicule v = entry.getKey();
                int rest = placesRestantes.getOrDefault(v, 0);
                if (rest <= 0 || !aFutures) {
                    vehiculesPartants.add(v);
                }
            }

            if (vehiculesPartants.isEmpty()) {
                Timestamp nextVehicule = trouverProchaineDisponibilite(nextFreeTime, departureWaveTime);
                Timestamp nextArrival = trouverProchaineArrivee(orderedReservations, remainingDemand, departureWaveTime);
                Timestamp nextTime = minTimestamp(nextVehicule, nextArrival);
                if (nextTime == null) {
                    break;
                }
                currentWaveTime = nextTime;
                continue;
            }

            Timestamp prochainTemps = null;
            for (Vehicule v : vehiculesPartants) {
                List<AffectationVehicule> chunks = vehiculesEnAttente.remove(v);
                placesRestantes.remove(v);

                List<Reservation> tour = new ArrayList<>();
                for (AffectationVehicule chunk : chunks) {
                    if (chunk.reservation != null && !tour.contains(chunk.reservation)) {
                        tour.add(chunk.reservation);
                    }
                }

                long durationMs = calculerDureeTournee(tour, hotelMap, distanceMatrix, param);
                Timestamp endTime = new Timestamp(departureWaveTime.getTime() + durationMs);

                for (AffectationVehicule chunk : chunks) {
                    // C'est maintenant qu'on fixe l'heure de depart officielle
                    chunk.heureDepart = departureWaveTime;
                    chunk.heureRetour = endTime;
                    if (assignmentChunksByReservation != null && chunk.idReservation != null) {
                        assignmentChunksByReservation.computeIfAbsent(chunk.idReservation, k -> new ArrayList<>())
                                .add(chunk);
                    }
                }

                nextFreeTime.put(v, endTime);
                dailyTripCount.put(v, dailyTripCount.getOrDefault(v, 0) + 1);

                if (prochainTemps == null || endTime.before(prochainTemps)) {
                    prochainTemps = endTime;
                }
            }

            if (prochainTemps != null) {
                currentWaveTime = prochainTemps;
            } else {
                Timestamp nextVehicule = trouverProchaineDisponibilite(nextFreeTime, departureWaveTime);
                Timestamp nextArrival = trouverProchaineArrivee(orderedReservations, remainingDemand, departureWaveTime);
                Timestamp nextTime = minTimestamp(nextVehicule, nextArrival);
                if (nextTime == null) {
                    break;
                }
                currentWaveTime = nextTime;
            }
        }

        if (assignmentChunksByReservation != null) {
            for (Map.Entry<String, List<AffectationVehicule>> e : assignmentChunksByReservation.entrySet()) {
                Timestamp minDep = null;
                for (AffectationVehicule chunk : e.getValue()) {
                    if (chunk == null || chunk.heureDepart == null) {
                        continue;
                    }
                    if (minDep == null || chunk.heureDepart.before(minDep)) {
                        minDep = chunk.heureDepart;
                    }
                }
                if (minDep != null) {
                    plannedDepartureTimes.put(e.getKey(), minDep);
                }
            }
        }

        for (Reservation r : orderedReservations) {
            if (r == null || r.getIdReservation() == null) {
                continue;
            }

            List<AffectationVehicule> chunks = assignmentChunksByReservation != null
                    ? assignmentChunksByReservation.getOrDefault(r.getIdReservation(), new ArrayList<>())
                    : new ArrayList<>();

            if (!chunks.isEmpty()) {
                Vehicule principal = choisirVehiculePrincipal(chunks, dailyTripCount, typeById);
                if (principal != null) {
                    assignments.put(r.getIdReservation(), principal);
                }

                if (splitDetails != null) {
                    List<String> splitInfo = new ArrayList<>();
                    for (AffectationVehicule chunk : chunks) {
                        splitInfo.add(chunk.vehicule.getIdVehicule() + ":" + chunk.passagers);
                    }
                    splitDetails.put(r.getIdReservation(), splitInfo);
                }
            }

            int remaining = remainingDemand.getOrDefault(r, 0);
            if (remaining > 0 && unassignedPassengers != null) {
                unassignedPassengers.put(r.getIdReservation(), remaining);
            }
        }

        return assignments;
    }

    private boolean aDemandesFutures(List<Reservation> orderedReservations,
            Map<Reservation, Integer> remainingDemand, Timestamp currentWaveTime) {
        if (orderedReservations == null || remainingDemand == null || currentWaveTime == null) {
            return false;
        }
        for (Reservation r : orderedReservations) {
            if (r == null || r.getDateResa() == null) {
                continue;
            }
            if (remainingDemand.getOrDefault(r, 0) > 0 && r.getDateResa().after(currentWaveTime)) {
                return true;
            }
        }
        return false;
    }

    private Timestamp resoudreHeureDepartGlobal(List<Reservation> orderedReservations,
            Map<Reservation, Integer> remainingDemand, Timestamp currentWaveTime, int waitMinutes) {
        if (currentWaveTime == null || waitMinutes <= 0) {
            return currentWaveTime;
        }

        long windowEnd = currentWaveTime.getTime() + (waitMinutes * 60000L);
        Timestamp departure = currentWaveTime;
        for (Reservation r : orderedReservations) {
            if (r == null || r.getDateResa() == null) {
                continue;
            }
            if (remainingDemand.getOrDefault(r, 0) <= 0) {
                continue;
            }
            Timestamp t = r.getDateResa();
            if (!t.before(currentWaveTime) && t.getTime() <= windowEnd && t.after(departure)) {
                departure = t;
            }
        }
        return departure;
    }

    private List<Reservation> construireReservationsEnAttente(List<Reservation> orderedReservations,
            Map<Reservation, Integer> remainingDemand, Timestamp currentWaveTime) {
        List<Reservation> pending = new ArrayList<>();
        for (Reservation r : orderedReservations) {
            if (r == null || r.getDateResa() == null) {
                continue;
            }
            if (!r.getDateResa().after(currentWaveTime) && remainingDemand.getOrDefault(r, 0) > 0) {
                pending.add(r);
            }
        }
        return pending;
    }

    private Timestamp trouverProchaineArrivee(List<Reservation> orderedReservations,
            Map<Reservation, Integer> remainingDemand, Timestamp currentWaveTime) {
        Timestamp min = null;
        for (Reservation r : orderedReservations) {
            if (r == null || r.getDateResa() == null || remainingDemand.getOrDefault(r, 0) <= 0) {
                continue;
            }
            if (r.getDateResa().after(currentWaveTime) && (min == null || r.getDateResa().before(min))) {
                min = r.getDateResa();
            }
        }
        return min;
    }

    private Timestamp minTimestamp(Timestamp a, Timestamp b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.before(b) ? a : b;
    }

    private List<Vehicule> trierVehiculesDisponiblesPourGroupe(List<Vehicule> available,
            Map<Vehicule, Timestamp> nextFreeTime, Timestamp currentTime,
            Map<Vehicule, Integer> dailyTripCount, Map<String, TypeCarburant> typeById,
            Map<String, PlageDisponibilite> disponibilitesVehicules) {
        List<Vehicule> vehicules = new ArrayList<>();
        LocalTime heureCourante = currentTime.toLocalDateTime().toLocalTime();
        for (Vehicule v : available) {
            Timestamp freeTime = nextFreeTime.getOrDefault(v, debutJourMaintenant());
            PlageDisponibilite plage = disponibilitesVehicules != null ? disponibilitesVehicules.get(v.getIdVehicule()) : null;
            if (!freeTime.after(currentTime) && estDansPlageHoraire(heureCourante, plage)) {
                vehicules.add(v);
            }
        }

        Collections.sort(vehicules, (a, b) -> {
            int tripA = dailyTripCount.getOrDefault(a, 0);
            int tripB = dailyTripCount.getOrDefault(b, 0);
            if (tripA != tripB) {
                return Integer.compare(tripA, tripB);
            }

            int fuelA = getPrioriteCarburant(typeById.get(a.getIdTypeCarburant()));
            int fuelB = getPrioriteCarburant(typeById.get(b.getIdTypeCarburant()));
            if (fuelA != fuelB) {
                return Integer.compare(fuelA, fuelB);
            }

            int capA = a.getNbrPlace() != null ? a.getNbrPlace() : 0;
            int capB = b.getNbrPlace() != null ? b.getNbrPlace() : 0;
            if (capA != capB) {
                return Integer.compare(capA, capB);
            }

            String idA = a.getIdVehicule() != null ? a.getIdVehicule() : "";
            String idB = b.getIdVehicule() != null ? b.getIdVehicule() : "";
            return idA.compareToIgnoreCase(idB);
        });

        return vehicules;
    }

    private boolean aEncoreDesPassagers(Map<Reservation, Integer> remainingDemand) {
        if (remainingDemand == null || remainingDemand.isEmpty()) {
            return false;
        }
        for (Integer val : remainingDemand.values()) {
            if (val != null && val > 0) {
                return true;
            }
        }
        return false;
    }

    private Timestamp trouverProchaineDisponibilite(Map<Vehicule, Timestamp> nextFreeTime, Timestamp currentTime) {
        Timestamp min = null;
        for (Timestamp ts : nextFreeTime.values()) {
            if (ts == null) {
                continue;
            }
            if (ts.after(currentTime) && (min == null || ts.before(min))) {
                min = ts;
            }
        }
        return min;
    }

    private static class AffectationCandidate {
        private final Vehicule vehicule;
        private final Reservation reservation;
        private final int passagers;
        private final int categorie;
        private final int ecart;
        private final int fragmentation;
        private final int tripCount;
        private final int fuelPriority;
        private final boolean vehiculePartiel;
        private final boolean reservationDejaEntamee;

        private AffectationCandidate(Vehicule vehicule, Reservation reservation, int passagers,
                int categorie, int ecart, int fragmentation, int tripCount, int fuelPriority,
                boolean vehiculePartiel, boolean reservationDejaEntamee) {
            this.vehicule = vehicule;
            this.reservation = reservation;
            this.passagers = passagers;
            this.categorie = categorie;
            this.ecart = ecart;
            this.fragmentation = fragmentation;
            this.tripCount = tripCount;
            this.fuelPriority = fuelPriority;
            this.vehiculePartiel = vehiculePartiel;
            this.reservationDejaEntamee = reservationDejaEntamee;
        }
    }

    private AffectationCandidate choisirMeilleureAffectation(List<Reservation> group, List<Vehicule> vehiculesDisponibles,
            Map<Reservation, Integer> remainingDemand, Map<Vehicule, Integer> placesRestantes,
            Map<Vehicule, Integer> dailyTripCount, Map<String, TypeCarburant> typeById,
            Map<Reservation, Set<String>> vehiculesParReservation) {
        AffectationCandidate best = null;
        int activeReservations = compterReservationsActives(group, remainingDemand);

        for (Vehicule v : vehiculesDisponibles) {
            int cap = placesRestantes.getOrDefault(v, 0);
            if (cap <= 0) {
                continue;
            }

            int trip = dailyTripCount.getOrDefault(v, 0);
            int fuel = getPrioriteCarburant(typeById.get(v.getIdTypeCarburant()));
            int nbrPlaceOriginal = v.getNbrPlace() != null ? v.getNbrPlace() : 0;
            boolean isPartiel = cap < nbrPlaceOriginal;

            for (Reservation r : group) {
                int demand = remainingDemand.getOrDefault(r, 0);
                if (demand <= 0) {
                    continue;
                }

                int affectes = Math.min(cap, demand);
                int categorie = demand <= cap ? 0 : 1;
                int ecart = Math.abs(cap - demand);
                int fragmentation = vehiculesParReservation.getOrDefault(r, Collections.emptySet()).size();
                boolean reservationDejaEntamee = fragmentation > 0;

                // Evite de splitter immediatement une unique reservation sur plusieurs vehicules vides.
                if (activeReservations == 1 && reservationDejaEntamee && !isPartiel) {
                    continue;
                }

                AffectationCandidate candidate = new AffectationCandidate(v, r, affectes, categorie, ecart,
                        fragmentation, trip, fuel, isPartiel, reservationDejaEntamee);
                if (estMeilleureAffectation(candidate, best)) {
                    best = candidate;
                }
            }
        }

        return best;
    }

    private boolean estMeilleureAffectation(AffectationCandidate candidate, AffectationCandidate current) {
        if (candidate == null) {
            return false;
        }
        if (current == null) {
            return true;
        }

        // Un vehicule partiel (deje  l'aeroport et non plein) a la priorite absolue
        if (candidate.vehiculePartiel != current.vehiculePartiel) {
            return candidate.vehiculePartiel;
        }

        // Regles specifiques selon si le vehicule est partiel ou vide
        if (candidate.vehiculePartiel) {
            // Un vehicule partiel cherche eOMBINER la place restante avec la demande la plus proche (minimise l'ecart)
            if (candidate.ecart != current.ecart) {
                return candidate.ecart < current.ecart;
            }
            if (candidate.passagers != current.passagers) {
                return candidate.passagers > current.passagers;
            }
        } else {
            // Pour un vehicule vide, on peut prioriser une reservation deja entamee
            // seulement si elle est plus ancienne que l'alternative non entamee.
            if (candidate.reservationDejaEntamee != current.reservationDejaEntamee) {
                if (candidate.reservationDejaEntamee
                        && reservationPlusAncienneOuEgale(candidate.reservation, current.reservation)) {
                    return true;
                }
                if (current.reservationDejaEntamee
                        && reservationPlusAncienneOuEgale(current.reservation, candidate.reservation)) {
                    return false;
                }
            }

            // Un vehicule vide cherche erendre le plus grand groupe possible pour rentabiliser
            if (candidate.passagers != current.passagers) {
                return candidate.passagers > current.passagers;
            }
            // En cas d'egalite sur le nombre affecte, l'ordre d'arrivee des reservations prime.
            if (candidate.reservation != null && current.reservation != null && !candidate.reservation.getIdReservation().equals(current.reservation.getIdReservation())) {
                java.sql.Timestamp dateCand = candidate.reservation.getDateResa();
                java.sql.Timestamp dateCurr = current.reservation.getDateResa();
                if (dateCand != null && dateCurr != null && !dateCand.equals(dateCurr)) {
                    return dateCand.before(dateCurr);
                }
            }
        }

        if (candidate.fragmentation != current.fragmentation) {
            return candidate.fragmentation < current.fragmentation;
        }
        if (candidate.categorie != current.categorie) {
            return candidate.categorie < current.categorie;
        }
        if (candidate.tripCount != current.tripCount) {
            return candidate.tripCount < current.tripCount;
        }
        if (candidate.fuelPriority != current.fuelPriority) {
            return candidate.fuelPriority < current.fuelPriority;
        }

        int capCandidate = candidate.vehicule.getNbrPlace() != null ? candidate.vehicule.getNbrPlace() : 0;
        int capCurrent = current.vehicule.getNbrPlace() != null ? current.vehicule.getNbrPlace() : 0;
        if (capCandidate != capCurrent) {
            return capCandidate < capCurrent;
        }

        String idVehCandidate = candidate.vehicule.getIdVehicule() != null ? candidate.vehicule.getIdVehicule() : "";
        String idVehCurrent = current.vehicule.getIdVehicule() != null ? current.vehicule.getIdVehicule() : "";
        int cmpVeh = idVehCandidate.compareToIgnoreCase(idVehCurrent);
        if (cmpVeh != 0) {
            return cmpVeh < 0;
        }

        String idResCandidate = candidate.reservation.getIdReservation() != null ? candidate.reservation.getIdReservation() : "";
        String idResCurrent = current.reservation.getIdReservation() != null ? current.reservation.getIdReservation() : "";
        return idResCandidate.compareToIgnoreCase(idResCurrent) < 0;
    }

    private int compterReservationsActives(List<Reservation> group, Map<Reservation, Integer> remainingDemand) {
        if (group == null || remainingDemand == null) {
            return 0;
        }
        int count = 0;
        for (Reservation r : group) {
            if (r != null && remainingDemand.getOrDefault(r, 0) > 0) {
                count++;
            }
        }
        return count;
    }

    private boolean reservationPlusAncienneOuEgale(Reservation first, Reservation second) {
        if (first == null) {
            return false;
        }
        if (second == null) {
            return true;
        }

        Timestamp firstDate = first.getDateResa();
        Timestamp secondDate = second.getDateResa();
        if (firstDate == null) {
            return false;
        }
        if (secondDate == null) {
            return true;
        }
        return !firstDate.after(secondDate);
    }

    private static class SlotVehiculeData {
        private final Map<String, List<Reservation>> slotToResa = new LinkedHashMap<>();
        private final Map<String, Map<String, Integer>> slotToAssignedPax = new LinkedHashMap<>();
    }

    private SlotVehiculeData construireSlotsVehicule(List<Reservation> reservations,
            Map<String, List<AffectationVehicule>> assignmentChunksByReservation) {
        SlotVehiculeData data = new SlotVehiculeData();
        if (reservations == null || reservations.isEmpty() || assignmentChunksByReservation == null
                || assignmentChunksByReservation.isEmpty()) {
            return data;
        }

        Map<String, Reservation> reservationById = new HashMap<>();
        for (Reservation reservation : reservations) {
            if (reservation != null && reservation.getIdReservation() != null) {
                reservationById.put(reservation.getIdReservation(), reservation);
            }
        }

        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        Map<String, Set<String>> slotReservationIds = new HashMap<>();

        for (Map.Entry<String, List<AffectationVehicule>> entry : assignmentChunksByReservation.entrySet()) {
            String idReservation = entry.getKey();
            Reservation reservation = reservationById.get(idReservation);
            if (reservation == null) {
                continue;
            }

            List<AffectationVehicule> chunks = entry.getValue();
            if (chunks == null) {
                continue;
            }

            for (AffectationVehicule chunk : chunks) {
                if (chunk == null || chunk.vehicule == null || chunk.vehicule.getIdVehicule() == null
                        || chunk.heureDepart == null || chunk.heureRetour == null || chunk.passagers <= 0) {
                    continue;
                }

                String key = chunk.vehicule.getIdVehicule() + "|" + df.format(chunk.heureDepart) + "|"
                        + df.format(chunk.heureRetour);

                Set<String> resaIds = slotReservationIds.computeIfAbsent(key, k -> new LinkedHashSet<>());
                if (!resaIds.contains(idReservation)) {
                    data.slotToResa.computeIfAbsent(key, k -> new ArrayList<>()).add(reservation);
                    resaIds.add(idReservation);
                }

                Map<String, Integer> paxByReservation = data.slotToAssignedPax.computeIfAbsent(key, k -> new HashMap<>());
                paxByReservation.put(idReservation, paxByReservation.getOrDefault(idReservation, 0) + chunk.passagers);
            }
        }

        return data;
    }

    private void synchroniserHorairesDepuisChunks(
            Map<String, List<AffectationVehicule>> assignmentChunksByReservation,
            Map<String, Timestamp> plannedDepartureTimes,
            Map<String, Timestamp> departureTimes,
            Map<String, Timestamp> arrivalTimes) {
        if (assignmentChunksByReservation == null || assignmentChunksByReservation.isEmpty()) {
            return;
        }

        for (Map.Entry<String, List<AffectationVehicule>> entry : assignmentChunksByReservation.entrySet()) {
            String idReservation = entry.getKey();
            List<AffectationVehicule> chunks = entry.getValue();
            if (idReservation == null || chunks == null || chunks.isEmpty()) {
                continue;
            }

            Timestamp minDepart = null;
            Timestamp maxRetour = null;
            for (AffectationVehicule chunk : chunks) {
                if (chunk == null) {
                    continue;
                }
                if (chunk.heureDepart != null && (minDepart == null || chunk.heureDepart.before(minDepart))) {
                    minDepart = chunk.heureDepart;
                }
                if (chunk.heureRetour != null && (maxRetour == null || chunk.heureRetour.after(maxRetour))) {
                    maxRetour = chunk.heureRetour;
                }
            }

            if (minDepart != null) {
                plannedDepartureTimes.put(idReservation, minDepart);
                departureTimes.put(idReservation, minDepart);
            }
            if (maxRetour != null) {
                arrivalTimes.put(idReservation, maxRetour);
            }
        }
    }

    private boolean estDansPlageHoraire(LocalTime heure, PlageDisponibilite plage) {
        PlageDisponibilite effective = plage != null ? plage : new PlageDisponibilite(LocalTime.MIDNIGHT, LocalTime.of(23, 59, 59));
        if (heure == null) {
            return false;
        }
        if (!effective.heureDebut.isAfter(effective.heureFin)) {
            return !heure.isBefore(effective.heureDebut) && !heure.isAfter(effective.heureFin);
        }
        return !heure.isBefore(effective.heureDebut) || !heure.isAfter(effective.heureFin);
    }

    private Reservation choisirReservationLaPlusProcheCapacite(List<Reservation> group,
            Map<Reservation, Integer> remainingDemand, int capaciteRestante) {
        Reservation best = null;
        int bestCategorie = Integer.MAX_VALUE; // 0: tient dans la capacite, 1: depasse la capacite
        int bestEcart = Integer.MAX_VALUE;
        int bestRemaining = -1;

        for (Reservation r : group) {
            if (r == null) {
                continue;
            }

            int demand = remainingDemand.getOrDefault(r, 0);
            if (demand <= 0) {
                continue;
            }

            int categorie = demand <= capaciteRestante ? 0 : 1;
            int ecart = Math.abs(capaciteRestante - demand);

            boolean isBetter = false;
            if (categorie < bestCategorie) {
                isBetter = true;
            } else if (categorie == bestCategorie && ecart < bestEcart) {
                isBetter = true;
            } else if (categorie == bestCategorie && ecart == bestEcart && demand > bestRemaining) {
                isBetter = true;
            } else if (categorie == bestCategorie && ecart == bestEcart && demand == bestRemaining && best != null) {
                String idR = r.getIdReservation() != null ? r.getIdReservation() : "";
                String idBest = best.getIdReservation() != null ? best.getIdReservation() : "";
                if (idR.compareToIgnoreCase(idBest) < 0) {
                    isBetter = true;
                }
            }

            if (isBetter) {
                best = r;
                bestCategorie = categorie;
                bestEcart = ecart;
                bestRemaining = demand;
            }
        }

        return best;
    }

    private void sauvegarderAssignations(Connection conn, List<Reservation> reservations,
            Map<String, List<AffectationVehicule>> assignmentChunksByReservation,
            Map<String, Timestamp> departureTimes, Map<String, Timestamp> arrivalTimes,
            Map<String, Integer> unassignedPassengers) throws Exception {
        if (reservations == null || reservations.isEmpty()) {
            return;
        }

        Set<String> reservationIds = new HashSet<>();
        for (Reservation r : reservations) {
            if (r != null && r.getIdReservation() != null && !r.getIdReservation().trim().isEmpty()) {
                reservationIds.add(r.getIdReservation());
            }
        }
        if (reservationIds.isEmpty()) {
            return;
        }

        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            String deleteSql = "DELETE FROM assignation_reservation WHERE id_reservation = ?";
            try (PreparedStatement psDelete = conn.prepareStatement(deleteSql)) {
                for (String idReservation : reservationIds) {
                    psDelete.setString(1, idReservation);
                    psDelete.addBatch();
                }
                psDelete.executeBatch();
            }

            String insertSql = "INSERT INTO assignation_reservation "
                    + "(id_reservation, id_vehicule, passagers_assignes, passagers_non_assignes, heure_depart, heure_retour, date_calcul) "
                    + "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                for (Reservation r : reservations) {
                    if (r == null || r.getIdReservation() == null) {
                        continue;
                    }

                    String idReservation = r.getIdReservation();
                    List<AffectationVehicule> chunks = assignmentChunksByReservation != null
                            ? assignmentChunksByReservation.get(idReservation)
                            : null;
                    Timestamp dep = departureTimes != null ? departureTimes.get(idReservation) : null;
                    Timestamp arr = arrivalTimes != null ? arrivalTimes.get(idReservation) : null;
                    int nonAssignes = unassignedPassengers != null
                            ? unassignedPassengers.getOrDefault(idReservation, 0)
                            : 0;

                    if (chunks != null && !chunks.isEmpty()) {
                        for (int i = 0; i < chunks.size(); i++) {
                            AffectationVehicule chunk = chunks.get(i);
                            Timestamp depChunk = chunk.heureDepart != null ? chunk.heureDepart : dep;
                            Timestamp arrChunk = chunk.heureRetour != null ? chunk.heureRetour : arr;
                            psInsert.setString(1, idReservation);
                            psInsert.setString(2, chunk.vehicule != null ? chunk.vehicule.getIdVehicule() : null);
                            psInsert.setInt(3, chunk.passagers);
                            psInsert.setInt(4, i == 0 ? nonAssignes : 0);
                            psInsert.setTimestamp(5, depChunk);
                            psInsert.setTimestamp(6, arrChunk);
                            psInsert.addBatch();
                        }
                    } else {
                        int nonAssignesLigne = nonAssignes > 0
                                ? nonAssignes
                                : (r.getNbrPassager() != null ? r.getNbrPassager() : 0);
                        psInsert.setString(1, idReservation);
                        psInsert.setString(2, null);
                        psInsert.setInt(3, 0);
                        psInsert.setInt(4, nonAssignesLigne);
                        psInsert.setTimestamp(5, dep);
                        psInsert.setTimestamp(6, arr);
                        psInsert.addBatch();
                    }
                }
                psInsert.executeBatch();
            }

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    private static class TrajetExecutionAggregate {
        private final Date dateJour;
        private final String idVehicule;
        private final Timestamp heureDepart;
        private final Timestamp heureRetour;
        private final Set<String> reservationIds = new LinkedHashSet<>();
        private int totalPassagers = 0;

        private TrajetExecutionAggregate(Date dateJour, String idVehicule, Timestamp heureDepart, Timestamp heureRetour) {
            this.dateJour = dateJour;
            this.idVehicule = idVehicule;
            this.heureDepart = heureDepart;
            this.heureRetour = heureRetour;
        }

        private void addReservation(String idReservation, int passagers) {
            reservationIds.add(idReservation);
            totalPassagers += Math.max(passagers, 0);
        }
    }

    private void sauvegarderTrajetsEtCompteurs(Connection conn, List<Reservation> reservations,
            Map<String, List<AffectationVehicule>> assignmentChunksByReservation,
            Map<String, Timestamp> departureTimes, Map<String, Timestamp> arrivalTimes) throws Exception {
        if (reservations == null || reservations.isEmpty()) {
            return;
        }

        Map<String, TrajetExecutionAggregate> trajets = new LinkedHashMap<>();
        Set<Date> datesConcernees = new HashSet<>();

        for (Reservation r : reservations) {
            if (r == null || r.getIdReservation() == null) {
                continue;
            }

            String idReservation = r.getIdReservation();
            Timestamp dep = departureTimes != null ? departureTimes.get(idReservation) : null;
            Timestamp arr = arrivalTimes != null ? arrivalTimes.get(idReservation) : null;
            List<AffectationVehicule> chunks = assignmentChunksByReservation != null
                    ? assignmentChunksByReservation.get(idReservation)
                    : null;

            if (dep == null || arr == null || chunks == null || chunks.isEmpty()) {
                continue;
            }

            Date dateJour = new Date(dep.getTime());
            datesConcernees.add(dateJour);

            for (AffectationVehicule chunk : chunks) {
                if (chunk == null || chunk.vehicule == null || chunk.vehicule.getIdVehicule() == null || chunk.passagers <= 0) {
                    continue;
                }

                Timestamp depChunk = chunk.heureDepart != null ? chunk.heureDepart : dep;
                Timestamp arrChunk = chunk.heureRetour != null ? chunk.heureRetour : arr;
                if (depChunk == null || arrChunk == null) {
                    continue;
                }

                Date dateJourChunk = new Date(depChunk.getTime());
                datesConcernees.add(dateJourChunk);

                String idVehicule = chunk.vehicule.getIdVehicule();
                String key = idVehicule + "|" + depChunk.getTime() + "|" + arrChunk.getTime();
                TrajetExecutionAggregate agg = trajets.get(key);
                if (agg == null) {
                    agg = new TrajetExecutionAggregate(dateJourChunk, idVehicule, depChunk, arrChunk);
                    trajets.put(key, agg);
                }
                agg.addReservation(idReservation, chunk.passagers);
            }
        }

        if (datesConcernees.isEmpty()) {
            return;
        }

        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement psDeleteTrajets = conn.prepareStatement("DELETE FROM trajet_execute WHERE date_jour = ?")) {
                for (Date dateJour : datesConcernees) {
                    psDeleteTrajets.setDate(1, dateJour);
                    psDeleteTrajets.addBatch();
                }
                psDeleteTrajets.executeBatch();
            }

            try (PreparedStatement psDeleteCompteurs = conn.prepareStatement("DELETE FROM vehicule_nombre_trajets WHERE date_jour = ?")) {
                for (Date dateJour : datesConcernees) {
                    psDeleteCompteurs.setDate(1, dateJour);
                    psDeleteCompteurs.addBatch();
                }
                psDeleteCompteurs.executeBatch();
            }

            Map<String, Integer> compteurParVehiculeJour = new HashMap<>();
            String insertTrajetSql = "INSERT INTO trajet_execute "
                    + "(date_jour, id_vehicule, heure_depart, heure_retour, nb_reservations, total_passagers, reservations_detail, date_calcul) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement psInsertTrajet = conn.prepareStatement(insertTrajetSql)) {
                for (TrajetExecutionAggregate agg : trajets.values()) {
                    psInsertTrajet.setDate(1, agg.dateJour);
                    psInsertTrajet.setString(2, agg.idVehicule);
                    psInsertTrajet.setTimestamp(3, agg.heureDepart);
                    psInsertTrajet.setTimestamp(4, agg.heureRetour);
                    psInsertTrajet.setInt(5, agg.reservationIds.size());
                    psInsertTrajet.setInt(6, agg.totalPassagers);
                    psInsertTrajet.setString(7, String.join(",", agg.reservationIds));
                    psInsertTrajet.addBatch();

                    String keyCompteur = agg.dateJour.toString() + "|" + agg.idVehicule;
                    compteurParVehiculeJour.put(keyCompteur, compteurParVehiculeJour.getOrDefault(keyCompteur, 0) + 1);
                }
                psInsertTrajet.executeBatch();
            }

            String insertCompteurSql = "INSERT INTO vehicule_nombre_trajets "
                    + "(date_jour, id_vehicule, nombre_trajets, date_calcul) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement psInsertCompteur = conn.prepareStatement(insertCompteurSql)) {
                for (Map.Entry<String, Integer> e : compteurParVehiculeJour.entrySet()) {
                    String[] parts = e.getKey().split("\\|", 2);
                    psInsertCompteur.setDate(1, Date.valueOf(parts[0]));
                    psInsertCompteur.setString(2, parts[1]);
                    psInsertCompteur.setInt(3, e.getValue());
                    psInsertCompteur.addBatch();
                }
                psInsertCompteur.executeBatch();
            }

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    private static class AffectationVehicule {
        private final Vehicule vehicule;
        private final Reservation reservation;
        private final String idReservation;
        private final int passagers;
        private Timestamp heureDepart;
        private Timestamp heureRetour;

        private AffectationVehicule(Vehicule vehicule, int passagers) {
            this(vehicule, null, passagers, null, null);
        }

        private AffectationVehicule(Vehicule vehicule, Reservation reservation, int passagers,
                Timestamp heureDepart, Timestamp heureRetour) {
            this.vehicule = vehicule;
            this.reservation = reservation;
            this.idReservation = reservation != null ? reservation.getIdReservation() : null;
            this.passagers = passagers;
            this.heureDepart = heureDepart;
            this.heureRetour = heureRetour;
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
            Vehicule fit = trouverMeilleurVehiculePourGroupe(reservation, available, simulatedCapacity, nextFreeTime,
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

    private Map<Vehicule, Timestamp> initialiserProchaineDisponibilite(List<Vehicule> vehicules,
            Map<String, PlageDisponibilite> disponibilitesVehicules,
            java.time.LocalDate dateReference) {
        Map<Vehicule, Timestamp> nextFreeTime = new HashMap<>();
        for (Vehicule v : vehicules) {
            LocalTime heureDebut = LocalTime.MIDNIGHT;
            if (v != null && disponibilitesVehicules != null) {
                PlageDisponibilite plage = disponibilitesVehicules.get(v.getIdVehicule());
                if (plage != null && plage.heureDebut != null) {
                    heureDebut = plage.heureDebut;
                }
            }
            nextFreeTime.put(v, Timestamp.valueOf(dateReference.atTime(heureDebut)));
        }
        return nextFreeTime;
    }

    private Map<String, PlageDisponibilite> chargerDisponibilites(Connection conn, List<Vehicule> vehicules) throws Exception {
        Map<String, PlageDisponibilite> disponibilites = new HashMap<>();
        String sql = "SELECT id_vehicule, heure_debut, heure_fin FROM disponibilite_vehicule";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Time heureDebut = rs.getTime("heure_debut");
                Time heureFin = rs.getTime("heure_fin");
                disponibilites.put(rs.getString("id_vehicule"), new PlageDisponibilite(
                        heureDebut != null ? heureDebut.toLocalTime() : LocalTime.MIDNIGHT,
                        heureFin != null ? heureFin.toLocalTime() : LocalTime.of(23, 59, 59)));
            }
        }

        if (vehicules != null) {
            for (Vehicule v : vehicules) {
                if (v != null && v.getIdVehicule() != null && !disponibilites.containsKey(v.getIdVehicule())) {
                    disponibilites.put(v.getIdVehicule(), new PlageDisponibilite(LocalTime.MIDNIGHT, LocalTime.of(23, 59, 59)));
                }
            }
        }
        return disponibilites;
    }

    private Timestamp debutJourMaintenant() {
        LocalDateTime now = LocalDateTime.now();
        return Timestamp.valueOf(now.toLocalDate().atStartOfDay());
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
            Timestamp freeTime = nextFreeTime.getOrDefault(v, debutJourMaintenant());

            // Si le vehicule peut accueillir la reservation et est libre
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
            Timestamp freeTime = nextFreeTime.getOrDefault(v, debutJourMaintenant());
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

    private void calculerHoraires(List<Reservation> reservations, Parametre param, Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix, Map<String, Vehicule> assignments,
            Map<String, Timestamp> plannedDepartureTimes,
            Map<String, Timestamp> departureTimes, Map<String, Timestamp> arrivalTimes) {
        if (param == null || param.getVitesseMoyenne() == null
                || param.getVitesseMoyenne().compareTo(BigDecimal.ZERO) <= 0)
            return;

        // Grouper par vehicule et heure pour calculer les tournees
        Map<Vehicule, Map<Timestamp, List<Reservation>>> tournees = new HashMap<>();
        for (Reservation r : reservations) {
            Vehicule v = assignments.get(r.getIdReservation());
            Timestamp plannedDeparture = plannedDepartureTimes.get(r.getIdReservation());
            if (v != null && plannedDeparture != null) {
                tournees.computeIfAbsent(v, k -> new HashMap<>())
                        .computeIfAbsent(plannedDeparture, k -> new ArrayList<>())
                        .add(r);
            }
        }

        for (Map.Entry<Vehicule, Map<Timestamp, List<Reservation>>> vEntry : tournees.entrySet()) {
            for (Map.Entry<Timestamp, List<Reservation>> tEntry : vEntry.getValue().entrySet()) {
                List<Reservation> tour = tEntry.getValue();
                Timestamp time = tEntry.getKey();

                // Calcul de la tournee optimisee (Greedy TSP eartir de l'aeroport LIEU001)
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
                        // Pas de distance trouvee pour le reste, on sort
                        break;
                    }
                }

                // Retour e'aeroport
                Distance retour = getDistance(distanceMatrix, currentLieu, "LIEU001");
                if (retour != null) {
                    totalDistance = totalDistance.add(retour.getKilometre());
                }

                // Temps total de trajet en ms
                BigDecimal travelTimeHours = totalDistance.divide(param.getVitesseMoyenne(), 4, RoundingMode.HALF_UP);
                long travelTimeMs = (long) (travelTimeHours.doubleValue() * 3600000L);

                // Appliquer les horaires eoutes les reservations de la tournee
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
        // Tenter l'inverse si non trouve
        if (matrix.containsKey(to)) {
            return matrix.get(to).get(from);
        }
        return null;
    }

    private Map<String, List<Reservation>> calculerOrdreTournees(Map<String, List<Reservation>> slotToResa,
            Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix) {

        Map<String, List<Reservation>> tourOrders = new LinkedHashMap<>();
        if (slotToResa == null || slotToResa.isEmpty()) {
            return tourOrders;
        }

        for (Map.Entry<String, List<Reservation>> entry : slotToResa.entrySet()) {
            List<Reservation> tour = entry.getValue();
            if (tour == null || tour.isEmpty()) {
                continue;
            }

            List<Reservation> orderedTour = calculerOrdreOptimal(tour, hotelMap, distanceMatrix);
            tourOrders.put(entry.getKey(), orderedTour);
        }

        return tourOrders;
    }

    private Map<String, Map<String, java.sql.Timestamp>> calculerHorairesDetailles(
            Map<String, List<Reservation>> tourOrders,
            Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix,
            Parametre param) {

        Map<String, Map<String, java.sql.Timestamp>> detailedTimes = new LinkedHashMap<>();
        if (tourOrders == null || tourOrders.isEmpty()) {
            return detailedTimes;
        }

        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Map.Entry<String, List<Reservation>> entry : tourOrders.entrySet()) {
            List<Reservation> tour = entry.getValue();
            if (tour == null || tour.isEmpty()) {
                continue;
            }

            Timestamp depTime = extraireHeureDepartDepuisCle(entry.getKey(), df);
            if (depTime == null) {
                continue;
            }

            Map<String, java.sql.Timestamp> times = calculerHeuresTournee(tour, hotelMap, distanceMatrix, param, depTime);
            detailedTimes.put(entry.getKey(), times);
        }

        return detailedTimes;
    }

    private Map<String, java.sql.Timestamp> calculerHeuresTournee(List<Reservation> tour, 
            Map<String, Hotel> hotelMap, Map<String, Map<String, Distance>> distanceMatrix, Parametre param, Timestamp startTime) {
        
        Map<String, java.sql.Timestamp> times = new HashMap<>();
        
        // Calculer l'ordre de la tournee et les heures
        List<Reservation> orderedTour = calculerOrdreOptimal(tour, hotelMap, distanceMatrix);
        calculerHeuresSegments(orderedTour, hotelMap, distanceMatrix, param, startTime, times);
        calculerHeureRetour(orderedTour, hotelMap, distanceMatrix, param, times);
        
        return times;
    }

    private Map<String, BigDecimal> calculerDistancesTournees(Map<String, List<Reservation>> slotToResa,
            Map<String, Hotel> hotelMap,
            Map<String, Map<String, Distance>> distanceMatrix,
            Map<String, List<Reservation>> tourOrders) {
        Map<String, BigDecimal> distances = new LinkedHashMap<>();
        if (slotToResa == null || slotToResa.isEmpty()) {
            return distances;
        }

        for (Map.Entry<String, List<Reservation>> entry : slotToResa.entrySet()) {
            String key = entry.getKey();
            List<Reservation> orderedTour = tourOrders != null && tourOrders.containsKey(key)
                    ? tourOrders.get(key)
                    : calculerOrdreOptimal(entry.getValue(), hotelMap, distanceMatrix);
            distances.put(key, calculerDistanceParcours(orderedTour, hotelMap, distanceMatrix));
        }

        return distances;
    }

    private Timestamp extraireHeureDepartDepuisCle(String key, java.text.SimpleDateFormat df) {
        if (key == null || df == null) {
            return null;
        }

        try {
            String[] parts = key.split("\\|", 3);
            if (parts.length < 2) {
                return null;
            }
            java.util.Date parsed = df.parse(parts[1]);
            if (parsed == null) {
                return null;
            }
            return new Timestamp(parsed.getTime());
        } catch (Exception ignore) {
            return null;
        }
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
                // Cas oee depart et l'arrivee sont identiques (ex: Hotel 1 -> Hotel 1)
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
            // Heure de depart vers cet hel
            times.put(nextResa.getIdReservation() + "_departure", currentTime);
            
            // Calculer le temps de trajet jusqu'eet hel
            Hotel h = hotelMap.get(nextResa.getIdHotel());
            if (h != null && param != null && param.getVitesseMoyenne() != null) {
                Distance d = getDistance(distanceMatrix, currentLieu, h.getIdLieu());
                if (d != null) {
                    BigDecimal travelTimeHours = d.getKilometre().divide(param.getVitesseMoyenne(), 4, RoundingMode.HALF_UP);
                    long travelTimeMs = (long) (travelTimeHours.doubleValue() * 3600000L);
                    currentTime = new Timestamp(currentTime.getTime() + travelTimeMs);
                }
            }
            
            // Heure d'arrivee eet hel
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
        
        // Heure de depart du dernier hel
        Timestamp currentTime = times.get(lastResa.getIdReservation() + "_arrival");
        times.put("return_departure", currentTime);
        
        // Calculer le temps de retour e'aeroport
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

