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
        // L'initialisation est vide car remplirFormulaire() est appelé dans setCategorieToEdit()
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
                showError("Validation", "Veuillez entrer le nom de la catégorie");
                return;
            }
            
            if (nom.length() < 3) {
                showError("Validation", "Le nom doit contenir au moins 3 caractères");
                return;
            }
            
            if (nom.length() > 100) {
                showError("Validation", "Le nom ne peut pas dépasser 100 caractères");
                return;
            }
            
            // Validation de la description
            if (description.length() > 500) {
                showError("Validation", "La description ne peut pas dépasser 500 caractères");
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
                showSuccess("Succès", "Catégorie ajoutée avec succès!");
            } else {
                // Si en mode édition, mettre à jour
                categorieService.modifier(categorie);
                showSuccess("Succès", "Catégorie modifiée avec succès!");
            }

            // Rafraîchir la table du contrôleur parent
            if (parentController != null) {
                parentController.rafraichirTable();
            }

            // Fermer la fenêtre
            stage.close();

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur base de données", "Une erreur est survenue: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur inattendue est survenue: " + e.getMessage());
        }
    }

    @FXML
    private void onAnnuler(ActionEvent event) {
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.show();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.show();
    }
}

