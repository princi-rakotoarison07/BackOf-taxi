# BackOf-taxi (Back-Office de Réservation)

Ce projet est le Back-Office du système de réservation de taxi, développé avec un **Framework Java Web personnalisé**.

## 🚀 Fonctionnalités

- **Formulaire de Réservation** : Saisie des réservations avec calendrier dynamique.
- **Gestion des Hôtels** : API REST pour récupérer la liste des hôtels.
- **API Réservations** : API REST exposant la liste des réservations au format JSON.
- **ORM Minimaliste** : Utilisation d'annotations (`@Table`, `@Column`) pour la persistance PostgreSQL.

## 🛠️ Configuration

Le fichier de configuration `src/main/resources/config.properties` est ignoré par Git pour des raisons de sécurité.

Veuillez créer ce fichier avec le contenu suivant :

```properties
base.package=com.taxi
db.url=jdbc:postgresql://localhost:5432/taxi_transfer
db.user=votre_utilisateur
db.password=votre_mot_de_passe
db.driver=org.postgresql.Driver
```

- **Base de données** : PostgreSQL (scripts disponibles dans `database/base.sql`).
- **Serveur** : Jetty 11 (compatible Jakarta EE).

## 🏃 Démarrage

Pour lancer le projet sans installation manuelle sur Tomcat :

```bash
mvn jetty:run
```

L'application sera disponible sur : [http://localhost:8080/BackOf-taxi](http://localhost:8080/BackOf-taxi)

### URLs utiles :

- Formulaire : `/reservation/form`
- API Hôtels : `/api/hotels`
- API Réservations : `/api/reservations`

## 📦 Structure

- `src/main/java/com/taxi/controller` : Contrôleurs du framework.
- `src/main/java/com/taxi/model` : Entités liées à la base de données.
- `modules/framework.jar` : Le framework personnalisé utilisé par le projet.
