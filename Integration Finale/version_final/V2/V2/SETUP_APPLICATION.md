# PHARMAX - Application de Gestion de Pharmacie

## Configuration et Lancement

### Erreur : "JavaFX runtime components are missing"

Si vous recevez cette erreur lors du lancement depuis IntelliJ, voici les solutions :

### Solution 1 : Utiliser le script PowerShell (RECOMMANDÉ)
```powershell
cd C:\Users\fatma\IdeaProjects\pidevjava
powershell -ExecutionPolicy Bypass -File run.ps1
```

### Solution 2 : Utiliser le script Batch
```cmd
cd C:\Users\fatma\IdeaProjects\pidevjava
run.bat
```

### Solution 3 : Configuration manuelle dans IntelliJ
1. Allez dans **Run > Edit Configurations**
2. Sélectionnez **Main** (ou créez une nouvelle configuration)
3. Configurez les options VM comme suit :
   ```
   --module-path "C:\Users\fatma\.m2\repository\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar;C:\Users\fatma\.m2\repository\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar;C:\Users\fatma\.m2\repository\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar;C:\Users\fatma\.m2\repository\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar" --add-modules javafx.controls,javafx.fxml
   ```
4. Cliquez sur **OK** et lancez l'application

### Navigation dans l'application

- **Frontoffice** : Affichage du catalogue de produits avec :
  - Panier
  - Mes commandes
  - Gestion des produits
  
- **Backoffice** : Gestion administrative avec :
  - Dashboard
  - Gestion des commandes

- **Navigation** : Cliquez sur **PHARMAX** (en haut de la barre) pour basculer entre le frontoffice et le backoffice

### Structure du Projet

- `src/main/java/` : Code source Java
- `src/main/resources/` : Fichiers FXML et CSS
- `target/classes/` : Classes compilées

### Dépendances

- **JavaFX 20.0.2** : Framework UI
- **MySQL Connector 8.0.26** : Driver JDBC pour MySQL
- **Java 17+** : Runtime requis

### Compilation

Tous les fichiers ont déjà été compilés dans `target/classes/`. Pour recompiler :

```powershell
cd C:\Users\fatma\IdeaProjects\pidevjava
&"C:\Users\fatma\.jdks\openjdk-26\bin\javac.exe" -d target\classes -cp "...classpath..." @files.txt
```

Ou utilisez Maven :
```bash
mvn clean compile
```

