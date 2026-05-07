@echo off
REM Diagnostic complet pour JavaFX sur Windows
REM Teste toutes les configurations possibles

echo ========================================
echo DIAGNOSTIC JAVAFX WINDOWS
echo ========================================
echo.

set JAVA_HOME=C:\Users\fatma\.jdks\openjdk-26
set JAVA_EXE=%JAVA_HOME%\bin\java.exe
set PROJECT_DIR=C:\Users\fatma\IdeaProjects\pidevjava
set M2_REPO=C:\Users\fatma\.m2\repository

echo [1] Verification des fichiers requis...
echo.

REM Vérifier que Java existe
if not exist "%JAVA_EXE%" (
    echo ERREUR: Java non trouve a %JAVA_EXE%
    goto :error
) else (
    echo OK: Java trouve a %JAVA_EXE%
)

REM Vérifier les JARs JavaFX
set MISSING_JARS=
for %%J in (base graphics controls fxml) do (
    if not exist "%M2_REPO%\org\openjfx\javafx-%%J\20.0.2\javafx-%%J-20.0.2-win.jar" (
        set MISSING_JARS=!MISSING_JARS! %%J
    )
)

if defined MISSING_JARS (
    echo ERREUR: JARs manquants:!MISSING_JARS!
    goto :error
) else (
    echo OK: Tous les JARs JavaFX trouves
)

REM Vérifier les classes compilees
if not exist "%PROJECT_DIR%\target\classes\org\example\Main.class" (
    echo ERREUR: Classes non compilees. Lancez d'abord compile_all_and_run.bat
    goto :error
) else (
    echo OK: Classes compilees trouvees
)

echo.
echo [2] Test des configurations...
echo.

REM Configuration 1: Module path avec ;
set MODULE_PATH=%M2_REPO%\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar

REM Classpath
set CLASSPATH=%PROJECT_DIR%\target\classes;%M2_REPO%\mysql\mysql-connector-java\8.0.26\mysql-connector-java-8.0.26.jar;%M2_REPO%\com\google\protobuf\protobuf-java\3.11.4\protobuf-java-3.11.4.jar;%M2_REPO%\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar;%M2_REPO%\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2.jar;%M2_REPO%\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar

echo Test 1: Configuration Windows correcte (;)
echo Commande: "%JAVA_EXE%" --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -cp "%CLASSPATH%" org.example.Main
echo.

"%JAVA_EXE%" --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -cp "%CLASSPATH%" org.example.Main > test_output.txt 2>&1

if errorlevel 1 (
    echo ERREUR dans Test 1:
    type test_output.txt
    echo.
    goto :error
) else (
    echo SUCCES: Test 1 passe!
    echo Sortie:
    type test_output.txt
    echo.
)

echo ========================================
echo DIAGNOSTIC TERMINE AVEC SUCCES
echo ========================================
echo.
echo Utilisez maintenant: run_app.bat ou compile_all_and_run.bat
echo.

goto :end

:error
echo.
echo ========================================
echo ERREUR DETECTEE - VOIR CI-DESSUS
echo ========================================
echo.

:end
pause
