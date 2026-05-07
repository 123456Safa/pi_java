@echo off
REM Script pour compiler et lancer l'application avec JavaFX

setlocal enabledelayedexpansion

set JAVA_HOME=C:\Users\fatma\.jdks\openjdk-26
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
set JAVAC_EXE=%JAVA_HOME%\bin\javac.exe
set PROJECT_DIR=C:\Users\fatma\IdeaProjects\pidevjava
set M2_REPO=C:\Users\fatma\.m2\repository
set SRC_DIR=%PROJECT_DIR%\src\main\java
set TARGET_DIR=%PROJECT_DIR%\target\classes
set RESOURCES_DIR=%PROJECT_DIR%\src\main\resources

echo ========================================
echo    Compilation du projet JavaFX
echo ========================================

REM Créer le répertoire target si nécessaire
if not exist "%TARGET_DIR%" mkdir "%TARGET_DIR%"

REM Module path pour JavaFX
set MODULE_PATH=%M2_REPO%\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar

REM Classpath pour la compilation
set COMPILE_CP=%M2_REPO%\mysql\mysql-connector-java\8.0.26\mysql-connector-java-8.0.26.jar;%M2_REPO%\com\google\protobuf\protobuf-java\3.11.4\protobuf-java-3.11.4.jar;%M2_REPO%\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2.jar;%M2_REPO%\com\itextpdf\itextpdf\5.5.13.3\itextpdf-5.5.13.3.jar;%M2_REPO%\com\sun\mail\javax.mail\1.6.2\javax.mail-1.6.2.jar;%M2_REPO%\javax\activation\activation\1.1.1\activation-1.1.1.jar

echo Recherche des fichiers sources...
dir /s /b "%SRC_DIR%\*.java" > "%PROJECT_DIR%\sources.txt"

echo Compilation...
"%JAVAC_EXE%" --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "%COMPILE_CP%" -d "%TARGET_DIR%" @"%PROJECT_DIR%\sources.txt"

if errorlevel 1 (
    del "%PROJECT_DIR%\sources.txt"
    echo.
    echo ERREUR: Compilation échouée!
    pause
    exit /b 1
)

del "%PROJECT_DIR%\sources.txt"
echo Compilation réussie!
echo.

REM Copier les ressources (FXML, CSS)
echo Copie des ressources...
xcopy "%RESOURCES_DIR%\*" "%TARGET_DIR%\" /S /Y /Q >nul 2>&1

echo ========================================
echo    Lancement de l'application
echo ========================================
echo.

REM Classpath pour l'exécution
set RUNTIME_CP=%TARGET_DIR%;%M2_REPO%\mysql\mysql-connector-java\8.0.26\mysql-connector-java-8.0.26.jar;%M2_REPO%\com\google\protobuf\protobuf-java\3.11.4\protobuf-java-3.11.4.jar;%M2_REPO%\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar;%M2_REPO%\com\itextpdf\itextpdf\5.5.13.3\itextpdf-5.5.13.3.jar;%M2_REPO%\com\sun\mail\javax.mail\1.6.2\javax.mail-1.6.2.jar;%M2_REPO%\javax\activation\activation\1.1.1\activation-1.1.1.jar

REM Lancer l'application
"%JAVA_EXE%" --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml --enable-native-access javafx.graphics -Dfile.encoding=UTF-8 -cp "%RUNTIME_CP%" org.example.Main

endlocal
pause

