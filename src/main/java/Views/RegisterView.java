package Views;

import Controllers.UserController;
import Services.RecaptchaService;
import utils.RecaptchaWidget;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLDataException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RegisterView {

    private final UserController userController;
    private final RecaptchaService recaptchaService;
    private final RecaptchaWidget recaptchaWidget;
    private final Consumer<Models.User> onRegisterSuccess;
    private final Runnable onBackToLogin;

    public RegisterView(UserController userController,
                        RecaptchaService recaptchaService,
                        RecaptchaWidget recaptchaWidget,
                        Consumer<Models.User> onRegisterSuccess,
                        Runnable onBackToLogin) {
        this.userController = userController;
        this.recaptchaService = recaptchaService;
        this.recaptchaWidget = recaptchaWidget;
        this.onRegisterSuccess = onRegisterSuccess;
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

        Label title = new Label("Create Account");
        title.getStyleClass().add("premium-title");

        Label subtitle = new Label("Join the PharmaX community");
        subtitle.setStyle("-fx-text-fill: #64748b;");

        GridPane form = new GridPane();
        form.setVgap(16);
        form.setHgap(16);
        form.setAlignment(Pos.CENTER);

        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.getStyleClass().add("modern-input");
        emailField.setPrefWidth(350);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 6 chars)");
        passwordField.getStyleClass().add("modern-input");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        firstNameField.getStyleClass().add("modern-input");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name (Optional)");
        lastNameField.getStyleClass().add("modern-input");

        form.add(new Label("Email"), 0, 0);
        form.add(emailField, 1, 0);
        
        form.add(new Label("First Name"), 0, 1);
        form.add(firstNameField, 1, 1);
        
        form.add(new Label("Last Name"), 0, 2);
        form.add(lastNameField, 1, 2);
        
        form.add(new Label("Password"), 0, 3);
        form.add(passwordField, 1, 3);

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        Button registerBtn = new Button("Register Now");
        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setOnAction(e -> handleRegister(emailField, passwordField, firstNameField, lastNameField, statusLabel, registerBtn));

        Button backBtn = new Button("← Back to Login");
        backBtn.getStyleClass().add("btn-outline");
        backBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setOnAction(e -> onBackToLogin.run());

        card.getChildren().addAll(title, subtitle, form, registerBtn, backBtn, statusLabel);
        root.getChildren().add(card);

        return root;
    }

    private void handleRegister(TextField email, PasswordField pass, TextField fname, TextField lname, Label status, Button btn) {
        if (email.getText().isEmpty() || pass.getText().isEmpty() || fname.getText().isEmpty()) {
            setError(status, "Please fill in all required fields.");
            return;
        }

        btn.setDisable(true);
        status.getStyleClass().setAll("feedback-success");
        status.setText("Verifying reCAPTCHA...");

        recaptchaWidget.requestToken("register", token -> {
            CompletableFuture.supplyAsync(() -> recaptchaService.verifyToken(token))
                    .thenAccept(result -> Platform.runLater(() -> {
                        if (result.isSuccess() && result.getScore() >= 0.5) {
                            try {
                                Models.User user = userController.register(email.getText(), pass.getText(), fname.getText(), lname.getText());
                                onRegisterSuccess.accept(user);
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

    private void setError(Label label, String msg) {
        label.getStyleClass().setAll("feedback-error");
        label.setText(msg);
    }
}
