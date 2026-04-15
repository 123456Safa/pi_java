package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.DataInitializer;

public class Main extends Application {
    public static void main(String[] args) {
        DataInitializer.initializeSampleData();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app-shell.fxml"));
        Scene scene = new Scene(loader.load(), 1360, 820);
        primaryStage.setTitle("PHARMAX - E-Pharmacy");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
