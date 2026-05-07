# PHARMAX - E-Pharmacy Desktop Application

## 📋 Architecture

### Structure du Projet
```
pidevjava/
├── src/main/java/
│   ├── org/example/
│   │   └── Main.java (Entry point - Front Office)
│   ├── controllers/
│   │   ├── frontoffice/
│   │   │   ├── FrontOfficeMainController.java (Navigation)
│   │   │   ├── CatalogueController.java (Affichage produits)
│   │   │   ├── PanierController.java (Gestion panier + commande)
│   │   │   └── HistoriqueController.java (Historique commandes)
│   │   └── ... (Back Office - inchangé)
│   ├── models/
│   │   ├── Produit.java (Produit e-commerce)
│   │   ├── Commandes.java (Commande)
│   │   ├── LigneCommandes.java (Ligne de commande)
│   │   ├── PanierItem.java (✨ Item panier avec propriétés JavaFX)
│   │   └── Client.java (✨ Infos client)
│   ├── services/
│   │   ├── ProduitService.java (CRUD produits)
│   │   ├── CommandeService.java (CRUD + enregistrerCommande)
│   │   ├── PanierService.java (✨ Gestion panier en mémoire)
│   │   ├── LigneCommandeService.java (CRUD lignes)
│   │   └── ... (autres services)
│   ├── utils/
│   │   └── MyConnection.java (Connexion MySQL)
│   └── resources/
│       └── frontoffice/
│           ├── main.fxml (Navigation principale)
│           ├── catalogue.fxml (Affichage produits)
│           ├── panier.fxml (Panier + validation)
│           └── historique.fxml (Historique commandes)
```

## 🎯 Workflow Utilisateur

### 1️⃣ Catalogue (Produits)
- Liste des produits avec:
  - Nom, Prix, Description
  - Image (placeholder)
  - Bouton "Ajouter au panier"
- Clic "Ajouter": 
  - Si produit existe → augmente quantité
  - Sinon → ajoute nouveau PanierItem

### 2️⃣ Panier
- Tableau éditable avec colonnes:
  - Produit | Prix | Quantité (Spinner) | Sous-total
- Boutons:
  - Supprimer (produit sélectionné)
  - Valider (ouvre formulaire client)
- Total général en temps réel

### 3️⃣ Formulaire Client
- Champs validés:
  - ✅ Nom (non vide)
  - ✅ Prénom (non vide)
  - ✅ Email (format valide)
  - ✅ Téléphone (min 8 caractères)
  - ✅ Adresse (non vide)
- Bouton "Passer la commande"

### 4️⃣ Enregistrement Commande
- Sauvegarde dans BD:
  - Table `commandes`: produits, totales, statut, date, utilisateur_id
  - Table `lignes_commandes`: nom, prix, quantite, sous_total, commande_id
- Affichage facture automatique
- Vidage du panier

### 5️⃣ Facture
- Vue détaillée avec:
  - Entête PHARMAX PHARMACY
  - Infos client
  - Tableau produits avec QTE/PRIX/TOTAL
  - Total général
  - Message de remerciement

### 6️⃣ Historique
- Tableau des commandes passées
- Selection → affiche détails lignes
- Colonnes: ID | Statut | Date | Total

## 🔧 Composants Clés

### PanierItem (Model)
```java
- produitId: IntegerProperty
- nom: StringProperty
- prix: DoubleProperty
- quantite: IntegerProperty (editable)
- sousTotal: DoubleProperty (bind à quantite * prix)
- description: StringProperty
- image: StringProperty
```
✨ Utilise JavaFX Properties pour binding automatique

### PanierService
```java
- Singleton avec ObservableList<PanierItem>
- ajouterAuPanier(): vérifie doublon, incrémente ou ajoute
- getTotal(): somme des sous-totaux
- getQuantiteTotale(): somme quantités
- viderPanier(): réinitialise après commande
```

### CommandeService.enregistrerCommande()
```java
1. Crée Commandes object
2. INSERT commandes → récupère ID
3. Pour chaque PanierItem → INSERT ligne_commandes
```

## 🎨 Design Moderne E-commerce

### Couleurs
- Vert (#27ae60): Boutons validés
- Rouge (#e74c3c): Panier, totaux
- Bleu (#3498db): Historique
- Gris (#2c3e50): Barre nav

### Layout
- BorderPane principal avec navigation top
- ScrollPane catalogue
- TableView panier/historique
- Dialogs pour formulaires

## 📊 BD Integration
```sql
-- Tables utilisées
commandes(id, produits, totales, statut, date, utilisateur_id)
lignes_commandes(id, nom, prix, quantite, sous_total, commande_id)
produits(id, nom, prix, description, stock)
```

## ✨ Fonctionnalités JavaFX

1. **Binding**: Sous-total lié à quantité × prix
2. **ObservableList**: Panier synchronisé avec UI
3. **TableCell Custom**: Spinner pour éditer quantités
4. **Dialogs**: Formulaire client + facture
5. **Validation**: TextFields avec contrôles
6. **Styling**: CSS inline pour design moderne
7. **Properties**: Getters/Setters auto pour binding

## 🚀 Exécution
```bash
java -m pidevjava/org.example.Main
```

L'application lance directement le Front Office (catalogue)!

## 📝 Notes
- Panier en mémoire (non persistant entre sessions)
- Facture générée en view (pas d'export PDF)
- Images produits: placeholder (à implémenter)
- Client: données temporaires (pas d'inscription)
- Historique: tous les clients confondus

