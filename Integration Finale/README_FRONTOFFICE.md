# 🏥 PHARMAX - E-Pharmacy Desktop Application

Une application desktop JavaFX pour la vente en ligne de produits pharmaceutiques avec workflow complet de commande.

## ✨ Fonctionnalités

### 🛍️ Catalogue
- Affichage de tous les produits depuis la BD
- Chaque produit affiche: nom, prix, description
- Bouton "Ajouter au panier" avec gestion des doublons
- Design moderne style e-commerce

### 🛒 Panier
- Vue panier avec tableau éditable
- Quantité modifiable via Spinner
- Sous-total automatique par produit
- Bouton supprimer
- Total général en temps réel

### 📋 Formulaire Client
- Validation complète:
  - Nom et Prénom (obligatoires)
  - Email (format valide)
  - Téléphone (min 8 caractères)
  - Adresse (obligatoire)
- Messages d'erreur clairs
- Dialogue modal

### 💾 Enregistrement Commande
- Sauvegarde en BD:
  - Table `commandes`: produits, totales, statut, date
  - Table `lignes_commandes`: détails produits
- Génération facture automatique

### 📄 Facture
- Affichage formaté avec:
  - En-tête PHARMAX PHARMACY
  - Infos client
  - Tableau produits + quantités + prix
  - Total général
  - Message de remerciement

### 📜 Historique
- Vue des commandes passées
- Tableau avec: ID | Statut | Date | Total
- Selection → détails lignes de commande

## 🏗️ Architecture

### Technologies
- **JavaFX 20** - Interface graphique
- **MySQL** - Base de données
- **MVC** - Pattern architectural
- **Service Layer** - Logique métier
- **ObservableList** - Panier en temps réel

### Structure Fichiers
```
controllers/frontoffice/
├── FrontOfficeMainController.java    # Navigation
├── CatalogueController.java          # Produits
├── PanierController.java             # Panier + commande
└── HistoriqueController.java         # Historique

models/
├── PanierItem.java                   # Item panier (Properties JavaFX)
└── Client.java                       # Info client

services/
├── PanierService.java                # Gestion panier en mémoire
├── CommandeService.java              # Commandes BD + enregistrement
├── ProduitService.java               # Produits
└── LigneCommandeService.java          # Lignes commandes

resources/frontoffice/
├── main.fxml                         # Navigation
├── catalogue.fxml                    # Catalogue
├── panier.fxml                       # Panier
└── historique.fxml                   # Historique
```

## 🎯 Workflow Utilisateur

1. **Accueil** → Affichage catalogue
2. **Navigation** → Clic "🛍️ Catalogue"
3. **Ajouter produit** → Clic "Ajouter au panier"
4. **Consulter panier** → Clic "🛒 Panier"
5. **Modifier quantités** → Spinner dans le tableau
6. **Valider panier** → Clic "Valider"
7. **Remplir formulaire** → Infos client + validation
8. **Passer commande** → Clic "Passer la commande"
9. **Voir facture** → Affichage automatique
10. **Historique** → Clic "📜 Historique" pour voir anciennes commandes

## 🚀 Démarrage Rapide

### Prérequis
- Java 26+
- Maven
- MySQL (BD nommée `pharm`)
- Les classes modèles et services du back office

### Installation

1. **Base de données**
```sql
USE pharm;

-- Tables existantes
CREATE TABLE produits (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100),
    prix DOUBLE,
    description TEXT,
    stock INT
);

CREATE TABLE commandes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    produits VARCHAR(255),
    totales DOUBLE,
    statut VARCHAR(50),
    date DATE,
    utilisateur_id INT
);

CREATE TABLE lignes_commandes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100),
    prix DOUBLE,
    quantite INT,
    sous_total DOUBLE,
    commande_id INT,
    FOREIGN KEY (commande_id) REFERENCES commandes(id)
);
```

2. **Insérer données test**
```sql
INSERT INTO produits (nom, prix, description, stock) VALUES
('Aspirine 500mg', 5.99, 'Anti-douleur classique', 50),
('Vitamines C', 8.50, 'Vitamine C pour immunité', 30),
('Antibiotique A', 15.00, 'Antibiotique premium', 20);
```

3. **Compiler et exécuter**
```bash
mvn clean compile
java -m pidevjava/org.example.Main
```

## 📦 Composants Clés

### PanierItem
Model avec JavaFX Properties:
- `produitId`, `nom`, `prix`, `quantite`, `sousTotal`
- Sous-total auto-calculé: `quantite × prix`

### PanierService
Service singleton:
- `ajouterAuPanier()` - Gère doublons automatiquement
- `getTotal()` - Somme tous les sous-totaux
- `viderPanier()` - Réinitialise après commande

### CommandeService.enregistrerCommande()
1. Crée objet Commandes
2. INSERT dans table commandes
3. INSERT chaque ligne dans lignes_commandes

## 🎨 Design

### Couleurs
- **Vert #27ae60**: Boutons validés
- **Rouge #e74c3c**: Panier, totaux importants
- **Bleu #3498db**: Historique
- **Gris #2c3e50**: Barre navigation

### Composants
- BorderPane pour layout principal
- ScrollPane pour catalogue
- TableView pour listes (panier, historique)
- Spinner pour éditer quantités
- Dialogs pour formulaires

## ✅ Validations

### Formulaire Client
- Nom: obligatoire
- Prénom: obligatoire
- Email: format valide (contient @)
- Téléphone: min 8 caractères
- Adresse: obligatoire

### Panier
- Refuse commande si panier vide
- Quantité min 1, max 100

## 📊 Schéma BD

```
produits
├── id (PK)
├── nom
├── prix
├── description
└── stock

commandes
├── id (PK)
├── produits
├── totales
├── statut
├── date
└── utilisateur_id

lignes_commandes
├── id (PK)
├── nom
├── prix
├── quantite
├── sous_total
└── commande_id (FK)
```

## 🔒 Sécurité
- Utilise PreparedStatement (protection SQL injection)
- Validation côté client avant envoi
- Utilise Singleton pattern pour DB connection

## 🐛 Debugging
- Console affiche logs de BD
- Alert dialogs pour erreurs utilisateur
- StackTrace en console pour erreurs dev

## 📝 Notes de Développement

### Panier en Mémoire
- `ObservableList<PanierItem>` dans `PanierService`
- Synchronisation auto avec UI (binding)
- Vidé après chaque commande

### Facture
- Vue JavaFX (pas d'export PDF)
- Formatée avec monospace font
- Peut être imprimée via Ctrl+P

### Historique
- Affiche TOUTES les commandes
- Pas de filtrage par utilisateur (impl future)

## 🚀 Améliorations Futures
1. Login utilisateur (traçabilité commandes)
2. Images produits (base64 ou fichiers)
3. Export facture PDF
4. Filtrage historique par date
5. Recherche produits
6. Favoris / wishlist
7. Codes promo / remises
8. Pagination panier
9. Notifications commande
10. Chat support

## 📞 Support
Pour des questions, vérifiez:
- Console pour erreurs BD
- Logs stack trace
- Vérifiez BD `pharm` existe et accessible

---

**PHARMAX v1.0** - Made with ❤️ in JavaFX

