package controllers.frontoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.Client;
import models.PanierItem;
import services.CommandeService;
import services.PanierService;

import java.sql.SQLException;

public class PanierController {
    @FXML private VBox panierList;
    @FXML private Label itemsCountLabel;
    @FXML private Label sousTotalLabel;
    @FXML private Label tvaLabel;
    @FXML private Label totalTTCLabel;
    @FXML private TextField nomField;
    @FXML private TextField emailField;
    @FXML private TextField adresseField;
    @FXML private TextField telField;
    @FXML private ComboBox<String> paiementBox;
    @FXML private Button confirmerBtn;
    @FXML private Button viderBtn;
    @FXML private Button continuerAchatsBtn;
    @FXML private Label nomErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label adresseErrorLabel;
    @FXML private Label telErrorLabel;

    private final PanierService panierService = PanierService.getInstance();
    private final CommandeService commandeService = new CommandeService();
    private final ObservableList<PanierItem> panier = panierService.getPanier();
    private Runnable onContinuerAchats;

    @FXML
    public void initialize() {
        paiementBox.setItems(FXCollections.observableArrayList(
                "Carte bancaire",
                "Paiement a la livraison"
        ));
        paiementBox.getSelectionModel().selectFirst();

        panier.addListener((javafx.collections.ListChangeListener<PanierItem>) change -> {
            afficherPanier();
            majResume();
        });

        viderBtn.setOnAction(event -> {
            panierService.viderPanier();
            afficherPanier();
            majResume();
        });

        continuerAchatsBtn.setOnAction(event -> revenirAuCatalogue());


        afficherPanier();
        majResume();
    }

    public void setOnContinuerAchats(Runnable onContinuerAchats) {
        this.onContinuerAchats = onContinuerAchats;
    }

    private void afficherPanier() {
        panierList.getChildren().clear();
        itemsCountLabel.setText(panier.size() + (panier.size() > 1 ? " articles" : " article"));

        if (panier.isEmpty()) {
            Label emptyState = new Label("Votre panier est vide.");
            emptyState.getStyleClass().add("empty-state");
            panierList.getChildren().add(emptyState);
            return;
        }

        for (PanierItem item : panier) {
            panierList.getChildren().add(creerCarteProduit(item));
        }
    }

    private VBox creerCarteProduit(PanierItem item) {
        VBox card = new VBox(12);
        card.getStyleClass().add("panier-card");

        HBox topRow = new HBox(14);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Node media = createProductMedia(item);

        VBox productInfo = new VBox(6);
        productInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(productInfo, Priority.ALWAYS);

        Label nom = new Label(item.getNom());
        nom.getStyleClass().add("product-name");

        Label description = new Label(item.getDescription() == null || item.getDescription().isBlank()
                ? "Produit de parapharmacie"
                : item.getDescription());
        description.getStyleClass().add("product-description");
        description.setWrapText(true);

        Label prix = new Label(String.format("%.2f DT / unite", item.getPrix()));
        prix.getStyleClass().add("product-price");

        productInfo.getChildren().addAll(nom, description, prix);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox subtotalBox = new VBox(4);
        subtotalBox.setAlignment(Pos.CENTER_RIGHT);
        subtotalBox.getStyleClass().add("subtotal-block");

        Label subtotalTitle = new Label("Sous-total");
        subtotalTitle.getStyleClass().add("subtotal-title");

        Label sousTotal = new Label(String.format("%.2f DT", item.getSousTotal()));
        sousTotal.getStyleClass().add("subtotal-value");
        subtotalBox.getChildren().addAll(subtotalTitle, sousTotal);

        topRow.getChildren().addAll(media, productInfo, spacer, subtotalBox);

        HBox actionsRow = new HBox(12);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        Button minusButton = new Button("-");
        minusButton.getStyleClass().addAll("qty-button", "qty-minus");
        minusButton.setOnAction(event -> {
            if (item.getQuantite() > 1) {
                panierService.diminuerQuantite(item);
            }
            afficherPanier();
            majResume();
        });

        Label quantiteLabel = new Label(String.valueOf(item.getQuantite()));
        quantiteLabel.getStyleClass().add("qty-value");
        quantiteLabel.setMinWidth(42);
        quantiteLabel.setAlignment(Pos.CENTER);

        Button plusButton = new Button("+");
        plusButton.getStyleClass().addAll("qty-button", "qty-plus");
        plusButton.setOnAction(event -> {
            panierService.augmenterQuantite(item);
            afficherPanier();
            majResume();
        });

        HBox quantityBox = new HBox(10, minusButton, quantiteLabel, plusButton);
        quantityBox.setAlignment(Pos.CENTER_LEFT);
        quantityBox.getStyleClass().add("quantity-box");

        Label unitPrice = new Label(String.format("Prix unitaire: %.2f DT", item.getPrix()));
        unitPrice.getStyleClass().add("product-description");

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);

