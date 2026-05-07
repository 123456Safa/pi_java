package services;

import models.Categorie;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService {
    private final Connection cnx;

    public CategorieService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    public void ajouter(Categorie c) throws SQLException {
        String sql = "INSERT INTO categorie (nom, description, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getDescription());
            if (c.getCreatedAt() != null) {
                ps.setTimestamp(3, new java.sql.Timestamp(c.getCreatedAt().getTime()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.executeUpdate();
        }
    }

    public List<Categorie> afficher() throws SQLException {
        List<Categorie> list = new ArrayList<>();
        String sql = "SELECT * FROM categorie";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Categorie(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getTimestamp("created_at")
                ));
            }
        }
        return list;
    }

    public void modifier(Categorie c) throws SQLException {
        String sql = "UPDATE categorie SET nom=?, description=?, created_at=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getDescription());
            if (c.getCreatedAt() != null) {
                ps.setTimestamp(3, new java.sql.Timestamp(c.getCreatedAt().getTime()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }
            ps.setInt(4, c.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM categorie WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
