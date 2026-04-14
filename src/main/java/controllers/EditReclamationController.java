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
    @FXML private Label titreCharCount;
    @FXML private Label descCharCount;
    @FXML private Label lblStatusActuel;

    private Reclamation r;
    private final ReclamationService service = new ReclamationService();
    private HomeReclamationController homeController;

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

    public void setData(Reclamation r) {
        this.r = r;
        titre.setText(r.getTitre());
        description.setText(r.getDescription());
        
        // Update character counts
        if (titreCharCount != null) {
            titreCharCount.setText(r.getTitre().length() + "/255");
        }
        if (descCharCount != null) {
            descCharCount.setText(r.getDescription().length() + "/2000");
        }
        
        // Display current status
        if (lblStatusActuel != null) {
            lblStatusActuel.setText(r.getStatut());
            String statusColor = getStatusColor(r.getStatut());
            lblStatusActuel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 5 10; -fx-border-radius: 3;");
        }
    }

    private String getStatusColor(String statut) {
        switch (statut.toUpperCase()) {
            case "EN ATTENTE":
                return "#FFA500";
            case "RÉSOLUE":
                return "#28a745";

            case "EN COURS":
                return "#17a2b8";
            default:
                return "#999999";
        }
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