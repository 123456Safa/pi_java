package Views;

import Controllers.UserController;
import Services.FaceAuthService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.concurrent.CompletableFuture;

public class ForgotPasswordView {

    private final UserController userController;
    private final FaceAuthService faceAuthService;
    private final Runnable onBackToLogin;

    private String resetEmailStorage;

    public ForgotPasswordView(UserController userController, FaceAuthService faceAuthService, Runnable onBackToLogin) {
        this.userController = userController;
        this.faceAuthService = faceAuthService;
        this.onBackToLogin = onBackToLogin;
    }

    public VBox buildView() {
        VBox root = new VBox(0);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("single-window-root");

        VBox card = new VBox(24);
        card.getStyleClass().add("premium-card");
        card.setPadding(new Insets(40));
        card.setMaxWidth(500);
        card.setAlignment(Pos.CENTER);

        Label title = new Label("Reset Password");
        title.getStyleClass().add("premium-title");

        Label subtitle = new Label("Follow the steps to recover your account");
        subtitle.setStyle("-fx-text-fill: #64748b;");

        StackPane stepContainer = new StackPane();
        
        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setAlignment(Pos.CENTER);

        // --- Step 1: Email Input ---
        VBox emailStep = new VBox(16);
        emailStep.setAlignment(Pos.CENTER);
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your account email");
        emailField.getStyleClass().add("modern-input");
        Button sendCodeBtn = new Button("Verify Account");
        sendCodeBtn.getStyleClass().add("btn-primary");
        sendCodeBtn.setMaxWidth(Double.MAX_VALUE);
        emailStep.getChildren().addAll(emailField, sendCodeBtn);

        // --- Step 1.5: Face Verification ---
        VBox faceStep = new VBox(16);
        faceStep.setAlignment(Pos.CENTER);
        Label faceInstr = new Label("Biometric Verification Required");
        faceInstr.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
        Button startScanBtn = new Button("\uD83D\uDCF7 Start Face Scan");
        startScanBtn.getStyleClass().add("btn-secondary");
        faceStep.getChildren().addAll(faceInstr, startScanBtn);
        faceStep.setVisible(false);

        // --- Step 2: Code Verification ---
        VBox codeStep = new VBox(16);
        codeStep.setAlignment(Pos.CENTER);
        TextField codeField = new TextField();
        codeField.setPromptText("Enter 6-digit code");
        codeField.getStyleClass().add("modern-input");
        Button verifyCodeBtn = new Button("Verify Code");
        verifyCodeBtn.getStyleClass().add("btn-primary");
        codeStep.getChildren().addAll(codeField, verifyCodeBtn);
        codeStep.setVisible(false);

        // --- Step 3: Password Reset ---
        VBox passwordStep = new VBox(16);
        passwordStep.setAlignment(Pos.CENTER);
        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("New Password");
        newPassField.getStyleClass().add("modern-input");
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm New Password");
        confirmPassField.getStyleClass().add("modern-input");
        Button resetBtn = new Button("Update Password");
        resetBtn.getStyleClass().add("btn-primary");
        passwordStep.getChildren().addAll(newPassField, confirmPassField, resetBtn);
        passwordStep.setVisible(false);

        stepContainer.getChildren().addAll(emailStep, faceStep, codeStep, passwordStep);

        Button backBtn = new Button("← Back to Login");
        backBtn.getStyleClass().add("btn-outline");
        backBtn.setOnAction(e -> onBackToLogin.run());

        // Logic
        sendCodeBtn.setOnAction(e -> {
            String email = emailField.getText();
            if (email == null || email.trim().isEmpty()) return;
            resetEmailStorage = email.trim().toLowerCase();
            sendCodeBtn.setDisable(true);
            statusLabel.setText("Checking account...");
            
            CompletableFuture.supplyAsync(() -> {
                try { 
                    return new Services.ServiceUser().findByEmail(email); 
                } catch (Exception ex) { 
                    return null; 
                }
            }).thenAccept(user -> Platform.runLater(() -> {
                sendCodeBtn.setDisable(false);
                if (user == null) {
                    setError(statusLabel, "No account found.");
                } else if (user.isFaceAuthEnabled()) {
                    transition(emailStep, faceStep);
                    setSuccess(statusLabel, "Face ID verification required.");
                } else {
                    triggerSendCode(email, statusLabel, emailStep, codeStep);
                }
            }));
        });

        startScanBtn.setOnAction(e -> {
            startScanBtn.setDisable(true);
            statusLabel.setText("Initializing camera... Please look at the screen.");
            faceAuthService.verifyFaceDetailedAsync(resetEmailStorage).thenAccept(result -> Platform.runLater(() -> {
                startScanBtn.setDisable(false);
                if (result != null && result.isMatch()) {
                    triggerSendCode(resetEmailStorage, statusLabel, faceStep, codeStep);
                } else {
                    String message = result != null ? result.getMessage() : null;
                    if (message == null || message.trim().isEmpty()) {
                        message = "Identity verification failed. Please try again.";
                    }
                    setError(statusLabel, message);
                }
            }));
        });

        verifyCodeBtn.setOnAction(e -> {
            try {
                userController.verifyResetCode(resetEmailStorage, codeField.getText());
                transition(codeStep, passwordStep);
                setSuccess(statusLabel, "Identity verified.");
            } catch (Exception ex) {
                setError(statusLabel, "Invalid code.");
            }
        });

        resetBtn.setOnAction(e -> {
            if (!newPassField.getText().equals(confirmPassField.getText())) {
                setError(statusLabel, "Passwords don't match.");
                return;
            }
            try {
                userController.resetPassword(resetEmailStorage, newPassField.getText());
                setSuccess(statusLabel, "Password reset! You can now login.");
                transition(passwordStep, new VBox(new Label("Success!"))); // dummy
                onBackToLogin.run();
            } catch (Exception ex) {
                setError(statusLabel, "Failed to reset password.");
            }
        });

        card.getChildren().addAll(title, subtitle, stepContainer, backBtn, statusLabel);
        root.getChildren().add(card);

        return root;
    }

    private void triggerSendCode(String email, Label status, VBox from, VBox to) {
        status.setText("Sending reset code to your email...");
        CompletableFuture.supplyAsync(() -> {
            try { 
                userController.requestPasswordReset(email);
                return true;
            } catch (Exception e) { 
                return e.getMessage();
            }
        }).thenAccept(result -> Platform.runLater(() -> {
            if (result instanceof Boolean && (Boolean)result) {
                setSuccess(status, "A reset code has been sent to your email.");
                transition(from, to);
            } else {
                setError(status, "Failed to send email: " + result);
            }
        }));
    }

    private void transition(VBox from, VBox to) {
        from.setVisible(false);
        from.setManaged(false);
        to.setVisible(true);
        to.setManaged(true);
    }

    private void setError(Label label, String msg) {
        label.getStyleClass().setAll("feedback-error");
        label.setText(msg);
    }

    private void setSuccess(Label label, String msg) {
        label.getStyleClass().setAll("feedback-success");
        label.setText(msg);
    }
}
