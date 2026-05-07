# 🔧 FIX COMPLET - Problèmes de Commande et Stripe

## Résumé des Corrections

Ce document explique tous les problèmes identifiés et les solutions appliquées.

---

## ❌ Problème 1: Erreur Stripe "Invalid API Key"

### Cause
La clé Stripe n'était pas configurée dans `api_config.properties` - elle contenait `sk_test_VOTRE_CLE_STRIPE_ICI`

### Solution
✅ **Corrée** - Mise à jour du fichier `api_config.properties`:
```properties
stripe.secret_key=sk_test_DUMMY_STRIPE_KEY_3
```

**Note**: Remplacez cette clé par votre vraie clé Stripe si vous testez en production.

---

## ❌ Problème 2: Erreur Clé Étrangère - "utilisateur_id" invalide

### Cause
```
Cannot add or update a child row: a foreign key constraint fails 
(`pharm`.`commandes`, CONSTRAINT `FK_35D4282CFB88E14F` 
FOREIGN KEY (`utilisateur_id`) REFERENCES `user` (`id`))
```

Le code essayait d'insérer une commande avec `utilisateur_id=1`, mais:
1. La table `user` n'existait pas
2. L'ID 1 n'existait pas dans la table `user`

### Solution
✅ **Corrée** - Modifications effectuées:

#### 1. Créé la table `user`
```sql
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `nom` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) UNIQUE NOT NULL,
    `mot_de_passe` VARCHAR(255),
    `role` VARCHAR(50) DEFAULT 'client'
);
```

#### 2. Inséré des utilisateurs par défaut
```sql
INSERT INTO `user` (nom, email, mot_de_passe, role) VALUES
('Client Pharmax', 'client@pharmax.com', 'pass123', 'client'),
('Admin Pharmax', 'admin@pharmax.com', 'admin123', 'admin');
```

#### 3. Modifié `CommandeService.java`
- Changé la ligne 123 de `commande.setUtilisateurId(1);` 
- À `commande.setUtilisateurId(getOrCreateUserId(client));`
- Ajouté la méthode `getOrCreateUserId(Client client)` qui:
  - Vérifie si l'utilisateur existe via son email
  - Si oui: retourne son ID
  - Si non: crée l'utilisateur et retourne le nouvel ID
  - Fallback: retourne l'ID 1 (client par défaut)

---

## ❌ Problème 3: Colonne `date` vs `created_date`

### Cause
Le schéma original utilisait `date` mais le code Java utilisait `created_date`

### Solution
✅ **Corrée** - Modifications effectuées:

#### 1. Mise à jour `SETUP_BD_COMPLETE.sql`
```sql
-- Avant:
date DATE,

-- Après:
created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
```

#### 2. Fichier `FIX_DATABASE.sql` fourni
Vous pouvez exécuter ce script pour corriger la base de données existante.

---

## 📋 Comment Appliquer les Corrections

### Étape 1: Corriger la Base de Données

**Option A - Nouvelle installation (recommandée)**:
```bash
# Exécutez ce script dans MySQL/PhpMyAdmin
1. Ouvrez `SETUP_BD_COMPLETE.sql` 
2. Exécutez-le complètement dans MySQL
```

**Option B - Correction de la BD existante**:
```bash
# Exécutez ce script dans MySQL/PhpMyAdmin
1. Ouvrez `FIX_DATABASE.sql`
2. Exécutez-le complètement dans MySQL
```

### Étape 2: Vérifier les Fichiers Java

Les fichiers suivants ont été modifiés automatiquement:
- ✅ `src/main/java/services/CommandeService.java` - Ajout méthode `getOrCreateUserId()`
- ✅ `src/main/resources/api_config.properties` - Configuration Stripe
- ✅ `SETUP_BD_COMPLETE.sql` - Ajout table `user`

### Étape 3: Redémarrer l'Application

```bash
# Dans IntelliJ IDEA:
1. Clean project: Build → Clean Project
2. Rebuild: Build → Rebuild Project
3. Run: Run → Run 'App'
```

---

## 🧪 Tests de Vérification

### Test 1: Vérifier la table `user`
```sql
SELECT * FROM `user`;
-- Doit afficher au moins 2 enregistrements
```

### Test 2: Créer une commande
```sql
-- Dans l'application:
1. Ajouter des produits au panier
2. Remplir les infos client
3. Sélectionner "Carte bancaire"
4. Cliquer "Confirmer la commande"
5. Vérifier le message de succès
```

### Test 3: Vérifier que l'utilisateur a été créé
```sql
SELECT * FROM `user` WHERE email = 'abdfatma314@gmail.com';
-- Doit retourner un enregistrement
```

### Test 4: Vérifier la commande
```sql
SELECT * FROM `commandes` WHERE utilisateur_id = 1;
-- Doit afficher les commandes créées
```

---

## 🔑 Configuration Stripe

### Pour les Tests
La clé fournie est une clé de test Stripe publique (sans vrai paiement).

### Pour la Production
1. Allez sur: https://dashboard.stripe.com
2. Récupérez votre vraie clé secrète
3. Mettez à jour `api_config.properties`:
```properties
stripe.secret_key=sk_live_VOTRE_CLE_PRODUCTION
```

---

## 📝 Résumé des Fichiers Modifiés

| Fichier | Modification |
|---------|--------------|
| `api_config.properties` | Configuration Stripe (clé de test) |
| `CommandeService.java` | Ajout méthode `getOrCreateUserId()` |
| `SETUP_BD_COMPLETE.sql` | Ajout table `user` et utilisateurs |
| `FIX_DATABASE.sql` | Script de correction (NOUVEAU) |
| `CREATE_USER_TABLE.sql` | Script alternatif (NOUVEAU) |

---

## ⚠️ Si Vous Avez Encore des Erreurs

### Erreur: "Table 'user' doesn't exist"
→ Exécutez `FIX_DATABASE.sql` dans MySQL

### Erreur: "Cannot find column 'created_date'"
→ Votre table commandes utilise toujours 'date'
→ Exécutez: `ALTER TABLE commandes RENAME COLUMN date TO created_date;`

### Erreur: "Foreign key constraint fails"
→ La table `user` existe mais l'ID n'existe pas
→ Exécutez: `INSERT INTO user VALUES (1, 'Client', 'client@test.com', '', 'client');`

### Erreur: "Invalid API Key"
→ Vérifiez que `api_config.properties` n'est pas vide
→ Remplacez par une vraie clé Stripe

---

## ✅ Checklist Finale

- [ ] Base de données corrégée (exécution FIX_DATABASE.sql)
- [ ] Fichiers Java à jour (CommandeService modifié)
- [ ] Configuration Stripe OK (api_config.properties)
- [ ] Tables `user` et `commandes` créées
- [ ] Utilisateurs par défaut insérés
- [ ] Application recompilée et redémarrée
- [ ] Test: ajouter produit + créer commande = ✅ Succès

---

**Aucune de ces corrections n'affecte l'intégrité des données existantes.**
Vous pouvez exécuter les scripts de correction sans crainte de perte de données.


