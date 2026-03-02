package com.taxi.controller;

import com.taxi.model.Parametre;
import com.taxi.util.DBConnection;
import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.annotation.PostMapping;
import framework.annotation.ModelAttribute;
import framework.annotation.Param;
import framework.utilitaire.ModelAndView;
import framework.annotation.RestController;
import java.sql.Connection;
import java.util.List;

@Controller
@RestController
public class ParametreController {

    @GetMapping("/api/parametres")
    public List<Parametre> listParametres() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            return Parametre.getAll(Parametre.class, conn);
        }
    }

    @GetMapping("/parametre/form")
    public ModelAndView showForm() {
        ModelAndView mv = new ModelAndView("/views/parametre/form.jsp");
        mv.addObject("pageTitle", "Paramètres");
        return mv;
    }

    @PostMapping("/parametre/insert")
    public ModelAndView insert(@ModelAttribute Parametre parametre) {
        ModelAndView mv = new ModelAndView("/views/parametre/form.jsp");
        mv.addObject("pageTitle", "Paramètres");
        try (Connection conn = DBConnection.getConnection()) {
            // Générer un ID automatiquement si non fourni
            if (parametre.getIdParametre() == null || parametre.getIdParametre().isEmpty()) {
                parametre.setIdParametre("PAR" + System.currentTimeMillis());
            }

            // Validation des valeurs
            if (parametre.getVitesseMoyenne() == null
                    || parametre.getVitesseMoyenne().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                mv.addObject("error", "La vitesse moyenne doit être supérieure à 0 ." + parametre.getVitesseMoyenne()
                        + parametre.getTempsAttente());
                return mv;
            }

            if (parametre.getTempsAttente() == null || parametre.getTempsAttente() <= 0) {
                mv.addObject("error", "Le temps d'attente doit être supérieur à 0");
                return mv;
            }

            parametre.insert(conn);
            mv.addObject("success", "Paramètre enregistré avec succès !");

            // Réinitialiser le formulaire
            mv.addObject("parametre", new Parametre());

        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("error", "Erreur lors de l'enregistrement : " + e.getMessage());
        }

        return mv;
    }

    @GetMapping("/parametre/delete")
    public ModelAndView delete(@Param("id") String id) {
        ModelAndView mv = new ModelAndView("/views/parametre/list.jsp");
        try (Connection conn = DBConnection.getConnection()) {
            Parametre.deleteById(Parametre.class, id, conn);
            List<Parametre> parametres = Parametre.getAll(Parametre.class, conn);
            mv.addObject("parametres", parametres);
            mv.addObject("successMessage", "Paramètre supprimé avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("errorMessage", "Erreur lors de la suppression : " + e.getMessage());
            try (Connection conn2 = DBConnection.getConnection()) {
                List<Parametre> parametres = Parametre.getAll(Parametre.class, conn2);
                mv.addObject("parametres", parametres);
            } catch (Exception ignore) {
            }
        }
        return mv;
    }

    @GetMapping("/parametre/list")
    public ModelAndView list() {
        ModelAndView mv = new ModelAndView("/views/parametre/list.jsp");
        mv.addObject("pageTitle", "Liste des Paramètres");

        try (Connection conn = DBConnection.getConnection()) {
            List<Parametre> parametres = Parametre.getAll(Parametre.class, conn);
            mv.addObject("parametres", parametres);
        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("errorMessage", "Erreur lors du chargement des paramètres : " + e.getMessage());
        }

        return mv;
    }
}
