package services;

import models.Produit;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProduitService implements IService<Produit> {
    private static final List<String> CANDIDATE_TABLE_NAMES = Arrays.asList(
            "produits",
            "produit"
    );

    private final Connection cnx;

    public ProduitService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void add(Produit p) throws SQLException {
        String sql = "INSERT INTO " + resolveTableName() + " (nom, prix, description, stock) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getPrix());
            ps.setString(3, p.getDescription());
            ps.setInt(4, p.getStock());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Produit p) throws SQLException {
        String sql = "UPDATE " + resolveTableName() + " SET nom=?, prix=?, description=?, stock=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setDouble(2, p.getPrix());
            ps.setString(3, p.getDescription());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getId());
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

    @Override
    public List<Produit> select() throws SQLException {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM " + resolveTableName();

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Produit(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getDouble("prix"),
                        rs.getString("description"),
                        rs.getInt("stock")
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
        throw new SQLException("Aucune table de produits trouvee.");
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
}
