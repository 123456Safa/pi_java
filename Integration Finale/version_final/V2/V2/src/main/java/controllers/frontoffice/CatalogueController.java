package controllers.frontoffice;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import models.PanierItem;
import models.Produit;
import services.PanierService;
import services.ProduitService;

import java.util.List;

public class CatalogueController {

    @FXML
    private VBox productsContainer;

    private ProduitService produitService;

    @FXML
    public void initialize() {
        loadProduits();
    }

    private void loadProduits() {
        try {
            if (produitService == null) {
                produitService = new ProduitService();
            }
            List<Produit> produits = produitService.select();

            javafx.scene.layout.TilePane container = new javafx.scene.layout.TilePane();
            container.setPrefColumns(3); // Attempt to show 3 cards per row
            container.setHgap(20);
            container.setVgap(20);
            container.setPadding(new Insets(20));
            container.getStyleClass().add("catalogue-list");

            if (produits.isEmpty()) {
                Label emptyLabel = new Label("Aucun produit disponible pour le moment.");
                emptyLabel.getStyleClass().add("empty-state");
                emptyLabel.setWrapText(true);
                container.getChildren().add(emptyLabel);
            } else {
                for (Produit produit : produits) {
                    container.getChildren().add(createProductCard(produit));
                }
            }

            productsContainer.getChildren().setAll(container);
        } catch (Exception e) {
            VBox errorContainer = new VBox();
            errorContainer.setPadding(new Insets(20));
            Label errorLabel = new Label("Erreur lors du chargement des produits.\n" + e.getMessage());
            errorLabel.getStyleClass().add("empty-state");
            errorLabel.setWrapText(true);
            errorContainer.getChildren().add(errorLabel);
            productsContainer.getChildren().setAll(errorContainer);
        }
    }

    private VBox createProductCard(Produit produit) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("product-card");
        card.setPrefWidth(240); // Fixed width for a uniform grid
        card.setMaxWidth(240);

        VBox imageBox = new VBox();
        imageBox.setPrefHeight(140);
        imageBox.setMinHeight(140);
        imageBox.getStyleClass().add("product-card-media");
        imageBox.setAlignment(javafx.geometry.Pos.CENTER);

        String initial = produit.getNom() == null || produit.getNom().isBlank()
                ? "+"
                : produit.getNom().substring(0, 1).toUpperCase();
        Label imagePlaceholder = new Label(initial);
        imagePlaceholder.getStyleClass().add("product-card-media-label");
        imageBox.getChildren().add(imagePlaceholder);

        VBox details = new VBox(8);
        VBox.setVgrow(details, Priority.ALWAYS);

        Label nomLabel = new Label(produit.getNom());
        nomLabel.getStyleClass().add("product-card-title");
        nomLabel.setWrapText(true);

        Label descriptionLabel = new Label(produit.getDescription() == null || produit.getDescription().isBlank()
                ? "Produit de parapharmacie"
                : produit.getDescription());
        descriptionLabel.getStyleClass().add("product-card-description");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setPrefHeight(40); // keep description height uniform

        Label prixLabel = new Label(String.format("%.2f DT", produit.getPrix()));
        prixLabel.getStyleClass().add("product-card-price");

        details.getChildren().addAll(nomLabel, descriptionLabel, prixLabel);

        Button ajouterBtn = new Button("Ajouter au panier");
        ajouterBtn.getStyleClass().addAll("fo-action-button", "fo-action-primary");
        ajouterBtn.setMaxWidth(Double.MAX_VALUE); // Full width button

        Label addedMessageLabel = new Label("Ajoute au panier");
        addedMessageLabel.getStyleClass().add("cart-inline-message");
        addedMessageLabel.setVisible(false);
        addedMessageLabel.setManaged(false);

        PauseTransition messageDelay = new PauseTransition(Duration.seconds(2));
        messageDelay.setOnFinished(event -> {
            addedMessageLabel.setVisible(false);
            addedMessageLabel.setManaged(false);
        });

        ajouterBtn.setOnAction(e -> ajouterAuPanier(produit, addedMessageLabel, messageDelay));

        VBox actionBox = new VBox(8, addedMessageLabel, ajouterBtn);
        actionBox.setAlignment(javafx.geometry.Pos.BOTTOM_CENTER);

        card.getChildren().addAll(imageBox, details, actionBox);
        return card;
    }

    private void ajouterAuPanier(Produit produit, Label addedMessageLabel, PauseTransition messageDelay) {
        PanierItem item = new PanierItem(
                produit.getId(),
                produit.getNom(),
                produit.getPrix(),
                produit.getDescription(),
                produit.getDescription()
        );
        PanierService.ajouterAuPanier(item);

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/frontoffice/add_to_cart_dialog.fxml"));
            javafx.scene.Parent root = loader.load();
            AddToCartDialogController controller = loader.getController();

            Runnable onContinue = () -> {
                // Do nothing, just stay on catalogue
            };
            Runnable onCheckout = () -> {
                // Navigate to Cart
                javafx.scene.control.Button panierBtn = (javafx.scene.control.Button) productsContainer.getScene().lookup("#panierButton");
                if (panierBtn != null) {
                    panierBtn.fire();
                }
            };

            controller.setDialogData(produit, onContinue, onCheckout);

            javafx.stage.Stage stage = new javafx.stage.Stage(javafx.stage.StageStyle.TRANSPARENT);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            try {
                scene.getStylesheets().add(getClass().getResource("/frontoffice/frontoffice.css").toExternalForm());
            } catch (Exception e) {}
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initOwner(productsContainer.getScene().getWindow());
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
