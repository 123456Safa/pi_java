package controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Produit;
import service.ProduitService;
import service.NotificationService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AppBarController {

    @FXML
    private Button btnNotifications;

    @FXML
    private Label lblNotifBadge;

    private ProduitService produitService = new ProduitService();
    private NotificationService notifService = new NotificationService();

    @FXML
    public void initialize() {
        // Initialiser le badge au démarrage
        mettreAJourBadge();

        // Créer une Timeline pour rafraîchir le badge toutes les 10 secondes (Temps Réel)
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> {
            mettreAJourBadge();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void mettreAJourBadge() {
        try {
            List<Produit> expiring = produitService.getExpiringProducts(7);
            List<Integer> ids = expiring.stream().map(Produit::getId).collect(Collectors.toList());
            
            // Compter uniquement les non lus et non supprimés dans la base de données
            int count = notifService.countUnread(ids);
            
            if (count > 0) {
                lblNotifBadge.setText(String.valueOf(count));
                lblNotifBadge.setVisible(true);
            } else {
                lblNotifBadge.setVisible(false);
            }
        } catch (SQLException e) {
            lblNotifBadge.setVisible(false);
        }
    }

    @FXML
    private void onNotifications() {
        try {
            List<Produit> expiringProducts = produitService.getExpiringProducts(7);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/NotificationDialog.fxml"));
            DialogPane dialogPane = loader.load();

            NotificationController controller = loader.getController();
            controller.setExpiringProducts(expiringProducts);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.NONE); // Permet de cliquer ailleurs sans bloquer
            dialogStage.initStyle(javafx.stage.StageStyle.UNDECORATED); // Supprime la barre de titre
            
            Scene scene = new Scene(dialogPane);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);

            // Positionnement précis (Dropdown)
            javafx.geometry.Bounds bounds = btnNotifications.localToScreen(btnNotifications.getBoundsInLocal());
            dialogStage.setX(bounds.getMinX() - 300); // Décalage vers la gauche pour aligner le bord droit
            dialogStage.setY(bounds.getMaxY() + 5);

            // Fermeture automatique si on clique ailleurs
            dialogStage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    dialogStage.close();
                    mettreAJourBadge();
                }
            });

            dialogStage.show();

        } catch (IOException e) {
            showError("Erreur", "Impossible de charger la fenêtre de notifications: " + e.getMessage());
        } catch (SQLException e) {
            showError("Erreur", "Problème de base de données: " + e.getMessage());
        }
    }

    @FXML
    private void onUser() {
        System.out.println("Clic sur le profil utilisateur");
        // Ajoutez ici la logique pour le profil si nécessaire
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
