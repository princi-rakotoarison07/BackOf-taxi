package testFramework.com.testframework;

import testFramework.com.testframework.model.Employe;
import java.sql.Connection;
import java.sql.DriverManager;

public class TestInsertion {
    public static void main(String[] args) {
        System.out.println("=== Test d'insertion généralisée ===");

        Employe emp = new Employe();
        emp.setNom("Rakoto");
        emp.setPrenom("Jean");
        emp.setAge(25);
        emp.setPoste("Développeur");

        System.out.println("Objet Employe créé : " + emp.getNom() + " " + emp.getPrenom());
        
        System.out.println("\nPour insérer cet employé, il suffit d'appeler :");
        System.out.println("emp.insert(connection);");

        // Note: Ce test nécessite une connexion JDBC valide pour s'exécuter réellement.
        // Exemple d'utilisation (commenté car pas de DB configurée) :
        /*
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/mabase", "user", "pass")) {
            emp.insert(conn);
            System.out.println("Insertion réussie !");
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        
        System.out.println("\nL'appel à emp.insert(conn) va générer et exécuter :");
        System.out.println("INSERT INTO employes (nom, prenom, age, poste) VALUES ('Rakoto', 'Jean', 25, 'Développeur')");
    }
}
