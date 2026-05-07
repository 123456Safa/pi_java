package services;

import models.Reclamation;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService {

    private final Connection cnx = MyConnection.getInstance().getConnection();

    public void updateStatus(Reclamation r) {
        try {
            String sql = "UPDATE reclamation SET statut=? WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, r.getStatut());
            ps.setInt(2, r.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Reclamation> searchByTitre(String titre) {
        List<Reclamation> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM reclamation WHERE titre LIKE ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, "%" + titre + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId(rs.getInt("id"));
                r.setTitre(rs.getString("titre"));
                r.setDescription(rs.getString("description"));
                r.setDateCreation(rs.getDate("date_creation"));
                r.setStatut(rs.getString("statut"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Reclamation> getAll(int userId) {
        List<Reclamation> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM reclamation WHERE user_id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId(rs.getInt("id"));
                r.setTitre(rs.getString("titre"));
                r.setDescription(rs.getString("description"));
                r.setDateCreation(rs.getDate("date_creation"));
                r.setStatut(rs.getString("statut"));
                r.setUserId(rs.getInt("user_id"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Reclamation> getAll() {
        List<Reclamation> list = new ArrayList<>();
        try {
            String sql = "SELECT * FROM reclamation ORDER BY id DESC";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId(rs.getInt("id"));
                r.setTitre(rs.getString("titre"));
                r.setDescription(rs.getString("description"));
                r.setDateCreation(rs.getDate("date_creation"));
                r.setStatut(rs.getString("statut"));
                list.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public String getUserEmailById(int userId) {
        try {
            String sql = "SELECT email FROM user WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getUserNameById(int userId) {
        try {
            String sql = "SELECT first_name FROM user WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("first_name");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Utilisateur";
    }

    public void update(Reclamation r) {
        try {
            String sql = "UPDATE reclamation SET titre=?, description=? WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, r.getTitre());
            ps.setString(2, r.getDescription());
            ps.setInt(3, r.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        try {
            String sql1 = "DELETE FROM reponse WHERE reclamation_id=?";
            PreparedStatement ps1 = cnx.prepareStatement(sql1);
            ps1.setInt(1, id);
            ps1.executeUpdate();

            String sql2 = "DELETE FROM reclamation WHERE id=?";
            PreparedStatement ps2 = cnx.prepareStatement(sql2);
            ps2.setInt(1, id);
            ps2.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void add(Reclamation r) {
        try {
            String sql = "INSERT INTO reclamation (titre, description, date_creation, statut, user_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, r.getTitre());
            ps.setString(2, r.getDescription());
            ps.setDate(3, new java.sql.Date(r.getDateCreation().getTime()));
            ps.setString(4, r.getStatut());
            ps.setInt(5, r.getUserId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
