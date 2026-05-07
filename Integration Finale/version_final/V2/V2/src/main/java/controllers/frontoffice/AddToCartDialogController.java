package controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.PanierItem;
import models.Produit;
import services.PanierService;

public class AddToCartDialogController {

    @FXML
    private Label productImageText;
    @FXML
    private Label productNameLabel;
    @FXML
    private Label productPriceLabel;
    @FXML
    private Label cartArticlesLabel;
    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label totalCartLabel;
    @FXML
    private Button closeButton;

    private Produit produit;
    private Runnable onContinue;
    private Runnable onCheckout;

    public void setDialogData(Produit produit, Runnable onContinue, Runnable onCheckout) {
        this.produit = produit;
        this.onContinue = onContinue;
        this.onCheckout = onCheckout;

        // Populate Left Side (Product Details)
        if (produit != null) {
            String initial = produit.getNom() == null || produit.getNom().isBlank() ? "+" : produit.getNom().substring(0, 1).toUpperCase();
            productImageText.setText(initial);
            productNameLabel.setText(produit.getNom());
            productPriceLabel.setText(String.format("%.3f TND", produit.getPrix()));
        }

        // Populate Right Side (Cart Summary)
        int numArticles = PanierService.getQuantiteTotale();
        double sousTotal = PanierService.getTotal();
        double fraisDePort = 7.000;
        double total = sousTotal + fraisDePort;

        cartArticlesLabel.setText("Il y a " + numArticles + " article(s) dans votre panier.");
        totalProductsLabel.setText(String.format("%.3f TND", sousTotal));
        totalCartLabel.setText(String.format("%.3f TND TTC", total));
    }

    @FXML
    private void handleClose() {
        closeStage();
    }

    @FXML
    private void handleContinue() {
        closeStage();
        if (onContinue != null) {
            onContinue.run();
        }
    }

    @FXML
    private void handleCheckout() {
        closeStage();
        if (onCheckout != null) {
            onCheckout.run();
        }
    }

    private void closeStage() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
