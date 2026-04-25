package org.example;

import controllers.SidebarController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.*;

import java.io.IOException;
import java.sql.Connection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            // Créer un BorderPane principal
            BorderPane root = new BorderPane();

            // Charger la sidebar
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/Sidebar.fxml"));
            VBox sidebarPane = sidebarLoader.load();
            root.setLeft(sidebarPane);

            // Charger l'app bar
            FXMLLoader appBarLoader = new FXMLLoader(getClass().getResource("/AppBar.fxml"));
            HBox appBar = appBarLoader.load();
            root.setTop(appBar);

            // Définir le BorderPane principal et la sidebar dans le SidebarController
            SidebarController.setMainLayout(root);
            SidebarController.setSidebar(sidebarPane);

            // Charger le contenu initial (Dashboard)
            FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
            VBox dashboardContent = dashboardLoader.load();
            root.setCenter(dashboardContent);

            // Créer la scène
            Scene scene = new Scene(root, 1400, 800);

            // Charger le fichier CSS
            String css = getClass().getResource("/style.css").toExternalForm();
            scene.getStylesheets().add(css);

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
