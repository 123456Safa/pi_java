package controllers;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import Model.Reclamation;
import Service.ReclamationService;

import java.util.Date;

public class FormReclamationController {

    @FXML
    private TextField titre;

    @FXML
    private TextField description;

    private final ReclamationService service = new ReclamationService();

    @FXML
    public void save() {

        Reclamation r = new Reclamation();

        r.setTitre(titre.getText());
        r.setDescription(description.getText());

        r.setDateCreation(new Date()); // date du PC
        r.setStatut("EN_COURS");

        service.add(r);

        close();
    }

    @FXML
    public void cancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) titre.getScene().getWindow();
        stage.close();
    }
}