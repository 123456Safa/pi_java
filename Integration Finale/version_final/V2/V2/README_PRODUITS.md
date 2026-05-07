# 🛍️ PHARMAX - E-Pharmacy | Guide de Démarrage Produits

## 🎯 Objectif
Ajouter 25 produits de test dans le catalogue pour pouvoir tester l'application.

## ⚡ Solution Rapide (30 secondes)

1. **Arrêtez l'application** (bouton rouge)
2. **Menu → Build → Clean Project**
3. **Menu → Build → Build Project**
4. **Appuyez sur F5** pour relancer
5. **Cliquez sur "🛍️ Catalogue"** → ✅ 25 produits affichés !

---

## 📚 Documentation Disponible

### 🚀 Pour Commencer Rapidement
- **`SOLUTION_RAPIDE.txt`** - Étapes rapides (2 min)
- **`GUIDE_AJOUT_PRODUITS.md`** - Guide complet en français

### 🔧 Scripts SQL
- **`AJOUTER_PRODUITS.sql`** - Script pour MySQL Workbench
- **`COPIER_COLLER.sql`** - Script prêt à copier-coller
- **`VERIFIER_PRODUITS.sql`** - Vérifier les produits en BD

### 🖥️ Scripts Utilitaires
- **`ajouter_produits.bat`** - Batch pour Windows
- **`insert_products.py`** - Script Python optionnel

---

## 📋 Résumé des Produits

**Total:** 25 produits pharmaceutiques
**Prix:** 2.99 DT à 15.99 DT
**Stock:** 20 à 120 unités par produit

**Catégories:**
- Douleurs et Fièvre
- Rhume et Grippe
- Digestion
- Vitamines et Minéraux
- Allergie
- Hygiène et Soins
- Beauté et Bien-être
- Et plus...

---

## ✅ Vérification

### Dans l'Application
1. Cliquez sur "🛍️ Catalogue"
2. Vous devez voir 25 produits avec :
   - Nom du produit
   - Prix en DT
   - Description
   - Bouton "Ajouter au panier"

### En Base de Données
```sql
USE pharm;
SELECT COUNT(*) FROM produits;  -- Doit afficher 25
```

---

## 🔧 Architecture de la Solution

**DataInitializer.java** s'exécute au démarrage et :
1. ✅ Crée la table `produits`
2. ✅ Vérifie s'il y a des produits
3. ✅ Insère 25 produits si vide
4. ✅ Affiche les logs

---

## ❓ Besoin d'Aide ?

1. **"Les produits ne s'affichent pas"**
   - Consultez `SOLUTION_RAPIDE.txt`
   - Vérifiez les logs dans la console IntelliJ

2. **"Erreur de connexion BD"**
   - Consultez `GUIDE_AJOUT_PRODUITS.md`
   - Vérifiez que MySQL est en cours d'exécution

3. **"Je veux insérer manuellement"**
   - Exécutez `AJOUTER_PRODUITS.sql` dans MySQL Workbench

---

## 🚀 Prêt à Tester !

**L'application est maintenant configurée pour afficher le catalogue avec 25 produits de test !**

Bonne chance avec vos tests ! 🎉

---

*Créé pour PHARMAX - E-Pharmacy*
*Date: Avril 2026*

