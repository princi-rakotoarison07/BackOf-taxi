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
            Map<String, Distance> distanceMap = construireMapDistance(distances);

            Map<String, Vehicule> assignments = assignerVehicules(filtered, vehicules, typeById);
            Map<String, Timestamp> departureTimes = new HashMap<>();
            Map<String, Timestamp> arrivalTimes = new HashMap<>();

            calculerHoraires(filtered, currentParam, hotelMap, distanceMap, departureTimes, arrivalTimes);

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

    private Map<String, Distance> construireMapDistance(List<Distance> distances) {
        Map<String, Distance> map = new HashMap<>();
        for (Distance d : distances) {
            if ("LIEU001".equals(d.getLieuFrom()))
                map.put(d.getLieuTo(), d);
        }
        return map;
    }

    private Map<String, Vehicule> assignerVehicules(List<Reservation> reservations, List<Vehicule> vehicules,
            Map<String, TypeCarburant> typeById) {
        Map<String, Vehicule> assignments = new HashMap<>();
        List<Vehicule> available = new ArrayList<>(vehicules);
        reservations.sort((a, b) -> b.getNbrPassager().compareTo(a.getNbrPassager()));

        for (Reservation r : reservations) {
            Vehicule best = trouverMeilleurVehicule(r, available, typeById);
            if (best != null) {
                assignments.put(r.getIdReservation(), best);
                available.remove(best);
            }
        }
        return assignments;
    }

    private Vehicule trouverMeilleurVehicule(Reservation r, List<Vehicule> available,
            Map<String, TypeCarburant> typeById) {
        Vehicule best = null;
        int bestScoreDiesel = -1;
        int bestDiff = Integer.MAX_VALUE;

        for (Vehicule v : available) {
            if (v.getNbrPlace() != null && r.getNbrPassager() != null && v.getNbrPlace() >= r.getNbrPassager()) {
                TypeCarburant t = typeById.get(v.getIdTypeCarburant());
                int dieselScore = (t != null && t.getCode() != null && t.getCode().equalsIgnoreCase("D")) ? 1 : 0;
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
            Map<String, Distance> distanceMap, Map<String, Timestamp> departureTimes,
            Map<String, Timestamp> arrivalTimes) {
        if (param == null || param.getVitesseMoyenne() == null
                || param.getVitesseMoyenne().compareTo(BigDecimal.ZERO) <= 0)
            return;

        for (Reservation r : reservations) {
            if (r.getDateResa() == null)
                continue;
            Hotel h = hotelMap.get(r.getIdHotel());
            if (h == null)
                continue;
            Distance d = distanceMap.get(h.getIdLieu());
            if (d == null)
                continue;

            BigDecimal travelTimeHours = d.getKilometre().divide(param.getVitesseMoyenne(), 4, RoundingMode.HALF_UP);
            long travelTimeMs = (long) (travelTimeHours.doubleValue() * 3600000L);

            departureTimes.put(r.getIdReservation(), r.getDateResa());
            arrivalTimes.put(r.getIdReservation(), new Timestamp(r.getDateResa().getTime() + (2 * travelTimeMs)));
        }
    }
}
