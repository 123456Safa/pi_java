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
    private ScrollPane cataloguePane;

    private final ProduitService produitService = new ProduitService();

    @FXML
    public void initialize() {
        loadProduits();
    }

    private void loadProduits() {
        try {
            List<Produit> produits = produitService.select();

            VBox container = new VBox(16);
            container.setPadding(new Insets(4));
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

            cataloguePane.setContent(container);
        } catch (Exception e) {
            VBox errorContainer = new VBox();
            errorContainer.setPadding(new Insets(20));
            Label errorLabel = new Label("Erreur lors du chargement des produits.\n" + e.getMessage());
            errorLabel.getStyleClass().add("empty-state");
            errorLabel.setWrapText(true);
            errorContainer.getChildren().add(errorLabel);
            cataloguePane.setContent(errorContainer);
        }
    }

    private HBox createProductCard(Produit produit) {
        HBox card = new HBox(18);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("product-card");

        VBox imageBox = new VBox();
        imageBox.setPrefWidth(92);
        imageBox.setPrefHeight(92);
        imageBox.getStyleClass().add("product-card-media");

        String initial = produit.getNom() == null || produit.getNom().isBlank()
                ? "+"
                : produit.getNom().substring(0, 1).toUpperCase();
        Label imagePlaceholder = new Label(initial);
        imagePlaceholder.getStyleClass().add("product-card-media-label");
        imageBox.getChildren().add(imagePlaceholder);

        VBox details = new VBox(8);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label nomLabel = new Label(produit.getNom());
        nomLabel.getStyleClass().add("product-card-title");

        Label descriptionLabel = new Label(produit.getDescription() == null || produit.getDescription().isBlank()
                ? "Produit de parapharmacie"
                : produit.getDescription());
        descriptionLabel.getStyleClass().add("product-card-description");
        descriptionLabel.setWrapText(true);

        Label prixLabel = new Label(String.format("%.2f DT", produit.getPrix()));
        prixLabel.getStyleClass().add("product-card-price");

        details.getChildren().addAll(nomLabel, descriptionLabel, prixLabel);

        Button ajouterBtn = new Button("Ajouter au panier");
        ajouterBtn.getStyleClass().addAll("fo-action-button", "fo-action-primary");

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
        actionBox.setFillWidth(false);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(imageBox, details, spacer, actionBox);
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

    }
}
