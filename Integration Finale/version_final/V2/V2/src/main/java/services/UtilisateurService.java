package services;

import models.Utilisateur;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurService implements IService<Utilisateur> {

    private Connection cnx;

    public UtilisateurService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void add(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateur(nom, email, mot_de_passe, role) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, u.getNom());
        ps.setString(2, u.getEmail());
        ps.setString(3, u.getMotDePasse());
        ps.setString(4, u.getRole());

        ps.executeUpdate();
        System.out.println("✅ Utilisateur ajouté");
    }

    @Override
    public void update(Utilisateur u) throws SQLException {
        String sql = "UPDATE utilisateur SET nom=?, email=?, mot_de_passe=?, role=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, u.getNom());
        ps.setString(2, u.getEmail());
        ps.setString(3, u.getMotDePasse());
        ps.setString(4, u.getRole());
        ps.setInt(5, u.getId());

        ps.executeUpdate();
        System.out.println("✏️ Utilisateur modifié");
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM utilisateur WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("🗑️ Utilisateur supprimé");
    }

    @Override
    public List<Utilisateur> select() throws SQLException {
        List<Utilisateur> list = new ArrayList<>();

        String sql = "SELECT * FROM utilisateur";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Utilisateur u = new Utilisateur(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("email"),
                    rs.getString("mot_de_passe"),
                    rs.getString("role")
            );

            list.add(u);
        }

        return list;
    }
}
