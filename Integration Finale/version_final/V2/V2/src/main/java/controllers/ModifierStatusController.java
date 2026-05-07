package controllers;

import models.Reclamation;
import services.ReclamationService;
import services.EmailService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ModifierStatusController {

    @FXML private ComboBox<String> statusBox;
    @FXML private Label lblReclamation;
    @FXML private Label lblStatusActuel;

    private Reclamation r;
    private HomeAdminController homeController;
    private DetailReclamationControlleradmin parentController;
    private final ReclamationService service = new ReclamationService();

    public void setHomeController(HomeAdminController homeController) {
        this.homeController = homeController;
    }

    public void setParentController(DetailReclamationControlleradmin parent) {
        this.parentController = parent;
    }

    @FXML
    public void initialize() {
        statusBox.getItems().addAll("EN ATTENTE", "EN COURS", "RÉSOLUE");
    }

    public void setData(Reclamation r) {
        this.r = r;
        lblReclamation.setText(r.getTitre());
        lblStatusActuel.setText(r.getStatut());
        lblStatusActuel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: "
                + getStatusColor(r.getStatut()) + "; -fx-padding: 5 10; -fx-border-radius: 3;");
        statusBox.setValue(r.getStatut());
    }

    private String getStatusColor(String statut) {
        switch (statut.toUpperCase()) {
            case "EN ATTENTE": return "#FFA500";
            case "RÉSOLUE": return "#28a745";
            case "FERMÉ": return "#6c757d";
            case "EN COURS": return "#17a2b8";
            default: return "#999999";
        }
    }

    @FXML
    public void save() {
        String newStatus = statusBox.getValue();
        String oldStatus = r.getStatut();
        r.setStatut(newStatus);
        service.updateStatus(r);

        if (newStatus != null && newStatus.equalsIgnoreCase("RÉSOLUE") && !oldStatus.equalsIgnoreCase("RÉSOLUE")) {
            sendResolutionEmail();
        }

        if (parentController != null) parentController.refreshStatus(newStatus);
        if (homeController != null) homeController.refresh();
        ((Stage) statusBox.getScene().getWindow()).close();
    }

    private void sendResolutionEmail() {
        String userEmail = "safabaalouch25@gmail.com";
        String userName = "Safa Baalouch";
        new Thread(() -> EmailService.sendReclamationResolvedEmail(userEmail, userName, r.getTitre())).start();
    }

    @FXML
    public void cancel() {
        ((Stage) statusBox.getScene().getWindow()).close();
    }
}
