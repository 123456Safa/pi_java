import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class TestInsertion {
    public static void main(String[] args) {
        try {
            String url = "jdbc:mysql://localhost:3306/pharm";
            String user = "root";
            String password = "";

            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connexion réussie\n");

            // 1. Test insertion SIMPLE commande
            System.out.println("=== TEST 1: INSERTION COMMANDE SIMPLE ===");
            String sqlCommande = "INSERT INTO commandes (produits, totales, statut, created_at, utilisateur_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sqlCommande, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, "[{\"nom\":\"Test\",\"quantite\":1,\"prix\":10.0,\"sousTotal\":10.0}]");
            ps.setDouble(2, 11.9);
            ps.setString(3, "En attente");
            ps.setString(4, new Timestamp(System.currentTimeMillis()).toString());
            ps.setInt(5, 1);

            int rows = ps.executeUpdate();
            System.out.println("✅ Insertion commande: " + rows + " ligne(s)");

            // Récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            int commandeId = 0;
            if (rs.next()) {
                commandeId = rs.getInt(1);
                System.out.println("✅ ID commande généré: " + commandeId);
            }

            // 2. Test insertion ligne_commandes
            System.out.println("\n=== TEST 2: INSERTION LIGNE ===");
            String sqlLigne = "INSERT INTO ligne_commandes (nom, prix, quantite, sous_total, commande_id) VALUES (?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sqlLigne);

            ps.setString(1, "Test Produit");
            ps.setDouble(2, 10.0);
            ps.setInt(3, 1);
            ps.setDouble(4, 10.0);
            ps.setInt(5, commandeId);

            rows = ps.executeUpdate();
            System.out.println("✅ Insertion ligne: " + rows + " ligne(s)");

            // 3. Vérifier
            System.out.println("\n=== VÉRIFICATION ===");
            rs = conn.createStatement().executeQuery("SELECT * FROM commandes WHERE id = " + commandeId);
            if (rs.next()) {
                System.out.println("✅ Commande trouvée: ID=" + rs.getInt("id") +
                                 ", Total=" + rs.getDouble("totales") +
                                 ", Statut=" + rs.getString("statut"));
            }

            rs = conn.createStatement().executeQuery("SELECT * FROM ligne_commandes WHERE commande_id = " + commandeId);
            if (rs.next()) {
                System.out.println("✅ Ligne trouvée: ID=" + rs.getInt("id") +
                                 ", Nom=" + rs.getString("nom") +
                                 ", Commande_ID=" + rs.getInt("commande_id"));
            }

            conn.close();
            System.out.println("\n✅ TEST RÉUSSI! L'insertion fonctionne!");

        } catch (SQLException e) {
            System.out.println("❌ ERREUR SQL: " + e.getMessage());
            System.out.println("Code: " + e.getErrorCode());
            System.out.println("SQLState: " + e.getSQLState());
            e.printStackTrace();
        }
    }
}
