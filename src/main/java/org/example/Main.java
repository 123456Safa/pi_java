package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/HomeAdmin.fxml")
            );

            Scene scene = new Scene(root);

            primaryStage.setTitle("Gestion Réclamations");
            primaryStage.setScene(scene);
            primaryStage.setWidth(900);
            primaryStage.setHeight(600);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}