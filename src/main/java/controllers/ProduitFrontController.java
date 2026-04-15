package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Produit;
import service.ProduitService;
import service.CategorieService;
import model.Categorie;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProduitFrontController {
    @FXML
    private VBox vboxProduits;

    private ProduitService produitService = new ProduitService();
    private CategorieService categorieService = new CategorieService();
    private Map<Integer, String> categorieMap = new HashMap<>();

    @FXML
    public void initialize() {
        vboxProduits.getStylesheets().add(getClass().getResource("/produitfront.css").toExternalForm());
        chargerCategories();
        afficherTousLesProduits();
    }

    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieService.afficher();
            categorieMap.clear();
            for (Categorie cat : categories) {
                categorieMap.put(cat.getId(), cat.getNom());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void afficherTousLesProduits() {
        vboxProduits.getChildren().clear();
        try {
            List<Produit> produits = produitService.afficher();
            for (Produit produit : produits) {
                vboxProduits.getChildren().add(creerCarteProduit(produit));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox creerCarteProduit(Produit produit) {
        HBox hbox = new HBox(20);
        hbox.getStyleClass().add("produit-card");
        hbox.setPrefHeight(160);

        ImageView imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);
        String imagePath = produit.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);
            if (file.exists()) {
                imageView.setImage(new Image(file.toURI().toString(), 120, 120, true, true));
            }
        }

        VBox vboxInfos = new VBox(8);
        Label lblNom = new Label(produit.getNom());
        lblNom.getStyleClass().add("label-nom");
        Label lblDesc = new Label(produit.getDescription());
        lblDesc.getStyleClass().add("label-desc");
        Label lblPrix = new Label(String.format("Prix : %.2f DTN", produit.getPrix()));
        lblPrix.getStyleClass().add("label-prix");
        Label lblCategorie = new Label("Catégorie : " + categorieMap.getOrDefault(produit.getCategorieId(), "N/A"));
        lblCategorie.getStyleClass().add("label-categorie");
        vboxInfos.getChildren().addAll(lblNom, lblDesc, lblPrix, lblCategorie);

        hbox.getChildren().addAll(imageView, vboxInfos);
        return hbox;
    }
}
