package models;

import com.google.gson.JsonArray;
import java.time.LocalDateTime;

/**
 * Represents a single message in the chatbot conversation history.
 * Used both for building the AI API request (conversation context)
 * and for rendering chat bubbles in the UI.
 */
public class ChatMessage {
    private int id;
    private int userId;

    /** "user", "model", or "tool" — matches OpenAI/OpenRouter API role names */
    private final String role;

    /** The text content of the message */
    private final String text;

    /** When the message was created (for UI display) */
    private final LocalDateTime timestamp;

    /** For tool calls (assistant role) */
    private JsonArray toolCalls;

    /** For tool results (tool role) */
    private String toolCallId;

    public ChatMessage(String role, String text) {
        this.role = role;
        this.text = text;
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(int id, int userId, String role, String text, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.role = role;
        this.text = text;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isUser() {
        return "user".equals(role);
    }

    public boolean isModel() {
        return "model".equals(role) || "assistant".equals(role);
    }

    public JsonArray getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(JsonArray toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getToolCallId() {
        return toolCallId;
    }

    public void setToolCallId(String toolCallId) {
        this.toolCallId = toolCallId;
    }

    @Override
    public String toString() {
        return "[" + role + "] " + text;
    }
}
