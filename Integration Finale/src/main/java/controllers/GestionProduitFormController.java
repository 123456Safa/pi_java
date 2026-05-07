package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import models.Produit;
import models.Categorie;
import services.ProduitService;
import services.CategorieService;
import services.OpenFDAService;
import services.ProductWorkflowService;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class GestionProduitFormController {
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
    private ProductWorkflowService workflowService = new ProductWorkflowService();
    private Stage stage;
    private GestionProduitController parentController;
    private Produit produitToEdit = null;
    private boolean isEditMode = false;
    private boolean isFdaValidated = false;
    private PauseTransition debounce = new PauseTransition(Duration.millis(700));
    private Task<Boolean> currentFdaTask = null;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setParentController(GestionProduitController parentController) {
        this.parentController = parentController;
    }

    public void setProduitToEdit(Produit produit) {
        this.produitToEdit = produit;
        this.isEditMode = true;
        remplirFormulaire();
    }

    @FXML
    public void initialize() {
        cbStatut.getItems().addAll("Valable", "Hors stock");
        chargerCategories();
        tfImage.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                afficherApercu(newValue);
            } else {
                imgPreview.setImage(null);
            }
        });
        configurerValidations();
    }

    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieService.afficher();
            cbCategorie.getItems().clear();
            cbCategorie.getItems().addAll(categories);
            cbCategorie.setCellFactory(param -> new ListCell<Categorie>() {
                @Override
                protected void updateItem(Categorie item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? "" : item.getNom());
                }
            });
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
            if (produitToEdit.getStatut() != null) cbStatut.setValue(produitToEdit.getStatut());
            for (Categorie cat : cbCategorie.getItems()) {
                if (cat.getId() == produitToEdit.getCategorieId()) {
                    cbCategorie.setValue(cat);
                    break;
                }
            }
            String imagePath = produitToEdit.getImage() != null ? produitToEdit.getImage() : "";
            tfImage.setText(imagePath);
            if (!imagePath.isEmpty()) afficherApercu(imagePath);
            if (produitToEdit.getDateExpiration() != null) {
                try {
                    LocalDate localDate = ((java.sql.Date) produitToEdit.getDateExpiration()).toLocalDate();
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
            String nom = tfNom.getText().trim();
            String description = taDescription.getText().trim();
            String prixStr = tfPrix.getText().trim();
            String quantiteStr = tfQuantite.getText().trim();
            String imageUrl = tfImage.getText().trim();

            if (nom.isEmpty() || nom.length() < 3 || nom.length() > 100) {
                showError("Validation", "Nom invalide (3-100 caractères requis)");
                return;
            }
            if (description.length() > 500) {
                showError("Validation", "La description ne peut pas dépasser 500 caractères");
                return;
            }
            if (prixStr.isEmpty()) {
                showError("Validation", "Veuillez entrer le prix");
                return;
            }
            double prix = 0;
            try {
                prix = Double.parseDouble(prixStr);
                if (prix < 0 || prix > 999999.99) {
                    showError("Validation", "Prix invalide");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Erreur", "Le prix doit être un nombre valide");
                return;
            }
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
            if (cbStatut.getValue() == null) {
                showError("Validation", "Veuillez choisir un statut");
                return;
            }
            if (cbCategorie.getValue() == null) {
                showError("Validation", "Veuillez choisir une catégorie");
                return;
            }
            if (dpDateExpiration.getValue() != null && dpDateExpiration.getValue().isBefore(LocalDate.now())) {
                showError("Validation", "La date d'expiration ne peut pas être dans le passé");
                return;
            }
            if (imageUrl.isEmpty()) {
                showError("Validation", "Veuillez sélectionner une image");
                return;
            }

            if (!isEditMode || !nom.equalsIgnoreCase(produitToEdit.getNom())) {
                if (isFdaValidated && nom.equalsIgnoreCase(tfNom.getText().trim())) {
                    enregistrerProduit(nom, description, prix, imageUrl, quantite);
                    return;
                }
                btnAjouter.setDisable(true);
                btnAjouter.setText("Vérification en cours...");
                final double finalPrix = prix;
                final int finalQuantite = quantite;
                Task<Boolean> fdaTask = new Task<>() {
                    @Override
                    protected Boolean call() throws Exception {
                        return OpenFDAService.isProductValid(nom);
                    }
                };
                fdaTask.setOnSucceeded(e -> {
                    btnAjouter.setDisable(false);
                    btnAjouter.setText(isEditMode ? "Modifier" : "Ajouter");
                    if (fdaTask.getValue()) {
                        isFdaValidated = true;
                        enregistrerProduit(nom, description, finalPrix, imageUrl, finalQuantite);
                    } else {
                        isFdaValidated = false;
                        showError("Validation openFDA", "Le produit '" + nom + "' n'a pas été trouvé dans la FDA.");
                        tfNom.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
                    }
                });
                fdaTask.setOnFailed(e -> {
                    btnAjouter.setDisable(false);
                    btnAjouter.setText(isEditMode ? "Modifier" : "Ajouter");
                    showError("Erreur de connexion", "Impossible de vérifier avec openFDA.");
                });
                new Thread(fdaTask).start();
            } else {
                enregistrerProduit(nom, description, prix, imageUrl, quantite);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur inattendue: " + e.getMessage());
        }
    }

    private void enregistrerProduit(String nom, String description, double prix, String imageUrl, int quantite) {
        btnAjouter.setDisable(true);
        btnAjouter.setText("Enregistrement...");

        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Produit produit = isEditMode ? produitToEdit : new Produit();
                produit.setNom(nom);
                produit.setDescription(description);
                produit.setPrix(prix);
                produit.setImage(imageUrl);
                produit.setStatut(cbStatut.getValue());
                produit.setQuantite(quantite);
                Categorie cat = cbCategorie.getValue();
                if (cat != null) produit.setCategorieId(cat.getId());
                if (dpDateExpiration.getValue() != null) {
                    produit.setDateExpiration(java.sql.Date.valueOf(dpDateExpiration.getValue()));
                }
                workflowService.processProductWorkflow(produit);
                if (!isEditMode) {
                    produit.setCreatedAt(new Date());
                    produitService.ajouter(produit);
                } else {
                    produitService.modifier(produit);
                }
                return null;
            }
        };

        saveTask.setOnSucceeded(e -> Platform.runLater(() -> {
            showSuccess("Succès", isEditMode ? "Produit modifié avec succès!" : "Produit ajouté avec succès!");
            if (parentController != null) parentController.rafraichirTable();
            stage.close();
        }));

        saveTask.setOnFailed(e -> Platform.runLater(() -> {
            btnAjouter.setDisable(false);
            btnAjouter.setText(isEditMode ? "Modifier" : "Ajouter");
            Throwable ex = saveTask.getException();
            if (ex != null) ex.printStackTrace();
            showError("Erreur", "Erreur lors de l'enregistrement: " + (ex != null ? ex.getMessage() : "inconnue"));
        }));

        new Thread(saveTask).start();
    }

    @FXML
    private void onAnnuler(ActionEvent event) {
        stage.close();
    }

    @FXML
    private void onGenerateClick(ActionEvent event) {
        String nom = tfNom.getText().trim();
        if (nom.isEmpty()) {
            showError("Attention", "Veuillez d'abord entrer un nom de produit.");
            return;
        }
        Button sourceBtn = null;
        if (event.getSource() instanceof Button) {
            sourceBtn = (Button) event.getSource();
            sourceBtn.setDisable(true);
        }
        taDescription.setPromptText("Chargement avec Gemini IA...");
        taDescription.setText("");
        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override
            protected String call() throws Exception {
                String response = services.GeminiService.generateDescription(nom);
                if (response != null && response.startsWith("Erreur")) {
                    throw new Exception(response);
                }
                return services.GeminiService.extractText(response);
            }
        };
        Button finalSourceBtn = sourceBtn;
        task.setOnSucceeded(e -> {
            if (finalSourceBtn != null) finalSourceBtn.setDisable(false);
            taDescription.setText(task.getValue());
            taDescription.setStyle("");
        });
        task.setOnFailed(e -> {
            if (finalSourceBtn != null) finalSourceBtn.setDisable(false);
            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Erreur inconnue";
            showError("Erreur IA", "Impossible de générer: " + errorMsg);
            taDescription.setPromptText("Entrez la description du produit");
        });
        new Thread(task).start();
    }

    @FXML
    private void onBrowseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"),
            new ExtensionFilter("Tous les fichiers", "*.*")
        );
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            tfImage.setText(selectedFile.getAbsolutePath());
            afficherApercu(selectedFile.getAbsolutePath());
        }
    }

    private void afficherApercu(String imagePath) {
        try {
            File file = new File(imagePath);
            if (file.exists()) {
                imgPreview.setImage(new Image(file.toURI().toString(), 100, 100, true, true));
            }
        } catch (Exception e) {
            System.err.println("Erreur aperçu image: " + e.getMessage());
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
        tfNom.textProperty().addListener((obs, oldVal, newVal) -> {
            validerNom();
            isFdaValidated = false;
            debounce.setOnFinished(event -> verifierNomFDA());
            debounce.playFromStart();
        });
        taDescription.textProperty().addListener((obs, oldVal, newVal) -> validerDescription());
        configurerValidationPrix();
        configurerValidationQuantite();
        cbStatut.valueProperty().addListener((obs, oldVal, newVal) -> validerStatut());
        cbCategorie.valueProperty().addListener((obs, oldVal, newVal) -> validerCategorie());
        dpDateExpiration.valueProperty().addListener((obs, oldVal, newVal) -> validerDateExpiration());
        tfImage.textProperty().addListener((obs, oldVal, newVal) -> validerImage());
    }

    private void configurerValidationPrix() {
        tfPrix.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String text = change.getControlNewText();
            return (text.isEmpty() || text.matches("\\d*(\\.\\d{0,2})?")) ? change : null;
        }));
    }

    private void configurerValidationQuantite() {
        tfQuantite.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String text = change.getControlNewText();
            return (text.isEmpty() || text.matches("\\d*")) ? change : null;
        }));
    }

    private void validerNom() {
        String nom = tfNom.getText().trim();
        lblNomValidation.getStyleClass().removeAll("validation-error", "validation-success");
        if (nom.isEmpty()) {
            lblNomValidation.setText("Veuillez entrer un nom");
            lblNomValidation.getStyleClass().add("validation-error");
        } else if (nom.length() < 3 || nom.length() > 100) {
            lblNomValidation.setText("3-100 caractères requis");
            lblNomValidation.getStyleClass().add("validation-error");
            tfNom.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
        } else {
            lblNomValidation.setText("✓ Format valide");
            lblNomValidation.getStyleClass().add("validation-success");
            tfNom.setStyle("-fx-border-color: #28a745; -fx-border-width: 2;");
        }
    }

    private void verifierNomFDA() {
        String nom = tfNom.getText().trim();
        if (nom.length() < 3) { isFdaValidated = false; return; }
        if (currentFdaTask != null && currentFdaTask.isRunning()) currentFdaTask.cancel();
        if (isEditMode && nom.equalsIgnoreCase(produitToEdit.getNom())) {
            isFdaValidated = true;
            lblNomValidation.setText("✓ Produit déjà vérifié (FDA)");
            return;
        }
        lblNomValidation.setText("Vérification FDA...");
        currentFdaTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return OpenFDAService.isProductValid(nom);
            }
        };
        currentFdaTask.setOnSucceeded(e -> {
            isFdaValidated = currentFdaTask.getValue();
            lblNomValidation.setText(isFdaValidated ? "✅ Validé par la FDA" : "❌ Non trouvé dans la FDA");
            lblNomValidation.getStyleClass().removeAll("validation-error", "validation-success");
            lblNomValidation.getStyleClass().add(isFdaValidated ? "validation-success" : "validation-error");
        });
        currentFdaTask.setOnFailed(e -> { if (!currentFdaTask.isCancelled()) lblNomValidation.setText("⚠ Erreur connexion FDA"); });
        Thread t = new Thread(currentFdaTask);
        t.setDaemon(true);
        t.start();
    }

    private void validerDescription() {
        int charCount = taDescription.getText().length();
        lblDescriptionValidation.getStyleClass().removeAll("validation-error", "validation-success");
        if (charCount > 500) {
            lblDescriptionValidation.setText("Maximum 500 caractères - " + charCount + "/500");
            lblDescriptionValidation.getStyleClass().add("validation-error");
        } else if (charCount > 0) {
            lblDescriptionValidation.setText("✓ " + charCount + "/500");
            lblDescriptionValidation.getStyleClass().add("validation-success");
        } else {
            lblDescriptionValidation.setText("Description optionnelle");
        }
    }

    private void validerPrix() {
        lblPrixValidation.getStyleClass().removeAll("validation-error", "validation-success");
        String prixStr = tfPrix.getText().trim();
        if (prixStr.isEmpty()) { lblPrixValidation.setText("Veuillez entrer le prix"); return; }
        try {
            double prix = Double.parseDouble(prixStr);
            if (prix < 0 || prix > 999999.99) {
                lblPrixValidation.setText("Valeur invalide"); lblPrixValidation.getStyleClass().add("validation-error");
            } else {
                lblPrixValidation.setText("✓ Prix valide"); lblPrixValidation.getStyleClass().add("validation-success");
            }
        } catch (NumberFormatException e) {
            lblPrixValidation.setText("Format invalide"); lblPrixValidation.getStyleClass().add("validation-error");
        }
    }

    private void validerQuantite() {
        lblQuantiteValidation.getStyleClass().removeAll("validation-error", "validation-success");
        String q = tfQuantite.getText().trim();
        if (q.isEmpty()) { lblQuantiteValidation.setText("Veuillez entrer la quantité"); return; }
        try {
            int quantite = Integer.parseInt(q);
            if (quantite <= 0) {
                lblQuantiteValidation.setText("Doit être > 0"); lblQuantiteValidation.getStyleClass().add("validation-error");
            } else {
                lblQuantiteValidation.setText("✓ Quantité valide"); lblQuantiteValidation.getStyleClass().add("validation-success");
            }
        } catch (NumberFormatException e) {
            lblQuantiteValidation.setText("Doit être un entier"); lblQuantiteValidation.getStyleClass().add("validation-error");
        }
    }

    private void validerStatut() {
        lblStatutValidation.getStyleClass().removeAll("validation-error", "validation-success");
        if (cbStatut.getValue() == null) {
            lblStatutValidation.setText("Veuillez choisir un statut"); lblStatutValidation.getStyleClass().add("validation-error");
        } else {
            lblStatutValidation.setText("✓ Statut sélectionné"); lblStatutValidation.getStyleClass().add("validation-success");
        }
    }

    private void validerCategorie() {
        lblCategorieValidation.getStyleClass().removeAll("validation-error", "validation-success");
        if (cbCategorie.getValue() == null) {
            lblCategorieValidation.setText("Veuillez choisir une catégorie"); lblCategorieValidation.getStyleClass().add("validation-error");
        } else {
            lblCategorieValidation.setText("✓ Catégorie sélectionnée"); lblCategorieValidation.getStyleClass().add("validation-success");
        }
    }

    private void validerDateExpiration() {
        lblDateExpirationValidation.getStyleClass().removeAll("validation-error", "validation-success");
        LocalDate dateExp = dpDateExpiration.getValue();
        if (dateExp == null) {
            lblDateExpirationValidation.setText("Veuillez sélectionner une date"); lblDateExpirationValidation.getStyleClass().add("validation-error");
        } else if (dateExp.isBefore(LocalDate.now())) {
            lblDateExpirationValidation.setText("Date dans le passé"); lblDateExpirationValidation.getStyleClass().add("validation-error");
        } else {
            lblDateExpirationValidation.setText("✓ Date valide"); lblDateExpirationValidation.getStyleClass().add("validation-success");
        }
    }

    private void validerImage() {
        lblImageValidation.getStyleClass().removeAll("validation-error", "validation-success");
        String imageUrl = tfImage.getText().trim();
        if (imageUrl.isEmpty()) {
            lblImageValidation.setText("Veuillez sélectionner une image"); lblImageValidation.getStyleClass().add("validation-error");
        } else if (!new File(imageUrl).exists()) {
            lblImageValidation.setText("Le fichier n'existe pas"); lblImageValidation.getStyleClass().add("validation-error");
        } else {
            lblImageValidation.setText("✓ Image valide"); lblImageValidation.getStyleClass().add("validation-success");
        }
    }
}
