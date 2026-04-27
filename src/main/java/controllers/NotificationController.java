package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;
import model.Produit;
import model.NotificationState;
import service.NotificationService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NotificationController {

    @FXML private ListView<NotificationItem> lvProduits;
    @FXML private ToggleButton btnFilterTout;
    @FXML private ToggleButton btnFilterNonLu;
    @FXML private ToggleGroup filterGroup;
    @FXML private Label lblMessage;

    private NotificationService notifService = new NotificationService();
    private ObservableList<NotificationItem> allItems = FXCollections.observableArrayList();

    public static class NotificationItem {
        public Produit produit;
        public NotificationState state;
        public String message;
        public String timeText;

        public NotificationItem(Produit p, NotificationState s, String msg, String time) {
            this.produit = p;
            this.state = s;
            this.message = msg;
            this.timeText = time;
        }
    }

    @FXML
    public void initialize() {
        lvProduits.setCellFactory(param -> new NotificationCell());

        filterGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                oldToggle.setSelected(true);
                return;
            }
            applyFilter();
        });
    }

    public void setExpiringProducts(List<Produit> produits) {
        allItems.clear();
        LocalDate today = LocalDate.now();

        try {
            // Récupérer les IDs des produits pour chercher leurs états en DB
            List<Integer> ids = produits.stream().map(Produit::getId).collect(Collectors.toList());
            Map<Integer, NotificationState> dbStates = notifService.getStatesForProducts(ids);

            for (Produit p : produits) {
                if (p.getDateExpiration() != null) {
                    // Récupérer l'état depuis la DB ou créer un état par défaut (Non lu)
                    NotificationState state = dbStates.getOrDefault(p.getId(), new NotificationState(p.getId(), false, false));

                    if (!state.isDismissed()) {
                        LocalDate expirationDate = ((java.sql.Date) p.getDateExpiration()).toLocalDate();
                        long daysRemaining = ChronoUnit.DAYS.between(today, expirationDate);
                        
                        String msg = "Le produit " + p.getNom() + " expire bientôt (" + p.getQuantite() + " en stock).";
                        
                        String timeTxt;
                        if (daysRemaining < 0) timeTxt = "Expiré il y a " + Math.abs(daysRemaining) + " j";
                        else if (daysRemaining == 0) timeTxt = "Aujourd'hui";
                        else if (daysRemaining == 1) timeTxt = "Demain";
                        else timeTxt = "Dans " + daysRemaining + " j";

                        allItems.add(new NotificationItem(p, state, msg, timeTxt));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        if (allItems.isEmpty()) {
            lblMessage.setText("Plus tôt");
        } else {
            lblMessage.setText("Aujourd'hui");
        }

        applyFilter();
    }

    private void applyFilter() {
        if (btnFilterNonLu.isSelected()) {
            lvProduits.setItems(FXCollections.observableArrayList(
                allItems.stream().filter(item -> !item.state.isRead()).collect(Collectors.toList())
            ));
        } else {
            lvProduits.setItems(allItems);
        }
    }

    private class NotificationCell extends ListCell<NotificationItem> {
        private HBox root = new HBox(15);
        private VBox textContainer = new VBox(4);
        private StackPane avatarPane = new StackPane();
        private Label lblMessageCell = new Label();
        private Label lblTime = new Label();
        private Circle unreadDot = new Circle(4);
        private MenuButton menuBtn = new MenuButton("...");

        public NotificationCell() {
            super();
            avatarPane.setMinWidth(50);
            avatarPane.setMinHeight(50);
            avatarPane.setMaxWidth(50);
            avatarPane.setMaxHeight(50);
            
            lblMessageCell.setWrapText(true);
            lblMessageCell.setMaxWidth(220);
            lblTime.getStyleClass().add("coral-notif-time");
            
            textContainer.getChildren().addAll(lblMessageCell, lblTime);
            textContainer.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(textContainer, Priority.ALWAYS);

            unreadDot.getStyleClass().add("coral-unread-dot");

            MenuItem markRead = new MenuItem("Marquer comme lu");
            MenuItem markUnread = new MenuItem("Marquer comme non lu");
            MenuItem delete = new MenuItem("Supprimer cette notification");

            markRead.setOnAction(e -> {
                try {
                    getItem().state.setRead(true);
                    notifService.updateState(getItem().produit.getId(), true, getItem().state.isDismissed());
                    updateItem(getItem(), false);
                    if (btnFilterNonLu.isSelected()) applyFilter();
                } catch (SQLException ex) { ex.printStackTrace(); }
            });

            markUnread.setOnAction(e -> {
                try {
                    getItem().state.setRead(false);
                    notifService.updateState(getItem().produit.getId(), false, getItem().state.isDismissed());
                    updateItem(getItem(), false);
                    applyFilter();
                } catch (SQLException ex) { ex.printStackTrace(); }
            });

            delete.setOnAction(e -> {
                try {
                    getItem().state.setDismissed(true);
                    notifService.updateState(getItem().produit.getId(), getItem().state.isRead(), true);
                    allItems.remove(getItem());
                    applyFilter();
                } catch (SQLException ex) { ex.printStackTrace(); }
            });

            menuBtn.getItems().addAll(markRead, markUnread, delete);
            menuBtn.getStyleClass().add("coral-menu-btn");
            // Hide the arrow button entirely in CSS
            
            root.getStyleClass().add("coral-item-root");
            root.setAlignment(Pos.CENTER_LEFT);
            root.getChildren().addAll(avatarPane, textContainer, unreadDot, menuBtn);
        }

        @Override
        protected void updateItem(NotificationItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                lblMessageCell.setText(item.message);
                lblTime.setText(item.timeText);

                avatarPane.getChildren().clear();
                boolean imageLoaded = false;
                if (item.produit.getImage() != null && !item.produit.getImage().isEmpty()) {
                    try {
                        java.io.File file = new java.io.File(item.produit.getImage());
                        if (file.exists()) {
                            javafx.scene.image.Image img = new javafx.scene.image.Image(file.toURI().toString(), 50, 50, false, true);
                            javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                            // Masque circulaire comme Facebook
                            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(25, 25, 25);
                            imgView.setClip(clip);
                            
                            // Bordure blanche subtile
                            javafx.scene.shape.Circle border = new javafx.scene.shape.Circle(25, 25, 26);
                            border.setFill(javafx.scene.paint.Color.TRANSPARENT);
                            border.setStroke(javafx.scene.paint.Color.web("#f1f2f6"));
                            border.setStrokeWidth(1);
                            
                            avatarPane.getChildren().addAll(imgView, border);
                            imageLoaded = true;
                        }
                    } catch (Exception ex) {}
                }
                
                if (!imageLoaded) {
                    Label fallbackLabel = new Label("📦");
                    fallbackLabel.setStyle("-fx-font-size: 24px; -fx-background-color: #3a3b3c; -fx-background-radius: 50%; -fx-min-width: 50; -fx-min-height: 50; -fx-alignment: center;");
                    avatarPane.getChildren().add(fallbackLabel);
                }

                if (item.state.isRead()) {
                    lblMessageCell.getStyleClass().setAll("coral-notif-text");
                    lblMessageCell.setStyle("-fx-font-weight: normal;");
                    unreadDot.setOpacity(0); // Hide but keep space
                } else {
                    lblMessageCell.getStyleClass().setAll("coral-notif-text", "coral-notif-text-bold");
                    unreadDot.setOpacity(1);
                    unreadDot.getStyleClass().setAll("coral-unread-dot");
                }
                setGraphic(root);
            }
        }
    }
}
