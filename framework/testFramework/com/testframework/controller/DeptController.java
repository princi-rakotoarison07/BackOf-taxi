package testFramework.com.testframework.controller;

import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.utilitaire.ModelAndView;
import testFramework.com.testframework.model.Departement;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DeptController {

    @GetMapping("/departements")
    public ModelAndView listDepartements() {
        List<Departement> depts = new ArrayList<>();
        depts.add(new Departement(1, "Informatique", "Département des sciences informatiques"));
        depts.add(new Departement(2, "Mathematiques", "Département de mathématiques"));
        depts.add(new Departement(3, "Physique", "Département de physique"));

        ModelAndView mv = new ModelAndView("/departements.jsp");
        mv.addObject("departements", depts);
        return mv;
    }
}
