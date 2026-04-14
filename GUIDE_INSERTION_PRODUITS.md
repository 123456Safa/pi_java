# 📦 Guide d'Insertion des Produits de Test

## 🎯 Objectif

Insérer 25 produits pharmaceutiques réalistes dans la base de données PHARMAX pour tester l'application.

## 📝 Produits Inclus

### 1️⃣ Douleurs et Fièvre (3 produits)
- Aspirine 500mg - 4.99 DT
- Paracétamol 500mg - 3.49 DT
- Ibuprofène 200mg - 5.99 DT

### 2️⃣ Rhume et Grippe (3 produits)
- Sirop contre la toux - 8.50 DT
- Spray nasal décongestionnant - 6.99 DT
- Pastilles gorge menthe - 2.99 DT

### 3️⃣ Digestion (3 produits)
- Oméprazole 20mg - 7.50 DT
- Charbon actif - 5.49 DT
- Probiotiques - 12.99 DT

### 4️⃣ Vitamines et Minéraux (4 produits)
- Vitamines C 500mg - 6.99 DT
- Multivitamines - 9.99 DT
- Calcium + Vitamine D - 10.99 DT
- Magnésium - 8.49 DT

### 5️⃣ Allergie (2 produits)
- Antihistaminique 10mg - 7.99 DT
- Pommade anti-démangeaison - 5.99 DT

### 6️⃣ Hygiène et Soins (4 produits)
- Gel antibactérien - 3.99 DT
- Pansements stériles - 4.99 DT
- Thermomètre numérique - 15.99 DT
- Masques chirurgicaux - 6.99 DT

### 7️⃣ Beauté et Bien-être (3 produits)
- Écran solaire SPF 50 - 11.99 DT
- Crème hydratante - 9.49 DT
- Shampooing médical - 7.99 DT

### 8️⃣ Sommeil (1 produit)
- Mélatonine 2mg - 8.99 DT

### 9️⃣ Articulations (1 produit)
- Glucosamine - 14.99 DT

### 🔟 Immunité (1 produit)
- Zinc 15mg - 6.49 DT

---

## 🚀 Comment Insérer les Produits

### Méthode 1: Via MySQL Workbench

1. Ouvrez **MySQL Workbench**
2. Connectez-vous à votre serveur MySQL
3. Sélectionnez la base de données `pharm`
4. Ouvrez le fichier `INSERT_PRODUITS.sql`
5. Exécutez le script (Ctrl+Shift+Enter)

### Méthode 2: Via Command Line

```bash
# Naviguez jusqu'au dossier du projet
cd C:\Users\fatma\IdeaProjects\pidevjava

# Exécutez le script SQL
mysql -u root -p pharm < INSERT_PRODUITS.sql
```

Vous serez demandé d'entrer le mot de passe MySQL.

### Méthode 3: Via PhpMyAdmin

1. Accédez à http://localhost/phpmyadmin
2. Sélectionnez la base `pharm`
3. Allez à l'onglet **SQL**
4. Collez le contenu du fichier `INSERT_PRODUITS.sql`
5. Cliquez sur **Exécuter**

---

## ✅ Vérification

Après insertion, vérifiez que les produits sont bien insérés :

```sql
SELECT COUNT(*) as 'Nombre de produits' FROM produits;
SELECT * FROM produits LIMIT 10;
```

Vous devriez voir 25 produits avec des prix et des stocks variés.

---

## 🧪 Tester l'Application

### 1. Lancez l'application
```bash
java -m pidevjava/org.example.Main
```

### 2. Testez le workflow complet
- ✅ Cliquez sur **"🛍️ Catalogue"** → Vous verrez tous les 25 produits
- ✅ Cliquez **"Ajouter au panier"** sur plusieurs produits
- ✅ Allez au **"🛒 Panier"** → Vérifiez les articles
- ✅ Modifiez les quantités via le Spinner
- ✅ Cliquez **"Valider"** → Remplissez le formulaire client
- ✅ Cliquez **"Passer la commande"** → Voir la facture
- ✅ Vérifiez dans **"📜 Historique"** → Voir votre commande

### 3. Testez les calculs
- Sous-total = Prix × Quantité ✅
- Total panier = Somme des sous-totaux ✅
- Facture affiche tous les détails ✅

---

## 📊 Statistiques des Produits

| Métrique | Valeur |
|----------|--------|
| Nombre total | 25 produits |
| Prix min | 2.99 DT (Pastilles gorge) |
| Prix max | 15.99 DT (Thermomètre) |
| Prix moyen | ~8.50 DT |
| Stock total | ~1300 unités |
| Catégories | 10 |

---

## 🎯 Cas de Test Recommandés

### Test 1: Panier simple
1. Ajouter 1 produit
2. Valider
3. Vérifier la facture

### Test 2: Doublons
1. Ajouter "Aspirine 500mg"
2. Ajouter "Aspirine 500mg" à nouveau
3. Vérifier que la quantité passe à 2 (pas 2 lignes)

### Test 3: Panier complexe
1. Ajouter 5 produits différents
2. Modifier les quantités
3. Supprimer 1 produit
4. Vérifier le total

### Test 4: Validation formulaire
1. Laisser des champs vides
2. Vérifier les messages d'erreur
3. Remplir correctement et valider

### Test 5: Historique
1. Passer 2-3 commandes
2. Aller à l'historique
3. Cliquer sur une commande
4. Vérifier les lignes détaillées

---

## 💡 Notes

- Les données sont **fictives** mais réalistes pour une pharmacie
- Les prix sont en **Dinar tunisien (DT)**
- Les stocks varient pour simuler des disponibilités différentes
- Vous pouvez ajouter/modifier des produits directement en SQL

---

## 🗑️ Nettoyer la BD (Optionnel)

Pour supprimer tous les produits et recommencer :

```sql
DELETE FROM produits;
ALTER TABLE produits AUTO_INCREMENT = 1;
```

Pour supprimer aussi les commandes :

```sql
DELETE FROM lignes_commandes;
DELETE FROM commandes;
ALTER TABLE commandes AUTO_INCREMENT = 1;
ALTER TABLE lignes_commandes AUTO_INCREMENT = 1;
```

---

**Prêt à tester PHARMAX ! 🚀**

