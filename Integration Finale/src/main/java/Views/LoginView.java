package Views;

import controllers.UserController;
import services.RecaptchaService;
import utils.RecaptchaWidget;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.sql.SQLDataException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LoginView {

    private final UserController userController;
    private final RecaptchaService recaptchaService;
    private final RecaptchaWidget recaptchaWidget;
    private final Consumer<models.User> onLoginSuccess;
    private final Runnable onShowRegister;
    private final Runnable onShowForgotPassword;

    public LoginView(UserController userController, 
                     RecaptchaService recaptchaService, 
                     RecaptchaWidget recaptchaWidget,
                     Consumer<models.User> onLoginSuccess,
                     Runnable onShowRegister,
                     Runnable onShowForgotPassword) {
        this.userController = userController;
        this.recaptchaService = recaptchaService;
        this.recaptchaWidget = recaptchaWidget;
        this.onLoginSuccess = onLoginSuccess;
        this.onShowRegister = onShowRegister;
        this.onShowForgotPassword = onShowForgotPassword;
    }

    public VBox buildView() {
        VBox root = new VBox(0);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("single-window-root");
        
        // Background Decoration (Glassmorphism inspired)
        StackPane container = new StackPane();
        
        VBox card = new VBox(24);
        card.getStyleClass().add("premium-card");
        card.setPadding(new Insets(40));
        card.setMaxWidth(450);
        card.setAlignment(Pos.CENTER);

        Label title = new Label("Welcome Back");
        title.getStyleClass().add("premium-title");
        
        Label subtitle = new Label("Sign in to your PharmaX account");
        subtitle.getStyleClass().add("text-sub");
        subtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14;");

        VBox form = new VBox(16);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.getStyleClass().add("modern-input");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("modern-input");

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(370);

        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        
        loginBtn.setOnAction(e -> handleLogin(emailField, passwordField, statusLabel, loginBtn));

        HBox linksRow = new HBox(10);
        linksRow.setAlignment(Pos.CENTER);
        
        Hyperlink forgotPwdLink = new Hyperlink("Forgot Password?");
        forgotPwdLink.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-underline: false;");
        forgotPwdLink.setOnAction(e -> onShowForgotPassword.run());
        
        linksRow.getChildren().add(forgotPwdLink);

        Separator separator = new Separator();
        
        Label orLabel = new Label("or continue with");
        orLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13;");

        Button googleBtn = new Button("\uD83D\uDD11  Google Account");
        googleBtn.getStyleClass().add("btn-secondary");
        googleBtn.setMaxWidth(Double.MAX_VALUE);
        googleBtn.setOnAction(e -> handleGoogleLogin(statusLabel, googleBtn));

        HBox footer = new HBox(5);
        footer.setAlignment(Pos.CENTER);
        Label noAccLabel = new Label("Don't have an account?");
        Hyperlink registerLink = new Hyperlink("Create one");
        registerLink.setOnAction(e -> onShowRegister.run());
        footer.getChildren().addAll(noAccLabel, registerLink);

        form.getChildren().addAll(emailField, passwordField, loginBtn, linksRow);
        card.getChildren().addAll(title, subtitle, form, separator, orLabel, googleBtn, footer, statusLabel);
        
        container.getChildren().add(card);
        root.getChildren().add(container);
        
        return root;
    }

    private void handleLogin(TextField emailField, PasswordField passwordField, Label status, Button btn) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            setError(status, "Please fill in all fields.");
            return;
        }

        btn.setDisable(true);
        status.getStyleClass().setAll("feedback-success");
        status.setText("Verifying reCAPTCHA...");

        recaptchaWidget.requestToken("login", token -> {
            CompletableFuture.supplyAsync(() -> recaptchaService.verifyToken(token))
                    .thenAccept(result -> Platform.runLater(() -> {
                        if (result.isSuccess() && result.getScore() >= 0.5) {
                            try {
                                models.User user = userController.login(email, password);
                                onLoginSuccess.accept(user);
                            } catch (SQLDataException ex) {
                                btn.setDisable(false);
                                setError(status, ex.getMessage());
                            } catch (Exception ex) {
                                btn.setDisable(false);
                                setError(status, "Error: " + ex.getMessage());
                            }
                        } else {
                            btn.setDisable(false);
                            setError(status, "Security verification failed.");
                        }
                    })).exceptionally(ex -> {
                        Platform.runLater(() -> {
                            btn.setDisable(false);
                            setError(status, "Verification error.");
                        });
                        return null;
                    });
        }, err -> {
            btn.setDisable(false);
            setError(status, err);
        });
    }

    private void handleGoogleLogin(Label status, Button btn) {
        btn.setDisable(true);
        status.getStyleClass().setAll("feedback-success");
        status.setText("Opening browser...");

        CompletableFuture.supplyAsync(() -> {
            try {
                return userController.loginWithGoogle();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).thenAccept(user -> Platform.runLater(() -> {
            btn.setDisable(false);
            onLoginSuccess.accept(user);
        })).exceptionally(ex -> {
            Platform.runLater(() -> {
                btn.setDisable(false);
                setError(status, "Google login failed: " + ex.getCause().getMessage());
            });
            return null;
        });
    }

    private void setError(Label label, String msg) {
        label.getStyleClass().setAll("feedback-error");
        label.setText(msg);
    }
}
