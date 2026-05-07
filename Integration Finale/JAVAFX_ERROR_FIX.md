# Résolution de l'erreur "JavaFX runtime components are missing"

## Cause du problème

IntelliJ IDEA ne passe pas automatiquement les options `--module-path` et `--add-modules` nécessaires pour que JavaFX fonctionne correctement. Sans ces options, le runtime JavaFX ne peut pas être chargé.

## Solutions

### Option 1 : Lancer via script PowerShell ✅ FONCTIONNE

**Fichier** : `run.ps1` (à la racine du projet)

```powershell
cd C:\Users\fatma\IdeaProjects\pidevjava
powershell -ExecutionPolicy Bypass -File run.ps1
```

**Avantages** :
- ✅ Fonctionne immédiatement
- ✅ Pas de configuration supplémentaire requise
- ✅ Reproduit les mêmes conditions que le terminal

### Option 2 : Lancer via script Batch

**Fichier** : `run.bat` (à la racine du projet)

```cmd
cd C:\Users\fatma\IdeaProjects\pidevjava
run.bat
```

### Option 3 : Configuration IntelliJ Run Configuration

La configuration a été créée dans `.idea/runConfigurations/Main.xml`, mais elle peut ne pas être reconnue immédiatement.

**Pour forcer IntelliJ à recharger les configurations** :
1. Fermez IntelliJ complètement
2. Supprimez le répertoire `.idea` et relancez IntelliJ
3. Ou : **File > Invalidate Caches and Restart**

### Option 4 : Terminal IntelliJ

Utilisez le terminal intégré d'IntelliJ :

```bash
powershell -ExecutionPolicy Bypass -File run.ps1
```

## Détails techniques

### Commande exécutée

```
java.exe `
  --module-path "path/to/javafx-base-20.0.2-win.jar;..." `
  --add-modules javafx.controls,javafx.fxml `
  -cp "target/classes;..." `
  org.example.Main
```

### Fichiers FXML modifiés

- `src/main/resources/app-shell.fxml` : Shell principal (sans barre supérieure)
- `src/main/resources/frontoffice/main.fxml` : Frontoffice (avec barre complète)

### Contrôleurs Java

- `AppShellController.java` : Gère la navigation entre modules
- `FrontOfficeMainController.java` : Contrôle le frontoffice avec navigation PHARMAX

## Vérification

Après lancement, vous devriez voir :
```
=== 🔧 INITIALISATION DE LA BASE DE DONNÉES ===
📋 Vérification/Création table 'produits'...
✅ Table 'produits' créée/vérifiée
📊 Vérification des données existantes...
   📌 Produits actuels en BD: 25
✅ Données déjà présentes (25 produits) - Aucune insertion nécessaire
✅ Initialisation TERMINÉE
```

Ensuite, l'interface JavaFX devrait s'ouvrir.

## Si le problème persiste

1. **Vérifier le répertoire des JAR** : 
   - Assurez-vous que `~/.m2/repository/org/openjfx/` existe et contient les fichiers JavaFX

2. **Vérifier la version Java** :
   ```
   C:\Users\fatma\.jdks\openjdk-26\bin\java.exe -version
   ```

3. **Vérifier le classpath** :
   - Le script `run.ps1` contient tous les JAR nécessaires

4. **Redémarrer IntelliJ** :
   - Les configurations peuvent ne pas être chargées sans redémarrage

5. **Nettoyer le cache** :
   ```
   rm -Recurse -Force C:\Users\fatma\IdeaProjects\pidevjava\.idea\caches
   ```

