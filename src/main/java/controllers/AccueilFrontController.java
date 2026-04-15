package controllers;

import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Categorie;
import model.Produit;
import service.CategorieService;
import service.ProduitService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AccueilFrontController {

    @FXML
    private FlowPane vboxProduitsAccueil;

    @FXML
    private TextField searchNomField;

    @FXML
    private ComboBox<Categorie> categorieComboBox;

    @FXML
    private TextField prixMinField;

    @FXML
    private TextField prixMaxField;

    @FXML
    private Button btnRechercher;

    @FXML
    private Button btnReinitialiser;

    private ProduitService produitService;
    private CategorieService categorieService;
    private List<Produit> tousLesProduits;

    @FXML
    public void initialize() {
        produitService = new ProduitService();
        categorieService = new CategorieService();

        // Charger les catégories
        chargerCategories();

        // Charger les produits
        chargerProduits();

        // Configurer les validations des champs de prix
        configurerValidationsPrix();

        // Ajouter les listeners pour la recherche en temps réel
        searchNomField.textProperty().addListener((obs, oldVal, newVal) -> filtrerProduits());
        categorieComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filtrerProduits());
        prixMinField.textProperty().addListener((obs, oldVal, newVal) -> filtrerProduits());
        prixMaxField.textProperty().addListener((obs, oldVal, newVal) -> filtrerProduits());

        // Boutons d'action
        btnRechercher.setOnAction(e -> filtrerProduits());
        btnReinitialiser.setOnAction(e -> reinitialiserFiltres());
    }

    /**
     * Configure les validations pour les champs de prix
     */
    private void configurerValidationsPrix() {
        // Validation pour prixMinField - uniquement des nombres
        prixMinField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String text = change.getControlNewText();
            if (text.isEmpty() || text.matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            }
            return null;
        }));

        // Validation pour prixMaxField - uniquement des nombres
        prixMaxField.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String text = change.getControlNewText();
            if (text.isEmpty() || text.matches("\\d*(\\.\\d{0,2})?")) {
                return change;
            }
            return null;
        }));

        // Ajouter un style de validation au focus
        prixMinField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !prixMinField.getText().isEmpty()) {
                validationChampPrix(prixMinField);
            }
        });

        prixMaxField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !prixMaxField.getText().isEmpty()) {
                validationChampPrix(prixMaxField);
            }
        });
    }

    /**
     * Valide un champ de prix
     */
    private void validationChampPrix(TextField champ) {
        try {
            double prix = Double.parseDouble(champ.getText());
            if (prix < 0) {
                champ.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
            } else {
                champ.setStyle("-fx-border-color: #28a745; -fx-border-width: 2;");
            }
        } catch (NumberFormatException e) {
            champ.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
        }
    }

    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieService.afficher();
            ObservableList<Categorie> observableCategories = FXCollections.observableArrayList(categories);
            categorieComboBox.setItems(observableCategories);
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des catégories: " + e.getMessage());
        }
    }

    private void chargerProduits() {
        try {
            tousLesProduits = produitService.afficher();
            afficherProduits(tousLesProduits);
        } catch (SQLException e) {
            e.printStackTrace();
            // Si la base de données n'est pas accessible, afficher les produits de démonstration
            chargerProduitsDemo();
        }
    }

    private void chargerProduitsDemo() {
        tousLesProduits = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Produit p = new Produit();
            p.setId(i);
            p.setNom("Produit " + i);
            p.setDescription("Description du produit " + i);
            p.setPrix((i * 10));
            p.setImage("https://via.placeholder.com/150");
            tousLesProduits.add(p);
        }
        afficherProduits(tousLesProduits);
    }

    private void filtrerProduits() {
        if (tousLesProduits == null || tousLesProduits.isEmpty()) {
            return;
        }

        String nomRecherche = searchNomField.getText().toLowerCase().trim();
        Categorie categorieSelectionnee = categorieComboBox.getValue();
        double prixMin = 0;
        double prixMax = Double.MAX_VALUE;

        try {
            if (!prixMinField.getText().trim().isEmpty()) {
                prixMin = Double.parseDouble(prixMinField.getText().trim());
            }
            if (!prixMaxField.getText().trim().isEmpty()) {
                prixMax = Double.parseDouble(prixMaxField.getText().trim());
            }
        } catch (NumberFormatException e) {
            System.err.println("Format de prix invalide");
            return;
        }

        final double minFinal = prixMin;
        final double maxFinal = prixMax;

        List<Produit> produitsFiltres = tousLesProduits.stream()
                .filter(p -> p.getNom().toLowerCase().contains(nomRecherche))
                .filter(p -> categorieSelectionnee == null || p.getCategorieId() == categorieSelectionnee.getId())
                .filter(p -> p.getPrix() >= minFinal && p.getPrix() <= maxFinal)
                .collect(Collectors.toList());

        afficherProduits(produitsFiltres);
    }

    private void reinitialiserFiltres() {
        searchNomField.clear();
        categorieComboBox.setValue(null);
        prixMinField.clear();
        prixMaxField.clear();
        afficherProduits(tousLesProduits);
    }

    private void afficherProduits(List<Produit> produits) {
        vboxProduitsAccueil.getChildren().clear();

        if (produits.isEmpty()) {
            Label aucunProduit = new Label("Aucun produit trouvé");
            aucunProduit.setStyle("-fx-font-size: 16; -fx-text-fill: #999; -fx-padding: 40;");
            vboxProduitsAccueil.setAlignment(Pos.CENTER);
            vboxProduitsAccueil.getChildren().add(aucunProduit);
            return;
        }

        vboxProduitsAccueil.setAlignment(Pos.TOP_LEFT);
        for (Produit produit : produits) {
            ajouterProduit(
                    produit.getNom(),
                    produit.getDescription(),
                    produit.getPrix() + " DT",
                    produit.getImage() != null ? produit.getImage() : "https://via.placeholder.com/150"
            );
        }
    }

    private void ajouterProduit(String nomProduit, String description, String prix, String imageUrl) {
        VBox card = new VBox(10);
        card.getStyleClass().add("produit-card");
        card.setAlignment(Pos.TOP_CENTER);

        // IMAGE
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(imageUrl, 150, 150, true, true);
            imageView.setImage(image);
        } catch (Exception e) {
            Image image = new Image("https://via.placeholder.com/150", 150, 150, true, true);
            imageView.setImage(image);
        }

        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        // LABELS
        Label nom = new Label(nomProduit);
        nom.getStyleClass().add("label-nom");
        nom.setWrapText(true);

        Label desc = new Label(description);
        desc.getStyleClass().add("label-desc");
        desc.setWrapText(true);

        Label prixLabel = new Label(prix);
        prixLabel.getStyleClass().add("label-prix");

        // ADD TO CARD
        card.getChildren().addAll(imageView, nom, desc, prixLabel);

        vboxProduitsAccueil.getChildren().add(card);
    }
}