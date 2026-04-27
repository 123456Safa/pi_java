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
    private void handlePayerFlouci() {
        try {
            // Extraire le montant numérique depuis le label (ex: "15.50 DT" -> 15.50)
            String montantStr = montantLabel.getText().replace(" DT", "").replace(",", ".");
            double montant = Double.parseDouble(montantStr);

            // On utilise un ID de commande fictif ou réel si disponible
            int dummyOrderId = (int) (Math.random() * 10000);

            String paymentId = services.FlouciPaymentService.genererPaiement(montant, dummyOrderId);

            if (paymentId != null) {
                // Dans un cas réel, on attendrait le retour du navigateur
                // Ici on ferme la fenêtre pour simuler le passage au paiement externe
                fermerFenetre();
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else {
                erreurLabel.setText("Erreur lors de la génération du lien Flouci.");
            }
        } catch (Exception e) {
            erreurLabel.setText("Erreur de traitement : " + e.getMessage());
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
        stage.close();
    }
}
