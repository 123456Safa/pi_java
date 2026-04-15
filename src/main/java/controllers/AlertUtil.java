package controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class AlertUtil {

    /**
     * Affiche une alerte de succès stylisée
     */
    public static void showSuccess(String title, String message) {
        showAlert(AlertType.INFORMATION, title, message, "#d4edda", "#155724", "#c3e6cb");
    }

    /**
     * Affiche une alerte d'erreur stylisée
     */
    public static void showError(String title, String message) {
        showAlert(AlertType.ERROR, title, message, "#f8d7da", "#721c24", "#f5c6cb");
    }

    /**
     * Affiche une alerte d'avertissement stylisée
     */
    public static void showWarning(String title, String message) {
        showAlert(AlertType.WARNING, title, message, "#fff3cd", "#856404", "#ffeaa7");
    }

    /**
     * Affiche une alerte d'information stylisée
     */
    public static void showInfo(String title, String message) {
        showAlert(AlertType.INFORMATION, title, message, "#d1ecf1", "#0c5460", "#bee5eb");
    }

    /**
     * Affiche une alerte avec styles personnalisés
     */
    private static void showAlert(AlertType type, String title, String message,
                                   String bgColor, String textColor, String borderColor) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();

        // Style du DialogPane
        dialogPane.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 16;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: " + textColor + ";"
        );

        // Style du contenu
        dialogPane.setContentText(message);

        // Appliquer le style au texte du contenu
        dialogPane.getChildren().stream()
            .filter(node -> node instanceof VBox)
            .forEach(node -> {
                ((VBox) node).setPadding(new Insets(10));
                ((VBox) node).setStyle("-fx-text-fill: " + textColor + ";");
                ((VBox) node).getChildren().stream()
                    .forEach(child -> child.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: 500;"));
            });

        // Style des boutons
        dialogPane.getButtonTypes().forEach(buttonType -> {
            var button = dialogPane.lookupButton(buttonType);
            if (button != null) {
                button.setStyle(
                    "-fx-padding: 8 16;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-radius: 6;" +
                    "-fx-border-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-background-color: #5856d6;" +
                    "-fx-text-fill: white;"
                );
            }
        });

        alert.showAndWait();
    }

    /**
     * Affiche une alerte de confirmation stylisée
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();

        // Style du DialogPane
        dialogPane.setStyle(
            "-fx-background-color: #fff3cd;" +
            "-fx-border-color: #ffeaa7;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 16;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #856404;"
        );

        // Style des boutons
        dialogPane.getButtonTypes().forEach(buttonType -> {
            var button = dialogPane.lookupButton(buttonType);
            if (button != null) {
                if (buttonType.getText().equalsIgnoreCase("OK") ||
                    buttonType.getText().equalsIgnoreCase("Yes")) {
                    button.setStyle(
                        "-fx-padding: 8 16;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-background-radius: 6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-color: #28a745;" +
                        "-fx-text-fill: white;"
                    );
                } else {
                    button.setStyle(
                        "-fx-padding: 8 16;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;" +
                        "-fx-background-radius: 6;" +
                        "-fx-border-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-color: #6c757d;" +
                        "-fx-text-fill: white;"
                    );
                }
            }
        });

        return alert.showAndWait().get().getButtonData().isDefaultButton();
    }
}

