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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.taxi.model.Hotel;
import com.taxi.model.Distance;
import com.taxi.model.Parametre;

@Controller
@RestController
public class VehiculeController {

    private static class PlageDisponibilite {
        private final LocalTime heureDebut;
        private final LocalTime heureFin;

        private PlageDisponibilite(LocalTime heureDebut, LocalTime heureFin) {
            this.heureDebut = heureDebut;
            this.heureFin = heureFin;
        }
    }

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
            Map<String, PlageDisponibilite> disponibilites = chargerDisponibilites(conn, vehicules);
            mv.addObject("vehicules", vehicules);
            mv.addObject("types", types);
            mv.addObject("disponibilites", formaterPlagesPourAffichage(disponibilites));
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
            PlageDisponibilite plage = chargerDisponibiliteVehicule(conn, id);
            mv.addObject("vehicule", v);
            mv.addObject("types", types);
            mv.addObject("vehiculeHeureDebut", formaterHeure(plage != null ? plage.heureDebut : LocalTime.MIDNIGHT));
            mv.addObject("vehiculeHeureFin", formaterHeure(plage != null ? plage.heureFin : LocalTime.of(23, 59)));
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
            Map<String, PlageDisponibilite> disponibilites = chargerDisponibilites(conn, vehicules);

            List<Reservation> filtered = filtrerReservations(reservations, date);

            Map<String, TypeCarburant> typeById = construireMapType(types);
            Map<String, Hotel> hotelMap = construireMapHotel(hotels);
            Map<String, Map<String, Distance>> distanceMatrix = construireMatriceDistance(distances);

            // Calculer les assignations pour toute la journée pour connaître les périodes
            // d'occupation
            Map<Vehicule, List<Tournee>> planning = calculerPlanningJournée(filtered, vehicules, typeById, hotelMap,
                    distanceMatrix, currentParam, disponibilites);

            Timestamp targetTime = null;
            if (date != null && !date.isEmpty() && time != null && !time.isEmpty()) {
                targetTime = Timestamp.valueOf(date + " " + time + ":00");
            } else if (date != null && !date.isEmpty()) {
                targetTime = Timestamp.valueOf(date + " 00:00:00");
            }

