package controllers;

import Model.Reclamation;
import Model.Reponse;
import Service.ReclamationService;
import Service.ReponseService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.util.List;

public class DetailReclamationControlleradmin {
    private final ReclamationService service = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();
    
    @FXML private Label titre, description, date, statut, statusActuel, reponseCount;
    @FXML private VBox reponseContainer;
    @FXML private Button btnAjouterReponse, btnModifier, btnSupprimer;
    
    private HomeAdminController homeController;
    private Reclamation r;

    public void setHomeController(HomeAdminController c) {
        this.homeController = c;
    }

    public void refreshReponses() {
        if (r != null) {
            loadReponses(r.getId());
        }
    }

    public void refreshStatus(String newStatus) {
        if (r != null) {
            r.setStatut(newStatus);
            statut.setText(newStatus);
            statusActuel.setText(newStatus);
            
            // Style the status badge
            String statusColor = getStatusColor(newStatus);
            statut.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 5 10; -fx-border-radius: 3;");
        }
    }

    public void setData(Reclamation r) {
        this.r = r;

        titre.setText(r.getTitre());
        description.setText(r.getDescription());
        date.setText("📅 " + r.getDateCreation());
        statut.setText(r.getStatut());
        
        // Style the status badge
        String statusColor = getStatusColor(r.getStatut());
        statut.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 5 10; -fx-border-radius: 3;");
        
        statusActuel.setText(r.getStatut());
        
        loadReponses(r.getId());
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

    private void loadReponses(int reclamationId) {
        reponseContainer.getChildren().clear();
        
        List<Reponse> list = reponseService.getByReclamationId(reclamationId);
        
        if (list == null || list.isEmpty()) {
            Label empty = new Label("Aucune réponse pour cette réclamation");
            empty.setStyle("-fx-font-size: 12; -fx-text-fill: #999999; -fx-padding: 20;");
            reponseContainer.getChildren().add(empty);
            reponseCount.setText("(0)");
            return;
        }
        
        reponseCount.setText("(" + list.size() + ")");
        
        for (Reponse rep : list) {
            VBox reponseCard = createReponseCard(rep);
            reponseContainer.getChildren().add(reponseCard);
        }
    }

    private VBox createReponseCard(Reponse rep) {
        VBox card = new VBox(8);
        card.setStyle("-fx-border-color: transparent transparent transparent #28a745; -fx-border-width: 0 0 0 4; -fx-background-color: #f9f9f9; -fx-padding: 15;");
        
        // Header with date
        HBox headerBox = new HBox(10);
        headerBox.setStyle("-fx-alignment: center-left;");
        
        Label dateLabel = new Label("📅 " + rep.getDateReponse());
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999999;");
        
        HBox.setHgrow(headerBox, Priority.ALWAYS);
        headerBox.getChildren().add(dateLabel);
        
        // Content
        Label contenu = new Label(rep.getContenu());
        contenu.setWrapText(true);
        contenu.setStyle("-fx-font-size: 12; -fx-text-fill: #555555;");
        
        // Action buttons
        HBox buttonBox = new HBox(8);
        buttonBox.setStyle("-fx-alignment: center-left;");
        
        Button editBtn = new Button("✏️ Éditer");
        editBtn.setStyle("-fx-font-size: 11; -fx-padding: 6 12; -fx-background-color: #FFA500; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");
        editBtn.setOnAction(e -> openEditReponse(rep));
        
        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-font-size: 11; -fx-padding: 6 12; -fx-background-color: #FF4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteReponse(rep));
        
        buttonBox.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(headerBox, contenu, buttonBox);
        return card;
    }

    private void openEditReponse(Reponse rep) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editreponse.fxml"));
            Parent root = loader.load();

            EditReponseController c = loader.getController();
            c.setReponse(rep, r.getTitre());
            c.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteReponse(Reponse rep) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la réponse");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                reponseService.delete(rep.getId());
                refreshReponses();
            }
        });
    }

    @FXML
    public void repondre() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reponseAdmin.fxml"));
            Parent root = loader.load();

            ReponseFormController c = loader.getController();
            c.setReclamation(r);
            c.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void modifier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modstatus.fxml"));
            Parent root = loader.load();

            ModifierStatusController c = loader.getController();
            c.setData(r);
            c.setHomeController(homeController);
            c.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void supprimer() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la réclamation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                service.delete(r.getId());
                if (homeController != null) {
                    homeController.refresh();
                }
                onBack();
            }
        });
    }

    @FXML
    public void onBack() {
        Stage stage = (Stage) titre.getScene().getWindow();
        stage.close();
    }
}