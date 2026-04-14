package controllers;

import Model.Reclamation;
import Model.Reponse;
import Service.ReponseService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.util.List;

public class DetailReclamationController {

    @FXML private Label lblTitre;
    @FXML private Label lblDate;
    @FXML private Label lblStatut;
    @FXML private TextArea txtDescription;
    @FXML private Label lblReponseCount;

    @FXML private VBox reponseContainer;
    @FXML private Button btnEditer;
    @FXML private Button btnSupprimer;

    private final ReponseService service = new ReponseService();
    private final Service.ReclamationService reclamationService = new Service.ReclamationService();
    private Reclamation currentReclamation;
    private HomeReclamationController homeController;

    public void setData(Reclamation r) {
        this.currentReclamation = r;

        lblTitre.setText(r.getTitre());
        lblDate.setText("📅 Créée le " + r.getDateCreation());
        lblStatut.setText(r.getStatut());
        
        // Style mta3 status
        String statusColor = getStatusColor(r.getStatut());
        lblStatut.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 5 10; -fx-border-radius: 3;");
        
        txtDescription.setText(r.getDescription());

        loadReponses(r.getId());
    }

    private String getStatusColor(String statut) {
        switch (statut.toUpperCase()) {
            case "EN ATTENTE":
                return "#FFA500";
            case "RÉSOLUE":
                return "#28a745";

            case "EN COURS":
                return "#17a2b8";
            default:
                return "#999999";
        }
    }
//reponse 7atinahoum fi vbox
    private void loadReponses(int reclamationId) {

        reponseContainer.getChildren().clear();

        List<Reponse> list = service.getByReclamationId(reclamationId);

        if (list == null || list.isEmpty()) {
            Label empty = new Label("Il y a pas de réponse");
            empty.setStyle("-fx-font-size: 12; -fx-text-fill: #999999; -fx-padding: 20;");
            reponseContainer.getChildren().add(empty);
            lblReponseCount.setText("(0)");
            return;
        }

        lblReponseCount.setText("(" + list.size() + ")");

        for (Reponse rep : list) {
            VBox reponseCard = createReponseCard(rep);
            reponseContainer.getChildren().add(reponseCard);
        }
    }

    private VBox createReponseCard(Reponse rep) {
        //
        VBox card = new VBox(8);
        card.setStyle("-fx-border-color: transparent transparent transparent #28a745; -fx-border-width: 0 0 0 4; -fx-background-color: #f9f9f9; -fx-padding: 15;");
        
        // Header with date only
        HBox headerBox = new HBox(10);
        headerBox.setStyle("-fx-alignment: center-left;");
        
        Label date = new Label("📅 " + rep.getDateReponse());
        date.setStyle("-fx-font-size: 11; -fx-text-fill: #999999;");
        
        headerBox.getChildren().add(date);
        
        // Content
        Label contenu = new Label(rep.getContenu());
        contenu.setWrapText(true);
        contenu.setStyle("-fx-font-size: 12; -fx-text-fill: #555555;");
        
        card.getChildren().addAll(headerBox, contenu);
        return card;
    }

    @FXML
    private void onBack() {
        Stage stage = (Stage) lblTitre.getScene().getWindow();
        stage.close();
    }

    public void setHomeController(HomeReclamationController c) {
        this.homeController = c;
    }

    @FXML
    private void onEdit() {
        if (currentReclamation == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditReclamation.fxml"));
            Parent root = loader.load();

            EditReclamationController c = loader.getController();
            c.setData(currentReclamation);
            if (homeController != null) {
                c.setHomeController(homeController);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Wait for the edit window to close
            
            // Refresh  UI
            setData(currentReclamation);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDelete() {
        if (currentReclamation == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la réclamation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                reclamationService.delete(currentReclamation.getId());
                if (homeController != null) {
                    homeController.load();
                }
                onBack();
            }
        });
    }
}