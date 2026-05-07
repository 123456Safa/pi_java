package controllers.frontoffice;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import services.SavedCardService;

public class StripeLinkController {

    @FXML private Label amountLabel;
    @FXML private TextField emailField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expField;
    @FXML private TextField cvcField;
    @FXML private CheckBox saveInfoCheckBox;
    @FXML private HBox savedCardBox;
    @FXML private Label savedCardLabel;
    @FXML private Label errorLabel;
    @FXML private Button payerBtn;

    private final SavedCardService savedCardService = SavedCardService.getInstance();
    private double amount;
    private String clientEmail;
    private Runnable onSuccess;
    private Runnable onCancel;
    private Runnable onPaymentValidated;

    public void initData(String email, double amount, Runnable onSuccess, Runnable onCancel) {
        this.amount = amount;
        this.clientEmail = email;
        this.onSuccess = onSuccess;
        this.onCancel = onCancel;

        amountLabel.setText(String.format("%.2f DT", amount));
        payerBtn.setText(String.format("Payer %.2f DT", amount));
        if (email != null && !email.isBlank()) {
            emailField.setText(email);
        }
        saveInfoCheckBox.setSelected(true);

        cardNumberField.textProperty().addListener((obs, oldValue, newValue) -> {
            String digits = newValue == null ? "" : newValue.replaceAll("\\D", "");
            if (digits.length() > 16) {
                digits = digits.substring(0, 16);
            }

            String formatted = formatCardNumber(digits);
            if (!formatted.equals(newValue)) {
                cardNumberField.setText(formatted);
                cardNumberField.positionCaret(formatted.length());
            }
        });

        loadSavedCardIfAvailable();
    }

    public void setOnPaymentValidated(Runnable onPaymentValidated) {
        this.onPaymentValidated = onPaymentValidated;
    }

    @FXML
    private void handlePayer() {
        errorLabel.setText("");

        String validationError = validatePaymentForm();
        if (validationError != null) {
            errorLabel.setText(validationError);
            return;
        }

        payerBtn.setDisable(true);
        payerBtn.setText("Traitement...");
        saveCardIfRequested();

        new Thread(() -> {
            try {
                Thread.sleep(900);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                fermerFenetre();
                if (onPaymentValidated != null) {
                    onPaymentValidated.run();
                }
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

    @FXML
    private void handleUseAnotherCard() {
        savedCardBox.setVisible(false);
        savedCardBox.setManaged(false);
        cardNumberField.clear();
        expField.clear();
        cvcField.clear();
        cardNumberField.requestFocus();
    }

    private String validatePaymentForm() {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String cardNumber = cardNumberField.getText() == null ? "" : cardNumberField.getText().replaceAll("\\s+", "");
        String expiry = expField.getText() == null ? "" : expField.getText().replace(" ", "").trim();
        String cvc = cvcField.getText() == null ? "" : cvcField.getText().trim();

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "Adresse e-mail invalide.";
        }
        if (!cardNumber.matches("\\d{16}")) {
            return "Le numero de carte doit contenir exactement 16 chiffres.";
        }
        if (!isValidLuhn(cardNumber)) {
            return "Numero de carte invalide.";
        }
        if (!isValidExpiry(expiry)) {
            return "Date d'expiration invalide. Format attendu : MM/AA.";
        }
        if (!cvc.matches("\\d{3,4}")) {
            return "CVC invalide.";
        }

        return null;
    }

    private void loadSavedCardIfAvailable() {
        savedCardService.getSavedCard(clientEmail).ifPresent(savedCard -> {
            cardNumberField.setText(formatCardNumber(savedCard.cardNumber()));
            expField.setText(savedCard.expiry());
            cvcField.setText(savedCard.cvc());
            savedCardLabel.setText(savedCard.maskedNumber());
            savedCardBox.setVisible(true);
            savedCardBox.setManaged(true);
        });
    }

    private void saveCardIfRequested() {
        if (!saveInfoCheckBox.isSelected()) {
            return;
        }

        savedCardService.saveCard(
                emailField.getText(),
                cardNumberField.getText(),
                expField.getText(),
                cvcField.getText()
        );
    }

    private boolean isValidExpiry(String expiry) {
        return expiry != null && expiry.replace(" ", "").matches("(0[1-9]|1[0-2])/?\\d{2}");
    }

    private boolean isValidLuhn(String number) {
        int sum = 0;
        boolean doubleDigit = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return sum % 10 == 0;
    }

    private String formatCardNumber(String digits) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(' ');
            }
            formatted.append(digits.charAt(i));
        }
        return formatted.toString();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) payerBtn.getScene().getWindow();
        stage.close();
    }
}
