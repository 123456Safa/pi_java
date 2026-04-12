package Service;

import Model.Reclamation;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService {

    Connection cnx = MyConnection.getInstance();
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
    // ================= GET ALL =================
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

    // ================= UPDATE =================
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

    // ================= DELETE =================
    public void delete(int id) {
        try {
            String sql = "DELETE FROM reclamation WHERE id=?";

            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void add(Reclamation r) {
        try {
            String sql = "INSERT INTO reclamation (titre, description, date_creation, statut) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, r.getTitre());
            ps.setString(2, r.getDescription());
            ps.setDate(3, new java.sql.Date(r.getDateCreation().getTime()));
            ps.setString(4, r.getStatut());


            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}