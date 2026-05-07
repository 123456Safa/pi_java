package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import models.Commandes;
import services.CommandeService;
import services.OrderQrCodeService;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QrScannerController {
    private static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile("\"nom\"\\s*:\\s*\"([^\"]+)\"");

    @FXML private TextField qrField;
    @FXML private Label commandeIdLabel;
    @FXML private Label produitsLabel;
    @FXML private Label totalLabel;
    @FXML private Label statutLabel;
    @FXML private Label dateLabel;
    @FXML private Button markDeliveredButton;

    private final CommandeService commandeService = new CommandeService();
    private final OrderQrCodeService qrCodeService = new OrderQrCodeService();
    private Commandes currentCommande;

    @FXML
    public void initialize() {
        markDeliveredButton.setDisable(true);
        qrField.setOnAction(event -> handleScan());
    }

    @FXML
    private void handleScan() {
        try {
            String token = qrCodeService.parseToken(qrField.getText());
            Commandes commande = commandeService.findByQrToken(token);
            if (commande == null) {
                clearCommande();
                showError("Commande introuvable", "Aucune commande trouvee pour ce QR.");
                return;
            }

            currentCommande = commande;
            afficherCommande(commande);
        } catch (IllegalArgumentException e) {
            clearCommande();
            showError("QR invalide", e.getMessage());
        } catch (SQLException e) {
            clearCommande();
            showError("Erreur base de donnees", e.getMessage());
        }
    }

    @FXML
    private void handleMarkDelivered() {
        if (currentCommande == null) {
            return;
        }

        try {
            commandeService.updateStatus(currentCommande.getId(), "Livré");
            currentCommande.setStatut("Livré");
            afficherCommande(currentCommande);
            showInfo("Statut modifie", "La commande #" + currentCommande.getId() + " est maintenant livree.");
        } catch (SQLException e) {
            showError("Erreur statut", "Impossible de passer la commande en Livree.");
        }
    }

    private void afficherCommande(Commandes commande) {
        commandeIdLabel.setText("#" + commande.getId());
        produitsLabel.setText(extractProductNames(commande.getProduits()));
        totalLabel.setText(String.format("%.2f DT", commande.getTotales()));
        statutLabel.setText(commande.getStatut());
        dateLabel.setText(commande.getCreatedAt());
        markDeliveredButton.setDisable(normalizeStatus(commande.getStatut()).contains("livre"));
    }

    private void clearCommande() {
        currentCommande = null;
        commandeIdLabel.setText("-");
        produitsLabel.setText("-");
        totalLabel.setText("-");
        statutLabel.setText("-");
        dateLabel.setText("-");
        markDeliveredButton.setDisable(true);
    }

    private String extractProductNames(String produits) {
        if (produits == null || produits.isBlank()) {
            return "";
        }

        Matcher matcher = PRODUCT_NAME_PATTERN.matcher(produits);
        StringBuilder names = new StringBuilder();
        while (matcher.find()) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(matcher.group(1));
        }
        return names.length() > 0 ? names.toString() : produits;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String normalizeStatus(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e");
    }
}
