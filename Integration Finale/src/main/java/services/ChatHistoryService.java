package services;

import models.ChatMessage;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistent storage of chat logs in the database.
 */
public class ChatHistoryService {

    private final Connection connection;

    public ChatHistoryService() {
        this.connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Saves a message to the chat_message table.
     */
    public void saveMessage(int userId, ChatMessage message) {
        if (userId <= 0) return;

        String sql = "INSERT INTO chat_message (user_id, role, content) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message.getRole());
            ps.setString(3, message.getText());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save chat message to DB: " + e.getMessage());
        }
    }

    /**
     * Loads the conversation history for a specific user from the database.
     */
    public List<ChatMessage> loadHistory(int userId) {
        List<ChatMessage> history = new ArrayList<>();
        if (userId <= 0) return history;

        String sql = "SELECT * FROM chat_message WHERE user_id = ? ORDER BY created_at ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatMessage msg = new ChatMessage(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("role"),
                            rs.getString("content"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    history.add(msg);
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to load chat history from DB: " + e.getMessage());
        }
        return history;
    }
}
