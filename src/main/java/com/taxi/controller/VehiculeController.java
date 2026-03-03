package com.taxi.controller;

import com.taxi.model.Vehicule;
import com.taxi.model.TypeCarburant;
import com.taxi.util.DBConnection;
import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.annotation.PostMapping;
import framework.annotation.Param;
import framework.annotation.ModelAttribute;
import framework.annotation.RestController;
import framework.utilitaire.ModelAndView;
import java.sql.Connection;
import java.util.List;

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

    // assignation supprimée, logique déplacée côté réservations

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
