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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ModelAndView disponible(@Param("date") String date) throws Exception {
        ModelAndView mv = new ModelAndView("/views/vehicule/disponible.jsp");
        mv.addObject("pageTitle", "Véhicules Disponibles");
        try (Connection conn = DBConnection.getConnection()) {
            List<Vehicule> vehicules = Vehicule.getAll(Vehicule.class, conn);
            List<TypeCarburant> types = TypeCarburant.getAll(TypeCarburant.class, conn);
            List<Reservation> reservations = Reservation.getAll(Reservation.class, conn);

            List<Reservation> filtered = new ArrayList<>();
            if (date != null && !date.isEmpty()) {
                Timestamp start = Timestamp.valueOf(date + " 00:00:00");
                Timestamp end = Timestamp.valueOf(date + " 23:59:59");
                for (Reservation r : reservations) {
                    if (r.getDateResa() != null && !r.getDateResa().before(start) && !r.getDateResa().after(end)) {
                        filtered.add(r);
                    }
                }
            }

            Map<String, TypeCarburant> typeById = new HashMap<>();
            for (TypeCarburant t : types) {
                typeById.put(t.getIdTypeCarburant(), t);
            }

            // Calculer les assignations pour savoir quels véhicules sont occupés
            List<Vehicule> busyVehicules = new ArrayList<>();
            List<Vehicule> pool = new ArrayList<>(vehicules);
            filtered.sort((a, b) -> b.getNbrPassager().compareTo(a.getNbrPassager()));
            
            for (Reservation r : filtered) {
                Vehicule best = null;
                int bestScoreDiesel = -1;
                int bestDiff = Integer.MAX_VALUE;
                for (Vehicule v : pool) {
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
                    busyVehicules.add(best);
                    pool.remove(best);
                }
            }

            // Les véhicules disponibles sont ceux qui ne sont pas occupés
            List<Vehicule> disponibles = new ArrayList<>();
            for (Vehicule v : vehicules) {
                boolean isBusy = false;
                for (Vehicule busy : busyVehicules) {
                    if (busy.getIdVehicule().equals(v.getIdVehicule())) {
                        isBusy = true;
                        break;
                    }
                }
                if (!isBusy) {
                    disponibles.add(v);
                }
            }

            mv.addObject("disponibles", disponibles);
            mv.addObject("types", types);
            mv.addObject("selectedDate", date);
        }
        return mv;
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
