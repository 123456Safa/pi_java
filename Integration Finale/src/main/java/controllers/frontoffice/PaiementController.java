package controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PaiementController {

    @FXML private Label montantLabel;
    @FXML private TextField carteField;
    @FXML private TextField expField;
    @FXML private TextField cvcField;
    @FXML private Label erreurLabel;

    private Runnable onSuccess;
    private Runnable onCancel;

    public void initData(double montant, Runnable onSuccess, Runnable onCancel) {
        montantLabel.setText(String.format("%.2f DT", montant));
        this.onSuccess = onSuccess;
        this.onCancel = onCancel;
    }

    @FXML
    private void handlePayer() {
        erreurLabel.setText("");

        String carte = carteField.getText().replaceAll("\\s+", "");
        String exp = expField.getText().trim();
        String cvc = cvcField.getText().trim();

        if (carte.length() != 16 || !carte.matches("\\d+")) {
            erreurLabel.setText("Numéro de carte invalide (16 chiffres requis).");
            return;
        }

        if (!exp.matches("(0[1-9]|1[0-2])/[0-9]{2}")) {
            erreurLabel.setText("Date d'expiration invalide (Format: MM/AA).");
            return;
        }

        if (cvc.length() != 3 || !cvc.matches("\\d+")) {
            erreurLabel.setText("CVC invalide (3 chiffres requis).");
            return;
        }

        // Si tout est valide, on simule un paiement réussi
        fermerFenetre();
        if (onSuccess != null) {
            onSuccess.run();
        }
    }

    @FXML
    private void handleAnnuler() {
        fermerFenetre();
        if (onCancel != null) {
            onCancel.run();
        }
    }

    private void fermerFenetre() {
        Stage stage = (Stage) montantLabel.getScene().getWindow();
        if (stage != null) stage.close();
    }
}
