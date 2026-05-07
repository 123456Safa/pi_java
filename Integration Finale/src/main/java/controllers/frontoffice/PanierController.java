package controllers.frontoffice;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
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
import services.DeliveryPriceCalculator;
import services.FormHistoryService;
import services.PanierService;
import services.PointsService;

import java.sql.SQLException;
import java.util.List;
import javafx.util.Duration;

public class PanierController {
    @FXML private VBox panierList;
    @FXML private Label itemsCountLabel;
    @FXML private Label sousTotalLabel;
    @FXML private Label livraisonLabel;
    @FXML private Label livraisonInfoLabel;
    @FXML private Label pointsReductionLabel;
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
    @FXML private Label pointsDisponiblesLabel;
    @FXML private Label pointsInfoLabel;
    @FXML private CheckBox utiliserPointsCheckBox;
    @FXML private CheckBox livraisonGratuiteCheckBox;

    private final PanierService panierService = PanierService.getInstance();
    private final CommandeService commandeService = new CommandeService();
    private final DeliveryPriceCalculator deliveryPriceCalculator = new DeliveryPriceCalculator();
    private final FormHistoryService formHistory = FormHistoryService.getInstance();
    private final PointsService pointsService = PointsService.getInstance();
    private final ObservableList<PanierItem> panier = panierService.getPanier();
    private final java.util.List<javafx.scene.control.ContextMenu> autocompleteMenus = new java.util.ArrayList<>();
    private final PauseTransition livraisonDebounce = new PauseTransition(Duration.millis(800));
    private Runnable onContinuerAchats;
    private double fraisLivraison = 0.0;
    private double fraisLivraisonCalcules = 0.0;
    private double reductionPoints = 0.0;
    private int pointsReductionUtilises = 0;
    private int pointsLivraisonUtilises = 0;
    private int livraisonRequestId = 0;

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

