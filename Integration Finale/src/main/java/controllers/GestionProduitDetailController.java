package controllers;

import models.Produit;
import models.Categorie;
import services.CategorieService;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class GestionProduitDetailController {
    @FXML private Label lblNom;
    @FXML private Label lblDescription;
    @FXML private Label lblPrix;
    @FXML private Label lblQuantite;
    @FXML private Label lblStatut;
    @FXML private Label lblCategorie;
    @FXML private Label lblDateExpiration;
    @FXML private Label lblPrixFinal;
    @FXML private Label lblPromoCode;
    @FXML private Label lblDiscount;
    @FXML private ImageView imgProduit;

    private Runnable onCloseCallback;
    private Stage stage;
    private Produit produit;
    private Map<Integer, String> categorieMap = new HashMap<>();

    @FXML
    public void initialize() {
        chargerCategories();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setOnCloseCallback(Runnable onCloseCallback) {
        this.onCloseCallback = onCloseCallback;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        afficherDetails();
    }

    private void chargerCategories() {
        try {
            CategorieService categorieService = new CategorieService();
            List<Categorie> categories = categorieService.afficher();
            categorieMap.clear();
            for (Categorie cat : categories) {
                categorieMap.put(cat.getId(), cat.getNom());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void afficherDetails() {
        if (produit != null) {
            lblNom.setText(produit.getNom());
            lblDescription.setText(produit.getDescription());
            lblPrix.setText(String.format("%.2f DTN", produit.getPrix()));
            lblQuantite.setText(String.valueOf(produit.getQuantite()));
            lblStatut.setText(produit.getStatut());
            lblCategorie.setText(categorieMap.getOrDefault(produit.getCategorieId(), "N/A"));
            lblDateExpiration.setText(produit.getDateExpiration() != null ? produit.getDateExpiration().toString() : "N/A");
            lblPrixFinal.setText(String.format("%.2f DTN", produit.getPrixFinal()));
            lblPromoCode.setText(produit.getPromoCode() != null ? produit.getPromoCode() : "-");
            lblDiscount.setText(String.format("-%.0f%%", produit.getDiscountPercentage()));

            String imagePath = produit.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    File file = new File(imagePath);
                    if (file.exists()) {
                        Image image = new Image(file.toURI().toString(), 200, 200, true, true);
                        imgProduit.setImage(image);
                    } else {
                        imgProduit.setImage(null);
                    }
                } catch (Exception e) {
                    imgProduit.setImage(null);
                    e.printStackTrace();
                }
            } else {
                imgProduit.setImage(null);
            }
        }
    }

    @FXML
    private void onFermer() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        } else if (stage != null) {
            stage.close();
        }
    }
}
