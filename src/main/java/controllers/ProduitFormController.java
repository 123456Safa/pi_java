package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import model.Produit;
import model.Categorie;
import service.ProduitService;
import service.CategorieService;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class ProduitFormController {
    @FXML private TextField tfNom;
    @FXML private TextArea taDescription;
    @FXML private TextField tfPrix;
    @FXML private TextField tfQuantite;
    @FXML private ComboBox<String> cbStatut;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private DatePicker dpDateExpiration;
    @FXML private TextField tfImage;
    @FXML private Button btnBrowseImage;
    @FXML private ImageView imgPreview;
    @FXML private Button btnAjouter;
    @FXML private Button btnAnnuler;
    @FXML private Label lblHeader;
    @FXML private Label lblNomValidation;
    @FXML private Label lblDescriptionValidation;
    @FXML private Label lblPrixValidation;
    @FXML private Label lblQuantiteValidation;
    @FXML private Label lblStatutValidation;
    @FXML private Label lblCategorieValidation;
    @FXML private Label lblDateExpirationValidation;
    @FXML private Label lblImageValidation;

    private ProduitService produitService = new ProduitService();
    private CategorieService categorieService = new CategorieService();
    private Stage stage;
    private ProduitController parentController;
    private Produit produitToEdit = null;
    private boolean isEditMode = false;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setParentController(ProduitController parentController) {
        this.parentController = parentController;
    }

    public void setProduitToEdit(Produit produit) {
        this.produitToEdit = produit;
        this.isEditMode = true;
        // Remplir le formulaire immédiatement après la configuration
        remplirFormulaire();
    }

    @FXML
    public void initialize() {
        // Initialiser les items du ComboBox Statut
        cbStatut.getItems().addAll("Valable", "Hors stock");

        // Charger les catégories depuis la base de données
        chargerCategories();
        
        // Ajouter un listener pour afficher l'aperçu quand on change le chemin de l'image
        tfImage.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                afficherApercu(newValue);
            } else {
                imgPreview.setImage(null);
            }
        });
        
        // Configurer les validations en temps réel
        configurerValidations();

        // Le remplissage du formulaire en mode édition est appelé dans setProduitToEdit()
    }
    
    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieService.afficher();
            cbCategorie.getItems().clear();
            cbCategorie.getItems().addAll(categories);
            
            // Utiliser un custom cell factory pour afficher le nom de la catégorie
            cbCategorie.setCellFactory(param -> new ListCell<Categorie>() {
                @Override
                protected void updateItem(Categorie item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNom());
                }
            });
            
            // Afficher le nom de la catégorie sélectionnée
            cbCategorie.setButtonCell(new ListCell<Categorie>() {
                @Override
                protected void updateItem(Categorie item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNom());
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de charger les catégories: " + e.getMessage());
        }
    }

    private void remplirFormulaire() {
        if (produitToEdit != null) {
            lblHeader.setText("Modifier le produit");
            tfNom.setText(produitToEdit.getNom() != null ? produitToEdit.getNom() : "");
            taDescription.setText(produitToEdit.getDescription() != null ? produitToEdit.getDescription() : "");
            tfPrix.setText(String.valueOf(produitToEdit.getPrix()));
            tfQuantite.setText(String.valueOf(produitToEdit.getQuantite()));
            
            if (produitToEdit.getStatut() != null) {
                cbStatut.setValue(produitToEdit.getStatut());
            }
            
            // Sélectionner la catégorie correspondante dans le ComboBox
            for (Categorie cat : cbCategorie.getItems()) {
                if (cat.getId() == produitToEdit.getCategorieId()) {
                    cbCategorie.setValue(cat);
                    break;
                }
            }
            
            String imagePath = produitToEdit.getImage() != null ? produitToEdit.getImage() : "";
            tfImage.setText(imagePath);

            // Afficher l'aperçu de l'image
            if (!imagePath.isEmpty()) {
                afficherApercu(imagePath);
            }

            if (produitToEdit.getDateExpiration() != null) {
                try {
                    LocalDate localDate = produitToEdit.getDateExpiration().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                    dpDateExpiration.setValue(localDate);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            btnAjouter.setText("Modifier");
        }
    }

    @FXML
    private void onAjouter(ActionEvent event) {
        try {
            // Validation des champs
            String nom = tfNom.getText().trim();
            String description = taDescription.getText().trim();
            String prixStr = tfPrix.getText().trim();
            String quantiteStr = tfQuantite.getText().trim();
            String imageUrl = tfImage.getText().trim();

            // Validation du nom
            if (nom.isEmpty()) {
                showError("Validation", "Veuillez entrer le nom du produit");
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

            // Validation du prix
            if (prixStr.isEmpty()) {
                showError("Validation", "Veuillez entrer le prix");
                return;
            }

            double prix = 0;
            try {
                prix = Double.parseDouble(prixStr);
                if (prix < 0) {
                    showError("Validation", "Le prix ne peut pas être négatif");
                    return;
                }
                if (prix > 999999.99) {
                    showError("Validation", "Le prix ne peut pas dépasser 999999.99");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Erreur", "Le prix doit être un nombre valide (ex: 19.99)");
                return;
            }

            // Validation de la quantité
            if (quantiteStr.isEmpty()) {
                showError("Validation", "Veuillez entrer la quantité");
                return;
            }

            int quantite = 0;
            try {
                quantite = Integer.parseInt(quantiteStr);
                if (quantite <= 0) {
                    showError("Validation", "La quantité doit être supérieure à 0");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Erreur", "La quantité doit être un nombre entier");
                return;
            }

            // Validation du statut
            if (cbStatut.getValue() == null) {
                showError("Validation", "Veuillez choisir un statut");
                return;
            }

            // Validation de la catégorie
            if (cbCategorie.getValue() == null) {
                showError("Validation", "Veuillez choisir une catégorie");
                return;
            }

            // Validation de la date d'expiration
            if (dpDateExpiration.getValue() != null) {
                LocalDate today = LocalDate.now();
                if (dpDateExpiration.getValue().isBefore(today)) {
                    showError("Validation", "La date d'expiration ne peut pas être dans le passé");
                    return;
                }
            }

            // Validation de l'URL image (obligatoire)
            if (imageUrl.isEmpty()) {
                showError("Validation", "Veuillez sélectionner une image pour le produit");
                return;
            }
            if (imageUrl.length() > 500) {
                showError("Validation", "L'URL de l'image ne peut pas dépasser 500 caractères");
                return;
            }

            // Créer ou obtenir le produit
            Produit produit = isEditMode ? produitToEdit : new Produit();
            produit.setNom(nom);
            produit.setDescription(description);
            produit.setPrix(prix);
            produit.setImage(imageUrl);
            produit.setStatut(cbStatut.getValue());
            produit.setQuantite(quantite);

            // Obtenir l'ID de catégorie depuis la catégorie sélectionnée
            Categorie categorieSelectionnee = cbCategorie.getValue();
            if (categorieSelectionnee != null) {
                produit.setCategorieId(categorieSelectionnee.getId());
            }

            // Convertir la date d'expiration
            if (dpDateExpiration.getValue() != null) {
                Date date = java.sql.Date.valueOf(dpDateExpiration.getValue());
                produit.setDateExpiration(date);
            }

            // Si en mode ajout, ajouter la date de création actuelle
            if (!isEditMode) {
                produit.setCreatedAt(new Date());
                produitService.ajouter(produit);
                showSuccess("Succès", "Produit ajouté avec succès!");
            } else {
                // Si en mode édition, mettre à jour
                produitService.modifier(produit);
                showSuccess("Succès", "Produit modifié avec succès!");
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

    @FXML
    private void onBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        // Ajouter les filtres pour les images
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Images (*.jpg, *.jpeg, *.png, *.gif, *.bmp)", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
            new ExtensionFilter("JPEG (*.jpg, *.jpeg)", "*.jpg", "*.jpeg"),
            new ExtensionFilter("PNG (*.png)", "*.png"),
            new ExtensionFilter("GIF (*.gif)", "*.gif"),
            new ExtensionFilter("BMP (*.bmp)", "*.bmp"),
            new ExtensionFilter("Tous les fichiers (*.*)", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            String imagePath = selectedFile.getAbsolutePath();
            tfImage.setText(imagePath);
            afficherApercu(imagePath);
        }
    }

    private void afficherApercu(String imagePath) {
        try {
            File file = new File(imagePath);
            if (file.exists()) {
                String imageUrl = file.toURI().toString();
                Image image = new Image(imageUrl, 100, 100, true, true);
                imgPreview.setImage(image);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
        }
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

    private void configurerValidations() {
        // Validation du nom
        tfNom.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validerNom();
            }
        });
        tfNom.textProperty().addListener((obs, oldVal, newVal) -> {
            validerNom();
        });

        // Validation de la description
        taDescription.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validerDescription();
            }
        });
        taDescription.textProperty().addListener((obs, oldVal, newVal) -> {
            validerDescription();
        });

        // Validation du prix
        configurerValidationPrix();

        // Validation de la quantité
        configurerValidationQuantite();

        // Validation du statut
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> {
            validerStatut();
        });

        // Validation de la catégorie
        cbCategorie.valueProperty().addListener((obs, oldVal, newVal) -> {
            validerCategorie();
        });

        // Validation de la date d'expiration
        dpDateExpiration.valueProperty().addListener((obs, oldVal, newVal) -> {
            validerDateExpiration();
        });

        // Validation de l'image
        tfImage.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                validerImage();
            }
        });
        tfImage.textProperty().addListener((obs, oldVal, newVal) -> {
            validerImage();
        });
    }

    private void configurerValidationPrix() {
        tfPrix.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String text = change.getControlNewText();
            if (text.isEmpty() || text.matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            }
            return null;
        }));

        tfPrix.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !tfPrix.getText().isEmpty()) {
                validerPrix();
            }
        });
        tfPrix.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!tfPrix.isFocused()) {
                validerPrix();
            }
        });
    }

    private void configurerValidationQuantite() {
        tfQuantite.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String text = change.getControlNewText();
            if (text.isEmpty() || text.matches("\\d*")) {
                return change;
            }
            return null;
        }));

        tfQuantite.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !tfQuantite.getText().isEmpty()) {
                validerQuantite();
            }
        });
        tfQuantite.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!tfQuantite.isFocused()) {
                validerQuantite();
            }
        });
    }

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

    private void validerDescription() {
        String description = taDescription.getText();
        lblDescriptionValidation.getStyleClass().removeAll("validation-error", "validation-success");

        int charCount = description.length();
        if (charCount > 500) {
            lblDescriptionValidation.setText("⚠ Maximum 500 caractères - " + charCount + "/500");
            lblDescriptionValidation.getStyleClass().add("validation-error");
            taDescription.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
        } else if (charCount > 0) {
            lblDescriptionValidation.setText("✓ " + charCount + "/500 caractères");
            lblDescriptionValidation.getStyleClass().add("validation-success");
            taDescription.setStyle("-fx-border-color: #28a745; -fx-border-width: 2;");
        } else {
            lblDescriptionValidation.setText("Description optionnelle");
            taDescription.setStyle("");
        }
    }

    private void validerPrix() {
        String prixStr = tfPrix.getText().trim();
        lblPrixValidation.getStyleClass().removeAll("validation-error", "validation-success");

        if (prixStr.isEmpty()) {
            lblPrixValidation.setText("⚠ Veuillez entrer le prix");
            lblPrixValidation.getStyleClass().add("validation-error");
            tfPrix.setStyle("");
        } else {
            try {
                double prix = Double.parseDouble(prixStr);
                if (prix < 0) {
                    lblPrixValidation.setText("⚠ Le prix ne peut pas être négatif");
                    lblPrixValidation.getStyleClass().add("validation-error");
                    tfPrix.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
                } else if (prix > 999999.99) {
                    lblPrixValidation.setText("⚠ Valeur maximale: 999999.99");
                    lblPrixValidation.getStyleClass().add("validation-error");
                    tfPrix.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
                } else {
                    lblPrixValidation.setText("✓ Prix valide");
                    lblPrixValidation.getStyleClass().add("validation-success");
                    tfPrix.setStyle("-fx-border-color: #28a745; -fx-border-width: 2;");
                }
            } catch (NumberFormatException e) {
                lblPrixValidation.setText("⚠ Format invalide (ex: 19.99)");
                lblPrixValidation.getStyleClass().add("validation-error");
                tfPrix.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
            }
        }
    }

    private void validerQuantite() {
        String quantiteStr = tfQuantite.getText().trim();
        lblQuantiteValidation.getStyleClass().removeAll("validation-error", "validation-success");

        if (quantiteStr.isEmpty()) {
            lblQuantiteValidation.setText("⚠ Veuillez entrer la quantité");
            lblQuantiteValidation.getStyleClass().add("validation-error");
            tfQuantite.setStyle("");
        } else {
            try {
                int quantite = Integer.parseInt(quantiteStr);
                if (quantite <= 0) {
                    lblQuantiteValidation.setText("⚠ La quantité doit être supérieure à 0");
                    lblQuantiteValidation.getStyleClass().add("validation-error");
                    tfQuantite.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
                } else if (quantite > 99999) {
                    lblQuantiteValidation.setText("⚠ Valeur maximale: 99999");
                    lblQuantiteValidation.getStyleClass().add("validation-error");
                    tfQuantite.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
                } else {
                    lblQuantiteValidation.setText("✓ Quantité valide");
                    lblQuantiteValidation.getStyleClass().add("validation-success");
                    tfQuantite.setStyle("-fx-border-color: #28a745; -fx-border-width: 2;");
                }
            } catch (NumberFormatException e) {
                lblQuantiteValidation.setText("⚠ Doit être un nombre entier");
                lblQuantiteValidation.getStyleClass().add("validation-error");
                tfQuantite.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
            }
        }
    }

    private void validerStatut() {
        lblStatutValidation.getStyleClass().removeAll("validation-error", "validation-success");

        if (cbStatut.getValue() == null) {
            lblStatutValidation.setText("⚠ Veuillez choisir un statut");
            lblStatutValidation.getStyleClass().add("validation-error");
            cbStatut.setStyle("");
        } else {
            lblStatutValidation.setText("✓ Statut sélectionné");
            lblStatutValidation.getStyleClass().add("validation-success");
            cbStatut.setStyle("-fx-border-color: #28a745; -fx-border-width: 2;");
        }
    }

    private void validerCategorie() {
        lblCategorieValidation.getStyleClass().removeAll("validation-error", "validation-success");

        if (cbCategorie.getValue() == null) {
            lblCategorieValidation.setText("⚠ Veuillez choisir une catégorie");
            lblCategorieValidation.getStyleClass().add("validation-error");
            cbCategorie.setStyle("");
        } else {
            lblCategorieValidation.setText("✓ Catégorie sélectionnée");
            lblCategorieValidation.getStyleClass().add("validation-success");
            cbCategorie.setStyle("-fx-border-color: #28a745; -fx-border-width: 2;");
        }
    }

    private void validerDateExpiration() {
        lblDateExpirationValidation.getStyleClass().removeAll("validation-error", "validation-success");

        LocalDate dateExp = dpDateExpiration.getValue();
        if (dateExp == null) {
            lblDateExpirationValidation.setText("⚠ Veuillez sélectionner une date d'expiration");
            lblDateExpirationValidation.getStyleClass().add("validation-error");
            dpDateExpiration.setStyle("");
        } else {
            // Vérifier que la date n'est pas dans le passé
            LocalDate today = LocalDate.now();
            if (dateExp.isBefore(today)) {
                lblDateExpirationValidation.setText("⚠ La date d'expiration ne peut pas être dans le passé");
                lblDateExpirationValidation.getStyleClass().add("validation-error");
                dpDateExpiration.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
            } else {
                lblDateExpirationValidation.setText("✓ Date valide");
                lblDateExpirationValidation.getStyleClass().add("validation-success");
                dpDateExpiration.setStyle("-fx-border-color: #28a745; -fx-border-width: 2;");
            }
        }
    }

    private void validerImage() {
        String imageUrl = tfImage.getText().trim();
        lblImageValidation.getStyleClass().removeAll("validation-error", "validation-success");

        // Vérifier si le champ image est vide
        if (imageUrl.isEmpty()) {
            lblImageValidation.setText("⚠ Veuillez sélectionner une image");
            lblImageValidation.getStyleClass().add("validation-error");
            tfImage.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
        } else {
            // Vérifier la validité du chemin de l'image
            File file = new File(imageUrl);
            if (!file.exists()) {
                lblImageValidation.setText("⚠ Le fichier image n'existe pas");
                lblImageValidation.getStyleClass().add("validation-error");
                tfImage.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
            } else {
                lblImageValidation.setText("✓ Image valide");
                lblImageValidation.getStyleClass().add("validation-success");
                tfImage.setStyle("-fx-border-color: #28a745; -fx-border-width: 2;");
            }
        }
    }
}
