# BackOf-taxi (Portail Central & Back-Office)

Ce projet est le point d'entrée central du système **inDrive** et son Back-Office de gestion de taxi, développé avec un **Framework Java Web personnalisé**.

## 🚀 Fonctionnalités

- **Portail Central (Redirection)** : Accès direct via `http://localhost:8080/` avec redirection automatique vers le portail de sélection.
- **Interface de Sélection (Home)** : Une page d'accueil moderne permettant de choisir entre le Back-Office et le Front-Office.
- **Gestion des Réservations** : Saisie multiple des réservations avec calendrier dynamique.
- **Assignation Intelligente** : Algorithme d'assignation des véhicules aux réservations selon la capacité et le carburant.
- **Gestion des Véhicules** : Liste, ajout, modification et suppression des véhicules.
- **APIs REST** : Exposition des données (Hôtels, Réservations, Carburants) au format JSON.

## 🎨 Identité Visuelle

L'interface a été refondue pour adopter le style **inDrive** :
- **Couleurs** : Noir, Blanc et Vert Fluo (`#c1f11d`).
- **Style** : Minimaliste, contrasté et ergonomique.

## 🛠️ Configuration

Le fichier `src/main/resources/config.properties` doit être configuré localement :

```properties
base.package=com.taxi
db.url=jdbc:postgresql://localhost:5432/taxi_transfer
db.user=votre_utilisateur
db.password=votre_mot_de_passe
db.driver=org.postgresql.Driver
```

- **Base de données** : PostgreSQL.
- **Serveur** : Jetty 11 (compatible Jakarta EE).

## 🏃 Démarrage

```bash
mvn jetty:run
```

L'application est disponible sur : [http://localhost:8080/](http://localhost:8080/)
*(Redirige vers [http://localhost:8080/BackOf-taxi/](http://localhost:8080/BackOf-taxi/))*

### URLs du Back-Office :

- **Accueil Portail** : `/BackOf-taxi/`
- **Formulaire Réservation** : `/BackOf-taxi/reservation/form`
- **Liste Véhicules** : `/BackOf-taxi/vehicule/list`
- **Paramètres** : `/BackOf-taxi/parametre/form`

## 📦 Structure

- `src/main/java/com/taxi/controller` : Contrôleurs gérant les routes préfixées `/BackOf-taxi/`.
- `src/main/webapp/views` : Pages JSP utilisant le layout unifié.
- `modules/framework.jar` : Framework Java Web personnalisé.
