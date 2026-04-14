package controllers;

import Model.Reclamation;
import Service.ReclamationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Date;

public class FormReclamationController {

    @FXML private TextField titre;
    @FXML private TextArea description;
    @FXML private Label titreError;
    @FXML private Label descError;
    @FXML private Label titreCharCount;
    @FXML private Label descCharCount;

    private HomeReclamationController homeController;
    private final ReclamationService service = new ReclamationService();

    @FXML
    public void initialize() {
        // Add character counter listeners
        titre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (titreCharCount != null) {
                titreCharCount.setText(newVal.length() + "/255");
            }
        });

        description.textProperty().addListener((obs, oldVal, newVal) -> {
            if (descCharCount != null) {
                descCharCount.setText(newVal.length() + "/2000");
            }
        });
    }

    public void setHomeController(HomeReclamationController c) {
        this.homeController = c;
    }

    @FXML
    public void save() {

        boolean valid = true;

        titreError.setText("");
        descError.setText("");

        String t = titre.getText();
        String d = description.getText();

        if (t == null || t.length() < 5) {
            titreError.setText("Min 5 caractères");
            valid = false;
        } else if (t.length() > 255) {
            titreError.setText("Max 255 caractères");
            valid = false;
        }

        if (d == null || d.length() < 20) {
            descError.setText("Min 20 caractères");
            valid = false;
        } else if (d.length() > 2000) {
            descError.setText("Max 2000 caractères");
            valid = false;
        }

        if (!valid) return;

        Reclamation r = new Reclamation();
        r.setTitre(t);
        r.setDescription(d);
        r.setDateCreation(new java.sql.Date(System.currentTimeMillis()));
        r.setStatut("EN ATTENTE");

        service.add(r);

        if (homeController != null) {
            homeController.load();
        }

        ((Stage) titre.getScene().getWindow()).close();
    }

    @FXML
    public void cancel() {
        ((Stage) titre.getScene().getWindow()).close();
    }
}