            List<Vehicule> disponibles = new ArrayList<>();
            for (Vehicule v : vehicules) {
                boolean isBusy = false;
                PlageDisponibilite plage = disponibilites.get(v.getIdVehicule());
                if (targetTime != null && !estDansPlageHoraire(targetTime.toLocalDateTime().toLocalTime(), plage)) {
                    isBusy = true;
                }
                List<Tournee> tournees = planning.get(v);
                if (!isBusy && tournees != null) {
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
            mv.addObject("disponibilites", formaterPlagesPourAffichage(disponibilites));
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
            Map<String, Map<String, Distance>> distanceMatrix, Parametre param,
            Map<String, PlageDisponibilite> disponibilites) {
        Map<Vehicule, List<Tournee>> planning = new HashMap<>();
        int waitMinutes = getTempsAttenteMinutes(param);
        List<List<Reservation>> waitWindows = construireFenetresAttente(reservations, waitMinutes);
        // Le planning est calcule par jour: au debut, tous les vehicules ont 0 trajet.
        Map<Vehicule, Integer> dailyTripCount = initialiserCompteurTrajets(vehicules);

        LocalDateTime baseDateTime = reservations != null && !reservations.isEmpty() && reservations.get(0).getDateResa() != null
            ? reservations.get(0).getDateResa().toLocalDateTime()
            : LocalDateTime.now();
        Map<Vehicule, Timestamp> nextFreeTime = initialiserProchaineDisponibilite(vehicules, disponibilites,
            baseDateTime.toLocalDate());

        for (List<Reservation> group : waitWindows) {
            Timestamp departureTime = resoudreHeureDepart(group, waitMinutes);
            if (departureTime == null)
                continue;

            group.sort((a, b) -> comparerReservationsIntraGroupe(a, b, hotelMap, distanceMatrix));

            Map<Reservation, Integer> remainingDemand = new HashMap<>();
            for (Reservation r : group) {
                remainingDemand.put(r, r.getNbrPassager() != null ? r.getNbrPassager() : 0);
            }

            Map<Vehicule, List<Reservation>> assignedToVehicule = new HashMap<>();

            Timestamp currentWaveTime = departureTime;
            while (aEncoreDesPassagers(remainingDemand)) {
                List<Vehicule> vehiculesDuGroupe = trierVehiculesDisponiblesPourGroupe(vehicules, nextFreeTime,
                        currentWaveTime, dailyTripCount, typeById, disponibilites);

                if (vehiculesDuGroupe.isEmpty()) {
                    Timestamp nextTime = trouverProchaineDisponibilite(nextFreeTime, currentWaveTime);
                    if (nextTime == null) {
                        break;
                    }
                    currentWaveTime = nextTime;
                    continue;
                }

                Map<Vehicule, Integer> remainingCapacityByVehicule = new HashMap<>();
                for (Vehicule v : vehiculesDuGroupe) {
                    remainingCapacityByVehicule.put(v, v.getNbrPlace() != null ? v.getNbrPlace() : 0);
                }

                Map<Vehicule, List<Reservation>> tourDeVague = new HashMap<>();
                while (true) {
                    AffectationCandidate best = choisirMeilleureAffectation(group, vehiculesDuGroupe, remainingDemand,
                            remainingCapacityByVehicule, dailyTripCount, typeById);
                    if (best == null) {
                        break;
                    }

                    remainingDemand.put(best.reservation,
                            remainingDemand.getOrDefault(best.reservation, 0) - best.passagers);
                    remainingCapacityByVehicule.put(best.vehicule,
                            remainingCapacityByVehicule.getOrDefault(best.vehicule, 0) - best.passagers);

                    List<Reservation> tour = tourDeVague.computeIfAbsent(best.vehicule, k -> new ArrayList<>());
                    if (!tour.contains(best.reservation)) {
                        tour.add(best.reservation);
                    }

                    List<Reservation> tourGlobal = assignedToVehicule.computeIfAbsent(best.vehicule, k -> new ArrayList<>());
                    if (!tourGlobal.contains(best.reservation)) {
                        tourGlobal.add(best.reservation);
                    }
                }

                if (tourDeVague.isEmpty()) {
                    Timestamp nextTime = trouverProchaineDisponibilite(nextFreeTime, currentWaveTime);
                    if (nextTime == null) {
                        break;
                    }
                    currentWaveTime = nextTime;
                    continue;
                }

                Timestamp prochainTemps = null;
                for (Map.Entry<Vehicule, List<Reservation>> entry : tourDeVague.entrySet()) {
                    Vehicule v = entry.getKey();
                    List<Reservation> tourResas = entry.getValue();
                    long durationMs = calculerDureeTournee(tourResas, hotelMap, distanceMatrix, param);
                    Timestamp endTime = new Timestamp(currentWaveTime.getTime() + durationMs);
                    nextFreeTime.put(v, endTime);
                    dailyTripCount.put(v, dailyTripCount.getOrDefault(v, 0) + 1);
                    planning.computeIfAbsent(v, k -> new ArrayList<>()).add(new Tournee(currentWaveTime, endTime));
                    if (prochainTemps == null || endTime.before(prochainTemps)) {
                        prochainTemps = endTime;
                    }
                }

                if (!aEncoreDesPassagers(remainingDemand)) {
                    break;
                }

                if (prochainTemps == null) {
                    break;
                }
                currentWaveTime = prochainTemps;
            }
        }
        return planning;
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
        private final int tripCount;
        private final int fuelPriority;

        private AffectationCandidate(Vehicule vehicule, Reservation reservation, int passagers,
                int categorie, int ecart, int tripCount, int fuelPriority) {
            this.vehicule = vehicule;
            this.reservation = reservation;
            this.passagers = passagers;
            this.categorie = categorie;
            this.ecart = ecart;
            this.tripCount = tripCount;
            this.fuelPriority = fuelPriority;
        }
    }

    private AffectationCandidate choisirMeilleureAffectation(List<Reservation> group, List<Vehicule> vehiculesDisponibles,
            Map<Reservation, Integer> remainingDemand, Map<Vehicule, Integer> remainingCapacityByVehicule,
            Map<Vehicule, Integer> dailyTripCount, Map<String, TypeCarburant> typeById) {
        AffectationCandidate best = null;

        for (Vehicule v : vehiculesDisponibles) {
            int cap = remainingCapacityByVehicule.getOrDefault(v, 0);
            if (cap <= 0) {
                continue;
            }

            int trip = dailyTripCount.getOrDefault(v, 0);
            int fuel = getPrioriteCarburant(typeById.get(v.getIdTypeCarburant()));

            for (Reservation r : group) {
                int demand = remainingDemand.getOrDefault(r, 0);
                if (demand <= 0) {
                    continue;
                }

                int affectes = Math.min(cap, demand);
                int categorie = demand <= cap ? 0 : 1;
                int ecart = Math.abs(cap - demand);

                AffectationCandidate candidate = new AffectationCandidate(v, r, affectes, categorie, ecart, trip, fuel);
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

        if (candidate.categorie != current.categorie) {
            return candidate.categorie < current.categorie;
        }
        if (candidate.ecart != current.ecart) {
            return candidate.ecart < current.ecart;
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

    private List<Vehicule> trierVehiculesDisponiblesPourGroupe(List<Vehicule> available,
            Map<Vehicule, Timestamp> nextFreeTime, Timestamp currentTime,
            Map<Vehicule, Integer> dailyTripCount, Map<String, TypeCarburant> typeById,
            Map<String, PlageDisponibilite> disponibilites) {
        List<Vehicule> vehicules = new ArrayList<>();
        LocalTime heureCourante = currentTime.toLocalDateTime().toLocalTime();
        for (Vehicule v : available) {
            Timestamp freeTime = nextFreeTime.getOrDefault(v, debutJourMaintenant());
            PlageDisponibilite plage = disponibilites != null ? disponibilites.get(v.getIdVehicule()) : null;
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

    private Reservation choisirReservationLaPlusProcheCapacite(List<Reservation> group,
            Map<Reservation, Integer> remainingDemand, int capaciteRestante) {
        Reservation best = null;
        int bestCategorie = Integer.MAX_VALUE;
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

    private Map<Vehicule, Timestamp> initialiserProchaineDisponibilite(List<Vehicule> vehicules,
            Map<String, PlageDisponibilite> disponibilites, java.time.LocalDate dateReference) {
        Map<Vehicule, Timestamp> nextFreeTime = new HashMap<>();
        for (Vehicule v : vehicules) {
            PlageDisponibilite plage = disponibilites != null ? disponibilites.get(v.getIdVehicule()) : null;
            LocalTime debut = plage != null ? plage.heureDebut : LocalTime.MIDNIGHT;
            nextFreeTime.put(v, Timestamp.valueOf(dateReference.atTime(debut)));
        }
        return nextFreeTime;
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
            Timestamp freeTime = nextFreeTime.getOrDefault(v, debutJourMaintenant());

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

    private Timestamp debutJourMaintenant() {
        LocalDateTime now = LocalDateTime.now();
        return Timestamp.valueOf(now.toLocalDate().atStartOfDay());
    }

    private LocalTime parseHeureInput(String heureInput) {
        if (heureInput == null || heureInput.trim().isEmpty()) {
            return null;
        }
        String normalized = heureInput.trim();
        if (normalized.length() == 5) {
            normalized += ":00";
        }
        return LocalTime.parse(normalized);
    }

    private PlageDisponibilite chargerDisponibiliteVehicule(Connection conn, String idVehicule) throws Exception {
        String sql = "SELECT heure_debut, heure_fin FROM disponibilite_vehicule WHERE id_vehicule = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idVehicule);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Time debut = rs.getTime("heure_debut");
                    Time fin = rs.getTime("heure_fin");
                    return new PlageDisponibilite(
                            debut != null ? debut.toLocalTime() : LocalTime.MIDNIGHT,
                            fin != null ? fin.toLocalTime() : LocalTime.of(23, 59, 59));
                }
            }
        }
        return null;
    }

    private Map<String, PlageDisponibilite> chargerDisponibilites(Connection conn, List<Vehicule> vehicules) throws Exception {
        Map<String, PlageDisponibilite> map = new HashMap<>();
        String sql = "SELECT id_vehicule, heure_debut, heure_fin FROM disponibilite_vehicule";
        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Time debut = rs.getTime("heure_debut");
                Time fin = rs.getTime("heure_fin");
                map.put(rs.getString("id_vehicule"), new PlageDisponibilite(
                        debut != null ? debut.toLocalTime() : LocalTime.MIDNIGHT,
                        fin != null ? fin.toLocalTime() : LocalTime.of(23, 59, 59)));
            }
        }

        if (vehicules != null) {
            for (Vehicule v : vehicules) {
                if (v != null && v.getIdVehicule() != null && !map.containsKey(v.getIdVehicule())) {
                    map.put(v.getIdVehicule(), new PlageDisponibilite(LocalTime.MIDNIGHT, LocalTime.of(23, 59, 59)));
                }
            }
        }
        return map;
    }

    private void upsertDisponibiliteVehicule(Connection conn, String idVehicule, LocalTime heureDebut, LocalTime heureFin) throws Exception {
        String sql = "INSERT INTO disponibilite_vehicule (id_vehicule, heure_debut, heure_fin, date_maj) "
                + "VALUES (?, ?, ?, CURRENT_TIMESTAMP) "
                + "ON CONFLICT (id_vehicule) DO UPDATE "
                + "SET heure_debut = EXCLUDED.heure_debut, heure_fin = EXCLUDED.heure_fin, date_maj = CURRENT_TIMESTAMP";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idVehicule);
            ps.setTime(2, Time.valueOf(heureDebut));
            ps.setTime(3, Time.valueOf(heureFin));
            ps.executeUpdate();
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

    private String formaterHeure(LocalTime heure) {
        if (heure == null) {
            return "";
        }
        String raw = heure.toString();
        return raw.length() >= 5 ? raw.substring(0, 5) : raw;
    }

    private Map<String, String> formaterPlagesPourAffichage(Map<String, PlageDisponibilite> disponibilites) {
        Map<String, String> affichage = new HashMap<>();
        if (disponibilites == null) {
            return affichage;
        }
        for (Map.Entry<String, PlageDisponibilite> e : disponibilites.entrySet()) {
            LocalTime debut = e.getValue() != null ? e.getValue().heureDebut : LocalTime.MIDNIGHT;
            LocalTime fin = e.getValue() != null ? e.getValue().heureFin : LocalTime.of(23, 59, 59);
            affichage.put(e.getKey(), formaterHeure(debut) + " - " + formaterHeure(fin));
        }
        return affichage;
    }

    private Set<String> parseVehiculeIds(String vehiculeIdsInput) {
        Set<String> ids = new LinkedHashSet<>();
        if (vehiculeIdsInput == null || vehiculeIdsInput.trim().isEmpty()) {
            return ids;
        }
        String[] parts = vehiculeIdsInput.split("[,;\\s]+");
        for (String part : parts) {
            if (part != null) {
                String id = part.trim();
                if (!id.isEmpty()) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    @PostMapping("/BackOf-taxi/vehicule/save")
    public ModelAndView save(@ModelAttribute Vehicule vehicule,
            @Param("heureDebutInput") String heureDebutInput,
            @Param("heureFinInput") String heureFinInput) {
        ModelAndView mv = new ModelAndView("/views/vehicule/form.jsp");
        try (Connection conn = DBConnection.getConnection()) {
            LocalTime heureDebut = parseHeureInput(heureDebutInput);
            LocalTime heureFin = parseHeureInput(heureFinInput);
            vehicule.insert(conn);
            upsertDisponibiliteVehicule(conn, vehicule.getIdVehicule(),
                    heureDebut != null ? heureDebut : LocalTime.MIDNIGHT,
                    heureFin != null ? heureFin : LocalTime.of(23, 59, 59));
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
    public ModelAndView update(@ModelAttribute Vehicule vehicule,
            @Param("heureDebutInput") String heureDebutInput,
            @Param("heureFinInput") String heureFinInput) {
        ModelAndView mv = new ModelAndView("/views/vehicule/form.jsp");
        try (Connection conn = DBConnection.getConnection()) {
            LocalTime heureDebut = parseHeureInput(heureDebutInput);
            LocalTime heureFin = parseHeureInput(heureFinInput);
            vehicule.update(conn);
            PlageDisponibilite existante = chargerDisponibiliteVehicule(conn, vehicule.getIdVehicule());
            LocalTime heureDebutFinale = heureDebut;
            LocalTime heureFinFinale = heureFin;
            if (heureDebutFinale == null) {
                heureDebutFinale = existante != null ? existante.heureDebut : LocalTime.MIDNIGHT;
            }
            if (heureFinFinale == null) {
                heureFinFinale = existante != null ? existante.heureFin : LocalTime.of(23, 59, 59);
            }
            upsertDisponibiliteVehicule(conn, vehicule.getIdVehicule(), heureDebutFinale, heureFinFinale);
            mv.addObject("successMessage", "Véhicule mis à jour avec succès !");
            mv.addObject("vehicule", vehicule);
            mv.addObject("vehiculeHeureDebut", formaterHeure(heureDebutFinale));
            mv.addObject("vehiculeHeureFin", formaterHeure(heureFinFinale));

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

    @PostMapping("/BackOf-taxi/vehicule/disponibilite/update-group")
    public ModelAndView updateDisponibiliteGroupe(@Param("vehiculeIds") String vehiculeIdsInput,
            @Param("heureDebutInput") String heureDebutInput,
            @Param("heureFinInput") String heureFinInput) {
        ModelAndView mv = new ModelAndView("/BackOf-taxi/vehicule/list");
        mv.setRedirect(true);

        LocalTime heureDebut = parseHeureInput(heureDebutInput);
        LocalTime heureFin = parseHeureInput(heureFinInput);
        LocalTime heureDebutFinale = heureDebut != null ? heureDebut : LocalTime.MIDNIGHT;
        LocalTime heureFinFinale = heureFin != null ? heureFin : LocalTime.of(23, 59, 59);

        Set<String> ids = parseVehiculeIds(vehiculeIdsInput);
        if (ids.isEmpty()) {
            return mv;
        }

        try (Connection conn = DBConnection.getConnection()) {
            for (String idVehicule : ids) {
                upsertDisponibiliteVehicule(conn, idVehicule, heureDebutFinale, heureFinFinale);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
