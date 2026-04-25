package service;

import model.Produit;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService {
    private Connection cnx;

    public ProduitService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void ajouter(Produit p) throws SQLException {
        String req = "INSERT INTO produit (nom, description, prix, image, date_expiration, statut, created_at, quantite, categorie_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
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
        ps.executeUpdate();
    }

    public List<Produit> afficher() throws SQLException {
        List<Produit> list = new ArrayList<>();
        String req = "SELECT * FROM produit";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Produit p = new Produit(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getDouble("prix"),
                rs.getString("image"),
                rs.getDate("date_expiration"),
                rs.getString("statut"),
                rs.getTimestamp("created_at"),
                rs.getInt("quantite"),
                rs.getInt("categorie_id")
            );
            list.add(p);
        }
        return list;
    }

    public void modifier(Produit p) throws SQLException {
        String req = "UPDATE produit SET nom=?, description=?, prix=?, image=?, date_expiration=?, statut=?, created_at=?, quantite=?, categorie_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
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
        ps.setInt(10, p.getId());
        ps.executeUpdate();
    }

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM produit WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public List<Produit> getExpiringProducts(int days) throws SQLException {
        List<Produit> list = new ArrayList<>();
        String req = "SELECT * FROM produit WHERE date_expiration IS NOT NULL AND date_expiration <= DATE_ADD(CURDATE(), INTERVAL ? DAY) AND date_expiration >= CURDATE()";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, days);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Produit p = new Produit(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getDouble("prix"),
                rs.getString("image"),
                rs.getDate("date_expiration"),
                rs.getString("statut"),
                rs.getTimestamp("created_at"),
                rs.getInt("quantite"),
                rs.getInt("categorie_id")
            );
            list.add(p);
        }
        return list;
    }
}
