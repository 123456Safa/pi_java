package Views;

import Controllers.UserController;
import Models.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Consumer;

public class TwoFactorAuthView {

    private final UserController userController;
    private final Consumer<User> onVerifySuccess;
    private final Runnable onCancel;
    private User pendingUser;

    public TwoFactorAuthView(UserController userController, Consumer<User> onVerifySuccess, Runnable onCancel) {
        this.userController = userController;
        this.onVerifySuccess = onVerifySuccess;
        this.onCancel = onCancel;
    }

    public void setPendingUser(User user) {
        this.pendingUser = user;
    }

    public VBox buildView() {
        VBox root = new VBox(0);
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("single-window-root");

        VBox card = new VBox(24);
        card.getStyleClass().add("premium-card");
        card.setPadding(new Insets(40));
        card.setMaxWidth(450);
        card.setAlignment(Pos.CENTER);

        Label icon = new Label("\uD83D\uDD12");
        icon.setStyle("-fx-font-size: 48;");

        Label title = new Label("Secure Access");
        title.getStyleClass().add("premium-title");

        Label subtitle = new Label("Please enter the 6-digit code from your Authenticator app.");
        subtitle.setStyle("-fx-text-fill: #64748b; -fx-text-alignment: center;");
        subtitle.setWrapText(true);

        TextField codeField = new TextField();
        codeField.setPromptText("000000");
        codeField.getStyleClass().add("modern-input");
        codeField.setAlignment(Pos.CENTER);
        codeField.setStyle("-fx-font-size: 24;");

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);

        Button verifyBtn = new Button("Verify & Continue");
        verifyBtn.getStyleClass().add("btn-primary");
        verifyBtn.setMaxWidth(Double.MAX_VALUE);
        verifyBtn.setOnAction(e -> {
            try {
                if (userController.verify2FALogin(pendingUser, codeField.getText())) {
                    onVerifySuccess.accept(pendingUser);
                }
            } catch (Exception ex) {
                statusLabel.setText(ex.getMessage());
                statusLabel.getStyleClass().setAll("feedback-error");
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-outline");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setOnAction(e -> onCancel.run());

        card.getChildren().addAll(icon, title, subtitle, codeField, verifyBtn, cancelBtn, statusLabel);
        root.getChildren().add(card);

        return root;
    }
}
