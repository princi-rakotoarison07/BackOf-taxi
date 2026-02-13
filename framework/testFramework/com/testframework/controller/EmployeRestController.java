package testFramework.com.testframework.controller;

import framework.annotation.RestController;
import framework.annotation.GetMapping;
import framework.annotation.PostMapping;
import framework.annotation.ModelAttribute;
import jakarta.servlet.http.HttpServletRequest;
import testFramework.com.testframework.model.Employe;
import testFramework.com.testframework.model.Departement;
import testFramework.com.testframework.model.Ville;

import java.util.HashMap;
import java.util.Map;

@RestController
public class EmployeRestController {

    // GET /api/employe/sample -> return a simple sample employee as JSON
    @GetMapping("/api/employe/sample")
    public Employe sample() {
        Employe emp = new Employe();
        emp.setNom("Doe");
        emp.setPrenom("John");
        emp.setAge(28);
        emp.setPoste("Développeur");

        Departement d = new Departement();
        d.setId(1);
        d.setNom("Informatique");
        d.setDescription("IT Department");

        Ville v = new Ville();
        v.setId(10);
        v.setNom("Paris");
        v.setCodePostal("75000");
        d.setVille(v);

        emp.setDept(d);
        return emp;
    }


    @GetMapping("/api/employe")
    public Employe getEmploye(@ModelAttribute Employe emp) {
        return emp;
    }


    @GetMapping("/api/employe/{id}")
    public Map<String, Object> getById(HttpServletRequest request) {
        String id = (String) request.getAttribute("id");
        Map<String, Object> res = new HashMap<>();
        res.put("id", id);
        res.put("status", "ok");
        return res;
    }

  
    @PostMapping("/api/employe")
    public Employe create(@ModelAttribute Employe emp) {
        
        return emp;
    }
}
