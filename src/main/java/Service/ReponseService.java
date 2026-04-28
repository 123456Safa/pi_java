package Service;

import Model.Reponse;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReponseService    {




    Connection cnx = MyConnection.getInstance();
    public void add(Reponse r) {
        try {
            String sql = "INSERT INTO reponse (contenu, date_reponse, reclamation_id) VALUES (?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, r.getContenu());
            ps.setDate(2, new java.sql.Date(r.getDateReponse().getTime()));
            ps.setInt(3, r.getReclamationId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<Reponse> getByReclamationId(int id) {

        List<Reponse> list = new ArrayList<>();

        try {
            String sql = "SELECT * FROM reponse WHERE reclamation_id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Reponse r = new Reponse();
                r.setId(rs.getInt("id"));
                r.setContenu(rs.getString("contenu"));
                r.setDateReponse(rs.getDate("date_reponse"));
                r.setReclamationId(rs.getInt("reclamation_id"));

                list.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void update(Reponse r) {
        try {
            String sql = "UPDATE reponse SET contenu=?, date_reponse=? WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setString(1, r.getContenu());
            ps.setDate(2, new java.sql.Date(r.getDateReponse().getTime()));
            ps.setInt(3, r.getId());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        try {
            String sql = "DELETE FROM reponse WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}