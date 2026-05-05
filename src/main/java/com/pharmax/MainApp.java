package com.pharmax;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * MainApp — Legacy entry point, now redirects to the unified PharmaX AppShell.
 * IntelliJ run configurations pointing here will launch the integrated platform.
 */
public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Start Notification API Server in a separate thread
        new Thread(() -> {
            try {
                new api.NotificationApiServer().start();
            } catch (Exception e) {
                System.err.println("⚠ Notification server failed to start: " + e.getMessage());
            }
        }).start();

        // Load the integrated AppShell (Front Office / Back Office)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app-shell.fxml"));
        Scene scene = new Scene(loader.load(), 1360, 820);

        // Load the combined stylesheet
        String appShellCss = getClass().getResource("/app-shell.css") != null
                ? getClass().getResource("/app-shell.css").toExternalForm() : null;
        if (appShellCss != null) {
            scene.getStylesheets().add(appShellCss);
        }

        primaryStage.setTitle("PHARMAX — E-Pharmacy Platform");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
