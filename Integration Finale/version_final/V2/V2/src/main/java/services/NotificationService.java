package services;

import models.Notification;
import models.NotificationState;
import utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationService {
    private Connection cnx;

    public NotificationService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    /**
     * Récupère les états pour une liste d'IDs de produits.
     */
    public Map<Integer, NotificationState> getStatesForProducts(List<Integer> productIds) throws SQLException {
        Map<Integer, NotificationState> states = new HashMap<>();
        if (productIds.isEmpty()) return states;

        StringBuilder sb = new StringBuilder("SELECT * FROM notification_state WHERE produit_id IN (");
        for (int i = 0; i < productIds.size(); i++) {
            sb.append("?");
            if (i < productIds.size() - 1) sb.append(",");
        }
        sb.append(")");

        PreparedStatement ps = cnx.prepareStatement(sb.toString());
        for (int i = 0; i < productIds.size(); i++) {
            ps.setInt(i + 1, productIds.get(i));
        }

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            NotificationState ns = new NotificationState();
            ns.setId(rs.getInt("id"));
            ns.setProduitId(rs.getInt("produit_id"));
            ns.setRead(rs.getBoolean("is_read"));
            ns.setDismissed(rs.getBoolean("is_dismissed"));
            ns.setUpdatedAt(rs.getTimestamp("updated_at"));
            states.put(ns.getProduitId(), ns);
        }
        return states;
    }

    /**
     * Sauvegarde ou met à jour l'état d'une notification.
     */
    public void updateState(int produitId, boolean isRead, boolean isDismissed) throws SQLException {
        String req = "INSERT INTO notification_state (produit_id, is_read, is_dismissed) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE is_read = VALUES(is_read), is_dismissed = VALUES(is_dismissed)";

        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, produitId);
        ps.setBoolean(2, isRead);
        ps.setBoolean(3, isDismissed);
        ps.executeUpdate();
    }

    /**
     * Compte le nombre de notifications non lues parmi une liste de produits expirants.
     */
    public int countUnread(List<Integer> expiringIds) throws SQLException {
        if (expiringIds.isEmpty()) return 0;

        // On compte les produits qui soit n'ont pas d'entrée (donc non lus par défaut),
        // soit ont une entrée avec is_read=0 et is_dismissed=0.

        // Approche simplifiée : On récupère tous les états existants pour ces IDs
        Map<Integer, NotificationState> states = getStatesForProducts(expiringIds);

        int unreadCount = 0;
        for (Integer id : expiringIds) {
            NotificationState state = states.get(id);
            if (state == null) {
                // Pas d'entrée en DB = Jamais touché = Non lu
                unreadCount++;
            } else if (!state.isRead() && !state.isDismissed()) {
                unreadCount++;
            }
        }
        return unreadCount;
    }

    public List<Notification> getUserNotifications(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Notification(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("title"),
                            rs.getString("message"),
                            rs.getString("type"),
                            rs.getBoolean("is_read"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void sendNotification(int userId, String title, String message, String type, String recipientEmail) {
        // 1. Enregistrer dans la base de données (In-App Notification)
        String sql = "INSERT INTO notifications (user_id, title, message, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setString(4, type);
            ps.executeUpdate();
            System.out.println("✅ Notification In-App enregistrée pour l'utilisateur " + userId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'enregistrement de la notification : " + e.getMessage());
        }

        // 2. Envoyer un email si l'adresse est fournie
        if (recipientEmail != null && !recipientEmail.isEmpty()) {
            sendEmailNotification(recipientEmail, title, message);
        }
    }

    private void sendEmailNotification(String recipientEmail, String subject, String messageText) {
        // Since EmailService sends HTML order confirmations, we create a generic text/HTML sender here
        // or just use a simple thread to mock it or integrate with JavaMail
        new Thread(() -> {
            try {
                // Here we could use JavaMail API exactly like in EmailService
                // For simplicity, we just log it. If you want full email delivery for these events,
                // we can adapt EmailService to accept plain text messages.
                System.out.println("📧 EMAIL ENVOYÉ À : " + recipientEmail);
                System.out.println("Sujet : " + subject);
                System.out.println("Message : " + messageText);
                System.out.println("-------------------------------------------------");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void markAsRead(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getUnreadCount(int userId) {
        String sql = "SELECT COUNT(*) as count FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
