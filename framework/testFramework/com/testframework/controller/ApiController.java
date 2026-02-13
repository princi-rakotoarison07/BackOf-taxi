package testFramework.com.testframework.controller;

import framework.annotation.PostMapping;
import framework.annotation.RequestBody;
import framework.annotation.RestController;
import testFramework.com.testframework.model.Employe;
import java.sql.Connection;
import java.sql.DriverManager;

@RestController
public class ApiController {

    @PostMapping("/api/employes")
    public String saveEmploye(@RequestBody Employe emp) {
        System.out.println("JSON reçu et converti en objet Employe : " + emp.getNom());
        
        // Simulation d'insertion en base
        // try (Connection conn = ...) {
        //     emp.insert(conn);
        //     return "{\"status\": \"success\", \"message\": \"Employé inséré\"}";
        // } catch (Exception e) {
        //     return "{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}";
        // }
        
        return "{\"status\": \"success\", \"message\": \"Objet " + emp.getNom() + " reçu via JSON\"}";
    }
}
