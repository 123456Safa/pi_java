# ✅ Produits de Test - Configuration Complète

## 📦 Données Prêtes pour Tester

J'ai ajouté **25 produits pharmaceutiques réalistes** à votre application PHARMAX!

---

## 🚀 Deux Méthodes d'Insertion

### Méthode 1: Automatique (RECOMMANDÉE) ✨

L'application insérera automatiquement les produits au premier démarrage!

```bash
java -m pidevjava/org.example.Main
```

La première fois que vous lancez :
```
✅ Connecté à la base de données
com.mysql.cj.jdbc.ConnectionImpl@...
com.mysql.cj.jdbc.ConnectionImpl@...
📦 Insertion des données de test...
✅ 25 produits insérés avec succès!
```

**Comment ça marche:**
1. `DataInitializer.initializeSampleData()` vérifie si produits existent
2. S'il n'y en a pas → insère 25 produits
3. S'il y en a déjà → skip (ne duplique pas)

### Méthode 2: Manuelle

Exécutez le fichier SQL fourni:

**Fichier:** `INSERT_PRODUITS.sql`

**Avec MySQL Workbench:**
1. Ouvrez le fichier dans Workbench
2. Ctrl+Shift+Enter pour exécuter

**Avec command line:**
```bash
mysql -u root -p pharm < INSERT_PRODUITS.sql
```

---

## 📊 Produits Insérés

### Catégories (10 catégories - 25 produits)

| # | Catégorie | Produits | Prix Min | Prix Max |
|---|-----------|----------|----------|----------|
| 1 | Douleurs/Fièvre | 3 | 3.49 | 5.99 |
| 2 | Rhume/Grippe | 3 | 2.99 | 8.50 |
| 3 | Digestion | 3 | 5.49 | 12.99 |
| 4 | Vitamines | 4 | 6.99 | 10.99 |
| 5 | Allergie | 2 | 5.99 | 7.99 |
| 6 | Hygiène/Soins | 4 | 3.99 | 15.99 |
| 7 | Beauté | 3 | 7.99 | 11.99 |
| 8 | Sommeil | 1 | 8.99 | 8.99 |
| 9 | Articulations | 1 | 14.99 | 14.99 |
| 10 | Immunité | 1 | 6.49 | 6.49 |

### Statistiques

- **Total Produits:** 25
- **Prix Minimum:** 2.99 DT (Pastilles gorge)
- **Prix Maximum:** 15.99 DT (Thermomètre)
- **Prix Moyen:** ~8.50 DT
- **Stock Total:** ~1300 unités

---

## 🧪 Tester l'Application

### 1. Lancez l'application
```bash
cd C:\Users\fatma\IdeaProjects\pidevjava
java -m pidevjava/org.example.Main
```

### 2. Dans l'application

#### Test 1: Afficher Catalogue
- Cliquez **"🛍️ Catalogue"**
- Vous verrez une liste scrollable avec les 25 produits
- Chaque produit affiche: Nom | Prix | Description | Bouton "Ajouter au panier"

#### Test 2: Ajouter au Panier
- Cliquez **"Ajouter au panier"** sur plusieurs produits
- Les prix varient (2.99 à 15.99 DT)

#### Test 3: Gestion Doublons
- Ajouter "Aspirine 500mg"
- Ajouter "Aspirine 500mg" à nouveau
- ✅ Vérifier que quantité passe à 2 (pas 2 lignes)

#### Test 4: Éditer Quantités
- Allez au **"🛒 Panier"**
- Cliquez sur la colonne "Quantité"
- Modifiez avec le Spinner (1-100)
- ✅ Sous-total se recalcule automatiquement

#### Test 5: Commande Complète
1. Ajouter 3-4 produits variés
2. Aller au Panier
3. Cliquer "Valider"
4. Remplir formulaire client (avec validation)
5. Cliquer "Passer la commande"
6. ✅ Voir facture générale
7. ✅ Aller à Historique pour voir la commande

---

## 💾 Fichiers Créés

| Fichier | Description |
|---------|-------------|
| `INSERT_PRODUITS.sql` | Script SQL pour insertion manuelle |
| `GUIDE_INSERTION_PRODUITS.md` | Guide détaillé d'insertion |
| `utils/DataInitializer.java` | Classe Java pour insertion automatique |
| Main.java | Mis à jour avec appel DataInitializer |

---

## 📝 Exemples de Produits

```
🟢 DOULEURS/FIÈVRE
  - Aspirine 500mg (4.99 DT) × 50 unités
  - Paracétamol 500mg (3.49 DT) × 75 unités
  - Ibuprofène 200mg (5.99 DT) × 60 unités

🟢 RHUME/GRIPPE
  - Sirop contre la toux (8.50 DT) × 30 unités
  - Spray nasal (6.99 DT) × 45 unités
  - Pastilles gorge (2.99 DT) × 100 unités

🟢 DIGESTION
  - Oméprazole 20mg (7.50 DT) × 40 unités
  - Charbon actif (5.49 DT) × 55 unités
  - Probiotiques (12.99 DT) × 25 unités

🟢 VITAMINES
  - Vitamines C 500mg (6.99 DT) × 80 unités
  - Multivitamines (9.99 DT) × 45 unités
  - Calcium + Vitamine D (10.99 DT) × 35 unités
  - Magnésium (8.49 DT) × 50 unités

... et 10 produits supplémentaires
```

---

## ✅ Checklist de Test

- [ ] Lancer l'app et voir "📦 Insertion des données"
- [ ] Cliquer Catalogue et voir 25 produits
- [ ] Ajouter 1 produit au panier
- [ ] Ajouter le même produit → quantité +1
- [ ] Ajouter 3 produits différents
- [ ] Modifier quantités avec Spinner
- [ ] Supprimer un produit
- [ ] Valider et remplir formulaire client
- [ ] Passer commande et voir facture
- [ ] Vérifier facture avec totaux corrects
- [ ] Aller à Historique et voir la commande
- [ ] Faire une 2ème commande
- [ ] Vérifier deux commandes dans l'Historique

---

## 🎉 Prêt à Tester!

Lancez simplement l'application et testez le workflow complet:

```bash
java -m pidevjava/org.example.Main
```

Les 25 produits pharmaceutiques seront automatiquement disponibles! 🚀

**Bon test! 💊**

