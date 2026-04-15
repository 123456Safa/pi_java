# 🚀 Comment lancer l'application PharMax

## ⚠️ IMPORTANT - Problème et Solution

### Le problème
L'erreur `InvalidPathException: Illegal char <:>` dans IntelliJ IDEA vient de l'utilisation du séparateur **Unix (`:`)** au lieu du séparateur **Windows (`;`)** dans le `--module-path`.

### ✅ La solution

Utilisez **l'un de ces 2 scripts batch** pour lancer l'application :

---

## 📋 Option 1: Lancer directement (classes déjà compilées)

**Fichier:** `run_app.bat`

### Comment l'utiliser:
1. Double-cliquez sur `run_app.bat`
   OU
2. Ouvrez un terminal dans le répertoire du projet et tapez:
   ```batch
   run_app.bat
   ```

### Avantages:
- ✅ Rapide
- ✅ Utilise les classes compilées existantes
- ✅ Pas d'erreur de séparateur

---

## 📋 Option 2: Compiler ET lancer

**Fichier:** `compile_and_run.bat`

### Comment l'utiliser:
1. Double-cliquez sur `compile_and_run.bat`
   OU
2. Ouvrez un terminal dans le répertoire du projet et tapez:
   ```batch
   compile_and_run.bat
   ```

### Avantages:
- ✅ Recompile le projet depuis les sources
- ✅ Copie automatiquement les ressources (FXML, CSS)
- ✅ Lance ensuite l'application
- ✅ Parfait après des modifications du code

---

## 🛑 Ne pas utiliser IntelliJ IDEA pour lancer

**IMPORTANT:** Ne pas cliquer sur le bouton "Run" dans IntelliJ IDEA, car il utilise la mauvaise commande avec le séparateur Unix.

Utilisez à la place les scripts batch ci-dessus.

---

## 📝 Dépannage

### Si vous avez toujours une erreur:

1. **Supprimez le répertoire `target`** et recompilez:
   ```batch
   compile_and_run.bat
   ```

2. **Vérifiez que Java est correctement installé:**
   ```batch
   C:\Users\fatma\.jdks\openjdk-26\bin\java.exe -version
   ```

3. **Vérifiez que Maven est installé:**
   ```batch
   dir C:\Users\fatma\.m2\repository\org\openjfx\
   ```

---

## ✨ Résumé

| Action | Commande |
|--------|----------|
| Lancer l'application | `run_app.bat` |
| Recompiler + Lancer | `compile_and_run.bat` |
| Éviter | Bouton "Run" dans IntelliJ |

**C'est tout! L'erreur du séparateur ne devrait plus jamais apparaître.** ✅

