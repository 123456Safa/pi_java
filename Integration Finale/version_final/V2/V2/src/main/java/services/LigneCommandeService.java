package services;

import models.LigneCommandes;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LigneCommandeService implements IService<LigneCommandes> {
    private static final List<String> CANDIDATE_TABLE_NAMES = Arrays.asList(
            "ligne_commande",
            "ligne_commandes",
            "lignes_commandes",
            "lignecommandes",
            "lignescommande"
    );

    private final Connection cnx;

    public LigneCommandeService() {
        cnx = MyConnection.getInstance().getConnection();
        ensureTableExists();
    }

    @Override
    public void add(LigneCommandes ligne) throws SQLException {
        String sql = "INSERT INTO " + resolveTableName() + " (nom, prix, quantite, sous_total, commande_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, ligne.getNom());
            ps.setDouble(2, ligne.getPrix());
            ps.setInt(3, ligne.getQuantite());
            ps.setDouble(4, ligne.getSousTotal());
            ps.setInt(5, ligne.getCommandeId());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(LigneCommandes ligne) throws SQLException {
        String sql = "UPDATE " + resolveTableName() + " SET nom=?, prix=?, quantite=?, sous_total=?, commande_id=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, ligne.getNom());
            ps.setDouble(2, ligne.getPrix());
            ps.setInt(3, ligne.getQuantite());
            ps.setDouble(4, ligne.getSousTotal());
            ps.setInt(5, ligne.getCommandeId());
            ps.setInt(6, ligne.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM " + resolveTableName() + " WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void deleteByCommandeId(int commandeId) throws SQLException {
        String sql = "DELETE FROM " + resolveTableName() + " WHERE commande_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<LigneCommandes> select() throws SQLException {
        List<LigneCommandes> list = new ArrayList<>();
        String sql = "SELECT * FROM " + resolveTableName();

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new LigneCommandes(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getDouble("prix"),
                        rs.getInt("quantite"),
                        rs.getDouble("sous_total"),
                        rs.getInt("commande_id")
                ));
            }
        }

        return list;
    }

    private String resolveTableName() throws SQLException {
        for (String tableName : CANDIDATE_TABLE_NAMES) {
            if (canQueryTable(tableName)) {
                return tableName;
            }
        }
        throw new SQLException("Aucune table de lignes de commande trouvee.");
    }

    private boolean canQueryTable(String tableName) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE 1 = 0";
        try (Statement st = cnx.createStatement()) {
            st.executeQuery(sql);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private void ensureTableExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS lignes_commandes (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    nom VARCHAR(100),
                    prix DOUBLE,
                    quantite INT,
                    sous_total DOUBLE,
                    commande_id INT,
                    FOREIGN KEY (commande_id) REFERENCES commandes(id) ON DELETE CASCADE
                )
                """;

        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Impossible de verifier/creer la table lignes_commandes: " + e.getMessage());
        }
    }
}
