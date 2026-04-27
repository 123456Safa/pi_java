package controllers.frontoffice;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class StripeLinkController {

    @FXML private TextField emailField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expField;
    @FXML private TextField cvcField;
    @FXML private Label errorLabel;
    @FXML private Button payerBtn;

    private Runnable onSuccess;
    private Runnable onCancel;

    public void initData(String email, double amount, Runnable onSuccess, Runnable onCancel) {
        if (email != null && !email.isEmpty()) {
            emailField.setText(email);
        }
        payerBtn.setText(String.format("Payer %.2f DT", amount));
        
        this.onSuccess = onSuccess;
        this.onCancel = onCancel;
    }

    @FXML
    private void handlePayer() {
        errorLabel.setText("");
        
        // Validation simple
        if (cardNumberField.getText().replaceAll("\\s+", "").length() < 16) {
            errorLabel.setText("Numéro de carte invalide.");
            return;
        }
        if (!expField.getText().matches("(0[1-9]|1[0-2])\\/?([0-9]{2})")) {
            errorLabel.setText("Date d'expiration invalide (MM/AA).");
            return;
        }
        if (cvcField.getText().length() < 3) {
            errorLabel.setText("CVC invalide.");
            return;
        }

        payerBtn.setDisable(true);
        payerBtn.setText("Traitement...");
        payerBtn.setStyle("-fx-background-color: #00b35c; -fx-text-fill: white; -fx-font-size: 16; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 14 0;");
        
        // Simuler un délai de traitement réseau (1.5s)
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> {
                fermerFenetre();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            });
        }).start();
    }

    @FXML
    private void handleAnnuler() {
        fermerFenetre();
        if (onCancel != null) {
            onCancel.run();
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) payerBtn.getScene().getWindow();
        stage.close();
    }
}
