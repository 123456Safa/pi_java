package Test;

import Controllers.ChatbotController;
import Controllers.UserController;
import Models.User;
<<<<<<< Updated upstream
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
=======
import Services.FaceAuthService;
import Services.RecaptchaService;
import Views.*;
import utils.RecaptchaWidget;
import javafx.application.Application;
import javafx.application.Platform;
>>>>>>> Stashed changes
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private final UserController userController = new UserController();
<<<<<<< Updated upstream
    private final ObservableList<User> userData = FXCollections.observableArrayList();
=======
    private final RecaptchaService recaptchaService = new RecaptchaService();
    private RecaptchaWidget recaptchaWidget;
    private ChatbotController chatbotController;
    private final FaceAuthService faceAuthService = new FaceAuthService();
>>>>>>> Stashed changes

    private StackPane root;
    private User currentUser;
    
    // View References
    private LoginView loginView;
    private RegisterView registerView;
    private ForgotPasswordView forgotPasswordView;
    private TwoFactorAuthView twoFactorAuthView;

    private enum ViewType { AUTH, APP }
    private ViewType currentViewType = ViewType.AUTH;

    @Override
    public void start(Stage stage) {
        root = new StackPane();
<<<<<<< Updated upstream

        appPane = buildAppPane();
        loginPane = buildLoginPane();
        registerPane = buildRegisterPane();

        root.getChildren().addAll(appPane, loginPane, registerPane);
=======
        recaptchaWidget = new RecaptchaWidget();
        
        // --- Initialize Views ---
        loginView = new LoginView(userController, recaptchaService, recaptchaWidget, 
                                  this::handleLoginSuccess, this::showRegister, this::showForgotPassword);
        
        registerView = new RegisterView(userController, recaptchaService, recaptchaWidget, 
                                        this::handleLoginSuccess, this::showLogin);
        
        forgotPasswordView = new ForgotPasswordView(userController, faceAuthService, this::showLogin);
        
        twoFactorAuthView = new TwoFactorAuthView(userController, this::enterApp, this::showLogin);

        // --- Chatbot Setup ---
        chatbotController = new ChatbotController();
        chatbotController.setUiActionCallback(action -> {
            if ("open_profile".equals(action)) showProfile();
        });
>>>>>>> Stashed changes

        Button chatBubbleButton = chatbotController.buildChatBubbleButton();
        chatBubbleButton.setVisible(false);
        VBox chatPanel = chatbotController.buildChatPanel();

        // --- Layout ---
        root.getChildren().addAll(recaptchaWidget.getWebView(), chatPanel, chatBubbleButton);
        StackPane.setAlignment(recaptchaWidget.getWebView(), Pos.BOTTOM_RIGHT);
        StackPane.setMargin(recaptchaWidget.getWebView(), new Insets(0, 24, 24, 0));
        
        StackPane.setAlignment(chatBubbleButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatBubbleButton, new Insets(0, 24, 24, 0));
        StackPane.setAlignment(chatPanel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatPanel, new Insets(0, 24, 90, 0));

        showLogin();

        Scene scene = new Scene(root, 1280, 850);
        if (getClass().getResource("/styles/user-module.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles/user-module.css").toExternalForm());
        }

        stage.setTitle("PharmaX - Secure Access");
        stage.setScene(scene);
        stage.show();
    }

    private void showLogin() {
        currentViewType = ViewType.AUTH;
        recaptchaWidget.getWebView().setVisible(true);
        setView(loginView.buildView());
    }

    private void showRegister() {
        currentViewType = ViewType.AUTH;
        recaptchaWidget.getWebView().setVisible(true);
        setView(registerView.buildView());
    }

    private void showForgotPassword() {
        recaptchaWidget.getWebView().setVisible(false);
        setView(forgotPasswordView.buildView());
    }

    private void handleLoginSuccess(User user) {
        if (user.getGoogleAuthenticatorSecret() != null && !user.getGoogleAuthenticatorSecret().isEmpty()) {
            recaptchaWidget.getWebView().setVisible(false);
            twoFactorAuthView.setPendingUser(user);
            setView(twoFactorAuthView.buildView());
        } else {
            enterApp(user);
        }
    }

