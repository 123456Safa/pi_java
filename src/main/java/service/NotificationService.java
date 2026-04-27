package service;

import model.NotificationState;
import utils.MyDataBase;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationService {
    private Connection cnx;

    public NotificationService() {
        cnx = MyDataBase.getInstance().getCnx();
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
}
