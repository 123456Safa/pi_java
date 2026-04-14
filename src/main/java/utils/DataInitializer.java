package utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Initialise les données de test
 * CRÉE la table ET insère les produits
 */
public class DataInitializer {

    public static void initializeSampleData() {
        try {
            Connection conn = MyConnection.getInstance().getConnection();

            if (conn == null) {
                System.err.println("❌ Erreur: Connexion BD est null!");
                return;
            }

            System.out.println("\n=== 🔧 INITIALISATION DE LA BASE DE DONNÉES ===");

            // 1. CRÉER la table si elle n'existe pas
            System.out.println("📋 Vérification/Création table 'produits'...");
            String createTableSQL = "CREATE TABLE IF NOT EXISTS produits (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "nom VARCHAR(255)," +
                    "prix DOUBLE," +
                    "description TEXT," +
                    "stock INT" +
                    ")";

            try {
                Statement st = conn.createStatement();
                st.executeUpdate(createTableSQL);
                System.out.println("✅ Table 'produits' créée/vérifiée");
            } catch (Exception e) {
                System.err.println("❌ Erreur création table: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            // 2. Vérifier si données existent
            System.out.println("📊 Vérification des données existantes...");
            int existingCount = 0;
            try {
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM produits");
                if (rs.next()) {
                    existingCount = rs.getInt(1);
                }
                System.out.println("   📌 Produits actuels en BD: " + existingCount);
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du comptage: " + e.getMessage());
                e.printStackTrace();
            }

            // 3. Si vide, INSÉRER les produits
            if (existingCount == 0) {
                System.out.println("\n📦 INSERTION de 25 produits de test...");

                String[] products = {
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Aspirine 500mg', 4.99, 'Analgésique - 20 comprimés', 50)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Paracétamol 500mg', 3.49, 'Anti-douleur - 16 comprimés', 75)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Ibuprofène 200mg', 5.99, 'Anti-inflammatoire - 24 comprimés', 60)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Sirop contre la toux', 8.50, 'Expectorant - 200ml', 30)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Spray nasal', 6.99, 'Décongestionnant - 15ml', 45)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Pastilles gorge', 2.99, 'Menthe - 20 pastilles', 100)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Oméprazole 20mg', 7.50, 'Anti-reflux - 14 gélules', 40)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Charbon actif', 5.49, 'Troubles digestifs - 30 comprimés', 55)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Probiotiques', 12.99, 'Flore intestinale - 30 gélules', 25)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Vitamines C', 6.99, 'Vitamine C - 30 comprimés', 80)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Multivitamines', 9.99, 'Complément - 30 comprimés', 45)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Calcium + D', 10.99, 'Santé osseuse - 30 comprimés', 35)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Magnésium', 8.49, 'Relaxation - 60 gélules', 50)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Antihistaminique', 7.99, 'Anti-allergène - 30 comprimés', 65)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Pommade anti-itch', 5.99, 'Crème - 50g', 40)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Gel antibactérien', 3.99, 'Désinfectant - 100ml', 120)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Pansements', 4.99, '30 pansements', 90)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Thermomètre', 15.99, 'Sans contact - 1 unité', 20)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Masques', 6.99, '50 masques', 75)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Écran solaire', 11.99, 'SPF 50 - 200ml', 55)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Crème hydratante', 9.49, 'Soin peau - 100ml', 40)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Shampooing', 7.99, 'Médical - 250ml', 35)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Mélatonine', 8.99, 'Sommeil - 60 comprimés', 50)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Glucosamine', 14.99, 'Articulations - 60 gélules', 25)",
                    "INSERT INTO produits (nom, prix, description, stock) VALUES ('Zinc', 6.49, 'Immunité - 30 comprimés', 70)"
                };

                int inserted = 0;
                int failed = 0;
                Statement stmt = conn.createStatement();
                
                for (int i = 0; i < products.length; i++) {
                    try {
                        stmt.executeUpdate(products[i]);
                        inserted++;
                        System.out.println("   ✅ [" + (i+1) + "/25] Produit inséré");
                    } catch (Exception e) {
                        failed++;
                        System.err.println("   ❌ [" + (i+1) + "/25] Erreur: " + e.getMessage());
                    }
                }

                System.out.println("\n✅ RÉSULTAT: " + inserted + "/" + products.length + " produits insérés avec succès!");
                if (failed > 0) {
                    System.err.println("⚠️  " + failed + " insertions ont échoué");
                }

                // Vérification finale
                try {
                    Statement stVerif = conn.createStatement();
                    ResultSet rs = stVerif.executeQuery("SELECT COUNT(*) FROM produits");
                    if (rs.next()) {
                        int finalCount = rs.getInt(1);
                        System.out.println("✅ TOTAL EN BD: " + finalCount + " produits");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erreur lors de la vérification: " + e.getMessage());
                }
            } else {
                System.out.println("✅ Données déjà présentes (" + existingCount + " produits) - Aucune insertion nécessaire");
            }

            System.out.println("✅ Initialisation TERMINÉE\n");

        } catch (Exception e) {
            System.err.println("❌ ERREUR CRITIQUE LORS DE L'INITIALISATION:");
            e.printStackTrace();
        }
    }
}
