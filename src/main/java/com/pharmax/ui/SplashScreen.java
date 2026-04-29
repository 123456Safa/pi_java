package com.pharmax.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * SplashScreen — Modern loading screen with animated spinner.
 * Shows while the application initializes in the background.
 */
public class SplashScreen {

    private Stage stage;
    private Label messageLabel;
    private Label spinnerLabel;
    private int spinnerIndex = 0;
    private final String[] SPINNER_CHARS = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    private AnimationTimer animationTimer;

    public SplashScreen() {
        createStage();
    }

    private void createStage() {
        stage = new Stage(StageStyle.UNDECORATED);
        stage.setWidth(500);
        stage.setHeight(400);
        stage.centerOnScreen();

        // Main Container
        VBox root = new VBox(20);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setBackground(new Background(
                new BackgroundFill(
                        Color.web("#2d8659"),
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )
        ));

        // Logo/Title
        Label logo = new Label("⚕");
        logo.setStyle(
                "-fx-font-size: 60;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;"
        );

        Label title = new Label("PharmaX");
        title.setStyle(
                "-fx-font-size: 32;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: white;" +
                "-fx-font-family: 'Segoe UI';"
        );

        Label subtitle = new Label("Blog & Comment Management");
        subtitle.setStyle(
                "-fx-font-size: 13;" +
                "-fx-text-fill: rgba(255,255,255,0.8);" +
                "-fx-font-family: 'Segoe UI';"
        );

        // Spinner
        spinnerLabel = new Label(SPINNER_CHARS[0]);
        spinnerLabel.setStyle(
                "-fx-font-size: 28;" +
                "-fx-text-fill: rgba(255,255,255,0.9);" +
                "-fx-font-family: monospace;"
        );

        // Message
        messageLabel = new Label("Initializing application...");
        messageLabel.setStyle(
                "-fx-font-size: 13;" +
                "-fx-text-fill: rgba(255,255,255,0.85);" +
                "-fx-font-family: 'Segoe UI';"
        );

        // Progress indicator (visual)
        VBox progressContainer = new VBox(8);
        progressContainer.setAlignment(Pos.CENTER);
        progressContainer.setStyle(
                "-fx-border-color: rgba(255,255,255,0.3);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 4;" +
                "-fx-background-color: rgba(0,0,0,0.15);" +
                "-fx-padding: 12;"
        );

        Label version = new Label("Version 1.0");
        version.setStyle(
                "-fx-font-size: 11;" +
                "-fx-text-fill: rgba(255,255,255,0.6);"
        );

        progressContainer.getChildren().addAll(spinnerLabel, messageLabel);

        root.getChildren().addAll(logo, title, subtitle, progressContainer, version);

        Scene scene = new Scene(root);
        stage.setScene(scene);

        // Start animation
        startAnimation();
    }

    private void startAnimation() {
        animationTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 80_000_000) {  // 80ms
                    spinnerIndex = (spinnerIndex + 1) % SPINNER_CHARS.length;
                    spinnerLabel.setText(SPINNER_CHARS[spinnerIndex]);
                    lastUpdate = now;
                }
            }
        };
        animationTimer.start();
    }

    public void show() {
        Platform.runLater(() -> stage.show());
    }

    public void updateMessage(String message) {
        Platform.runLater(() -> messageLabel.setText(message));
    }

    public void close() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        Platform.runLater(() -> stage.close());
    }

    public Stage getStage() {
        return stage;
    }
}
