package controllers;

import models.ChatMessage;
import models.User;
import services.ChatHistoryService;
import services.FunctionCallDispatcher;
import services.OpenRouterChatService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Controls the chatbot UI panel: manages the chat bubble button,
 * the expandable chat window, message display, and communication
 * with the OpenRouterChatService.
 */
public class ChatbotController {

    private final List<ChatMessage> conversationHistory = new ArrayList<>();
    private final FunctionCallDispatcher dispatcher;
    private final OpenRouterChatService aiService;
    private final ChatHistoryService historyService;

    // UI Components
    private Button chatBubbleButton;
    private VBox chatPanel;
    private VBox messagesContainer;
    private ScrollPane messagesScrollPane;
    private TextArea inputField;
    private Label typingIndicator;
    private boolean chatPanelVisible = false;

    /** Callback to refresh the main app's user table/stats after CRUD */
    private Runnable onDataChanged;
    /** Callback to perform UI actions requested by AI */
    private Consumer<String> uiActionCallback;

    private User currentUser;

    public ChatbotController() {
        this.dispatcher = new FunctionCallDispatcher();
        this.aiService = new OpenRouterChatService(dispatcher);
        this.historyService = new ChatHistoryService();
    }

    /**
     * Sets the callback that fires when the chatbot performs a CRUD operation.
     * The main app should use this to refresh the user table and statistics.
     */
    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
        this.dispatcher.setOnDataChanged(() -> Platform.runLater(() -> {
            if (this.onDataChanged != null) {
                this.onDataChanged.run();
            }
        }));
    }

    public void setUiActionCallback(Consumer<String> callback) {
        this.uiActionCallback = callback;
        this.dispatcher.setUiActionCallback(action -> Platform.runLater(() -> {
            if (this.uiActionCallback != null) {
                this.uiActionCallback.accept(action);
            }
        }));
    }

    public void setLoggedUser(User user) {
        this.currentUser = user;
    }

    /**
     * Builds and returns the floating chat bubble button.
     */
    public Button buildChatBubbleButton() {
        chatBubbleButton = new Button("\uD83D\uDDE8"); // 🗨 speech bubble
        chatBubbleButton.getStyleClass().add("chat-bubble-btn");
        chatBubbleButton.setOnAction(e -> toggleChatPanel());
        return chatBubbleButton;
    }

    /**
     * Builds and returns the full chat panel (header + messages + input bar).
     */
    public VBox buildChatPanel() {
        chatPanel = new VBox();
        chatPanel.getStyleClass().add("chat-panel");
        chatPanel.setVisible(false);
        chatPanel.setManaged(false);
        chatPanel.setPrefWidth(380);
        chatPanel.setPrefHeight(520);
        chatPanel.setMaxWidth(380);
        chatPanel.setMaxHeight(520);

        // --- Header ---
        Label headerTitle = new Label("\uD83E\uDD16  PharmaX Assistant");
        headerTitle.getStyleClass().add("chat-header-title");
        HBox.setHgrow(headerTitle, Priority.ALWAYS);
        headerTitle.setMaxWidth(Double.MAX_VALUE);

        Button closeButton = new Button("✕");
        closeButton.getStyleClass().add("chat-close-btn");
        closeButton.setOnAction(e -> toggleChatPanel());

        HBox header = new HBox(8, headerTitle, closeButton);
        header.getStyleClass().add("chat-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));

        // --- Messages Area ---
        messagesContainer = new VBox(8);
        messagesContainer.setPadding(new Insets(12));
        messagesContainer.getStyleClass().add("chat-messages-container");

        messagesScrollPane = new ScrollPane(messagesContainer);
        messagesScrollPane.getStyleClass().add("chat-scroll-pane");
        messagesScrollPane.setFitToWidth(true);
        messagesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        messagesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(messagesScrollPane, Priority.ALWAYS);

        // Typing indicator
        typingIndicator = new Label("⏳ Thinking...");
        typingIndicator.getStyleClass().add("chat-typing-indicator");
        typingIndicator.setVisible(false);
        typingIndicator.setManaged(false);

        // --- Input Bar ---
        inputField = new TextArea();
        inputField.setPromptText("Type a message...");
        inputField.getStyleClass().add("chat-input-field");
        inputField.setPrefHeight(40);
        inputField.setMinHeight(40);
        inputField.setMaxHeight(40);
        inputField.setWrapText(false); // Enable horizontal scrolling
        HBox.setHgrow(inputField, Priority.ALWAYS);

        // Enter key sends message, Shift+Enter (if we wanted it) or just regular typing
        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER && !event.isShiftDown()) {
                sendMessage();
                event.consume();
            }
        });

        // Add horizontal mouse-wheel scrolling support
        inputField.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                double newScroll = inputField.getScrollLeft() - event.getDeltaY();
                inputField.setScrollLeft(newScroll);
                event.consume();
            }
        });

        Button sendButton = new Button("➤");
        sendButton.getStyleClass().add("chat-send-btn");
        sendButton.setOnAction(e -> sendMessage());

        HBox inputBar = new HBox(8, inputField, sendButton);
        inputBar.getStyleClass().add("chat-input-bar");
        inputBar.setAlignment(Pos.CENTER_LEFT);
        inputBar.setPadding(new Insets(12, 16, 12, 16));

        chatPanel.getChildren().addAll(header, messagesScrollPane, typingIndicator, inputBar);

        return chatPanel;
    }

    /**
     * Shows or hides the chat panel. Adds a welcome message on first open.
     */
    public void toggleChatPanel() {
        chatPanelVisible = !chatPanelVisible;
        chatPanel.setVisible(chatPanelVisible);
        chatPanel.setManaged(chatPanelVisible);

        if (chatPanelVisible && conversationHistory.isEmpty()) {
            // 1. Load persistent history from file
            if (currentUser != null) {
                List<ChatMessage> savedHistory = historyService.loadHistory(currentUser.getId());
                if (!savedHistory.isEmpty()) {
                    conversationHistory.addAll(savedHistory);
                    for (ChatMessage msg : savedHistory) {
                        addMessageToUI(msg.getRole(), msg.getText(), false); // false = don't re-save
                    }
                }
            }

            // 2. Add welcome message if still empty
            if (conversationHistory.isEmpty()) {
                String welcome = "Hi" +
                        (currentUser != null ? " " + currentUser.getFirstName() : "") +
                        "! \uD83D\uDC4B I'm PharmaX Assistant.\n\n" +
                        "I can help you with:\n" +
                        "• Creating, updating, or deleting users\n" +
                        "• Searching and listing users\n" +
                        "• Viewing user statistics\n" +
                        "• Answering general questions\n\n" +
                        "Just tell me what you need!";
                addMessageToUI("model", welcome, true);
                conversationHistory.add(new ChatMessage("model", welcome));
            }
        }

        if (chatPanelVisible) {
            Platform.runLater(() -> inputField.requestFocus());
        }
    }

    /**
     * Clears the conversation history and message UI (e.g. on logout).
     */
    public void clearHistory() {
        conversationHistory.clear();
        if (messagesContainer != null) {
            messagesContainer.getChildren().clear();
        }
        chatPanelVisible = false;
        if (chatPanel != null) {
            chatPanel.setVisible(false);
            chatPanel.setManaged(false);
        }
    }

    /**
     * Returns true if the AI API key is properly configured.
     */
    public boolean isConfigured() {
        return aiService.isConfigured();
    }

    // -------------------------------------------------------------------------
    //  Message handling
    // -------------------------------------------------------------------------

    private void sendMessage() {
        String text = inputField.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        String userText = text.trim();
        inputField.clear();

        // Add user message to UI and history
        addMessageToUI("user", userText, true);
        ChatMessage userMsg = new ChatMessage("user", userText);
        conversationHistory.add(userMsg);

        // Persistent Logging
        if (currentUser != null) {
            historyService.saveMessage(currentUser.getId(), userMsg);
        }

        // Show typing indicator
        showTypingIndicator();

        // Disable input while processing
        inputField.setDisable(true);

        // Send to AI on a background thread
        CompletableFuture.supplyAsync(() -> {
            try {
                return aiService.sendMessage(conversationHistory);
            } catch (Exception e) {
                return "❌ Error: " + e.getMessage();
            }
        }).thenAccept(response -> Platform.runLater(() -> {
            hideTypingIndicator();
            inputField.setDisable(false);
            inputField.requestFocus();

            // Add AI response to UI and history
            addMessageToUI("model", response, true);
            ChatMessage modelMsg = new ChatMessage("model", response);
            conversationHistory.add(modelMsg);

            // Persistent Logging
            if (currentUser != null) {
                historyService.saveMessage(currentUser.getId(), modelMsg);
            }
        }));
    }

    private void addMessageToUI(String role, String text, boolean saveToHistory) {
        TextArea messageArea = new TextArea(text);
        messageArea.setWrapText(true);
        messageArea.setEditable(false);
        messageArea.setFocusTraversable(false); // Doesn't steal Tab focus, but remains selectable
        messageArea.setMaxWidth(280);
        messageArea.setMinHeight(0);

        // Auto-size the TextArea to fit its content (no internal scrollbar)
        autoSizeTextArea(messageArea, 280);

        // Right-click context menu with Copy
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setOnAction(e -> {
            String selected = messageArea.getSelectedText();
            if (selected == null || selected.isEmpty()) {
                selected = messageArea.getText();
            }
            ClipboardContent content = new ClipboardContent();
            content.putString(selected);
            Clipboard.getSystemClipboard().setContent(content);
        });
        MenuItem copyAllItem = new MenuItem("Copy All");
        copyAllItem.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(messageArea.getText());
            Clipboard.getSystemClipboard().setContent(content);
        });
        contextMenu.getItems().addAll(copyItem, copyAllItem);
        messageArea.setContextMenu(contextMenu);

        HBox messageRow = new HBox();
        messageRow.setPadding(new Insets(2, 0, 2, 0));

        if ("user".equals(role)) {
            messageArea.getStyleClass().add("chat-message-user");
            messageRow.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageArea.getStyleClass().add("chat-message-ai");
            messageRow.setAlignment(Pos.CENTER_LEFT);
        }

        messageRow.getChildren().add(messageArea);
        messagesContainer.getChildren().add(messageRow);

        // Auto-scroll to bottom
        Platform.runLater(() -> {
            messagesScrollPane.setVvalue(1.0);
            Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
        });
    }

    /**
     * Calculates the needed height for a TextArea based on its text content
     * and sets prefHeight so it displays without internal scrollbars.
     */
    private void autoSizeTextArea(TextArea textArea, double maxWidth) {
        // Use a helper Text node to measure the rendered height
        Platform.runLater(() -> {
            Text helper = new Text(textArea.getText());
            helper.setFont(textArea.getFont());
            // Account for padding inside the TextArea (approx 28px for paddings)
            helper.setWrappingWidth(maxWidth - 36);
            double textHeight = helper.getLayoutBounds().getHeight() + 28;
            textArea.setPrefHeight(Math.max(40, textHeight));
            textArea.setMaxHeight(Math.max(40, textHeight));
        });
    }

    private void showTypingIndicator() {
        typingIndicator.setVisible(true);
        typingIndicator.setManaged(true);
    }

    private void hideTypingIndicator() {
        typingIndicator.setVisible(false);
        typingIndicator.setManaged(false);
    }
}
