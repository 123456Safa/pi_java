package services;

import models.Produit;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements IService<Produit> {

    private Connection cnx;

    public ProduitService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void add(Produit p) throws SQLException {
        String sql = "INSERT INTO produits(nom, prix, description, stock) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, p.getNom());
        ps.setDouble(2, p.getPrix());
        ps.setString(3, p.getDescription());
        ps.setInt(4, p.getStock());

        ps.executeUpdate();
        System.out.println("✅ Produit ajouté");
    }

    @Override
    public void update(Produit p) throws SQLException {
        String sql = "UPDATE produits SET nom=?, prix=?, description=?, stock=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, p.getNom());
        ps.setDouble(2, p.getPrix());
        ps.setString(3, p.getDescription());
        ps.setInt(4, p.getStock());
        ps.setInt(5, p.getId());

        ps.executeUpdate();
        System.out.println("✏️ Produit modifié");
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM produits WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("🗑️ Produit supprimé");
    }

    @Override
    public List<Produit> select() throws SQLException {
        List<Produit> list = new ArrayList<>();

        String sql = "SELECT * FROM produits";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Produit p = new Produit(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getDouble("prix"),
                    rs.getString("description"),
                    rs.getInt("stock")
            );

            list.add(p);
        }

        return list;
    }
}
