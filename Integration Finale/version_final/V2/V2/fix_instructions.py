#!/usr/bin/env python3
"""
Script de correction automatique de la base de données PHARMAX
Exécute les corrections SQL pour corriger les erreurs de commande
"""

import subprocess
import sys
import os

def main():
    print("=" * 60)
    print("🔧 CORRECTION AUTOMATIQUE - PHARMAX")
    print("=" * 60)
    print()

    print("📋 Corrections à appliquer:")
    print("   1. Créer la table 'user'")
    print("   2. Corriger la colonne 'date' → 'created_date'")
    print("   3. Ajouter contrainte de clé étrangère")
    print("   4. Insérer utilisateurs par défaut")
    print()

    print("⚠️  IMPORTANT:")
    print("   Assurez-vous que:")
    print("   - MySQL est en cours d'exécution")
    print("   - Vous avez accès à la base de données 'pharm'")
    print("   - Vous avez les droits administrateur")
    print()

    print("Fichiers SQL à exécuter (dans cet ordre):")
    print("   1. SETUP_BD_COMPLETE.sql")
    print("   2. OU FIX_DATABASE.sql (pour une BD existante)")
    print()

    print("📄 Pour les utilisateurs IntelliJ IDEA:")
    print("   1. Ouvrez 'Database' panel (View → Tool Windows → Database)")
    print("   2. Clic-droit sur la connexion MySQL")
    print("   3. Sélectionnez 'Execute SQL Script'")
    print("   4. Choisissez le fichier SQL")
    print()

    print("📄 Pour les utilisateurs PhpMyAdmin:")
    print("   1. Allez sur http://localhost/phpmyadmin")
    print("   2. Sélectionnez la base 'pharm'")
    print("   3. Cliquez sur 'Importer'")
    print("   4. Choisissez le fichier SQL")
    print()

    print("📄 Pour les utilisateurs MySQL Workbench:")
    print("   1. File → Open SQL Script")
    print("   2. Sélectionnez le fichier SQL")
    print("   3. Cliquez sur 'Execute'")
    print()

    print("✅ Une fois les scripts exécutés:")
    print("   1. Recompiler le projet (Build → Rebuild)")
    print("   2. Redémarrer l'application")
    print("   3. Tester la création de commande")
    print()

if __name__ == "__main__":
    main()

