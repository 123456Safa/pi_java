# 🔍 Guide Troubleshooting - Catalogue Vide

## 🚀 Étapes pour Diagnostiquer

### Étape 1: Vérifier la Connexion BD
Lancez l'app et regardez la console:

```
✅ Connecté à la base de données
com.mysql.cj.jdbc.ConnectionImpl@...
com.mysql.cj.jdbc.ConnectionImpl@...
```

✅ = Connexion OK
❌ = Connexion échouée → Vérifiez les credentials MySQL

---

### Étape 2: Vérifier l'Initialisation des Données
Regardez les logs suivants dans la console:

```
📊 Produits existants: 0
📦 Insertion des données de test...
✅ 25/25 produits insérés!
✅ Vérification finale: 25 produits en BD
```

**Problèmes possibles:**

1. **"📊 Produits existants: 0"** suivi de rien
   - Table `produits` n'existe pas
   - BD n'est pas `pharm`
   - Erreur SQL silencieuse

2. **"❌ Erreur lors de la vérification de la table"**
   - Vérifiez que la table existe:
   ```sql
   SHOW TABLES FROM pharm;
   ```

3. **"✅ 0/25 produits insérés!"**
   - Permissions manquantes sur la BD
   - Erreur SQL dans les INSERT

---

### Étape 3: Vérifier le Chargement du Catalogue
Allez dans l'app et cliquez "🛍️ Catalogue", vérifiez la console:

```
🔄 Chargement des produits...
📦 Nombre de produits chargés: 25
✅ Affichage de 25 produits
   - Aspirine 500mg (4.99 DT)
   - Paracétamol 500mg (3.49 DT)
   ...
```

✅ = Catalogue charge correctement
❌ = Les produits ne s'affichent pas

---

## 🛠️ Solutions par Problème

### Problème 1: Aucune données insérées

**Symptôme:**
```
📊 Produits existants: 0
... puis plus rien (timeout)
```

**Solution:**
Vérifiez manuellement que la BD fonctionne:

```bash
mysql -u root -p
```

```sql
USE pharm;
SHOW TABLES;
DESC produits;
SELECT COUNT(*) FROM produits;
```

Si table n'existe pas, créez-la:
```sql
CREATE TABLE IF NOT EXISTS produits (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100),
    prix DOUBLE,
    description TEXT,
    stock INT
);
```

---

### Problème 2: Données insérées mais catalogue vide

**Symptôme:**
```
✅ 25/25 produits insérés!
✅ Vérification finale: 25 produits en BD

[mais dans l'app]
❌ Aucun produit disponible
```

**Solution:**
Le problème est dans la requête SQL ou le service.

**Vérifiez:**
```sql
-- Connaitre le nom exact de la BD
SELECT DATABASE();

-- Lister les produits
SELECT * FROM produits LIMIT 5;

-- Vérifier les noms de colonnes
DESC produits;
```

Assurez-vous que ProduitService utilise le bon nom de table:
```java
// ✅ Correct
String sql = "SELECT * FROM produits";  // pluriel

// ❌ Faux
String sql = "SELECT * FROM produit";   // singulier
```

---

### Problème 3: Erreur SQL affichée

**Symptôme:**
```
❌ ERREUR:
Table 'pharm.produit' doesn't exist
```

**Solution:**
La table s'appelle `produits` (pluriel), pas `produit`.

Changez dans **ProduitService.java**:
```java
// Avant
String sql = "SELECT * FROM produit";

// Après
String sql = "SELECT * FROM produits";
```

---

## ✅ Checklist Complète

- [ ] MySQL est démarré
- [ ] BD `pharm` existe: `SHOW DATABASES;`
- [ ] Table `produits` existe: `SHOW TABLES FROM pharm;`
- [ ] Table a les bonnes colonnes: `DESC produits;`
- [ ] Données insérées: `SELECT COUNT(*) FROM produits;` → 25
- [ ] Console affiche logs d'initialisation
- [ ] Console affiche logs de chargement
- [ ] Aucune erreur SQL affichée
- [ ] App affiche les 25 produits

---

## 🚀 Commande Complète pour Tester

1. **Arrêtez l'app** (si running)

2. **Vérifiez la BD:**
```bash
mysql -u root -p pharm
mysql> SELECT COUNT(*) FROM produits;
mysql> exit
```

3. **Relancez l'app:**
```bash
cd C:\Users\fatma\IdeaProjects\pidevjava
java -m pidevjava/org.example.Main
```

4. **Regardez la console** pour les 3 phases:
   - ✅ Connexion BD OK
   - ✅ Insertion données OK (ou déjà existantes)
   - ✅ Chargement catalogue OK

5. **Cliquez "🛍️ Catalogue"** et vérifiez les produits

---

## 📞 Messages d'Erreur Courants

| Erreur | Cause | Solution |
|--------|-------|----------|
| `Connection refused` | MySQL not running | `mysql.server start` ou service MySQL |
| `Unknown database 'pharm'` | BD n'existe pas | Créer la BD: `CREATE DATABASE pharm;` |
| `Table doesn't exist` | Mauvais nom de table | Vérifier: `SHOW TABLES;` |
| `Access denied` | Credentials incorrects | Vérifier user/password dans MyConnection |
| `0 produits` | Données pas insérées | Insérer manuellement ou vérifier INSERT |

---

## 💾 Reset Complet (si tout s'est mal passé)

```sql
-- Se connecter à MySQL
mysql -u root -p

-- Supprimer l'ancienne BD
DROP DATABASE IF EXISTS pharm;

-- Créer la nouvelle BD
CREATE DATABASE pharm;
USE pharm;

-- Créer les tables
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

-- Vérifier
SHOW TABLES;
```

Puis relancez l'app - elle insérera automatiquement les 25 produits!

---

**Status:** Application prête avec logs complets de diagnostic

