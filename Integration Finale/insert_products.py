#!/usr/bin/env python3
import mysql.connector
from mysql.connector import Error

try:
    # Connexion à MySQL
    connection = mysql.connector.connect(
        host='localhost',
        user='root',
        password='',
        database='pharm'
    )

    if connection.is_connected():
        cursor = connection.cursor()

        # Créer la table si elle n'existe pas
        print("📋 Création de la table 'produits'...")
        create_table_sql = """
        CREATE TABLE IF NOT EXISTS produits (
            id INT PRIMARY KEY AUTO_INCREMENT,
            nom VARCHAR(100),
            prix DOUBLE,
            description TEXT,
            stock INT
        )
        """
        cursor.execute(create_table_sql)
        print("✅ Table créée/vérifiée")

        # Vérifier si des produits existent déjà
        cursor.execute("SELECT COUNT(*) FROM produits")
        count = cursor.fetchone()[0]
        print(f"📊 Produits actuels: {count}")

        if count == 0:
            # Insérer les produits
            print("📦 Insertion des 25 produits...")
            products = [
                ("Aspirine 500mg", 4.99, "Analgésique - 20 comprimés", 50),
                ("Paracétamol 500mg", 3.49, "Anti-douleur - 16 comprimés", 75),
                ("Ibuprofène 200mg", 5.99, "Anti-inflammatoire - 24 comprimés", 60),
                ("Sirop contre la toux", 8.50, "Expectorant - 200ml", 30),
                ("Spray nasal", 6.99, "Décongestionnant - 15ml", 45),
                ("Pastilles gorge", 2.99, "Menthe - 20 pastilles", 100),
                ("Oméprazole 20mg", 7.50, "Anti-reflux - 14 gélules", 40),
                ("Charbon actif", 5.49, "Troubles digestifs - 30 comprimés", 55),
                ("Probiotiques", 12.99, "Flore intestinale - 30 gélules", 25),
                ("Vitamines C", 6.99, "Vitamine C - 30 comprimés", 80),
                ("Multivitamines", 9.99, "Complément - 30 comprimés", 45),
                ("Calcium + D", 10.99, "Santé osseuse - 30 comprimés", 35),
                ("Magnésium", 8.49, "Relaxation - 60 gélules", 50),
                ("Antihistaminique", 7.99, "Anti-allergène - 30 comprimés", 65),
                ("Pommade anti-itch", 5.99, "Crème - 50g", 40),
                ("Gel antibactérien", 3.99, "Désinfectant - 100ml", 120),
                ("Pansements", 4.99, "30 pansements", 90),
                ("Thermomètre", 15.99, "Sans contact - 1 unité", 20),
                ("Masques", 6.99, "50 masques", 75),
                ("Écran solaire", 11.99, "SPF 50 - 200ml", 55),
                ("Crème hydratante", 9.49, "Soin peau - 100ml", 40),
                ("Shampooing", 7.99, "Médical - 250ml", 35),
                ("Mélatonine", 8.99, "Sommeil - 60 comprimés", 50),
                ("Glucosamine", 14.99, "Articulations - 60 gélules", 25),
                ("Zinc", 6.49, "Immunité - 30 comprimés", 70)
            ]

            insert_sql = "INSERT INTO produits (nom, prix, description, stock) VALUES (%s, %s, %s, %s)"

            for product in products:
                cursor.execute(insert_sql, product)

            connection.commit()
            print(f"✅ {len(products)} produits insérés avec succès!")
        else:
            print(f"✅ La table contient déjà {count} produits")
            # Ajouter des produits de test
            print("📦 Ajout de produits de test...")
            test_products = [
                ("Test Médicament A", 9.99, "Médicament de test A", 10),
                ("Test Médicament B", 14.99, "Médicament de test B", 15)
            ]
            insert_sql = "INSERT INTO produits (nom, prix, description, stock) VALUES (%s, %s, %s, %s)"
            for product in test_products:
                cursor.execute(insert_sql, product)
            connection.commit()
            print(f"✅ {len(test_products)} produits de test ajoutés!")

        # Vérification finale
        cursor.execute("SELECT COUNT(*) FROM produits")
        final_count = cursor.fetchone()[0]
        cursor.execute("SELECT nom, prix FROM produits LIMIT 3")
        print(f"\n✅ TOTAL FINAL: {final_count} produits en BD")
        print("\nExemples:")
        for (nom, prix) in cursor.fetchall():
            print(f"  - {nom}: {prix} DT")

        cursor.close()

except Error as e:
    print(f"❌ Erreur MySQL: {e}")
except Exception as e:
    print(f"❌ Erreur: {e}")
finally:
    if connection.is_connected():
        connection.close()
        print("\n✅ Connexion fermée")
