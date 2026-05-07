package Views;

import controllers.UserController;
import models.User;
import models.ChatMessage;
import services.ChatHistoryService;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class UserProfileView {

    private static final String UPLOADS_DIR = "uploads/avatars";

    private final UserController userController;
    private final services.FaceAuthService faceAuthService;
    private final User currentUser;
    private final Runnable onShowUsers;
    private final Runnable onUserUpdated;
    private final Runnable onLogout;
    private final Runnable onShowHome;
    private final Runnable onShowLogs;
    private final ChatHistoryService historyService;

    private ImageView avatarImageView;
    private Label statusLabel;

    public UserProfileView(UserController userController, services.FaceAuthService faceAuthService, User currentUser,
            Runnable onShowUsers, Runnable onShowProfile, Runnable onShowLogs, Runnable onLogout, Runnable onShowHome) {
        this.userController = userController;
        this.faceAuthService = faceAuthService;
        this.currentUser = currentUser;
        this.onShowUsers = onShowUsers;
        this.onUserUpdated = onShowProfile;
        this.onShowLogs = onShowLogs;
        this.onLogout = onLogout;
        this.onShowHome = onShowHome;
        this.historyService = new ChatHistoryService();
    }

    public Region buildView() {
        // --- Main Content Area (no sidebar — unified dashboard provides it) ---
        VBox contentArea = new VBox(0);
        HBox header = buildHeaderBar();

        VBox content = new VBox(30);
        content.setPadding(new Insets(40, 60, 40, 60));
        content.setAlignment(Pos.TOP_CENTER);

        VBox profileSummary = buildProfileSummary();

        HBox detailsGrid = new HBox(30);
        detailsGrid.setAlignment(Pos.TOP_CENTER);

        VBox leftCol = new VBox(20);
        leftCol.setPrefWidth(500);
        leftCol.getChildren().addAll(buildGeneralInfoCard(), buildSecuritySection());

        VBox rightCol = new VBox(20);
        rightCol.setPrefWidth(500);
        rightCol.getChildren().addAll(buildMetadataCard(), buildChatHistorySection());

        detailsGrid.getChildren().addAll(leftCol, rightCol);

        content.getChildren().addAll(profileSummary, detailsGrid);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        contentArea.getChildren().addAll(header, scroll);

        return contentArea;
    }

    private HBox buildHeaderBar() {
        HBox bar = new HBox(20);
        bar.getStyleClass().add("premium-header");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(15, 30, 15, 30));

        Button backBtn = new Button("\u2190 Back");
        backBtn.getStyleClass().add("btn-back-header");
        backBtn.setOnAction(e -> onShowUsers.run());

        Label title = new Label("Profile Settings");
        title.getStyleClass().add("premium-title");
        HBox.setHgrow(title, Priority.ALWAYS);
        title.setMaxWidth(Double.MAX_VALUE);

        Button logoutBtn = new Button("\uD83D\uDEAA Sign Out");
        logoutBtn.getStyleClass().add("btn-outline-sm");
        logoutBtn.setStyle("-fx-border-color: #ef4444; -fx-text-fill: #ef4444;");
        logoutBtn.setOnAction(e -> onLogout.run());

        bar.getChildren().addAll(backBtn, title, logoutBtn);
        return bar;
    }

    private VBox buildProfileSummary() {
        VBox card = new VBox(15);
        card.getStyleClass().add("premium-card");
        card.setPadding(new Insets(30));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(1030);

        avatarImageView = new ImageView();
        avatarImageView.setFitWidth(100);
        avatarImageView.setFitHeight(100);
        Circle clip = new Circle(50, 50, 50);
        avatarImageView.setClip(clip);
        loadAvatar();

        Button changeBtn = new Button("\uD83D\uDCF7 Change Photo");
        changeBtn.getStyleClass().add("btn-outline");
        changeBtn.setOnAction(e -> handleChangePhoto());

        Label name = new Label(currentUser.getFirstName() + " "
                + (currentUser.getLastName() != null ? currentUser.getLastName() : ""));
        name.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        Label badge = new Label(userController.toRoleLabel(currentUser.getRoles()).toUpperCase());
        badge.getStyleClass().addAll("badge",
                UserController.ROLE_ADMIN.equalsIgnoreCase(userController.toRoleLabel(currentUser.getRoles()))
                        ? "badge-admin"
                        : "badge-user");

        card.getChildren().addAll(avatarImageView, changeBtn, name, badge);
        return card;
    }

    private VBox buildGeneralInfoCard() {
        VBox card = new VBox(20);
        card.getStyleClass().add("premium-card");
        card.setPadding(new Insets(25));

        Label title = new Label("General Information");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setVgap(15);
        form.setHgap(15);

        TextField email = new TextField(currentUser.getEmail());
        email.getStyleClass().add("modern-input");

        TextField fname = new TextField(currentUser.getFirstName());
        fname.getStyleClass().add("modern-input");

        TextField lname = new TextField(currentUser.getLastName() != null ? currentUser.getLastName() : "");
        lname.getStyleClass().add("modern-input");

        TextField phone = new TextField(currentUser.getPhoneNumber() != null ? currentUser.getPhoneNumber() : "");
        phone.getStyleClass().add("modern-input");

        PasswordField pwd = new PasswordField();
        pwd.setPromptText("Enter new password to change");
        pwd.getStyleClass().add("modern-input");

        form.add(createLabeledField("Email Address", email), 0, 0);
        form.add(createLabeledField("First Name", fname), 0, 1);
        form.add(createLabeledField("Last Name", lname), 1, 1);
        form.add(createLabeledField("Phone Number", phone), 0, 2);
        form.add(createLabeledField("Account Password", pwd), 1, 2);

        Button updateBtn = new Button("Save Changes");
        updateBtn.getStyleClass().add("btn-primary");
        updateBtn.setOnAction(
                e -> handleUpdate(email.getText(), fname.getText(), lname.getText(), phone.getText(), pwd.getText()));

        statusLabel = new Label();

        card.getChildren().addAll(title, form, updateBtn, statusLabel);
        return card;
    }

    private VBox createLabeledField(String labelText, Control field) {
        VBox group = new VBox(6);
        group.getStyleClass().add("input-group");
        Label label = new Label(labelText.toUpperCase());
        label.getStyleClass().add("modern-label");
        group.getChildren().addAll(label, field);
        return group;
    }

    private VBox buildSecuritySection() {
        VBox card = new VBox(20);
        card.getStyleClass().add("premium-card");
        card.setPadding(new Insets(25));

        Label title = new Label("Security & Privacy");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        boolean tfaEnabled = currentUser.getGoogleAuthenticatorSecret() != null
                && !currentUser.getGoogleAuthenticatorSecret().isEmpty();
        HBox tfaRow = buildSecurityToggle("Two-Factor Auth (TOTP)", tfaEnabled, this::handle2FASetup);

        HBox faceRow = buildSecurityToggle("Face Recognition (Biometrics)", currentUser.isFaceAuthEnabled(),
                this::handleFaceIDSetup);

        card.getChildren().addAll(title, tfaRow, faceRow);
        return card;
    }

    private HBox buildSecurityToggle(String label, boolean enabled, Runnable onAction) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8;");

        Label l = new Label(label);
        l.getStyleClass().add("security-card-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(enabled ? "ENABLED" : "DISABLED");
        status.setStyle("-fx-text-fill: " + (enabled ? "#059669" : "#dc2626") + "; -fx-font-weight: bold;");

        Button btn = new Button(enabled ? "Disable" : "Setup");
        btn.getStyleClass().add("btn-outline");
        btn.setOnAction(e -> onAction.run());

        row.getChildren().addAll(l, spacer, status, btn);
        return row;
    }

    private void handle2FASetup() {
        if (currentUser.getGoogleAuthenticatorSecret() != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Disable 2FA?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait().ifPresent(type -> {
                if (type == ButtonType.YES) {
                    try {
                        userController.disable2FA(currentUser);
                        onUserUpdated.run();
                    } catch (Exception ex) {
                        setError(ex.getMessage());
                    }
                }
            });
        } else {
            try {
                Image qr = userController.begin2FASetup(currentUser);
                ImageView iv = new ImageView(qr);
                iv.setFitWidth(200);
                iv.setFitHeight(200);

                VBox qrCard = new VBox(iv);
                qrCard.getStyleClass().add("qr-card");
                qrCard.setAlignment(Pos.CENTER);

                VBox content = new VBox(20);
                content.getStyleClass().add("modern-dialog-root");
                content.setAlignment(Pos.CENTER);
                content.setPrefWidth(400);

                Label dTitle = new Label("Secure Your Account");
                dTitle.getStyleClass().add("dialog-header-title");

                Label dSubtitle = new Label("Scan this code with Google Authenticator to enable 2FA protection.");
                dSubtitle.getStyleClass().add("dialog-subtitle");
                dSubtitle.setWrapText(true);
                dSubtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

                TextField codeF = new TextField();
                codeF.setPromptText("Enter 6-digit code");
                codeF.getStyleClass().add("modern-input");
                codeF.setAlignment(Pos.CENTER);
                codeF.setStyle("-fx-font-size: 18; -fx-letter-spacing: 2;");

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("PharmaX 2FA Setup");

                DialogPane dp = dialog.getDialogPane();
                dp.setContent(content);
                dp.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                dp.getStylesheets().add(getClass().getResource("/styles/user-module.css").toExternalForm());

                content.getChildren().addAll(dTitle, dSubtitle, qrCard, codeF);

                dialog.showAndWait().ifPresent(type -> {
                    if (type == ButtonType.OK) {
                        try {
                            userController.confirm2FASetup(currentUser, codeF.getText());
                            onUserUpdated.run();
                        } catch (Exception ex) {
                            setError(ex.getMessage());
                        }
                    }
                });
            } catch (Exception ex) {
                setError(ex.getMessage());
            }
        }
    }

    private void handleFaceIDSetup() {
        if (currentUser.isFaceAuthEnabled()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Disable Face Recognition?", ButtonType.YES,
                    ButtonType.NO);
            confirm.showAndWait().ifPresent(type -> {
                if (type == ButtonType.YES) {
                    try {
                        currentUser.setFaceAuthEnabled(false);
                        userController.updateUser(currentUser, userController.toRoleLabel(currentUser.getRoles()));
                        onUserUpdated.run();
                    } catch (Exception ex) {
                        setError(ex.getMessage());
                    }
                }
            });
        } else {
            VBox content = new VBox(20);
            content.getStyleClass().add("modern-dialog-root");
            content.setAlignment(Pos.CENTER);
            content.setPrefWidth(400);

            Label dTitle = new Label("Face Recognition Setup");
            dTitle.getStyleClass().add("dialog-header-title");

            Label dSubtitle = new Label(
                    "Secure your account with biometric authentication. Ensure your camera is visible and well-lit.");
            dSubtitle.getStyleClass().add("dialog-subtitle");
            dSubtitle.setWrapText(true);
            dSubtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            VBox iconBox = new VBox(new Label("\uD83D\uDCF1")); // Phone/Face icon emoji
            iconBox.setAlignment(Pos.CENTER);
            iconBox.setStyle("-fx-font-size: 50; -fx-padding: 20;");
            iconBox.getStyleClass().add("qr-card");

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("PharmaX Face ID");

            DialogPane dp = dialog.getDialogPane();
            dp.setContent(content);
            dp.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dp.getStylesheets().add(getClass().getResource("/styles/user-module.css").toExternalForm());

            content.getChildren().addAll(dTitle, dSubtitle, iconBox, new Label("Click OK to begin enrollment."));

            dialog.showAndWait().ifPresent(type -> {
                if (type == ButtonType.OK) {
                    Alert loadingAlert = new Alert(Alert.AlertType.NONE, "Initializing camera... Please stay still.",
                            ButtonType.CANCEL);
                    loadingAlert.setTitle("Face ID Enrollment");
                    loadingAlert.show();

                    faceAuthService.enrollFaceAsync(currentUser.getEmail())
                            .thenAccept(embedding -> javafx.application.Platform.runLater(() -> {
                                loadingAlert.close();
                                if (embedding != null) {
                                    try {
                                        currentUser.setFaceAuthEnabled(true);
                                        currentUser.setFaceEncoding(embedding);
                                        userController.updateUser(currentUser,
                                                userController.toRoleLabel(currentUser.getRoles()));
                                        onUserUpdated.run();
                                        new Alert(Alert.AlertType.INFORMATION, "Biometric enrollment successful!")
                                                .show();
                                    } catch (Exception ex) {
                                        setError("Failed to update user: " + ex.getMessage());
                                    }
                                } else {
                                    new Alert(Alert.AlertType.ERROR, "Biometric enrollment failed. Please try again.")
                                            .show();
                                }
                            }));
                }
            });
        }
    }

    private VBox buildMetadataCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("premium-card");
        card.setPadding(new Insets(25));

        Label title = new Label("Account Insights");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        VBox stats = new VBox(10);
        stats.getChildren().addAll(
                createMetadataRow("Account Status", currentUser.getStatus(), "#10b981"),
                createMetadataRow("Member Since",
                        currentUser.getCreatedAt() != null ? currentUser.getCreatedAt().toString().substring(0, 10)
                                : "N/A",
                        "#64748b"),
                createMetadataRow("Last Updated",
                        currentUser.getUpdatedAt() != null ? currentUser.getUpdatedAt().toString().substring(0, 10)
                                : "N/A",
                        "#64748b"));

        card.getChildren().addAll(title, stats);
        return card;
    }

    private HBox createMetadataRow(String label, String value, String color) {
        HBox row = new HBox(10);
        Label l = new Label(label + ":");
        l.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
        Label v = new Label(value);
        v.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        row.getChildren().addAll(l, v);
        return row;
    }

    private VBox buildChatHistorySection() {
        VBox card = new VBox(15);
        card.getStyleClass().add("premium-card");
        card.setPadding(new Insets(25));
        VBox.setVgrow(card, Priority.ALWAYS);

        Label title = new Label("Recent Activity (Chat)");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        VBox container = new VBox(10);
        List<ChatMessage> history = historyService.loadHistory(currentUser.getId());

        if (history.isEmpty()) {
            Label placeholder = new Label("No recent conversations found.");
            placeholder.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
            container.getChildren().add(placeholder);
        } else {
            for (ChatMessage msg : history) {
                Label bubble = new Label(msg.getText());
                bubble.setWrapText(true);
                bubble.setMaxWidth(400);
                bubble.getStyleClass()
                        .add(msg.getRole().equalsIgnoreCase("user") ? "chat-bubble-user" : "chat-bubble-ai");
                bubble.setPadding(new Insets(8, 12, 8, 12));

                HBox row = new HBox(bubble);
                row.setAlignment(msg.getRole().equalsIgnoreCase("user") ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                container.getChildren().add(row);
            }
        }

        ScrollPane s = new ScrollPane(container);
        s.setFitToWidth(true);
        s.setPrefHeight(250);
        s.setStyle("-fx-background-color: transparent;");

        card.getChildren().addAll(title, s);
        return card;
    }

    private void loadAvatar() {
        String avatarPath = currentUser.getAvatar();
        if (avatarPath != null && !avatarPath.trim().isEmpty()) {
            try {
                File avatarFile = new File(avatarPath);
                if (avatarFile.exists()) {
                    avatarImageView.setImage(new Image(avatarFile.toURI().toString()));
                    return;
                }
            } catch (Exception ignored) {
            }
        }
        avatarImageView.setImage(generateDefaultAvatar());
    }

    private Image generateDefaultAvatar() {
        int size = 100;
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(size, size);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#10b981"));
        gc.fillOval(0, 0, size, size);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", FontWeight.BOLD, 40));
        String initials = (currentUser.getFirstName() != null && !currentUser.getFirstName().isEmpty())
                ? (currentUser.getFirstName().charAt(0) + "").toUpperCase()
                : "U";
        gc.fillText(initials, 35, 65);
        return canvas.snapshot(null, null);
    }

    private void handleUpdate(String email, String firstName, String lastName, String phone, String password) {
        User updated = new User(currentUser.getId(), email, currentUser.getRoles(),
                (password == null || password.isEmpty()) ? currentUser.getPassword() : password,
                firstName, lastName);
        updated.setAvatar(currentUser.getAvatar());
        updated.setPhoneNumber(phone);
        try {
            userController.updateUser(updated, userController.toRoleLabel(currentUser.getRoles()));
            currentUser.setEmail(email);
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            currentUser.setPhoneNumber(phone);
            if (password != null && !password.isEmpty())
                currentUser.setPassword(password);

            setSuccess("Profile updated successfully.");
            if (onUserUpdated != null)
                onUserUpdated.run();
        } catch (Exception ex) {
            setError(ex.getMessage());
        }
    }

    private void setSuccess(String message) {
        statusLabel.getStyleClass().setAll("feedback-success");
        statusLabel.setText(message);
    }

    private void setError(String message) {
        statusLabel.getStyleClass().setAll("feedback-error");
        statusLabel.setText(message);
    }

    public void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(avatarImageView.getScene().getWindow());
        if (file != null) {
            try {
                Path dest = Paths.get(UPLOADS_DIR,
                        "user_" + currentUser.getId() + "_" + System.currentTimeMillis()
                                + (file.getName().lastIndexOf('.') >= 0
                                        ? file.getName().substring(file.getName().lastIndexOf('.'))
                                        : ".png"));
                Files.createDirectories(dest.getParent());
                Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                currentUser.setAvatar(dest.toString().replace("\\", "/"));
                userController.updateUser(currentUser, userController.toRoleLabel(currentUser.getRoles()));
                loadAvatar();
                if (onUserUpdated != null)
                    onUserUpdated.run();
            } catch (Exception ex) {
                setError("Upload failed: " + ex.getMessage());
            }
        }
    }
}
