package controllers;

import Model.Reclamation;
import Service.ReclamationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;



public class DetailReclamationControlleradmin {
    private final ReclamationService service = new ReclamationService();
    @FXML private Label titre, description, date, statut;
    private HomeAdminController homeController;

    public void setHomeController(HomeAdminController c) {
        this.homeController = c;
    }
    private Reclamation r;

    public void setData(Reclamation r) {
        this.r = r;

        titre.setText(r.getTitre());
        description.setText(r.getDescription());
        date.setText(r.getDateCreation().toString());
        statut.setText(r.getStatut());
    }

    @FXML
    public void repondre() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reponseAdmin.fxml"));
            Parent root = loader.load();

            ReponseFormController c = loader.getController();
            c.setReclamation(r);

            Stage stage = new Stage();   // ✅ نفس stage
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void modifier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modstatus.fxml"));
            Parent root = loader.load();

            ModifierStatusController c = loader.getController();
            c.setData(r); // نبعث reclamation

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}