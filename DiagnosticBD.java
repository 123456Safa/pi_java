import java.sql.*;

public class DiagnosticBD {
    public static void main(String[] args) {
        try {
            // Connexion
            String url = "jdbc:mysql://localhost:3306/pharm";
            String user = "root";
            String password = "";

            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connexion réussie à la BD pharm\n");

            // 1. Vérifier les tables
            System.out.println("=== TABLES EXISTANTES ===");
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                System.out.println("📊 Table: " + tables.getString("TABLE_NAME"));
            }

            // 2. Structure de la table commandes
            System.out.println("\n=== STRUCTURE TABLE COMMANDES ===");
            ResultSet columns = meta.getColumns(null, null, "commandes", null);
            while (columns.next()) {
                System.out.println("  📌 " + columns.getString("COLUMN_NAME") +
                                 " → " + columns.getString("TYPE_NAME"));
            }

            // 3. Contenu de la table commandes
            System.out.println("\n=== CONTENU TABLE COMMANDES ===");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM commandes");
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            if (!rs.next()) {
                System.out.println("❌ AUCUNE COMMANDE DANS LA BD!");
            } else {
                System.out.println("✅ Commandes trouvées:");
                do {
                    System.out.print("  📦 ");
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(rsmd.getColumnName(i) + "=" + rs.getObject(i) + " | ");
                    }
                    System.out.println();
                } while (rs.next());
            }

            // 4. Structure lignes_commandes
            System.out.println("\n=== STRUCTURE TABLE LIGNES_COMMANDES ===");
            columns = meta.getColumns(null, null, "lignes_commandes", null);
            if (!columns.next()) {
                System.out.println("❌ Table lignes_commandes n'existe pas!");
            } else {
                do {
                    System.out.println("  📌 " + columns.getString("COLUMN_NAME") +
                                     " → " + columns.getString("TYPE_NAME"));
                } while (columns.next());
            }

            // 5. Count
            System.out.println("\n=== STATISTIQUES ===");
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM commandes");
            if (rs.next()) {
                System.out.println("📊 Total commandes: " + rs.getInt("total"));
            }

            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM lignes_commandes");
            if (rs.next()) {
                System.out.println("📊 Total lignes_commandes: " + rs.getInt("total"));
            }

            // 6. Test insertion
            System.out.println("\n=== TEST INSERTION ===");
            String sqlTest = "INSERT INTO commandes (produits, totales, statut, created_at, utilisateur_id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlTest)) {
                ps.setString(1, "[{\"nom\":\"Test\",\"quantite\":1,\"prix\":10.0,\"sousTotal\":10.0}]");
                ps.setDouble(2, 11.90);
                ps.setString(3, "En attente");
                ps.setString(4, new Timestamp(System.currentTimeMillis()).toString());
                ps.setInt(5, 1);

                int result = ps.executeUpdate();
                System.out.println("✅ Insertion test réussie: " + result + " ligne(s) inséré(es)");

                // Vérifier l'insertion
                rs = stmt.executeQuery("SELECT COUNT(*) as total FROM commandes");
                if (rs.next()) {
                    System.out.println("📊 Total commandes après test: " + rs.getInt("total"));
                }
            } catch (SQLException e) {
                System.out.println("❌ ERREUR INSERTION: " + e.getMessage());
                e.printStackTrace();
            }

            conn.close();
            System.out.println("\n✅ Diagnostic terminé!");

        } catch (SQLException e) {
            System.out.println("❌ ERREUR CONNEXION BD: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
