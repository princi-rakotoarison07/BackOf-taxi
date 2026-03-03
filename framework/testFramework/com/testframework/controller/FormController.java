package testFramework.com.testframework.controller;

import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.utilitaire.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class FormController {

    // Affiche le formulaire
    @GetMapping("/formDept")
    public ModelAndView showForm() {
        return new ModelAndView("/formDept.jsp");
    }

    // Reçoit les données (GET pour simplicité). On pourrait faire POST si on ajoute une annotation.
    @GetMapping("/submitDept")
    public String submitDept(HttpServletRequest request) {
        String id = request.getParameter("id");
        String nom = request.getParameter("nom");
        String description = request.getParameter("description");

        System.out.println("[FormController] Données reçues -> id=" + id + ", nom=" + nom + ", description=" + description);

        return "Formulaire reçu. Consultez le terminal Tomcat pour voir les valeurs.";
    }
}
