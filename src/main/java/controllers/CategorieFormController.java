package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Categorie;
import service.CategorieService;

import java.sql.SQLException;
import java.util.Date;

public class CategorieFormController {
    @FXML private TextField tfNom;
    @FXML private TextArea taDescription;
    @FXML private Button btnAjouter;
    @FXML private Button btnAnnuler;
    @FXML private Label lblHeader;
    @FXML private Label lblNomValidation;
    @FXML private Label lblDescriptionValidation;

    private CategorieService categorieService = new CategorieService();
    private Stage stage;
    private CategorieController parentController;
    private Categorie categorieToEdit = null;
    private boolean isEditMode = false;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setParentController(CategorieController parentController) {
        this.parentController = parentController;
    }

    public void setCategorieToEdit(Categorie categorie) {
        this.categorieToEdit = categorie;
        this.isEditMode = true;
        // Remplir le formulaire immédiatement après la configuration
        remplirFormulaire();
    }

    @FXML
    public void initialize() {
        // Ajouter les listeners pour la validation en temps réel
        tfNom.textProperty().addListener((obs, oldVal, newVal) -> validerNom());
        taDescription.textProperty().addListener((obs, oldVal, newVal) -> validerDescription());
    }

    /**
     * Valide le champ Nom en temps réel
     */
    private void validerNom() {
        String nom = tfNom.getText().trim();
        lblNomValidation.getStyleClass().removeAll("validation-error", "validation-success");

        if (nom.isEmpty()) {
            lblNomValidation.setText("⚠ Veuillez entrer un nom");
            lblNomValidation.getStyleClass().add("validation-error");
            tfNom.setStyle("");
        } else if (nom.length() < 3) {
            lblNomValidation.setText("⚠ Minimum 3 caractères");
            lblNomValidation.getStyleClass().add("validation-error");
            tfNom.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
        } else if (nom.length() > 100) {
            lblNomValidation.setText("⚠ Maximum 100 caractères");
            lblNomValidation.getStyleClass().add("validation-error");
            tfNom.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
        } else {
            lblNomValidation.setText("✓ Nom valide");
            lblNomValidation.getStyleClass().add("validation-success");
            tfNom.setStyle("-fx-border-color: #28a745; -fx-border-width: 2;");
        }
    }

    /**
     * Valide le champ Description en temps réel
     */
    private void validerDescription() {
        String description = taDescription.getText().trim();
        lblDescriptionValidation.getStyleClass().removeAll("validation-error", "validation-success");

        if (description.isEmpty()) {
            lblDescriptionValidation.setText("");
            taDescription.setStyle("");
        } else if (description.length() > 500) {
            lblDescriptionValidation.setText("⚠ Maximum 500 caractères");
            lblDescriptionValidation.getStyleClass().add("validation-error");
            taDescription.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
        } else {
            lblDescriptionValidation.setText("✓ Description valide (" + description.length() + "/500)");
            lblDescriptionValidation.getStyleClass().add("validation-success");
            taDescription.setStyle("-fx-border-color: #28a745; -fx-border-width: 2;");
        }
    }

    private void remplirFormulaire() {
        if (categorieToEdit != null) {
            lblHeader.setText("Modifier la catégorie");
            tfNom.setText(categorieToEdit.getNom() != null ? categorieToEdit.getNom() : "");
            taDescription.setText(categorieToEdit.getDescription() != null ? categorieToEdit.getDescription() : "");
            btnAjouter.setText("Modifier");
        }
    }

    @FXML
    private void onAjouter(ActionEvent event) {
        try {
            // Validation des champs
            String nom = tfNom.getText().trim();
            String description = taDescription.getText().trim();
            
            // Validation du nom
            if (nom.isEmpty()) {
                lblNomValidation.setText("❌ Veuillez entrer un nom");
                lblNomValidation.getStyleClass().removeAll("validation-success");
                lblNomValidation.getStyleClass().add("validation-error");
                return;
            }
            
            if (nom.length() < 3) {
                lblNomValidation.setText("❌ Minimum 3 caractères");
                lblNomValidation.getStyleClass().removeAll("validation-success");
                lblNomValidation.getStyleClass().add("validation-error");
                return;
            }
            
            if (nom.length() > 100) {
                lblNomValidation.setText("❌ Maximum 100 caractères");
                lblNomValidation.getStyleClass().removeAll("validation-success");
                lblNomValidation.getStyleClass().add("validation-error");
                return;
            }
            
            // Validation de la description
            if (description.length() > 500) {
                lblDescriptionValidation.setText("❌ Maximum 500 caractères");
                lblDescriptionValidation.getStyleClass().removeAll("validation-success");
                lblDescriptionValidation.getStyleClass().add("validation-error");
                return;
            }

            // Créer ou obtenir la catégorie
            Categorie categorie = isEditMode ? categorieToEdit : new Categorie();
            categorie.setNom(nom);
            categorie.setDescription(description);

            // Si en mode ajout, ajouter la date de création actuelle
            if (!isEditMode) {
                categorie.setCreatedAt(new Date());
                categorieService.ajouter(categorie);
            } else {
                // Si en mode édition, mettre à jour
                categorieService.modifier(categorie);
            }

            // Afficher un message de succès temporaire
            String messageSucces = isEditMode ? "Catégorie modifiée avec succès! ✓" : "Catégorie ajoutée avec succès! ✓";
            lblNomValidation.setText(messageSucces);
            lblNomValidation.getStyleClass().removeAll("validation-error");
            lblNomValidation.getStyleClass().add("validation-success");

            // Rafraîchir la table du contrôleur parent
            if (parentController != null) {
                parentController.rafraichirTable();
            }

            // Fermer après un court délai
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1000));
            pause.setOnFinished(e -> stage.close());
            pause.play();

        } catch (SQLException e) {
            e.printStackTrace();
            lblNomValidation.setText("❌ Erreur base de données");
            lblNomValidation.getStyleClass().removeAll("validation-success");
            lblNomValidation.getStyleClass().add("validation-error");
        } catch (Exception e) {
            e.printStackTrace();
            lblNomValidation.setText("❌ Erreur inattendue");
            lblNomValidation.getStyleClass().removeAll("validation-success");
            lblNomValidation.getStyleClass().add("validation-error");
        }
    }

    @FXML
    private void onAnnuler(ActionEvent event) {
        stage.close();
    }
}

