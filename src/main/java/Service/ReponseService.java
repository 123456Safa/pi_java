package Service;

import Model.Reponse;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ReponseService {




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
}