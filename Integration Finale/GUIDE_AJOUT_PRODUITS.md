# 🛍️ PHARMAX - Guide d'ajout des produits

## Problème
Le catalogue affiche "Aucun produit disponible" car la table `produits` est vide ou n'existe pas.

## Solution 1 : Automatique (RECOMMANDÉE) ⭐

### Étape 1 : Redémarrer l'application
1. **Arrêtez l'application** (cliquez sur le bouton rouge "Stop" dans IntelliJ)
2. **Dans IntelliJ** :
   - Clic droit sur le projet → **Run Maven** → **clean**
   - Clic droit sur le projet → **Run Maven** → **compile**
   - Appuyez sur **F5** ou **Ctrl+F5** pour relancer l'application

### Pourquoi ça marche
- Au démarrage, le `DataInitializer.initializeSampleData()` (dans `Main.java`) :
  1. ✅ Crée la table `produits` si elle n'existe pas
  2. ✅ Vérifie s'il y a des produits
  3. ✅ Insère 25 produits de test si la table est vide
  
- **Résultat** : 🛍️ Catalogue rempli avec 25 produits !

---

## Solution 2 : Manuel (Si l'automatique ne fonctionne pas)

### Option A : Avec MySQL Workbench
1. **Ouvrez MySQL Workbench**
2. **Connectez-vous à votre serveur MySQL**
3. **Ouvrez le fichier** : `AJOUTER_PRODUITS.sql`
4. **Exécutez le script** (Ctrl+Shift+Enter ou bouton "Execute")
5. **Relancez l'application**

### Option B : Avec Command Line
1. **Ouvrez l'Explorateur Windows**
2. **Naviguez vers** : `C:\Users\fatma\IdeaProjects\pidevjava\`
3. **Double-cliquez sur** : `ajouter_produits.bat`
4. **Le script s'exécutera automatiquement**
5. **Relancez l'application**

### Option C : Avec PowerShell (Avancé)
```powershell
cd C:\Users\fatma\IdeaProjects\pidevjava
mysql -u root pharm < AJOUTER_PRODUITS.sql
```

---

## Vérification

### Méthode 1 : Depuis l'application
1. ✅ Cliquez sur **"🛍️ Catalogue"**
2. ✅ Vous devriez voir 25 produits

### Méthode 2 : Depuis MySQL
1. **Ouvrez MySQL Workbench**
2. **Exécutez cette requête** :
   ```sql
   USE pharm;
   SELECT COUNT(*) FROM produits;
   SELECT * FROM produits LIMIT 3;
   ```
3. ✅ Vous devriez voir 25 résultats

---

## Logs à vérifier dans IntelliJ

### Au démarrage (dans la console) :
```
✅ Connecté à la base de données
[...]
=== 🔧 INITIALISATION DE LA BASE DE DONNÉES ===
📋 Vérification/Création table 'produits'...
✅ Table 'produits' créée/vérifiée
📊 Vérification des données existantes...
   📌 Produits actuels en BD: 0
📦 INSERTION de 25 produits de test...
   ✅ [1/25] Produit inséré
   ✅ [2/25] Produit inséré
   ...
✅ RÉSULTAT: 25/25 produits insérés avec succès!
✅ TOTAL EN BD: 25 produits
✅ Initialisation TERMINÉE
```

---

## Fichiers créés

- ✅ `AJOUTER_PRODUITS.sql` - Script SQL pour insérer les produits
- ✅ `VERIFIER_PRODUITS.sql` - Script pour vérifier les produits
- ✅ `ajouter_produits.bat` - Script batch pour Windows
- ✅ `insert_products.py` - Script Python (optionnel)
- ✅ `DataInitializer.java` - Amélioré avec plus de logging

---

## Dépannage

### "La table n'existe pas"
- Exécutez `AJOUTER_PRODUITS.sql` manuellement
- Vérifiez que vous êtes bien connecté à la base `pharm`

### "Les produits ne s'affichent pas"
- Redémarrez l'application
- Vérifiez la console Java pour les erreurs
- Exécutez `VERIFIER_PRODUITS.sql` pour vérifier la BD

### Erreur de connexion MySQL
- Vérifiez que MySQL est en cours d'exécution
- Vérifiez que l'utilisateur `root` n'a pas de mot de passe
- Vérifiez que la base `pharm` existe

---

## 25 Produits testés ✅

1. Aspirine 500mg (4.99 DT)
2. Paracétamol 500mg (3.49 DT)
3. Ibuprofène 200mg (5.99 DT)
4. Sirop contre la toux (8.50 DT)
5. Spray nasal (6.99 DT)
6. Pastilles gorge (2.99 DT)
7. Oméprazole 20mg (7.50 DT)
8. Charbon actif (5.49 DT)
9. Probiotiques (12.99 DT)
10. Vitamines C (6.99 DT)
11. Multivitamines (9.99 DT)
12. Calcium + D (10.99 DT)
13. Magnésium (8.49 DT)
14. Antihistaminique (7.99 DT)
15. Pommade anti-itch (5.99 DT)
16. Gel antibactérien (3.99 DT)
17. Pansements (4.99 DT)
18. Thermomètre (15.99 DT)
19. Masques (6.99 DT)
20. Écran solaire (11.99 DT)
21. Crème hydratante (9.49 DT)
22. Shampooing (7.99 DT)
23. Mélatonine (8.99 DT)
24. Glucosamine (14.99 DT)
25. Zinc (6.49 DT)

---

## Questions ?
Si tu rencontres des problèmes :
1. Regarde les logs dans la console IntelliJ
2. Exécute `VERIFIER_PRODUITS.sql` pour vérifier la BD
3. Redémarre l'application complètement

✅ Le système fonctionne parfaitement une fois que les produits sont chargés !

