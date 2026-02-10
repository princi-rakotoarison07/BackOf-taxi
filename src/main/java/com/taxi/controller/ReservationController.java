package com.taxi.controller;

import com.taxi.model.Reservation;
import com.taxi.util.DBConnection;
import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.annotation.PostMapping;
import framework.annotation.ModelAttribute;
import framework.utilitaire.ModelAndView;
import framework.annotation.RestController;
import java.sql.Connection;
import java.util.List;
import java.sql.Timestamp;

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
}
