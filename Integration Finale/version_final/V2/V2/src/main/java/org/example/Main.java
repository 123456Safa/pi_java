package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Start Notification API Server in a separate thread
        new Thread(() -> {
            new api.NotificationApiServer().start();
        }).start();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app-shell.fxml"));
        Scene scene = new Scene(loader.load(), 1360, 820);
        primaryStage.setTitle("PHARMAX - E-Pharmacy");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
