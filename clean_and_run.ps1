#!/usr/bin/env powershell

# Script pour nettoyer le cache IntelliJ et relancer l'application

Write-Host "===================================" -ForegroundColor Cyan
Write-Host "Nettoyage du cache IntelliJ" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan

# Fermer IntelliJ si ouvert
Write-Host "Fermeture d'IntelliJ..."
Get-Process idea64, idea -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 3

# Supprimer les caches
Write-Host "Suppression du cache..." -ForegroundColor Yellow
Remove-Item -Path ".idea/caches" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path ".idea/indexing" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path ".idea/shelf" -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "Cache supprime !" -ForegroundColor Green

Write-Host ""
Write-Host "===================================" -ForegroundColor Cyan
Write-Host "Lancement de l'application" -ForegroundColor Cyan
Write-Host "===================================" -ForegroundColor Cyan
Write-Host ""

# Configuration des chemins
$javaHome = "C:\Users\fatma\.jdks\openjdk-26"
$m2Repo = "C:\Users\fatma\.m2\repository"

# Build classpath
$classpath = @(
    "target\classes",
    "$m2Repo\mysql\mysql-connector-java\8.0.26\mysql-connector-java-8.0.26.jar",
    "$m2Repo\com\google\protobuf\protobuf-java\3.11.4\protobuf-java-3.11.4.jar",
    "$m2Repo\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2.jar",
    "$m2Repo\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar",
    "$m2Repo\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2.jar",
    "$m2Repo\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar",
    "$m2Repo\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2.jar",
    "$m2Repo\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar",
    "$m2Repo\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2.jar",
    "$m2Repo\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar"
) -join ";"

$modulePath = @(
    "$m2Repo\org\openjfx\javafx-base\20.0.2\javafx-base-20.0.2-win.jar",
    "$m2Repo\org\openjfx\javafx-graphics\20.0.2\javafx-graphics-20.0.2-win.jar",
    "$m2Repo\org\openjfx\javafx-controls\20.0.2\javafx-controls-20.0.2-win.jar",
    "$m2Repo\org\openjfx\javafx-fxml\20.0.2\javafx-fxml-20.0.2-win.jar"
) -join ";"

Write-Host "Lancement PHARMAX Application..." -ForegroundColor Green

& "$javaHome\bin\java.exe" `
    --module-path "$modulePath" `
    --add-modules javafx.controls,javafx.fxml `
    --enable-native-access javafx.graphics `
    -cp "$classpath" `
    org.example.Main

Write-Host ""
Write-Host "Application terminée." -ForegroundColor Cyan

