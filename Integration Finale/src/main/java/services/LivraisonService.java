package services;

import models.Livraison;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LivraisonService implements IService<Livraison> {
    private final Connection cnx;

    public LivraisonService() {
        cnx = MyConnection.getInstance().getConnection();
        ensureTableExists();
    }

    @Override
    public void add(Livraison livraison) throws SQLException {
        String sql = "INSERT INTO livraisons (last_name, first_name, email, adresse, tel, created_at, commande_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, livraison.getLastName());
            ps.setString(2, livraison.getFirstName());
            ps.setString(3, livraison.getEmail());
            ps.setString(4, livraison.getAdresse());
            ps.setString(5, livraison.getTel());
            ps.setString(6, livraison.getCreatedAt());
            ps.setInt(7, livraison.getCommandeId());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Livraison livraison) throws SQLException {
        String sql = "UPDATE livraisons SET last_name=?, first_name=?, email=?, adresse=?, tel=?, created_at=?, commande_id=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, livraison.getLastName());
            ps.setString(2, livraison.getFirstName());
            ps.setString(3, livraison.getEmail());
            ps.setString(4, livraison.getAdresse());
            ps.setString(5, livraison.getTel());
            ps.setString(6, livraison.getCreatedAt());
            ps.setInt(7, livraison.getCommandeId());
            ps.setInt(8, livraison.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM livraisons WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void deleteByCommandeId(int commandeId) throws SQLException {
        String sql = "DELETE FROM livraisons WHERE commande_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Livraison> select() throws SQLException {
        List<Livraison> list = new ArrayList<>();
        String sql = "SELECT * FROM livraisons";

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Livraison(
                        rs.getInt("id"),
                        rs.getString("last_name"),
                        rs.getString("first_name"),
                        rs.getString("email"),
                        rs.getString("adresse"),
                        rs.getString("tel"),
                        rs.getString("created_at"),
                        rs.getInt("commande_id")
                ));
            }
        }

        return list;
    }

    private void ensureTableExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS livraisons (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    last_name VARCHAR(100),
                    first_name VARCHAR(100),
                    email VARCHAR(255),
                    adresse VARCHAR(255),
                    tel VARCHAR(30),
                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    commande_id INT,
                    FOREIGN KEY (commande_id) REFERENCES commandes(id) ON DELETE CASCADE
                )
                """;

        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Impossible de verifier/creer la table livraisons: " + e.getMessage());
        }
    }
}
