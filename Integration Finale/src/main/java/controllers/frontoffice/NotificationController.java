package controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import models.Notification;
import services.NotificationService;

import java.text.SimpleDateFormat;
import java.util.List;

public class NotificationController {

    @FXML private VBox notificationsContainer;
    @FXML private Label unreadLabel;

    private final NotificationService notificationService = new NotificationService();
    // Default user ID (same as used elsewhere in the app)
    private static final int CURRENT_USER_ID = 1;

    @FXML
    public void initialize() {
        loadNotifications();
    }

    private void loadNotifications() {
        notificationsContainer.getChildren().clear();

        // Load in-app notifications from the database
        List<Notification> notifications = notificationService.getUserNotifications(CURRENT_USER_ID);

        // Update unread badge
        int unreadCount = notificationService.getUnreadCount(CURRENT_USER_ID);
        if (unreadLabel != null) {
            unreadLabel.setText(unreadCount + " non lue(s)");
        }

        if (notifications.isEmpty()) {
            Label emptyLabel = new Label("🔔 Aucune notification pour le moment.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #999; -fx-padding: 40;");
            notificationsContainer.getChildren().add(emptyLabel);
            return;
        }

        // Mark all as read when viewing
        notificationService.markAsRead(CURRENT_USER_ID);

        for (Notification notif : notifications) {
            notificationsContainer.getChildren().add(createNotificationCard(notif));
        }
    }

    private HBox createNotificationCard(Notification notif) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(
                "-fx-background-color: " + (notif.isRead() ? "#ffffff" : "#f0f4ff") + ";" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: #e8ecf1;" +
                "-fx-border-radius: 12;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 2);"
        );

        // Icon based on notification type
        String icon = getIconForType(notif.getType());
        Label iconLabel = new Label(icon);
        iconLabel.setStyle(
                "-fx-font-size: 24px;" +
                "-fx-min-width: 48; -fx-min-height: 48;" +
                "-fx-max-width: 48; -fx-max-height: 48;" +
                "-fx-alignment: center;" +
                "-fx-background-color: " + getColorForType(notif.getType()) + ";" +
                "-fx-background-radius: 50%;"
        );

        // Unread indicator dot
        if (!notif.isRead()) {
            Circle dot = new Circle(5);
            dot.setStyle("-fx-fill: #5856d6;");
        }

        // Text content
        VBox textBox = new VBox(4);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label titleLabel = new Label(notif.getTitle());
        titleLabel.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: " + (notif.isRead() ? "normal" : "bold") + ";" +
                "-fx-text-fill: #2d3436;"
        );
        titleLabel.setWrapText(true);

        Label messageLabel = new Label(notif.getMessage());
        messageLabel.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-text-fill: #636e72;"
        );
        messageLabel.setWrapText(true);

        Label timeLabel = new Label(formatTime(notif.getCreatedAt()));
        timeLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #b2bec3;"
        );

        textBox.getChildren().addAll(titleLabel, messageLabel, timeLabel);

        card.getChildren().addAll(iconLabel, textBox);

        return card;
    }

    private String getIconForType(String type) {
        if (type == null) return "🔔";
        return switch (type) {
            case "ORDER_CONFIRMED", "ORDER_VALIDATED" -> "✅";
            case "ORDER_SHIPPED", "ORDER_EXPEDIEE" -> "🚚";
            case "ORDER_DELIVERED", "ORDER_LIVREE" -> "📦";
            case "ORDER_CANCELLED", "ORDER_ANNULEE" -> "❌";
            case "ORDER_STATUS" -> "📋";
            default -> "🔔";
        };
    }

    private String getColorForType(String type) {
        if (type == null) return "#f0f0f0";
        return switch (type) {
            case "ORDER_CONFIRMED", "ORDER_VALIDATED" -> "#e8f5e9";
            case "ORDER_SHIPPED", "ORDER_EXPEDIEE" -> "#e3f2fd";
            case "ORDER_DELIVERED", "ORDER_LIVREE" -> "#f3e5f5";
            case "ORDER_CANCELLED", "ORDER_ANNULEE" -> "#ffebee";
            case "ORDER_STATUS" -> "#fff3e0";
            default -> "#f5f5f5";
        };
    }

    private String formatTime(java.sql.Timestamp ts) {
        if (ts == null) return "";
        long diff = System.currentTimeMillis() - ts.getTime();
        long minutes = diff / (1000 * 60);
        long hours = diff / (1000 * 60 * 60);
        long days = diff / (1000 * 60 * 60 * 24);

        if (minutes < 1) return "À l'instant";
        if (minutes < 60) return "Il y a " + minutes + " min";
        if (hours < 24) return "Il y a " + hours + "h";
        if (days < 7) return "Il y a " + days + " jour(s)";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ts);
    }
}
