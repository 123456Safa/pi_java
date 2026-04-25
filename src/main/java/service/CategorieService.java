package service;

import model.Categorie;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService {
    private Connection cnx;

    public CategorieService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void ajouter(Categorie c) throws SQLException {
        String req = "INSERT INTO categorie (nom, description, created_at) VALUES (/?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getDescription());
        if (c.getCreatedAt() != null) {
            ps.setTimestamp(3, new java.sql.Timestamp(c.getCreatedAt().getTime()));
        } else {
            ps.setNull(3, Types.TIMESTAMP);
        }
        ps.executeUpdate();
    }

    public List<Categorie> afficher() throws SQLException {
        List<Categorie> list = new ArrayList<>();
        String req = "SELECT * FROM categorie";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Categorie c = new Categorie(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("description"),
                rs.getTimestamp("created_at")
            );
            list.add(c);
        }
        return list;
    }

    public void modifier(Categorie c) throws SQLException {
        String req = "UPDATE categorie SET nom=?, description=?, created_at=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
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

    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM categorie WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}

