# PharmaX — Application Desktop Pharmacie E-Commerce

PharmaX est une application desktop Java/JavaFX complète pour la gestion d'une pharmacie en ligne. Elle intègre un front-office e-commerce pour les clients, un back-office d'administration, un blog de santé, un chatbot IA, et un système de paiement multi-passerelles, le tout dans une seule application unifiée.

---

## Table des Matieres

- [Apercu General](#apercu-general)
- [Stack Technologique](#stack-technologique)
- [Architecture du Projet](#architecture-du-projet)
- [Modules Principaux](#modules-principaux)
- [Authentification et Securite](#authentification-et-securite)
- [Prerequis et Installation](#prerequis-et-installation)
- [Configuration](#configuration)
- [Lancement de l'Application](#lancement-de-lapplication)
- [Structure des Fichiers](#structure-des-fichiers)

---

## Apercu General

PharmaX est une application de gestion pharmaceutique destinee a deux types d'utilisateurs :

- **Client (Front Office)** : Parcourir le catalogue de produits, ajouter au panier, passer des commandes, payer en ligne, consulter les factures, suivre les livraisons, deposer des reclamations, et lire des articles de sante.
- **Administrateur (Back Office)** : Gerer les produits et categories, traiter les commandes, repondre aux reclamations, publier des articles, moderer les commentaires, et consulter les statistiques.

L'application inclut egalement un chatbot IA base sur un LLM (OpenRouter) pour l'assistance client, ainsi qu'un systeme de scraping web qui recupere automatiquement des articles de sante.

---

## Stack Technologique

| Couche | Technologies |
|--------|-------------|
| **Interface Utilisateur** | JavaFX 20.0.2, FXML, CSS |
| **Base de Donnees** | MySQL 8.0.28 |
| **Authentification** | Google OAuth2, Google Authenticator (TOTP 2FA), Reconnaissance Faciale (OpenCV/JavaCV), BCrypt, reCAPTCHA |
| **Paiement** | Stripe API, Flouc Payment Gateway |
| **IA et Chatbot** | OpenRouter LLM, Google Gemini API |
| **Email** | Mailjet API, JavaMail (SMTP) |
| **PDF et Export** | iTextPDF 5.5.13, Apache POI 5.2.3 (Excel) |
| **Web Scraping** | Jsoup 1.17.2 |
| **QR Code** | ZXing |
| **Build** | Maven 3.x, Java 17 |
| **Logging** | Logback, SLF4J |

---

## Architecture du Projet

L'application suit une architecture **MVC (Model-View-Controller)** organisee en couches :

```
src/main/java/
├── org/example/
│   └── Main.java               Point d'entree principal (authentification + shell)
├── models/                     Modeles metier (User, Produit, Commandes, etc.)
├── services/                   Logique metier et acces aux donnees
├── controllers/
│   ├── frontoffice/            Controleurs cote client (catalogue, panier, commandes)
│   └── *.java                  Controleurs back-office et communs
├── Views/                      Vues JavaFX pour l'authentification
├── com/pharmax/
│   ├── controller/             Controleurs du module blog
│   ├── model/                  Modeles du blog (Article, Commentaire)
│   └── service/                Services du blog (AI, Scraping, Email)
└── utils/                      Utilitaires (connexion DB, helpers)

src/main/resources/
├── frontoffice/                Fichiers FXML + CSS du front-office
├── backoffice/                 Fichiers FXML du back-office admin
├── gestion/                    FXML gestion produits/categories
├── reclamation/                FXML gestion des reclamations
└── db.properties               Configuration de la base de donnees
```

---

## Modules Principaux

### 1. E-Commerce Front Office
- Catalogue de produits avec recherche et filtres
- Panier d'achat avec gestion des quantites
- Processus de commande complet (checkout)
- Generation de factures PDF (iTextPDF)
- Historique des commandes et suivi des livraisons
- Systeme de notifications pour le client

### 2. Gestion des Utilisateurs
- Inscription et connexion par email/mot de passe
- Connexion via Google OAuth2
- Authentification a deux facteurs (2FA) avec Google Authenticator (TOTP)
- Reconnaissance faciale pour la connexion (OpenCV)
- Hachage des mots de passe BCrypt
- Protection reCAPTCHA sur les formulaires

### 3. Administration Back Office
- Tableau de bord avec statistiques (`HomeAdminController`)
- CRUD complet pour les produits et categories
- Gestion et traitement des commandes
- Gestion des utilisateurs et des roles
- Suivi des livraisons
- Journalisation des actions administrateur

### 4. Blog de Sante
- Publication et gestion d'articles (brouillon / publie)
- Articles bilingues (francais / anglais)
- Commentaires avec systeme de moderation et filtrage IA
- Scraping automatique d'articles de sante via Jsoup
- Generation de contenu par IA (Google Gemini)
- Notifications par email aux abonnes (Mailjet)
- Systeme de likes

### 5. Paiement en Ligne
- Integration **Stripe** (cartes bancaires internationales)
- Integration **Flouc** (paiement local tunisien)
- Generation de liens de paiement securises
- Confirmation de commande post-paiement

### 6. Chatbot IA
- Interface de conversation en temps reel
- Moteur LLM via **OpenRouter**
- Consultation de la base medicamenteuse via **OpenFDA**
- Disponible depuis toutes les pages de l'application

### 7. Reclamations
- Depot de reclamation par le client
- Suivi de statut
- Reponse de l'administrateur
- Archivage des reclamations traitees

### 8. Notifications
- Notifications in-app pour les clients et l'admin
- Envoi d'emails automatiques (commandes, livraisons, blog)

---

## Authentification et Securite

Le flux d'authentification au demarrage :

```
Demarrage
   └─> Ecran de connexion (email + mot de passe)
          ├─> Google OAuth2 (connexion via Google)
          ├─> Reconnaissance faciale
          └─> Connexion classique
                 └─> 2FA activee ? → Verification TOTP (Google Authenticator)
                        ├─> Utilisateur ROLE_ADMIN → Back Office
                        └─> Utilisateur standard → Front Office
```

---

## Prerequis et Installation

### Prerequis
- **Java 17** (JDK 17+)
- **Maven 3.8+**
- **MySQL 8.0+**
- **JavaFX 20** (inclu dans les dependances Maven)
- (Optionnel) Webcam pour la reconnaissance faciale

### Installation de la Base de Donnees

1. Creer la base de donnees MySQL :
```sql
CREATE DATABASE pharmjava CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Importer le schema SQL si disponible (fichier `.sql` a la racine du projet).

3. Configurer la connexion dans [src/main/resources/db.properties](src/main/resources/db.properties).

### Compilation

```bash
mvn clean install -DskipTests
```

---

## Configuration

### Base de Donnees

Fichier : [src/main/resources/db.properties](src/main/resources/db.properties)

```properties
db.url=jdbc:mysql://localhost:3306/pharmjava
db.username=root
db.password=
```

### APIs Externes

Les cles API sont configurees dans les fichiers de service correspondants ou en variables d'environnement :

| Service | Fichier de configuration |
|---------|--------------------------|
| Google OAuth2 | `GoogleAuthService.java` |
| Stripe | `StripePaymentService.java` |
| Mailjet | `MailjetService.java` |
| OpenRouter (LLM) | `OpenRouterChatService.java` |
| Google Gemini | `GeminiService.java` |
| reCAPTCHA | `RecaptchaService.java` |

Pour la configuration Google OAuth2, consulter [GOOGLE_OAUTH_SETUP.md](GOOGLE_OAUTH_SETUP.md).

---

## Lancement de l'Application

### Via Maven

```bash
mvn javafx:run
```

### Via JAR

```bash
mvn package
java --module-path /chemin/vers/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar target/pharmax-integrated-1.0-SNAPSHOT.jar
```

### Point d'entree principal

La classe principale est `org.example.Main`. Elle initialise la base de donnees, affiche l'ecran de splash, puis charge l'interface de connexion.

---

## Structure des Fichiers

```
Integration Finale/
├── README.md
├── pom.xml
├── GOOGLE_OAUTH_SETUP.md
├── ARCHITECTURE_FRONTOFFICE.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── org/example/Main.java
│   │   │   ├── models/
│   │   │   │   ├── User.java
│   │   │   │   ├── Produit.java
│   │   │   │   ├── Commandes.java
│   │   │   │   ├── LigneCommandes.java
│   │   │   │   ├── Categorie.java
│   │   │   │   ├── Livraison.java
│   │   │   │   ├── Reclamation.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── PanierItem.java
│   │   │   │   └── Client.java
│   │   │   ├── services/
│   │   │   │   ├── ProduitService.java
│   │   │   │   ├── CommandeService.java
│   │   │   │   ├── ServiceUser.java
│   │   │   │   ├── FaceAuthService.java
│   │   │   │   ├── StripePaymentService.java
│   │   │   │   ├── OpenRouterChatService.java
│   │   │   │   └── ...
│   │   │   ├── controllers/
│   │   │   │   ├── frontoffice/
│   │   │   │   │   ├── CatalogueController.java
│   │   │   │   │   ├── PanierController.java
│   │   │   │   │   ├── HistoriqueController.java
│   │   │   │   │   └── FactureController.java
│   │   │   │   ├── HomeAdminController.java
│   │   │   │   ├── GestionProduitController.java
│   │   │   │   ├── CommandeController.java
│   │   │   │   └── ChatbotController.java
│   │   │   └── com/pharmax/
│   │   │       ├── model/Article.java
│   │   │       ├── controller/
│   │   │       └── service/
│   │   └── resources/
│   │       ├── db.properties
│   │       ├── frontoffice/
│   │       ├── backoffice/
│   │       ├── gestion/
│   │       └── reclamation/
│   └── test/
└── target/
```

---

## Auteurs

Projet de fin d'etudes (PIDEV) — Application d'integration finale.  
Equipe de developpement — Esprit School of Engineering.
