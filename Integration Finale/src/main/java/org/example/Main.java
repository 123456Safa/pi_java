package org.example;

import com.pharmax.ui.SplashScreen;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.DataInitializer;

/**
 * PharmaX — Unified Entry Point
 * Integrates 4 management modules:
 *   1. Gestion Articles/Blog (com.pharmax.*)
 *   2. Gestion Commandes/Orders (controllers.*)
 *   3. Gestion Réclamations (controllers.*)
 *   4. Gestion Produits & Catégories (controllers.*)
 */
public class Main extends Application {

    private static Main instance;

    private Stage primaryStage;
    private StackPane root;

    private controllers.UserController userController;
    private services.RecaptchaService recaptchaService;
    private utils.RecaptchaWidget recaptchaWidget;
    private services.FaceAuthService faceAuthService;
    private controllers.ChatbotController chatbotController;
    private models.User currentUser;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;
        this.primaryStage = primaryStage;
        
        SplashScreen splash = new SplashScreen();
        splash.show();
        splash.updateMessage("Initialisation de PharmaX...");

        new Thread(() -> {
            try {
                splash.updateMessage("Connexion à la base de données...");
                DataInitializer.initializeSampleData();
                splash.updateMessage("Chargement de l'interface...");
                Thread.sleep(600);
            } catch (Exception e) {
                System.err.println("⚠ Data init warning: " + e.getMessage());
            }

            Platform.runLater(() -> {
                splash.close();
                initAuthFlow();
            });
        }, "pharmax-init").start();
    }

    private void initAuthFlow() {
        userController = new controllers.UserController();
        recaptchaService = new services.RecaptchaService();
        recaptchaWidget = new utils.RecaptchaWidget();
        faceAuthService = new services.FaceAuthService();
        chatbotController = new controllers.ChatbotController();
        chatbotController.setUiActionCallback(action -> {
            if ("open_profile".equals(action)) showProfile();
        });

        root = new StackPane();
        
        Button chatBubbleButton = chatbotController.buildChatBubbleButton();
        chatBubbleButton.setVisible(false);
        VBox chatPanel = chatbotController.buildChatPanel();

        root.getChildren().addAll(recaptchaWidget.getWebView(), chatPanel, chatBubbleButton);
        StackPane.setAlignment(recaptchaWidget.getWebView(), javafx.geometry.Pos.BOTTOM_RIGHT);
        StackPane.setMargin(recaptchaWidget.getWebView(), new javafx.geometry.Insets(0, 24, 24, 0));
        
        StackPane.setAlignment(chatBubbleButton, javafx.geometry.Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatBubbleButton, new javafx.geometry.Insets(0, 24, 24, 0));
        StackPane.setAlignment(chatPanel, javafx.geometry.Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatPanel, new javafx.geometry.Insets(0, 24, 90, 0));

        showLogin();

        Scene scene = new Scene(root, 1360, 820);
        if (getClass().getResource("/styles/user-module.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles/user-module.css").toExternalForm());
        }

        primaryStage.setTitle("PharmaX - Secure Access");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void setView(javafx.scene.Node node) {
        Platform.runLater(() -> {
            java.util.List<javafx.scene.Node> toRemove = new java.util.ArrayList<>();
            for (javafx.scene.Node child : root.getChildren()) {
                if (child != recaptchaWidget.getWebView() && 
                    !child.getStyleClass().contains("chat-panel") && 
                    !child.getStyleClass().contains("chat-bubble-btn")) {
                    toRemove.add(child);
                }
            }
            root.getChildren().removeAll(toRemove);
            root.getChildren().add(0, node);
        });
    }

    private void showLogin() {
        recaptchaWidget.getWebView().setVisible(true);
        Views.LoginView loginView = new Views.LoginView(userController, recaptchaService, recaptchaWidget, 
            this::handleLoginSuccess, this::showRegister, this::showForgotPassword);
        setView(loginView.buildView());
    }

    private void showRegister() {
        recaptchaWidget.getWebView().setVisible(true);
        Views.RegisterView registerView = new Views.RegisterView(userController, recaptchaService, recaptchaWidget, 
            this::handleLoginSuccess, this::showLogin);
        setView(registerView.buildView());
    }

    private void showForgotPassword() {
        recaptchaWidget.getWebView().setVisible(false);
        Views.ForgotPasswordView fpView = new Views.ForgotPasswordView(userController, faceAuthService, this::showLogin);
        setView(fpView.buildView());
    }

    private void handleLoginSuccess(models.User user) {
        if (user.getGoogleAuthenticatorSecret() != null && !user.getGoogleAuthenticatorSecret().isEmpty()) {
            recaptchaWidget.getWebView().setVisible(false);
            Views.TwoFactorAuthView twoFactorAuthView = new Views.TwoFactorAuthView(userController, this::enterApp, this::showLogin);
            twoFactorAuthView.setPendingUser(user);
            setView(twoFactorAuthView.buildView());
        } else {
            enterApp(user);
        }
    }

    private void enterApp(models.User user) {
        this.currentUser = user;
        utils.SessionManager.getInstance().setCurrentUser(user);
        chatbotController.setLoggedUser(user);
        
        root.getChildren().stream()
            .filter(n -> n.getStyleClass().contains("chat-bubble-btn"))
            .forEach(n -> n.setVisible(true));

        recaptchaWidget.getWebView().setVisible(false);

        // All users enter through the AppShell (Front Office).
        // Admins are automatically routed to the Back Office afterwards.
        showAppShell();
        if (user.isAdmin()) {
            // Give the AppShell a moment to initialize, then navigate to Back Office
            javafx.application.Platform.runLater(() -> {
                controllers.AppShellController shell = controllers.AppShellController.getInstance();
                if (shell != null) {
                    shell.showBackOffice();
                }
            });
        }
    }

    private void showAppShell() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app-shell.fxml"));
            setView(loader.load());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProfile() {
        // Navigate to profile within the Back Office unified dashboard
        controllers.AppShellController shell = controllers.AppShellController.getInstance();
        if (shell != null) {
            shell.showBackOffice();
        }
    }

    private void showLogs() {
        // Navigate to logs within the Back Office unified dashboard
        controllers.AppShellController shell = controllers.AppShellController.getInstance();
        if (shell != null) {
            shell.showBackOffice();
        }
    }

    private void showUserCreate() {
        // Navigate to user create within the Back Office unified dashboard
        controllers.AppShellController shell = controllers.AppShellController.getInstance();
        if (shell != null) {
            shell.showBackOffice();
        }
    }

    private void showUserEdit(models.User target) {
        // Navigate to user edit within the Back Office unified dashboard
        controllers.AppShellController shell = controllers.AppShellController.getInstance();
        if (shell != null) {
            shell.showBackOffice();
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public void logout() {
        this.currentUser = null;
        utils.SessionManager.getInstance().setCurrentUser(null);
        chatbotController.setLoggedUser(null);
        
        root.getChildren().stream()
            .filter(n -> n.getStyleClass().contains("chat-bubble-btn"))
            .forEach(n -> n.setVisible(false));
            
        showLogin();
    }
}
