package services;

import models.Produit;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements IService<Produit> {

    private final Connection cnx;

    public ProduitService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void add(Produit p) throws SQLException {
        String sql = "INSERT INTO produit (nom, description, prix, image, date_expiration, statut, created_at, quantite, categorie_id, prix_final, promo_code, discount_percentage) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrix());
            ps.setString(4, p.getImage());
            if (p.getDateExpiration() != null) {
                ps.setDate(5, new java.sql.Date(p.getDateExpiration().getTime()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setString(6, p.getStatut());
            if (p.getCreatedAt() != null) {
                ps.setTimestamp(7, new java.sql.Timestamp(p.getCreatedAt().getTime()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            ps.setInt(8, p.getQuantite());
            ps.setInt(9, p.getCategorieId());
            ps.setDouble(10, p.getPrixFinal());
            ps.setString(11, p.getPromoCode());
            ps.setDouble(12, p.getDiscountPercentage());
            ps.executeUpdate();
        }
    }

    /** Alias for add — used by GestionProduitFormController */
    public void ajouter(Produit p) throws SQLException {
        add(p);
    }

    @Override
    public List<Produit> select() throws SQLException {
        return afficher();
    }

    public List<Produit> afficher() throws SQLException {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM produit";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public void update(Produit p) throws SQLException {
        modifier(p);
    }

    public void modifier(Produit p) throws SQLException {
        String sql = "UPDATE produit SET nom=?, description=?, prix=?, image=?, date_expiration=?, statut=?, created_at=?, quantite=?, categorie_id=?, prix_final=?, promo_code=?, discount_percentage=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setString(2, p.getDescription());
            ps.setDouble(3, p.getPrix());
            ps.setString(4, p.getImage());
            if (p.getDateExpiration() != null) {
                ps.setDate(5, new java.sql.Date(p.getDateExpiration().getTime()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setString(6, p.getStatut());
            if (p.getCreatedAt() != null) {
                ps.setTimestamp(7, new java.sql.Timestamp(p.getCreatedAt().getTime()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            ps.setInt(8, p.getQuantite());
            ps.setInt(9, p.getCategorieId());
            ps.setDouble(10, p.getPrixFinal());
            ps.setString(11, p.getPromoCode());
            ps.setDouble(12, p.getDiscountPercentage());
            ps.setInt(13, p.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        supprimer(id);
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM produit WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Produit> getExpiringProducts(int days) throws SQLException {
        List<Produit> list = new ArrayList<>();
        String sql = "SELECT * FROM produit WHERE date_expiration IS NOT NULL AND date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Produit mapRow(ResultSet rs) throws SQLException {
        return new Produit(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getDouble("prix"),
                rs.getDouble("prix_final"),
                rs.getString("image"),
                rs.getDate("date_expiration"),
                rs.getString("statut"),
                rs.getTimestamp("created_at"),
                rs.getInt("quantite"),
                rs.getInt("categorie_id"),
                rs.getString("promo_code"),
                rs.getDouble("discount_percentage")
        );
    }
}
