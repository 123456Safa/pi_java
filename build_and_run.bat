@echo off
REM Script complet pour compiler TOUS les fichiers Java et copier les ressources

setlocal enabledelayedexpansion

set JAVA_HOME=C:\Users\fatma\.jdks\openjdk-26
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
set JAVAC_EXE=%JAVA_HOME%\bin\javac.exe
set PROJECT_DIR=C:\Users\fatma\IdeaProjects\pidevjava
set M2_REPO=C:\Users\fatma\.m2\repository
set SRC_DIR=%PROJECT_DIR%\src\main\java
set RESOURCES_DIR=%PROJECT_DIR%\src\main\resources
set TARGET_DIR=%PROJECT_DIR%\target\classes

echo ========================================
echo    Compilation complète du projet JavaFX
echo ========================================

REM Créer le répertoire target si nécessaire
if not exist "%TARGET_DIR%" mkdir "%TARGET_DIR%"

REM Module path pour JavaFX (séparateur Windows: ;)
set MODULE_PATH=%M2_REPO%\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar

REM Classpath pour la compilation
set COMPILE_CP=%M2_REPO%\mysql\mysql-connector-java\8.0.26\mysql-connector-java-8.0.26.jar;%M2_REPO%\com\google\protobuf\protobuf-java\3.11.4\protobuf-java-3.11.4.jar;%M2_REPO%\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2.jar

echo Compilation de tous les fichiers Java...
"%JAVAC_EXE%" --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml -cp "%COMPILE_CP%" -d "%TARGET_DIR%" -encoding UTF-8 "%SRC_DIR%\org\example\*.java" "%SRC_DIR%\controllers\*.java" "%SRC_DIR%\controllers\frontoffice\*.java" "%SRC_DIR%\controllers\backoffice\*.java" "%SRC_DIR%\models\*.java" "%SRC_DIR%\services\*.java" "%SRC_DIR%\utils\*.java"

if errorlevel 1 (
    echo.
    echo ERREUR: Compilation échouée!
    pause
    exit /b 1
)

REM Compiler récursivement TOUS les fichiers Java
for /r "%SRC_DIR%" %%F in (*.java) do (
    "%JAVAC_EXE%" --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml -cp "%COMPILE_CP%;%TARGET_DIR%" -d "%TARGET_DIR%" -encoding UTF-8 "%%F" 2>nul
)

echo Compilation réussie!
echo.

REM Copier les ressources (FXML, CSS, images, etc.)
echo Copie des ressources...
if exist "%RESOURCES_DIR%" (
    echo Copie depuis: %RESOURCES_DIR%
    echo Vers: %TARGET_DIR%
    xcopy "%RESOURCES_DIR%\*" "%TARGET_DIR%\" /S /Y /Q >nul 2>&1
    if errorlevel 1 (
        echo AVERTISSEMENT: Erreur lors de la copie des ressources
    ) else (
        echo Ressources copiées avec succès
    )
) else (
    echo AVERTISSEMENT: Répertoire ressources non trouvé: %RESOURCES_DIR%
)

REM Vérifier que les fichiers FXML sont copiés
if exist "%TARGET_DIR%\app-shell.fxml" (
    echo OK: app-shell.fxml trouvé
) else (
    echo ERREUR: app-shell.fxml manquant!
)

echo.
echo ========================================
echo    Lancement de l'application
echo ========================================
echo.

REM Classpath pour l'exécution (séparateur Windows: ;)
set RUNTIME_CP=%TARGET_DIR%;%M2_REPO%\mysql\mysql-connector-java\8.0.26\mysql-connector-java-8.0.26.jar;%M2_REPO%\com\google\protobuf\protobuf-java\3.11.4\protobuf-java-3.11.4.jar;%M2_REPO%\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar

REM Lancer l'application
echo Lancement avec la configuration Windows correcte...
"%JAVA_EXE%" --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -Dfile.encoding=UTF-8 -cp "%RUNTIME_CP%" org.example.Main

endlocal
pause
