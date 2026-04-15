package controllers;

import model.Produit;
import model.Categorie;
import service.CategorieService;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ProduitDetailController {
    @FXML private Label lblNom;
    @FXML private Label lblDescription;
    @FXML private Label lblPrix;
    @FXML private Label lblQuantite;
    @FXML private Label lblStatut;
    @FXML private Label lblCategorie;
    @FXML private Label lblDateExpiration;
    @FXML private ImageView imgProduit;

    private Stage stage;
    private Produit produit;
    private Map<Integer, String> categorieMap = new HashMap<>();

    @FXML
    public void initialize() {
        // Charger les catégories pour le mapping
        chargerCategories();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
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

            // Afficher l'image
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
        if (stage != null) {
            stage.close();
        }
    }
}
