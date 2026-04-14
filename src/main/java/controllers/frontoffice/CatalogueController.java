package controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Produit;
import models.PanierItem;
import services.ProduitService;
import services.PanierService;

import java.util.List;

public class CatalogueController {

    @FXML
    private ScrollPane cataloguePane;

    private ProduitService produitService = new ProduitService();

    @FXML
    public void initialize() {
        loadProduits();
    }

    private void loadProduits() {
        try {
            System.out.println("🔄 Chargement des produits...");
            List<Produit> produits = produitService.select();
            System.out.println("📦 Nombre de produits chargés: " + produits.size());
            
            if (produits.isEmpty()) {
                System.err.println("⚠️ ATTENTION: Aucun produit trouvé!");
                System.err.println("   Vérifiez:");
                System.err.println("   1. La BD 'pharm' existe");
                System.err.println("   2. La table 'produits' existe");
                System.err.println("   3. Des données ont été insérées");
            }
            
            VBox container = new VBox();
            container.setSpacing(10);
            container.setPadding(new Insets(10));
            container.setStyle("-fx-font-family: 'Arial'; -fx-background-color: #f5f5f5;");

            if (produits.isEmpty()) {
                Label emptyLabel = new Label("❌ Aucun produit disponible.\n\nVérifiez la base de données 'pharm' et la table 'produits'.\nAssurez-vous que les données ont été insérées.");
                emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #e74c3c; -fx-padding: 20;");
                emptyLabel.setWrapText(true);
                container.getChildren().add(emptyLabel);
            } else {
                System.out.println("✅ Affichage de " + produits.size() + " produits");
                for (Produit produit : produits) {
                    System.out.println("   - " + produit.getNom() + " (" + produit.getPrix() + " DT)");
                    HBox productCard = createProductCard(produit);
                    container.getChildren().add(productCard);
                }
            }

            cataloguePane.setContent(container);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des produits:");
            e.printStackTrace();
            
            VBox errorContainer = new VBox();
            errorContainer.setPadding(new Insets(20));
            Label errorLabel = new Label("❌ ERREUR:\n" + e.getMessage());
            errorLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #e74c3c;");
            errorLabel.setWrapText(true);
            errorContainer.getChildren().add(errorLabel);
            cataloguePane.setContent(errorContainer);
        }
    }

    private HBox createProductCard(Produit produit) {
        HBox card = new HBox();
        card.setSpacing(15);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: white;");

        // Image placeholder
        VBox imageBox = new VBox();
        imageBox.setPrefWidth(100);
        imageBox.setPrefHeight(100);
        imageBox.setStyle("-fx-background-color: #e0e0e0; -fx-border-radius: 5;");
        Label imagePlaceholder = new Label("Image");
        imagePlaceholder.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");
        imageBox.getChildren().add(imagePlaceholder);

        // Product details
        VBox details = new VBox();
        details.setSpacing(5);
        details.setPrefWidth(300);

        Label nomLabel = new Label(produit.getNom());
        nomLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        Label descriptionLabel = new Label(produit.getDescription());
        descriptionLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        descriptionLabel.setWrapText(true);

        Label prixLabel = new Label(String.format("%.2f DT", produit.getPrix()));
        prixLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");

        details.getChildren().addAll(nomLabel, descriptionLabel, prixLabel);

        // Add to cart button
        Button ajouterBtn = new Button("Ajouter au panier");
        ajouterBtn.setStyle("-fx-font-size: 12; -fx-padding: 8px 15px; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-border-radius: 3;");
        ajouterBtn.setOnAction(e -> ajouterAuPanier(produit));

        HBox.setHgrow(details, javafx.scene.layout.Priority.ALWAYS);
        card.getChildren().addAll(imageBox, details, ajouterBtn);

        return card;
    }

    private void ajouterAuPanier(Produit produit) {
        PanierItem item = new PanierItem(
            produit.getId(),
            produit.getNom(),
            produit.getPrix(),
            produit.getDescription(),
            produit.getDescription()
        );
        PanierService.ajouterAuPanier(item);
        // Show confirmation
        System.out.println("✅ " + produit.getNom() + " ajouté au panier!");
    }
}
