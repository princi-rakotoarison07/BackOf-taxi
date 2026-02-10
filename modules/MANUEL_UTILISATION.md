# Manuel d'utilisation du Framework Java Web

Ce framework est une solution légère pour construire des applications web en Java, inspirée par le pattern MVC. Il utilise des annotations pour simplifier la gestion des routes, des paramètres et de la sécurité.

## Table des Matières
1. [Configuration](#configuration)
2. [Contrôleurs](#contrôleurs)
3. [Routage](#routage)
4. [Gestion des Paramètres](#gestion-des-paramètres)
5. [Upload de Fichiers](#upload-de-fichiers)
6. [Gestion de la Session](#gestion-de-la-session)
7. [Sécurité et Autorisation](#sécurité-et-autorisation)
8. [Vues et Réponses](#vues-et-réponses)
9. [Persistance et Insertion (ORM)](#persistance-et-insertion-orm)
10. [Support JSON (Request Body)](#support-json-request-body)

---

## Configuration

### 1. Fichier `web.xml`
Vous devez configurer la `FrontServlet` comme dispatcher principal et le `ResourceFilter` pour gérer les fichiers statiques.

```xml
<filter>
    <filter-name>ResourceFilter</filter-name>
    <filter-class>framework.utilitaire.ResourceFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>ResourceFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>

<servlet>
    <servlet-name>FrontServlet</servlet-name>
    <servlet-class>framework.servlet.FrontServlet</servlet-class>
    <multipart-config>
        <location></location>
        <max-file-size>-1</max-file-size>
        <max-request-size>-1</max-request-size>
        <file-size-threshold>0</file-size-threshold>
    </multipart-config>
</servlet>
<servlet-mapping>
    <servlet-name>FrontServlet</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
```

### 2. Fichier `config.properties`
Créez un fichier `config.properties` dans votre dossier `resources` pour spécifier le package de base à scanner.

```properties
base.package=com.votreprojet.controller
```

---

## Contrôleurs

Un contrôleur est une classe Java annotée avec `@Controller` ou `@RestController`.

- `@Controller` : Pour les méthodes retournant des vues (JSP).
- `@RestController` : Pour les méthodes retournant des données (JSON).

```java
@Controller
public class MonController {
    // ...
}
```

---

## Routage

Utilisez les annotations `@GetMapping` et `@PostMapping` sur les méthodes de vos contrôleurs.

```java
@GetMapping("/accueil")
public ModelAndView index() {
    return new ModelAndView("index.jsp");
}

@PostMapping("/valider")
public ModelAndView submit() {
    // ...
}
```

---

## Gestion des Paramètres

### 1. Paramètres simples (`@Param`)
Utilisez `@Param` pour lier un paramètre de requête HTTP à un argument de méthode.

```java
@GetMapping("/details")
public ModelAndView details(@Param("id") Integer id) {
    // ...
}
```

### 2. Objets (`@ModelAttribute`)
Utilisez `@ModelAttribute` pour lier automatiquement les paramètres de formulaire aux champs d'un objet.

```java
@PostMapping("/save")
public ModelAndView save(@ModelAttribute Client client) {
    // Les champs du formulaire (ex: nom, email) seront injectés dans l'objet client
}
```

---

## Upload de Fichiers

Le framework gère l'upload de fichiers via la classe `UploadedFile`.

```java
@PostMapping("/upload")
public ModelAndView handleUpload(@Param("photo") UploadedFile file) {
    byte[] content = file.getBytes();
    String fileName = file.getOriginalFilename();
    // Sauvegardez le fichier...
}
```

---

## Gestion de la Session

Vous pouvez accéder à la session en injectant un `Map` (ou `SessionMap`) annoté avec `@Session` dans les paramètres de votre méthode.

```java
@PostMapping("/login")
public String doLogin(@Session Map<String, Object> session) {
    session.put("user", "admin");
    return "success.jsp";
}
```

*Note : Le framework injecte automatiquement une instance de `SessionMap` qui encapsule la session HTTP.*

---

## Sécurité et Autorisation

Le framework propose une gestion simple des accès via `@Authorized` et `@Role`. Ces annotations peuvent être placées sur la **classe** (s'applique à toutes les méthodes) ou sur une **méthode** spécifique.

- `@Authorized` : Restreint l'accès aux utilisateurs connectés.
- `@Role({"ADMIN", "USER"})` : Restreint l'accès à un ou plusieurs rôles spécifiques.

```java
@Authorized
@Controller
public class AdminController {

    @Role("ADMIN")
    @GetMapping("/admin/dashboard")
    public ModelAndView dashboard() {
        // ...
    }
}
```

*Note : Configurez les noms des attributs de session dans `web.xml` via les paramètres de contexte `auth.session.attribute` (défaut: "user") et `role.session.attribute` (défaut: "role").*

---

## Injection de Dépendances Native

En plus des paramètres annotés, vous pouvez injecter directement les objets suivants dans vos méthodes de contrôleur :
- `HttpServletRequest`
- `HttpServletResponse`

```java
@GetMapping("/test")
public void test(HttpServletRequest request, HttpServletResponse response) {
    // Utilisation directe des objets Jakarta Servlet
}
```

---

## Vues et Réponses

### 1. ModelAndView
Utilisé pour renvoyer une JSP avec des données.

```java
ModelAndView mv = new ModelAndView("profil.jsp");
mv.addObject("nom", "Jean");
return mv;
```

### 2. JSON (`@RestController`)
Si le contrôleur est annoté avec `@RestController`, les objets retournés seront automatiquement convertis en JSON.

```java
@RestController
public class ApiController {
    @GetMapping("/api/data")
    public List<String> getData() {
        return Arrays.asList("A", "B", "C");
    }
}
```

---

## Exemple Complet

```java
package com.test.controller;

import framework.annotation.*;
import framework.utilitaire.ModelAndView;

@Controller
public class HelloController {

    @GetMapping("/hello")
    public ModelAndView sayHello(@Param("nom") String name) {
        ModelAndView mv = new ModelAndView("hello.jsp");
        mv.addObject("message", "Bonjour " + name);
        return mv;
    }
}

---

## Persistance et Insertion (ORM)

Le framework propose une couche de persistance simplifiée de type ORM pour automatiser les insertions SQL sans écrire de requêtes manuelles.

### 1. Annotations de Mapping
- `@Table(name = "nom_table")` : (Optionnel) Définit le nom de la table en base. Par défaut, utilise le nom de la classe.
- `@Column(name = "nom_colonne")` : (Optionnel) Définit le nom de la colonne. Par défaut, utilise le nom du champ.

> **Important** : Pour les colonnes auto-incrémentées (comme `SERIAL` en PostgreSQL), ne mettez pas l'annotation `@Column`. Cela permettra au framework d'ignorer ces champs lors de l'insertion et de laisser la base de données générer la valeur.

### 2. Utilisation du modèle
Pour bénéficier de l'insertion automatique, vos classes de domaine doivent hériter de la classe `framework.utilitaire.Model`.

```java
@Table(name = "employes")
public class Employe extends Model {
    
    @Column(name = "nom")
    private String nom;
    
    private Integer age; // Sera mappé sur la colonne "age" par défaut
    
    // Getters et Setters obligatoires
}
```

### 3. Exécuter une insertion
Il suffit d'appeler la méthode `.insert(Connection conn)` sur votre objet.

```java
public void sauvegarder(Employe emp, Connection conn) throws Exception {
    emp.insert(conn); 
    // Génère automatiquement : INSERT INTO employes (nom, age) VALUES (?, ?)
}
```

### 4. Exemple d'intégration avec Spring Boot
Bien que le framework soit autonome, il peut être utilisé au sein d'un projet Spring Boot pour sa couche ORM simplifiée.

#### Insertion de données
```java
@PostMapping("/save")
@ResponseBody
public Map<String, Object> save(@ModelAttribute MonModele obj) {
    try (Connection conn = dataSource.getConnection()) {
        obj.insert(conn); // Utilisation de l'ORM du framework
        return Map.of("status", "success");
    } catch (Exception e) {
        return Map.of("status", "error", "message", e.getMessage());
    }
}
```

#### Liste des données (API JSON)
Pour retourner une liste d'objets au format JSON :
```java
@GetMapping("/reservation")
@ResponseBody
public List<Reservation> listReservations() {
    List<Reservation> reservations = new ArrayList<>();
    try (Connection conn = dataSource.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM reservation")) {
        while (rs.next()) {
            Reservation r = new Reservation();
            // Mapping manuel (ou via réflexion utilisant les annotations @Column du framework)
            r.setIdReservation(rs.getInt("id_reservation"));
            // ...
            reservations.add(r);
        }
    } catch (Exception e) { e.printStackTrace(); }
    return reservations;
}
```

---

## Support JSON (Request Body)

Le framework permet de recevoir des données au format JSON depuis le front-end et de les transformer automatiquement en objets Java.

### Utilisation de `@RequestBody`
Pour récupérer un objet envoyé en JSON (via `fetch` ou `axios` par exemple), utilisez l'annotation `@RequestBody` dans les paramètres de votre contrôleur.

```java
@RestController
public class MonApiController {

    @PostMapping("/api/save")
    public String save(@RequestBody MonModele obj, Connection conn) throws Exception {
        // Le JSON est déjà converti en objet 'obj'
        obj.insert(conn); 
        return "{\"status\": \"success\"}";
    }
}
```

> **Note** : Le parseur JSON interne est conçu pour des objets simples (plats). Pour des structures très complexes, il est recommandé d'ajouter une bibliothèque comme Gson ou Jackson dans votre dossier `lib`.
```