        Button supprimer = new Button("Supprimer");
        supprimer.getStyleClass().add("delete-button");
        supprimer.setOnAction(event -> {
            panierService.supprimerProduit(item);
            afficherPanier();
            majResume();
        });

        actionsRow.getChildren().addAll(quantityBox, unitPrice, actionSpacer, supprimer);

        card.getChildren().addAll(topRow, actionsRow);
        return card;
    }

    private Node createProductMedia(PanierItem item) {
        StackPane media = new StackPane();
        media.getStyleClass().add("product-media");

        String imagePath = item.getImage();
        if (imagePath != null && !imagePath.isBlank()) {
            try {
                Image image = new Image(imagePath, 76, 76, true, true, true);
                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(76);
                    imageView.setFitHeight(76);
                    imageView.setPreserveRatio(true);
                    media.getChildren().add(imageView);
                    return media;
                }
            } catch (Exception ignored) {
                // Falls back to an icon tile when the image path is invalid.
            }
        }

        Label fallback = new Label(extractProductGlyph(item.getNom()));
        fallback.getStyleClass().add("product-media-icon");
        media.getChildren().add(fallback);
        return media;
    }

    private String extractProductGlyph(String nomProduit) {
        if (nomProduit == null || nomProduit.isBlank()) {
            return "+";
        }
        return nomProduit.substring(0, 1).toUpperCase();
    }

    private void majResume() {
        double sousTotal = panierService.getSousTotal();
        double tva = sousTotal * 0.19;
        double total = sousTotal + tva;

        sousTotalLabel.setText(String.format("%.2f DT", sousTotal));
        tvaLabel.setText(String.format("%.2f DT", tva));
        totalTTCLabel.setText(String.format("%.2f DT", total));
    }

    @FXML
    public void confirmerCommande() {
        validerCommande();
    }

    private void validerCommande() {
        if (panier.isEmpty()) {
            showError("Panier vide", "Ajoutez au moins un produit avant de confirmer la commande.");
            return;
        }

        Client client = validerEtConstruireClient();
        if (client == null) {
            return;
        }

        try {
            commandeService.enregistrerCommande(
                    client,
                    PanierService.getPanierStatic(),
                    paiementBox.getValue()
            );

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION, "Commande enregistree avec succes.", ButtonType.OK);
            successAlert.setTitle("Succes");
            successAlert.setHeaderText(null);
            successAlert.showAndWait();

            panierService.viderPanier();
            resetClientFields();
            afficherPanier();
            majResume();
        } catch (SQLException e) {
            showError("Erreur base de donnees", "La commande n'a pas pu etre enregistree.\n" + e.getMessage());
        }
    }

    private Client validerEtConstruireClient() {
        boolean valide = true;
        String nomComplet = nomField.getText().trim();
        String email = emailField.getText().trim();
        String adresse = adresseField.getText().trim();
        String telephone = telField.getText().trim();

        // Validation du nom
        boolean nomValide = !nomComplet.isEmpty();
        valide &= applyValidationStyle(nomField, nomValide, nomErrorLabel);

        // Validation de l'email
        boolean emailValide = email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
        valide &= applyValidationStyle(emailField, emailValide, emailErrorLabel);

        // Validation de l'adresse
        boolean adresseValide = !adresse.isEmpty();
        valide &= applyValidationStyle(adresseField, adresseValide, adresseErrorLabel);

        // Validation du téléphone
        boolean telValide = telephone.matches("^\\d{8}$");
        valide &= applyValidationStyle(telField, telValide, telErrorLabel);

        if (!valide) {
            return null;
        }

        Client client = new Client();
        client.setNom(nomComplet);
        client.setPrenom("");
        client.setEmail(email);
        client.setTelephone(telephone);
        client.setAdresse(adresse);
        return client;
    }

    private boolean applyValidationStyle(TextField field, boolean isValid, Label errorLabel) {
        if (isValid) {
            field.setStyle("-fx-border-color: #1f9d63");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        } else {
            field.setStyle("-fx-border-color: #dc2626");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
        return isValid;
    }

    private void resetClientFields() {
        nomField.clear();
        emailField.clear();
        adresseField.clear();
        telField.clear();
        paiementBox.getSelectionModel().selectFirst();

        nomField.setStyle("");
        emailField.setStyle("");
        adresseField.setStyle("");
        telField.setStyle("");

        nomErrorLabel.setVisible(false);
        nomErrorLabel.setManaged(false);
        emailErrorLabel.setVisible(false);
        emailErrorLabel.setManaged(false);
        adresseErrorLabel.setVisible(false);
        adresseErrorLabel.setManaged(false);
        telErrorLabel.setVisible(false);
        telErrorLabel.setManaged(false);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void revenirAuCatalogue() {
        if (onContinuerAchats != null) {
            onContinuerAchats.run();
        }
    }
}
