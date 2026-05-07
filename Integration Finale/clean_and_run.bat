@echo off
REM Script pour nettoyer le cache IntelliJ et relancer l'application

echo ===================================
echo Nettoyage du cache IntelliJ
echo ===================================

REM Fermer IntelliJ si ouvert
taskkill /F /IM idea64.exe >nul 2>&1
taskkill /F /IM idea.exe >nul 2>&1

REM Attendre que les processus se terminent
timeout /t 3 /nobreak

REM Supprimer les caches
echo Suppression du cache...
rmdir /S /Q ".idea\caches" 2>nul
rmdir /S /Q ".idea\indexing" 2>nul
rmdir /S /Q ".idea\shelf" 2>nul

echo Cache supprime !

echo.
echo ===================================
echo Lancement de l'application
echo ===================================
echo.

REM Lancer l'application avec les bonnes options JavaFX
set JAVA_HOME=C:\Users\fatma\.jdks\openjdk-26
set CLASSPATH=target\classes
set CLASSPATH=%CLASSPATH%;C:\Users\fatma\.m2\repository\mysql\mysql-connector-java\8.0.26\mysql-connector-java-8.0.26.jar
set CLASSPATH=%CLASSPATH%;C:\Users\fatma\.m2\repository\com\google\protobuf\protobuf-java\3.11.4\protobuf-java-3.11.4.jar
set CLASSPATH=%CLASSPATH%;C:\Users\fatma\.m2\repository\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2.jar
set CLASSPATH=%CLASSPATH%;C:\Users\fatma\.m2\repository\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar
set CLASSPATH=%CLASSPATH%;C:\Users\fatma\.m2\repository\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2.jar
set CLASSPATH=%CLASSPATH%;C:\Users\fatma\.m2\repository\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar
set CLASSPATH=%CLASSPATH%;C:\Users\fatma\.m2\repository\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2.jar
set CLASSPATH=%CLASSPATH%;C:\Users\fatma\.m2\repository\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar
set CLASSPATH=%CLASSPATH%;C:\Users\fatma\.m2\repository\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2.jar
set CLASSPATH=%CLASSPATH%;C:\Users\fatma\.m2\repository\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar

echo Lancement PHARMAX...
"%JAVA_HOME%\bin\java.exe" ^
  --module-path "C:\Users\fatma\.m2\repository\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar;C:\Users\fatma\.m2\repository\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar;C:\Users\fatma\.m2\repository\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar;C:\Users\fatma\.m2\repository\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar" ^
  --add-modules javafx.controls,javafx.fxml ^
  --enable-native-access javafx.graphics ^
  -cp "%CLASSPATH%" ^
  org.example.Main

pause

