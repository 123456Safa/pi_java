package Test;

import Controllers.UserController;
import Models.User;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private final UserController userController = new UserController();
    private final ObservableList<User> userData = FXCollections.observableArrayList();

    private StackPane root;
    private VBox loginPane;
    private VBox registerPane;
    private BorderPane appPane;

    private User currentUser;
    private Label currentUserLabel;

    private TextField searchField;
    private ComboBox<String> sortByCombo;
    private ComboBox<String> directionCombo;

    private Label totalUsersLabel;
    private Label adminUsersLabel;
    private Label normalUsersLabel;

    @Override
    public void start(Stage stage) {
        root = new StackPane();

        appPane = buildAppPane();
        loginPane = buildLoginPane();
        registerPane = buildRegisterPane();

        root.getChildren().addAll(appPane, loginPane, registerPane);

        showLoginPane();

        Scene scene = new Scene(root, 1220, 820);
        if (getClass().getResource("/styles/user-module.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles/user-module.css").toExternalForm());
        }

        stage.setTitle("User App - Login Required");
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildLoginPane() {
        VBox screen = new VBox(14);
        screen.setPadding(new Insets(24));
        screen.setAlignment(Pos.CENTER);
        screen.getStyleClass().add("single-window-root");

        Label title = new Label("Login");
        title.getStyleClass().add("single-window-title");

        VBox card = buildSectionContainer("Connect to continue");
        card.setMaxWidth(420);

        TextField loginEmail = new TextField();
        loginEmail.setPromptText("Email");

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
            }
        });

        section.getChildren().addAll(form, createButton, status);
        return section;
    }

    private VBox buildReadSection() {
        VBox section = buildSectionContainer("Read Users");
        TableView<User> table = buildUserTable();
        Label status = new Label();

        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("btn-admin-view");
        refreshButton.setOnAction(event -> refreshUsersAndStats("Users loaded.", status));

        VBox.setVgrow(table, Priority.ALWAYS);
        section.getChildren().addAll(table, refreshButton, status);
        return section;
    }

    private VBox buildUpdateSection() {
        VBox section = buildSectionContainer("Update User");

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

        TableView<User> table = buildUserTable();
        User[] selectedHolder = new User[1];

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            selectedHolder[0] = selected;
            if (selected == null) {
                return;
            }

            emailField.setText(selected.getEmail());
            passwordField.setText(selected.getPassword());
            firstNameField.setText(selected.getFirstName());
            lastNameField.setText(selected.getLastName() == null ? "" : selected.getLastName());
            roleCombo.setValue(userController.toRoleLabel(selected.getRoles()));
        });

        Label status = new Label();

        Button useSelected = new Button("Use Selected");
        useSelected.getStyleClass().add("btn-admin-view");
        useSelected.setOnAction(event -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                setError(status, "Select a row first.");
                return;
            }
            selectedHolder[0] = selected;
            emailField.setText(selected.getEmail());
            passwordField.setText(selected.getPassword());
            firstNameField.setText(selected.getFirstName());
            lastNameField.setText(selected.getLastName() == null ? "" : selected.getLastName());
            roleCombo.setValue(userController.toRoleLabel(selected.getRoles()));
            setSuccess(status, "Form filled from selected row.");
        });

        Button updateButton = new Button("Update User");
        updateButton.getStyleClass().add("btn-admin-edit");
        updateButton.setOnAction(event -> {
            User selected = selectedHolder[0];
            if (selected == null || selected.getId() <= 0) {
                setError(status, "Select a row to update.");
                return;
            }

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
                    selected.getId(),
                    emailField.getText(),
                    "",
                    passwordField.getText(),
                    firstNameField.getText(),
                    lastNameField.getText()
            );

            try {
                userController.updateUser(user, roleCombo.getValue());
                setSuccess(status, "User updated successfully.");
                refreshUsersAndStats(null, status);
            } catch (SQLDataException ex) {
                setError(status, ex.getMessage());
            }
        });

        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("btn-admin-view");
        refreshButton.setOnAction(event -> refreshUsersAndStats("Users loaded.", status));

        HBox actions = new HBox(10, useSelected, updateButton, refreshButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(table, Priority.ALWAYS);
        section.getChildren().addAll(form, actions, table, status);
        return section;
    }

    private VBox buildDeleteSection() {
        VBox section = buildSectionContainer("Delete User");

        TableView<User> table = buildUserTable();
        Label status = new Label();

        Button deleteButton = new Button("Delete Selected User");
        deleteButton.getStyleClass().add("btn-admin-delete");
        deleteButton.setOnAction(event -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null || selected.getId() <= 0) {
                setError(status, "Select a row to delete.");
                return;
            }

            try {
                userController.deleteUserById(selected.getId());
                setSuccess(status, "User deleted successfully.");
                refreshUsersAndStats(null, status);
            } catch (SQLDataException ex) {
                setError(status, ex.getMessage());
            }
        });

        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("btn-admin-view");
        refreshButton.setOnAction(event -> refreshUsersAndStats("Users loaded.", status));

        HBox actions = new HBox(10, deleteButton, refreshButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(table, Priority.ALWAYS);
        section.getChildren().addAll(table, actions, status);
        return section;
    }

    private VBox buildSectionContainer(String title) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        VBox container = new VBox(12, titleLabel);
        container.setPadding(new Insets(14));
        container.getStyleClass().addAll("user-card", "admin-form");
        return container;
    }

    private GridPane buildFormGrid() {
        GridPane gridPane = new GridPane();
        gridPane.setVgap(8);
        gridPane.setHgap(12);
        return gridPane;
    }

    private void addFormRow(GridPane gridPane, int rowIndex, String labelText, Node field) {
        Label label = new Label(labelText + " :");
        label.setMinWidth(120);
        gridPane.add(label, 0, rowIndex);

        if (field instanceof TextField) {
            ((TextField) field).setPrefWidth(380);
        }
        if (field instanceof ComboBox) {
            ((ComboBox<?>) field).setPrefWidth(380);
        }

        gridPane.add(field, 1, rowIndex);
    }

    private TableView<User> buildUserTable() {
        TableView<User> table = new TableView<>();
        table.setItems(userData);

        TableColumn<User, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));

        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));

        TableColumn<User, String> roleColumn = new TableColumn<>("Role");
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(userController.toRoleLabel(cellData.getValue().getRoles())));

        TableColumn<User, String> passwordColumn = new TableColumn<>("Password");
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));

        TableColumn<User, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));

        TableColumn<User, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));

        table.getColumns().addAll(idColumn, emailColumn, roleColumn, passwordColumn, firstNameColumn, lastNameColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(430);
        return table;
    }

    private void activateSection(StackPane stackPane, Node active, Button activeButton, Button... inactiveButtons) {
        for (Node section : stackPane.getChildren()) {
            boolean visible = section == active;
            section.setVisible(visible);
            section.setManaged(visible);
        }

        activeButton.getStyleClass().remove("nav-btn-active");
        activeButton.getStyleClass().add("nav-btn-active");
        for (Button button : inactiveButtons) {
            button.getStyleClass().remove("nav-btn-active");
        }
    }

    private void refreshUsersAndStats(String successMessage, Label statusLabel) {
        try {
            boolean ascending = "Ascending".equalsIgnoreCase(directionCombo.getValue());
            userData.setAll(userController.searchAndSortUsers(searchField.getText(), sortByCombo.getValue(), ascending));

            int[] stats = userController.getStatistics();
            totalUsersLabel.setText(String.valueOf(stats[0]));
            adminUsersLabel.setText(String.valueOf(stats[1]));
            normalUsersLabel.setText(String.valueOf(stats[2]));

            if (statusLabel != null && successMessage != null) {
                setSuccess(statusLabel, successMessage);
            }
        } catch (SQLDataException ex) {
            if (statusLabel != null) {
                setError(statusLabel, ex.getMessage());
            }
        } catch (Exception ex) {
            if (statusLabel != null) {
                setError(statusLabel, "Database unavailable: " + ex.getMessage());
            }
        }
    }

    private void enterApp(User user) {
        currentUser = user;
        currentUserLabel.setText("Connected as: " + user.getEmail());

        loginPane.setVisible(false);
        loginPane.setManaged(false);
        registerPane.setVisible(false);
        registerPane.setManaged(false);

        appPane.setVisible(true);
        appPane.setManaged(true);

        refreshUsersAndStats(null, null);
    }

    private void showLoginPane() {
        currentUser = null;
        appPane.setVisible(false);
        appPane.setManaged(false);
        registerPane.setVisible(false);
        registerPane.setManaged(false);
        loginPane.setVisible(true);
        loginPane.setManaged(true);
    }

    private void showRegisterPane() {
        appPane.setVisible(false);
        appPane.setManaged(false);
        loginPane.setVisible(false);
        loginPane.setManaged(false);
        registerPane.setVisible(true);
        registerPane.setManaged(true);
    }

    private void logout() {
        currentUser = null;
        currentUserLabel.setText("Not connected");
        showLoginPane();
    }

    private void setSuccess(Label label, String message) {
        label.getStyleClass().removeAll("feedback-error", "feedback-success");
        label.getStyleClass().add("feedback-success");
        label.setText(message);
    }

    private void setError(Label label, String message) {
        label.getStyleClass().removeAll("feedback-error", "feedback-success");
        label.getStyleClass().add("feedback-error");
        label.setText(message);
    }

    private String validateLoginInputs(String email, String password) {
        List<String> errors = new ArrayList<>();

        if (email == null || email.trim().isEmpty()) {
            errors.add("Email: required.");
        } else if (!userController.isValidEmail(email)) {
            errors.add("Email: invalid format (example: user@example.com).");
        }

        if (password == null || password.isEmpty()) {
            errors.add("Password: required.");
        }

        return buildValidationMessage(errors);
    }

    private String validateRegisterInputs(String email, String password, String firstName, String lastName) {
        List<String> errors = new ArrayList<>();

        if (email == null || email.trim().isEmpty()) {
            errors.add("Email: required.");
        } else if (!userController.isValidEmail(email)) {
            errors.add("Email: invalid format (example: user@example.com).");
        }

        if (password == null || password.isEmpty()) {
            errors.add("Password: required.");
        } else if (!userController.isStrongPassword(password)) {
            errors.add("Password: must contain at least 6 characters.");
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            errors.add("First name: required.");
        } else if (!userController.isValidName(firstName)) {
            errors.add("First name: letters/spaces/hyphen/apostrophe only (1-50 chars).");
        }

        if (lastName != null && !lastName.trim().isEmpty() && !userController.isValidName(lastName)) {
            errors.add("Last name: letters/spaces/hyphen/apostrophe only (1-50 chars), or leave empty.");
        }

        return buildValidationMessage(errors);
    }

    private String validateUserFormInputs(String email, String password, String firstName, String lastName, String role) {
        List<String> errors = new ArrayList<>();

        if (email == null || email.trim().isEmpty()) {
            errors.add("Email: required.");
        } else if (!userController.isValidEmail(email)) {
            errors.add("Email: invalid format (example: user@example.com).");
        }

        if (password == null || password.isEmpty()) {
            errors.add("Password: required.");
        } else if (!userController.isStrongPassword(password)) {
            errors.add("Password: must contain at least 6 characters.");
        }

        if (firstName == null || firstName.trim().isEmpty()) {
            errors.add("First name: required.");
        } else if (!userController.isValidName(firstName)) {
            errors.add("First name: letters/spaces/hyphen/apostrophe only (1-50 chars).");
        }

        if (lastName != null && !lastName.trim().isEmpty() && !userController.isValidName(lastName)) {
            errors.add("Last name: letters/spaces/hyphen/apostrophe only (1-50 chars), or leave empty.");
        }

        if (role == null || role.trim().isEmpty()) {
            errors.add("Role: required (Admin or Normal User).");
        }

        return buildValidationMessage(errors);
    }

    private String buildValidationMessage(List<String> errors) {
        if (errors.isEmpty()) {
            return null;
        }

        StringBuilder builder = new StringBuilder("Please fix the following:\n");
        for (String error : errors) {
            builder.append("- ").append(error).append('\n');
        }

        return builder.toString().trim();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
