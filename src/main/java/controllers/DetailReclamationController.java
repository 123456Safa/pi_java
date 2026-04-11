package controllers;

import Model.Reclamation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DetailReclamationController {

    @FXML private Label lblTitre;
    @FXML private Label lblDate;
    @FXML private Label lblStatut;
    @FXML private TextArea txtDescription;

    public void setData(Reclamation r) {

        lblTitre.setText(r.getTitre());
        lblDate.setText(String.valueOf(r.getDateCreation()));
        lblStatut.setText(r.getStatut());
        txtDescription.setText(r.getDescription());
    }

    @FXML
    private void onBack() {
        Stage stage = (Stage) lblTitre.getScene().getWindow();
        stage.close();
    }
}