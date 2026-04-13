package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.*;

import java.io.IOException;
import java.sql.Connection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Créer un TabPane pour afficher les onglets
            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            // Charger l'onglet Produits
            FXMLLoader produitLoader = new FXMLLoader(getClass().getResource("/Produit.fxml"));
            BorderPane produitRoot = produitLoader.load();
            Tab produitTab = new Tab("Produits", produitRoot);
            tabPane.getTabs().add(produitTab);

            // Charger l'onglet Catégories
            FXMLLoader categorieLoader = new FXMLLoader(getClass().getResource("/Categorie.fxml"));
            BorderPane categorieRoot = categorieLoader.load();
            Tab categorieTab = new Tab("Catégories", categorieRoot);
            tabPane.getTabs().add(categorieTab);

            // Créer la scène
            Scene scene = new Scene(tabPane, 1400, 800);

            // Configurer la fenêtre
            primaryStage.setTitle("Gestion des Produits et Catégories");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du fichier FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Test de la connexion à la base de données
        Connection connection = MyDataBase.getInstance().getConnection();
        if (connection != null) {
            System.out.println("✓ Connexion à la base de données établie avec succès!");
        } else {
            System.out.println("✗ Erreur: Impossible de se connecter à la base de données!");
        }

        // Lancer l'application JavaFX
        launch(args);
    }
}
