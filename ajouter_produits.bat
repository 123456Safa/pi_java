@echo off
REM ===============================================
REM PHARMAX - Script d'insertion des produits
REM ===============================================

echo.
echo 📦 Ajout des 25 produits de test dans la base de données pharm...
echo.

REM Exécuter le fichier SQL
mysql -u root -h localhost pharm < AJOUTER_PRODUITS.sql

if %ERRORLEVEL% == 0 (
    echo.
    echo ✅ Produits ajoutés avec succès!
    echo.
) else (
    echo.
    echo ❌ Erreur lors de l'exécution du script SQL
    echo.
)

pause

