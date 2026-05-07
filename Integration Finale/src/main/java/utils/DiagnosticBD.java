package utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Classe de diagnostic - Vérifier la BD complètement
 * Lancez cette classe directement pour tester la BD
 */
public class DiagnosticBD {

    public static void main(String[] args) {
        System.out.println("🔍 DIAGNOSTIC BD - PHARMAX");
        System.out.println("═══════════════════════════════════════\n");

        try {
            // 1. Tester la connexion
            System.out.println("1️⃣  TEST CONNEXION");
            Connection conn = MyConnection.getInstance().getConnection();
            if (conn == null) {
                System.err.println("❌ Connexion NULL!");
                return;
            }
            System.out.println("✅ Connexion OK");
            System.out.println("   " + conn);

            // 2. Tester la BD courante
            System.out.println("\n2️⃣  TEST BD ACTUELLE");
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT DATABASE()");
            rs.next();
            String dbName = rs.getString(1);
            System.out.println("✅ BD actuelle: " + (dbName != null ? dbName : "NULL"));

            // 3. Lister les tables
            System.out.println("\n3️⃣  TABLES DISPONIBLES");
            rs = st.executeQuery("SHOW TABLES");
            int tableCount = 0;
            while (rs.next()) {
                String table = rs.getString(1);
                System.out.println("   📋 " + table);
                tableCount++;
            }
            System.out.println("✅ Total: " + tableCount + " tables");

            // 4. Vérifier la table 'produits'
            System.out.println("\n4️⃣  VÉRIFIER TABLE 'produits'");
            try {
                rs = st.executeQuery("SELECT COUNT(*) FROM produits");
                rs.next();
                int count = rs.getInt(1);
                System.out.println("✅ Table 'produits' existe");
                System.out.println("   Nombre de produits: " + count);

                if (count > 0) {
                    System.out.println("\n   Premiers produits:");
                    rs = st.executeQuery("SELECT id, nom, prix FROM produits LIMIT 3");
                    while (rs.next()) {
                        System.out.println("   - ID " + rs.getInt(1) + ": " + rs.getString(2) + " (" + rs.getDouble(3) + " DT)");
                    }
                } else {
                    System.err.println("⚠️  TABLE VIDE - Aucun produit!");
                }
            } catch (Exception e) {
                System.err.println("❌ Table 'produits' n'existe pas!");
                System.err.println("   " + e.getMessage());

                // Proposer la création
                System.out.println("\n   💡 Créer la table? Exécutez:");
                System.out.println("   CREATE TABLE produits (");
                System.out.println("       id INT PRIMARY KEY AUTO_INCREMENT,");
                System.out.println("       nom VARCHAR(100),");
                System.out.println("       prix DOUBLE,");
                System.out.println("       description TEXT,");
                System.out.println("       stock INT");
                System.out.println("   );");
            }

            // 5. Vérifier les autres tables
            System.out.println("\n5️⃣  VÉRIFIER AUTRES TABLES");
            try {
                rs = st.executeQuery("SELECT COUNT(*) FROM commandes");
                rs.next();
                System.out.println("✅ Table 'commandes' existe (" + rs.getInt(1) + " lignes)");
            } catch (Exception e) {
                System.err.println("⚠️  Table 'commandes' n'existe pas");
            }

            try {
                rs = st.executeQuery("SELECT COUNT(*) FROM lignes_commandes");
                rs.next();
                System.out.println("✅ Table 'lignes_commandes' existe (" + rs.getInt(1) + " lignes)");
            } catch (Exception e) {
                System.err.println("⚠️  Table 'lignes_commandes' n'existe pas");
            }

            System.out.println("\n═══════════════════════════════════════");
            System.out.println("✅ DIAGNOSTIC TERMINÉ");

        } catch (Exception e) {
            System.err.println("\n❌ ERREUR CRITIQUE:");
            e.printStackTrace();
        }
    }
}

