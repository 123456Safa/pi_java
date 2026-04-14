package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class MainController {

    @FXML
    private BorderPane mainPane;

    @FXML
    public void showProduits() {
        loadView("produit.fxml");
    }

    @FXML
    public void showCommandes() {
        loadView("commande.fxml");
    }

    @FXML
    public void showLignes() {
        loadView("ligneCommande.fxml");
    }

    @FXML
    public void showUtilisateurs() {
        loadView("utilisateur.fxml");
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxml));
            Parent view = loader.load();
            mainPane.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