        livraisonDebounce.setOnFinished(event -> calculerLivraisonDepuisAdresse());
        adresseField.textProperty().addListener((obs, oldValue, newValue) -> {
            fraisLivraison = 0.0;
            fraisLivraisonCalcules = 0.0;
            afficherInfoLivraison("", false);
            livraisonLabel.setText(newValue == null || newValue.trim().isEmpty() ? "A calculer" : "Calcul...");
            majResume();
            livraisonDebounce.playFromStart();
        });
        emailField.textProperty().addListener((obs, oldValue, newValue) -> majPointsClient());
        utiliserPointsCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> majResume());
        livraisonGratuiteCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> majResume());

        afficherPanier();
        majResume();
        majPointsClient();

        // Attach autocomplete history to each checkout field
        attachAutocomplete(nomField,     "nom");
        attachAutocomplete(emailField,   "email");
        attachAutocomplete(adresseField, "adresse");
        attachAutocomplete(telField,     "tel");

        // Pre-fill checkout fields from the logged-in user's session
        models.User sessionUser = utils.SessionManager.getInstance().getCurrentUser();
        if (sessionUser != null) {
            String fullName = ((sessionUser.getFirstName() != null ? sessionUser.getFirstName() : "")
                    + " " + (sessionUser.getLastName() != null ? sessionUser.getLastName() : "")).trim();
            if (!fullName.isEmpty() && nomField.getText().isEmpty()) {
                nomField.setText(fullName);
            }
            if (sessionUser.getEmail() != null && emailField.getText().isEmpty()) {
                emailField.setText(sessionUser.getEmail());
            }
            if (sessionUser.getPhoneNumber() != null && !sessionUser.getPhoneNumber().isEmpty() && telField.getText().isEmpty()) {
                telField.setText(sessionUser.getPhoneNumber());
            }
        }
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
        recalculerAvantagesPoints(sousTotal + tva);
        double total = sousTotal + tva + fraisLivraison - reductionPoints;

        sousTotalLabel.setText(String.format("%.2f DT", sousTotal));
        if (fraisLivraison > 0) {
            livraisonLabel.setText(String.format("%.2f DT", fraisLivraison));
        } else if (pointsLivraisonUtilises > 0) {
            livraisonLabel.setText("Gratuite");
        }
        pointsReductionLabel.setText(reductionPoints > 0 ? String.format("-%.2f DT", reductionPoints) : "0,00 DT");
        tvaLabel.setText(String.format("%.2f DT", tva));
        totalTTCLabel.setText(String.format("%.2f DT", total));
    }

    private void recalculerAvantagesPoints(double maxReductionBase) {
        int pointsDisponibles = pointsService.getPoints(emailField.getText());
        pointsLivraisonUtilises = 0;
        pointsReductionUtilises = 0;
        reductionPoints = 0.0;
        fraisLivraison = fraisLivraisonCalcules;

        if (livraisonGratuiteCheckBox.isSelected()
                && fraisLivraisonCalcules > 0
                && pointsDisponibles >= PointsService.FREE_DELIVERY_POINTS) {
            pointsLivraisonUtilises = PointsService.FREE_DELIVERY_POINTS;
            fraisLivraison = 0.0;
        }

        int pointsRestants = pointsDisponibles - pointsLivraisonUtilises;
        if (utiliserPointsCheckBox.isSelected() && pointsRestants >= PointsService.POINTS_PER_DISCOUNT_DINAR) {
            pointsReductionUtilises = Math.min(
                    pointsRestants,
                    pointsService.calculateDiscountPointsToUse(emailField.getText(), maxReductionBase)
            );
            reductionPoints = pointsService.pointsToDiscountAmount(pointsReductionUtilises);
        }

        livraisonGratuiteCheckBox.setDisable(pointsDisponibles < PointsService.FREE_DELIVERY_POINTS || fraisLivraisonCalcules <= 0);
        utiliserPointsCheckBox.setDisable(pointsRestants < PointsService.POINTS_PER_DISCOUNT_DINAR);
        pointsInfoLabel.setText(buildPointsInfo(pointsDisponibles));
    }

    private String buildPointsInfo(int pointsDisponibles) {
        int pointsDepenses = pointsLivraisonUtilises + pointsReductionUtilises;
        if (pointsDepenses == 0) {
            return "10 points = 1 DT de reduction. 50 points = livraison gratuite.";
        }
        return "Points utilises : " + pointsDepenses;
    }

    private void majPointsClient() {
        int points = pointsService.getPoints(emailField.getText());
        pointsDisponiblesLabel.setText("Points disponibles : " + points);
        majResume();
    }

    private void calculerLivraisonDepuisAdresse() {
        String adresseClient = adresseField.getText() == null ? "" : adresseField.getText().trim();
        if (adresseClient.isEmpty()) {
            fraisLivraison = 0.0;
            fraisLivraisonCalcules = 0.0;
            livraisonLabel.setText("A calculer");
            afficherInfoLivraison("", false);
            majResume();
            return;
        }

        int requestId = ++livraisonRequestId;
        livraisonLabel.setText("Calcul...");
        afficherInfoLivraison("Calcul local de la livraison...", true);

        Thread worker = new Thread(() -> {
            DeliveryPriceCalculator.DeliveryQuote quote = deliveryPriceCalculator.calculate(adresseClient);
            Platform.runLater(() -> {
                if (requestId != livraisonRequestId) {
                    return;
                }
                fraisLivraisonCalcules = quote.deliveryPrice();
                fraisLivraison = fraisLivraisonCalcules;
                livraisonLabel.setText(String.format("%.2f DT", fraisLivraison));
                afficherInfoLivraison(String.format("Distance estimee: %.2f km", quote.distanceKm()), true);
                majResume();
            });
        }, "delivery-api-client");
        worker.setDaemon(true);
        worker.start();
    }

    private void afficherInfoLivraison(String message, boolean visible) {
        livraisonInfoLabel.setText(message);
        livraisonInfoLabel.setVisible(visible);
        livraisonInfoLabel.setManaged(visible);
    }

    @FXML
    public void confirmerCommande() {
        if (panier.isEmpty()) {
            showError("Panier vide", "Ajoutez au moins un produit avant de confirmer la commande.");
            return;
        }

        Client client = validerEtConstruireClient();
        if (client == null) {
            return; // Les messages d'erreur sont déjà gérés dans la méthode
        }

        String modePaiement = paiementBox.getValue();
        if ("Carte bancaire".equals(modePaiement)) {
            afficherPopupStripeLink(client);
        } else {
            validerCommande(client);
        }
    }

    private void afficherPopupStripeLink(Client client) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/frontoffice/stripe_link.fxml"));
            javafx.scene.Parent root = loader.load();

            StripeLinkController controller = loader.getController();
            
            double sousTotal = panierService.getSousTotal();
            double total = sousTotal + (sousTotal * 0.19) + fraisLivraison - reductionPoints;
            
            controller.initData(client.getEmail(), total, 
                () -> validerCommande(client), 
                () -> System.out.println("Paiement Stripe annulé")
            );
            controller.setOnPaymentValidated(() -> enregistrerInfosClientDansHistorique(client));

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Stripe Checkout");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Erreur d'affichage", "Impossible d'afficher l'interface Stripe.");
        }
    }

    private void validerCommande(Client client) {
        System.out.println("=== DÉBUT VALIDATION COMMANDE ===");
        System.out.println("✅ Client validé: " + client.getNom());

        try {
            System.out.println("📦 Enregistrement de la commande...");
            System.out.println("   Panier items: " + PanierService.getPanierStatic().size());
            System.out.println("   Total: " + panierService.getTotal());
            System.out.println("   Mode paiement: " + paiementBox.getValue());

            models.CommandeConfirmation confirmation = commandeService.enregistrerCommande(
                    client,
                    PanierService.getPanierStatic(),
                    paiementBox.getValue(),
                    fraisLivraison,
                    reductionPoints
            );

            System.out.println("✅ Commande enregistrée avec succès!");

            int pointsDepenses = pointsLivraisonUtilises + pointsReductionUtilises;
            int pointsGagnes = pointsService.calculateEarnedPoints(confirmation.getTotalTtc());
            pointsService.applyOrderPoints(client.getEmail(), pointsDepenses, pointsGagnes);
            System.out.println("🎁 Points utilisés: " + pointsDepenses + " | Points gagnés: " + pointsGagnes);

            // Fermer tous les menus d'autocomplete avant d'ouvrir la facture
            hideAllAutocompleteMenus();
            afficherFacture(confirmation);

            // Save form values to history for future autocomplete
            enregistrerInfosClientDansHistorique(client);

            // Send HTML confirmation email (non-blocking background thread)
            String recipientEmail = emailField.getText().trim();
            services.EmailService.sendConfirmationAsync(confirmation, recipientEmail);

            // Create in-app notification for order validation
            services.NotificationService ns = new services.NotificationService();
            models.User notifUser = utils.SessionManager.getInstance().getCurrentUser();
            int notifUserId = (notifUser != null) ? notifUser.getId() : 0;
            ns.sendNotification(
                notifUserId,
                "Commande Validée 🛒", 
                "Votre commande #" + confirmation.getCommandeId() + " a bien été enregistrée. Merci pour votre confiance !", 
                "ORDER_VALIDATED", 
                null // Email already sent by EmailService
            );

            panierService.viderPanier();
            resetClientFields();
            afficherPanier();
            majResume();
        } catch (SQLException e) {
            System.out.println("❌ ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur base de donnees", "La commande n'a pas pu etre enregistree.\n" + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ ERREUR GÉNÉRALE: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Une erreur inattendue s'est produite:\n" + e.getMessage());
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

    private void enregistrerInfosClientDansHistorique(Client client) {
        if (client == null) {
            return;
        }

        formHistory.saveValue("nom", safeTrim(client.getNom()));
        formHistory.saveValue("email", safeTrim(client.getEmail()));
        formHistory.saveValue("adresse", safeTrim(client.getAdresse()));
        formHistory.saveValue("tel", safeTrim(client.getTelephone()));
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
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
        utiliserPointsCheckBox.setSelected(false);
        livraisonGratuiteCheckBox.setSelected(false);
        fraisLivraison = 0.0;
        fraisLivraisonCalcules = 0.0;
        reductionPoints = 0.0;
        pointsReductionUtilises = 0;
        pointsLivraisonUtilises = 0;
        livraisonLabel.setText("A calculer");
        pointsReductionLabel.setText("0,00 DT");
        majPointsClient();

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

    private void hideAllAutocompleteMenus() {
        autocompleteMenus.forEach(javafx.scene.control.ContextMenu::hide);
    }

    /**
     * Attaches a history-based autocomplete ContextMenu to a TextField.
     * The menu is shown when the field is clicked or its text changes.
     */
    private void attachAutocomplete(javafx.scene.control.TextField field, String historyKey) {
        javafx.scene.control.ContextMenu menu = new javafx.scene.control.ContextMenu();
        autocompleteMenus.add(menu); // track for later hiding
        menu.setStyle("-fx-background-radius: 8; -fx-background-color: white; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 14, 0, 0, 4);");

        Runnable refreshMenu = () -> {
            String typed = field.getText().toLowerCase().trim();
            List<String> history = formHistory.getHistory(historyKey);

            List<String> filtered = history.stream()
                    .filter(v -> typed.isBlank() || v.toLowerCase().contains(typed))
                    .toList();

            menu.getItems().clear();

            if (filtered.isEmpty()) {
                menu.hide();
                return;
            }

            for (String value : filtered) {
                javafx.scene.control.MenuItem item = new javafx.scene.control.MenuItem();

                javafx.scene.layout.HBox content = new javafx.scene.layout.HBox(10);
                content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                Label clockIcon = new Label("🕐");
                clockIcon.setStyle("-fx-font-size: 12; -fx-text-fill: #94a3b8;");
                Label valueLabel = new Label(value);
                valueLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #1e293b;");
                content.getChildren().addAll(clockIcon, valueLabel);
                item.setGraphic(content);

                item.setOnAction(e -> {
                    field.setText(value);
                    field.positionCaret(value.length());
                    menu.hide();
                });
                menu.getItems().add(item);
            }

            if (!menu.isShowing() && field.getScene() != null) {
                menu.show(field, javafx.geometry.Side.BOTTOM, 0, 2);
            }
        };

        // Show on click (show all history)
        field.setOnMouseClicked(e -> {
            if (!formHistory.getHistory(historyKey).isEmpty()) {
                refreshMenu.run();
            }
        });

        // Filter as user types
        field.textProperty().addListener((obs, oldVal, newVal) -> refreshMenu.run());

        // Hide when field loses focus
        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) menu.hide();
        });
    }

    private void afficherFacture(models.CommandeConfirmation confirmation) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/frontoffice/facture.fxml"));
            javafx.scene.Parent root = loader.load();

            FactureController controller = loader.getController();
            controller.setConfirmation(confirmation);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Facture - PHARMAX");
            stage.setScene(scene);
            stage.setMaximized(true);

            // APPLICATION_MODAL bloque toute l'application (y compris les popups ContextMenu)
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            // Différer l'ouverture au prochain pulse JavaFX pour que les popups se ferment d'abord
            javafx.application.Platform.runLater(() -> {
                stage.show();
                stage.toFront();
            });

        } catch (java.io.IOException e) {
            e.printStackTrace();
            showError("Erreur d'affichage", "Impossible d'afficher la facture.");
        }
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
