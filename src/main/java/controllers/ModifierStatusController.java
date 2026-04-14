package controllers;
import Model.Reclamation;
import Service.ReclamationService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ModifierStatusController {
    private HomeAdminController homeController;
    private DetailReclamationControlleradmin parentController;

    public void setHomeController(HomeAdminController homeController) {
        this.homeController = homeController;
    }

    public void setParentController(DetailReclamationControlleradmin parent) {
        this.parentController = parent;
    }
    
    @FXML private ComboBox<String> statusBox;
    @FXML private Label lblReclamation;
    @FXML private Label lblStatusActuel;

    private Reclamation r;
    private final ReclamationService service = new ReclamationService();

    @FXML
    public void initialize() {
        statusBox.getItems().addAll("EN ATTENTE", "EN COURS", "RÉSOLUE");
    }

    public void setData(Reclamation r) {
        this.r = r;
        
        // Display reclamation title
        lblReclamation.setText(r.getTitre());
        
        // Display current status
        lblStatusActuel.setText(r.getStatut());
        String statusColor = getStatusColor(r.getStatut());
        lblStatusActuel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 5 10; -fx-border-radius: 3;");
        
        // Set current status in combo box
        statusBox.setValue(r.getStatut());
    }

    private String getStatusColor(String statut) {
        switch (statut.toUpperCase()) {
            case "EN ATTENTE":
                return "#FFA500";
            case "RÉSOLUE":
                return "#28a745";
            case "FERMÉ":
                return "#6c757d";
            case "EN COURS":
                return "#17a2b8";
            default:
                return "#999999";
        }
    }

    @FXML
    public void save() {
        String newStatus = statusBox.getValue();
        r.setStatut(newStatus);
        service.updateStatus(r);

        // Refresh parent controller's status
        if (parentController != null) {
            parentController.refreshStatus(newStatus);
        }

        // Refresh home admin controller
        if (homeController != null) {
            homeController.refresh();
        }

        ((Stage) statusBox.getScene().getWindow()).close();
    }

    @FXML
    public void cancel() {
        ((Stage) statusBox.getScene().getWindow()).close();
    }
}