<<<<<<< Updated upstream
        PasswordField loginPassword = new PasswordField();
        loginPassword.setPromptText("Password");

        Label loginStatus = new Label();

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("btn-admin-view");
        loginButton.setOnAction(event -> {
            String validationMessage = validateLoginInputs(loginEmail.getText(), loginPassword.getText());
            if (validationMessage != null) {
                setError(loginStatus, validationMessage);
                return;
            }

            try {
                User loggedUser = userController.login(loginEmail.getText(), loginPassword.getText());
                loginEmail.clear();
                loginPassword.clear();
                enterApp(loggedUser);
            } catch (SQLDataException ex) {
                setError(loginStatus, ex.getMessage());
            } catch (Exception ex) {
                setError(loginStatus, "Database unavailable: " + ex.getMessage());
            }
        });

        Button toRegisterButton = new Button("Not registered? Create account");
        toRegisterButton.getStyleClass().add("btn-back");
        toRegisterButton.setOnAction(event -> showRegisterPane());

        card.getChildren().addAll(loginEmail, loginPassword, loginButton, toRegisterButton, loginStatus);
        screen.getChildren().addAll(title, card);
        return screen;
    }

    private VBox buildRegisterPane() {
        VBox screen = new VBox(14);
        screen.setPadding(new Insets(24));
        screen.setAlignment(Pos.CENTER);
        screen.getStyleClass().add("single-window-root");

        Label title = new Label("Register");
        title.getStyleClass().add("single-window-title");

        VBox card = buildSectionContainer("Create your account");
        card.setMaxWidth(420);

        TextField registerEmail = new TextField();
        registerEmail.setPromptText("Email");

        PasswordField registerPassword = new PasswordField();
        registerPassword.setPromptText("Password (min 6)");

        TextField registerFirstName = new TextField();
        registerFirstName.setPromptText("First Name");

        TextField registerLastName = new TextField();
        registerLastName.setPromptText("Last Name (optional)");

        Label registerStatus = new Label();

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().add("btn-admin-create");
        registerButton.setOnAction(event -> {
            String validationMessage = validateRegisterInputs(
                    registerEmail.getText(),
                    registerPassword.getText(),
                    registerFirstName.getText(),
                    registerLastName.getText()
            );
            if (validationMessage != null) {
                setError(registerStatus, validationMessage);
                return;
            }

            try {
                User createdUser = userController.register(
                        registerEmail.getText(),
                        registerPassword.getText(),
                        registerFirstName.getText(),
                        registerLastName.getText()
                );
                registerEmail.clear();
                registerPassword.clear();
                registerFirstName.clear();
                registerLastName.clear();
                // Auto-login immediately after successful registration
                enterApp(createdUser);
            } catch (SQLDataException ex) {
                setError(registerStatus, ex.getMessage());
            } catch (Exception ex) {
                setError(registerStatus, "Database unavailable: " + ex.getMessage());
            }
        });

        Button toLoginButton = new Button("Already have an account? Back to login");
        toLoginButton.getStyleClass().add("btn-back");
        toLoginButton.setOnAction(event -> showLoginPane());

        card.getChildren().addAll(registerEmail, registerPassword, registerFirstName, registerLastName, registerButton, toLoginButton, registerStatus);
        screen.getChildren().addAll(title, card);
        return screen;
    }

    private BorderPane buildAppPane() {
        BorderPane rootPane = new BorderPane();
        rootPane.getStyleClass().add("single-window-root");

        VBox top = new VBox(10);
        top.setPadding(new Insets(14));
        top.getStyleClass().add("single-window-header");

        Label title = new Label("PharmaX - User Management");
        title.getStyleClass().add("single-window-title");

        currentUserLabel = new Label("Not connected");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("btn-admin-delete");
        logoutButton.setOnAction(event -> logout());

        HBox titleRow = new HBox(10, title, currentUserLabel, logoutButton);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        HBox filterBar = buildFilterBar();
        HBox statsBar = buildStatsBar();

        StackPane content = new StackPane();
        VBox createSection = buildCreateSection();
        VBox readSection = buildReadSection();
        VBox updateSection = buildUpdateSection();
        VBox deleteSection = buildDeleteSection();
        content.getChildren().addAll(createSection, readSection, updateSection, deleteSection);

        Button createButton = buildNavButton("Create", true);
        Button readButton = buildNavButton("Read", false);
        Button updateButton = buildNavButton("Update", false);
        Button deleteButton = buildNavButton("Delete", false);

        createButton.setOnAction(event -> activateSection(content, createSection, createButton, readButton, updateButton, deleteButton));
        readButton.setOnAction(event -> activateSection(content, readSection, readButton, createButton, updateButton, deleteButton));
        updateButton.setOnAction(event -> activateSection(content, updateSection, updateButton, createButton, readButton, deleteButton));
        deleteButton.setOnAction(event -> activateSection(content, deleteSection, deleteButton, createButton, readButton, updateButton));

        HBox navBar = new HBox(8, createButton, readButton, updateButton, deleteButton);
        navBar.setAlignment(Pos.CENTER_LEFT);

        top.getChildren().addAll(titleRow, filterBar, statsBar, navBar);
        rootPane.setTop(top);
        rootPane.setCenter(content);

        activateSection(content, createSection, createButton, readButton, updateButton, deleteButton);
        return rootPane;
    }

    private HBox buildFilterBar() {
        searchField = new TextField();
        searchField.setPromptText("Search email, name, role");
        searchField.setPrefWidth(300);

        sortByCombo = new ComboBox<>();
        sortByCombo.getItems().addAll("ID", "Email", "First Name", "Last Name", "Role");
        sortByCombo.setValue("ID");

        directionCombo = new ComboBox<>();
        directionCombo.getItems().addAll("Ascending", "Descending");
        directionCombo.setValue("Ascending");

        Button apply = new Button("Apply");
        apply.getStyleClass().add("btn-admin-view");
        apply.setOnAction(event -> refreshUsersAndStats(null, null));

        Button reset = new Button("Reset");
        reset.getStyleClass().add("btn-admin-create");
        reset.setOnAction(event -> {
            searchField.clear();
            sortByCombo.setValue("ID");
            directionCombo.setValue("Ascending");
            refreshUsersAndStats(null, null);
        });

        HBox bar = new HBox(10,
                new Label("Search"), searchField,
                new Label("Sort"), sortByCombo,
                new Label("Order"), directionCombo,
                apply, reset
        );
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private HBox buildStatsBar() {
        totalUsersLabel = buildStatValue("0");
        adminUsersLabel = buildStatValue("0");
        normalUsersLabel = buildStatValue("0");

        HBox bar = new HBox(12,
                buildStatCard("Total Users", totalUsersLabel),
                buildStatCard("Admin Users", adminUsersLabel),
                buildStatCard("Normal Users", normalUsersLabel)
        );
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    private VBox buildStatCard(String title, Label valueLabel) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-title");
        VBox card = new VBox(4, titleLabel, valueLabel);
        card.getStyleClass().add("stat-card");
        return card;
    }

    private Label buildStatValue(String value) {
        Label label = new Label(value);
        label.getStyleClass().add("stat-value");
        return label;
    }

    private Button buildNavButton(String text, boolean active) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-btn");
        if (active) {
            button.getStyleClass().add("nav-btn-active");
        }
        return button;
    }

    private VBox buildCreateSection() {
        VBox section = buildSectionContainer("Create User");

        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(UserController.ROLE_ADMIN, UserController.ROLE_NORMAL_USER);
        roleCombo.setValue(UserController.ROLE_NORMAL_USER);

        GridPane form = buildFormGrid();
        addFormRow(form, 0, "Email", emailField);
        addFormRow(form, 1, "Role", roleCombo);
        addFormRow(form, 2, "Password", passwordField);
        addFormRow(form, 3, "First Name", firstNameField);
        addFormRow(form, 4, "Last Name", lastNameField);

        Label status = new Label();
        Button createButton = new Button("Create User");
        createButton.getStyleClass().add("btn-admin-create");
        createButton.setOnAction(event -> {
            String validationMessage = validateUserFormInputs(
                    emailField.getText(),
                    passwordField.getText(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    roleCombo.getValue()
            );
            if (validationMessage != null) {
                setError(status, validationMessage);
                return;
            }

            User user = new User(
                    emailField.getText(),
                    "",
                    passwordField.getText(),
                    firstNameField.getText(),
                    lastNameField.getText()
            );

            try {
                userController.createUser(user, roleCombo.getValue());
                setSuccess(status, "User created successfully.");
                emailField.clear();
                passwordField.clear();
                firstNameField.clear();
                lastNameField.clear();
                roleCombo.setValue(UserController.ROLE_NORMAL_USER);
                refreshUsersAndStats(null, status);
            } catch (SQLDataException ex) {
                setError(status, ex.getMessage());
=======
    private void setView(Node node) {
        Platform.runLater(() -> {
            List<Node> toRemove = new ArrayList<>();
            for (Node child : root.getChildren()) {
                if (child != recaptchaWidget.getWebView() && 
                    !child.getStyleClass().contains("chat-panel") && 
                    !child.getStyleClass().contains("chat-bubble-btn")) {
                    toRemove.add(child);
                }
            }
            root.getChildren().removeAll(toRemove);
            root.getChildren().add(0, node);
            
            // Hide reCAPTCHA if not on auth screens
            if (currentViewType != ViewType.AUTH) {
                recaptchaWidget.getWebView().setVisible(false);
>>>>>>> Stashed changes
            }
        });
    }

    private void enterApp(User user) {
        this.currentUser = user;
        currentViewType = ViewType.APP;
        chatbotController.setLoggedUser(user);
        
        // Show chatbot bubble
        root.getChildren().stream()
            .filter(n -> n.getStyleClass().contains("chat-bubble-btn"))
            .forEach(n -> n.setVisible(true));

        if (user.isAdmin()) {
            showAdminDashboard();
        } else {
            showUserHome();
        }
        recaptchaWidget.getWebView().setVisible(false);
    }

    private void showAdminDashboard() {
        AdminDashboardView dashboard = new AdminDashboardView(userController, currentUser, this::logout, this::showProfile, this::showLogs, this::showUserCreate, this::showUserEdit, this::showUserHome);
        recaptchaWidget.getWebView().setVisible(false);
        setView(dashboard.buildView());
    }

    private void showUserCreate() {
        UserCreateView createView = new UserCreateView(userController, this::showAdminDashboard, this::showAdminDashboard);
        setView(createView.buildView());
    }

    private void showUserEdit(User target) {
        UserEditView editView = new UserEditView(userController, target, this::showAdminDashboard, this::showAdminDashboard);
        setView(editView.buildView());
    }

    private void showUserHome() {
        UserHomeView home = new UserHomeView(currentUser, this::showProfile, this::logout);
        recaptchaWidget.getWebView().setVisible(false);
        setView(home.buildView());
    }

    private void showProfile() {
        Runnable back = currentUser.isAdmin() ? this::showAdminDashboard : this::showUserHome;
        UserProfileView profile = new UserProfileView(userController, faceAuthService, currentUser, back, this::showProfile, this::showLogs, this::logout, this::showUserHome);
        recaptchaWidget.getWebView().setVisible(false);
        setView(profile.buildView());
    }

    private void showLogs() {
        AdminLogsView logs = new AdminLogsView(this::showAdminDashboard, this::showProfile, this::showLogs, this::logout, this::showUserHome);
        recaptchaWidget.getWebView().setVisible(false);
        setView(logs.buildView());
    }

    private void logout() {
        this.currentUser = null;
        chatbotController.setLoggedUser(null);
        
        // Hide chatbot bubble
        root.getChildren().stream()
            .filter(n -> n.getStyleClass().contains("chat-bubble-btn"))
            .forEach(n -> n.setVisible(false));
            
        showLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
