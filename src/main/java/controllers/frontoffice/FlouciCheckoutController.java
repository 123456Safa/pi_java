package controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import services.FlouciPaymentService;

public class FlouciCheckoutController {

    @FXML private Label montantLabel;

    private double montant;
    private int commandeId;
    private Runnable onSuccess;

    public void setData(double montant, int commandeId, Runnable onSuccess) {
        this.montant = montant;
        this.commandeId = commandeId;
        this.onSuccess = onSuccess;
        this.montantLabel.setText(String.format("%.3f DT", montant));
    }

    @FXML
    private void handlePayer() {
        // Appelle le service Flouci que nous avons créé
        String paymentId = FlouciPaymentService.genererPaiement(montant, commandeId);

        if (paymentId != null) {
            System.out.println("✅ Paiement Flouci initié. ID: " + paymentId);
            
            // Fermer la fenêtre et confirmer (simulation pour le moment)
            closeWindow();
            if (onSuccess != null) {
                onSuccess.run();
            }
        }
    }

    @FXML
    private void handleAnnuler() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) montantLabel.getScene().getWindow();
        stage.close();
    }
}
