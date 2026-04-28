package controllers;
import Model.Reclamation;
import Service.ReclamationService;
import Service.EmailService;
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
/*
    @FXML
    public void save() {
        String newStatus = statusBox.getValue();
        String oldStatus = r.getStatut();
        r.setStatut(newStatus);
        service.updateStatus(r);

        // Envoyer un email si le statut passe à "RÉSOLUE"
        if (newStatus != null && newStatus.equalsIgnoreCase("RÉSOLUE") && !oldStatus.equalsIgnoreCase("RÉSOLUE")) {
            sendResolutionEmail();
        }

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
*/
    /**
     * Envoie un email au client pour informer que sa réclamation a été résolue
     *//*
    private void sendResolutionEmail() {
        try {
            // Récupérer l'email et le nom de l'utilisateur
            String userEmail = service.getUserEmailById(r.getUserId());
            String userName = service.getUserNameById(r.getUserId());

            if (userEmail != null && !userEmail.isEmpty()) {
                // Envoyer l'email de manière asynchrone pour ne pas bloquer l'interface
                new Thread(() -> {
                    EmailService.sendReclamationResolvedEmail(userEmail, userName, r.getTitre());
                }).start();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
        }
    }
*/
    private void sendResolutionEmail() {

        String userEmail = "safabaalouch25@gmail.com"; // ثابت
        String userName = "Safa Baalouch";

        new Thread(() -> {
            EmailService.sendReclamationResolvedEmail(
                    userEmail,
                    userName,
                    r.getTitre()
            );
        }).start();
    }
    @FXML
    public void save() {

        String newStatus = statusBox.getValue();
        String oldStatus = r.getStatut();

        r.setStatut(newStatus);
        service.updateStatus(r);

        // إرسال email فقط إذا تحولت إلى RÉSOLUE
        if (newStatus != null
                && newStatus.equalsIgnoreCase("RÉSOLUE")
                && !oldStatus.equalsIgnoreCase("RÉSOLUE")) {

            sendResolutionEmail();
        }

        if (parentController != null) {
            parentController.refreshStatus(newStatus);
        }

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