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
        cbStatut.getItems().addAll("Actif", "Inactif", "En rupture");
        
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
                if (quantite < 0) {
                    showError("Validation", "La quantité ne peut pas être négative");
                    return;
                }
                if (quantite > 99999) {
                    showError("Validation", "La quantité ne peut pas dépasser 99999");
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

            // Validation de l'URL image (facultatif mais si fourni, doit être valide)
            if (!imageUrl.isEmpty() && imageUrl.length() > 500) {
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
}







