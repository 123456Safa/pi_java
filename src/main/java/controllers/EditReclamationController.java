package controllers;

import Model.Reclamation;
import Service.ReclamationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditReclamationController {

    @FXML private TextField titre;
    @FXML private TextArea description;

    @FXML private Label titreError;
    @FXML private Label descError;

    private Reclamation r;
    private final ReclamationService service = new ReclamationService();
    private HomeReclamationController homeController;

    public void setHomeController(HomeReclamationController c) {
        this.homeController = c;
    }

    public void setData(Reclamation r) {
        this.r = r;
        titre.setText(r.getTitre());
        description.setText(r.getDescription());
    }

    @FXML
    public void save() {

        boolean valid = true;

        titreError.setText("");
        descError.setText("");

        String t = titre.getText();
        String d = description.getText();

        //  validation titre
        if (t == null || t.trim().length() < 5) {
            titreError.setText("Le titre doit contenir au moins 5 caractères");
            valid = false;
        } else if (t.length() > 255) {
            titreError.setText("Le titre ne doit pas dépasser 255 caractères");
            valid = false;
        }

        //  validation description
        if (d == null || d.trim().length() < 20) {
            descError.setText("La description doit contenir au moins 20 caractères");
            valid = false;
        } else if (d.length() > 2000) {
            descError.setText("La description ne doit pas dépasser 2000 caractères");
            valid = false;
        }

        if (!valid) return;

        // ✅ update DB
        r.setTitre(t);
        r.setDescription(d);
        service.update(r);

        // 🔥 refresh home
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