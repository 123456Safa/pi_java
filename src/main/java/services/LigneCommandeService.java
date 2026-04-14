package services;

import models.LigneCommandes;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LigneCommandeService implements IService<LigneCommandes> {

    private Connection cnx;

    public LigneCommandeService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void add(LigneCommandes l) throws SQLException {
        String sql = "INSERT INTO ligne_commande(nom, prix, quantite, sous_total, commande_id) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, l.getNom());
        ps.setDouble(2, l.getPrix());
        ps.setInt(3, l.getQuantite());
        ps.setDouble(4, l.getSousTotal());
        ps.setInt(5, l.getCommandeId());

        ps.executeUpdate();
        System.out.println("✅ Ligne commande ajoutée");
    }

    @Override
    public void update(LigneCommandes l) throws SQLException {
        String sql = "UPDATE ligne_commande SET nom=?, prix=?, quantite=?, sous_total=?, commande_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, l.getNom());
        ps.setDouble(2, l.getPrix());
        ps.setInt(3, l.getQuantite());
        ps.setDouble(4, l.getSousTotal());
        ps.setInt(5, l.getCommandeId());
        ps.setInt(6, l.getId());

        ps.executeUpdate();
        System.out.println("✏️ Ligne commande modifiée");
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM ligne_commande WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("🗑️ Ligne supprimée");
    }

    @Override
    public List<LigneCommandes> select() throws SQLException {
        List<LigneCommandes> list = new ArrayList<>();

        String sql = "SELECT * FROM ligne_commande";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            LigneCommandes l = new LigneCommandes(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getDouble("prix"),
                    rs.getInt("quantite"),
                    rs.getDouble("sous_total"),
                    rs.getInt("commande_id")
            );

            list.add(l);
        }

        return list;
    }
}