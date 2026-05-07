import java.sql.*;

public class CreateTables {
    public static void main(String[] args) {
        try {
            String url = "jdbc:mysql://localhost:3306/pharm";
            String user = "root";
            String password = "";

            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connexion réussie\n");

            Statement stmt = conn.createStatement();

            // 1. Créer ligne_commandes si n'existe pas
            String sql1 = "CREATE TABLE IF NOT EXISTS ligne_commandes (\n" +
                         "    id INT PRIMARY KEY AUTO_INCREMENT,\n" +
                         "    nom VARCHAR(255) NOT NULL,\n" +
                         "    prix DOUBLE NOT NULL,\n" +
                         "    quantite INT NOT NULL,\n" +
                         "    sous_total DOUBLE NOT NULL,\n" +
                         "    commande_id INT,\n" +
                         "    FOREIGN KEY (commande_id) REFERENCES commandes(id) ON DELETE CASCADE\n" +
                         ")";

            stmt.executeUpdate(sql1);
            System.out.println("✅ Table 'ligne_commandes' vérifiée/créée");

            // 2. Créer lignes_commandes si n'existe pas
            String sql2 = "CREATE TABLE IF NOT EXISTS lignes_commandes (\n" +
                         "    id INT PRIMARY KEY AUTO_INCREMENT,\n" +
                         "    nom VARCHAR(255) NOT NULL,\n" +
                         "    prix DOUBLE NOT NULL,\n" +
                         "    quantite INT NOT NULL,\n" +
                         "    sous_total DOUBLE NOT NULL,\n" +
                         "    commande_id INT,\n" +
                         "    FOREIGN KEY (commande_id) REFERENCES commandes(id) ON DELETE CASCADE\n" +
                         ")";

            stmt.executeUpdate(sql2);
            System.out.println("✅ Table 'lignes_commandes' vérifiée/créée");

            // 3. Ajouter les index
            try {
                stmt.executeUpdate("CREATE INDEX idx_ligne_commande_id ON ligne_commandes(commande_id)");
                System.out.println("✅ Index créé sur 'ligne_commandes'");
            } catch (Exception e) {
                System.out.println("ℹ️  Index existe déjà pour 'ligne_commandes'");
            }

            try {
                stmt.executeUpdate("CREATE INDEX idx_lignes_commande_id ON lignes_commandes(commande_id)");
                System.out.println("✅ Index créé sur 'lignes_commandes'");
            } catch (Exception e) {
                System.out.println("ℹ️  Index existe déjà pour 'lignes_commandes'");
            }

            // 4. Vérifier les tables
            System.out.println("\n=== VÉRIFICATION ===");

            // Vérifier ligne_commandes
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM ligne_commandes");
            if (rs.next()) {
                System.out.println("📊 Total 'ligne_commandes': " + rs.getInt("total"));
            }

            // Vérifier lignes_commandes
            rs = stmt.executeQuery("SELECT COUNT(*) as total FROM lignes_commandes");
            if (rs.next()) {
                System.out.println("📊 Total 'lignes_commandes': " + rs.getInt("total"));
            }

            // Test insertion
            System.out.println("\n=== TEST INSERTION ===");
            String sqlInsert = "INSERT INTO ligne_commandes (nom, prix, quantite, sous_total, commande_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sqlInsert);
            ps.setString(1, "Test Produit");
            ps.setDouble(2, 10.0);
            ps.setInt(3, 1);
            ps.setDouble(4, 10.0);
            ps.setInt(5, 1);

            int result = ps.executeUpdate();
            System.out.println("✅ Test insertion réussie: " + result + " ligne(s)");

            conn.close();
            System.out.println("\n✅ Configuration complète!");

        } catch (SQLException e) {
            System.out.println("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
