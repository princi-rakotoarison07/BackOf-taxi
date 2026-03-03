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
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

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
            List<Reservation> reservations = Reservation.getAll(Reservation.class, conn);
            List<Vehicule> vehicules = Vehicule.getAll(Vehicule.class, conn);
            List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);

            List<Reservation> filtered = new ArrayList<>();
            if (date != null && !date.isEmpty()) {
                Timestamp start = Timestamp.valueOf(date + " 00:00:00");
                Timestamp end = Timestamp.valueOf(date + " 23:59:59");
                for (Reservation r : reservations) {
                    if (r.getDateResa() != null && !r.getDateResa().before(start) && !r.getDateResa().after(end)) {
                        filtered.add(r);
                    }
                }
            } else {
                filtered = reservations;
            }

            Map<String, TypeCarburant> typeById = new HashMap<>();
            for (TypeCarburant t : types) {
                typeById.put(t.getIdTypeCarburant(), t);
            }

            Map<String, Vehicule> assignments = new HashMap<>();
            List<Vehicule> available = new ArrayList<>(vehicules);
            filtered.sort((a, b) -> b.getNbrPassager().compareTo(a.getNbrPassager()));
            for (Reservation r : filtered) {
                Vehicule best = null;
                int bestScoreDiesel = -1;
                int bestDiff = Integer.MAX_VALUE;
                for (Vehicule v : available) {
                    if (v.getNbrPlace() != null && r.getNbrPassager() != null
                            && v.getNbrPlace() >= r.getNbrPassager()) {
                        TypeCarburant t = typeById.get(v.getIdTypeCarburant());
                        int dieselScore = 0;
                        if (t != null && t.getCode() != null && t.getCode().equalsIgnoreCase("D")) {
                            dieselScore = 1;
                        }
                        int diff = v.getNbrPlace() - r.getNbrPassager();
                        if (diff < bestDiff || (diff == bestDiff && dieselScore > bestScoreDiesel)) {
                            best = v;
                            bestScoreDiesel = dieselScore;
                            bestDiff = diff;
                        }
                    }
                }
                if (best != null) {
                    assignments.put(r.getIdReservation(), best);
                    available.remove(best);
                }
            }

            mv.addObject("reservations", filtered);
            mv.addObject("types", types);
            mv.addObject("assignments", assignments);
            mv.addObject("selectedDate", date);
        }
        return mv;
    }
}
