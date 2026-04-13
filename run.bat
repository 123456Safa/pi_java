@echo off
REM Script pour executer l'application JavaFX

REM Ajouter Maven au PATH
set PATH=%PATH%;C:\Users\islem\maven\bin

REM Se placer dans le repertoire du projet
cd /d "C:\Users\islem\Desktop\piDev\projetDev"

REM Lancer l'application
mvn javafx:run

pause

