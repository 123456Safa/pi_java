# Configuration IntelliJ IDEA pour JavaFX

## Comment configurer correctement IntelliJ pour lancer l'application JavaFX

### Étape 1: Ouvrir les configurations de Run

1. Dans IntelliJ IDEA, allez à: **Run → Edit Configurations...**

### Étape 2: Créer une nouvelle configuration "Application"

1. Cliquez sur le **+** en haut à gauche
2. Sélectionnez **Application**
3. Nommez-la: `PharMax`

### Étape 3: Configurer les paramètres

Dans la fenêtre de configuration, complétez les champs suivants:

**Main class:**
```
org.example.Main
```

**Module:** Sélectionnez votre module `pidevjava`

**VM options:** (IMPORTANT - Ceci résout le problème!)
```
--module-path C:\Users\fatma\.m2\repository\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar;C:\Users\fatma\.m2\repository\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar;C:\Users\fatma\.m2\repository\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar;C:\Users\fatma\.m2\repository\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar --add-modules javafx.controls,javafx.fxml --enable-native-access javafx.graphics -Dfile.encoding=UTF-8
```

⚠️ **IMPORTANT:** Utilisez **`;` (point-virgule)** et non **`:` (deux-points)** comme séparateur!

**Working directory:**
```
C:\Users\fatma\IdeaProjects\pidevjava
```

### Étape 4: Cliquer sur "Apply" et "OK"

Maintenant, vous pouvez cliquer sur le bouton "Run" (Shift+F10) et ça devrait fonctionner!

---

## ⚠️ Si vous avez toujours des erreurs:

**Solution plus simple:** Utilisez les scripts batch à la place:
- `run_app.bat` - Lance l'application directement
- `compile_and_run.bat` - Recompile puis lance

Ces scripts gèrent automatiquement tous les paramètres!

---

## Vérification

Après configuration, vous devriez voir dans la console:
```
=== 🔧 INITIALISATION DE LA BASE DE DONNÉES ===
✅ Initialisation TERMINÉE
```

Et l'application JavaFX devrait démarrer sans erreur! ✅

