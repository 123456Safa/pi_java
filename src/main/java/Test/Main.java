package Test;

import Models.User;
import Services.ServiceUser;
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

public class Main extends Application {

    private final ServiceUser serviceUser = new ServiceUser();
    private final ObservableList<User> userData = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #eff6ff, #f8fafc);");

        StackPane content = new StackPane();
        VBox createPane = buildCreatePane();
        VBox readPane = buildReadPane();
        VBox updatePane = buildUpdatePane();
        VBox deletePane = buildDeletePane();
        content.getChildren().addAll(createPane, readPane, updatePane, deletePane);

        Button createButton = buildNavigationButton("Create");
        Button readButton = buildNavigationButton("Read");
        Button updateButton = buildNavigationButton("Update");
        Button deleteButton = buildNavigationButton("Delete");

        createButton.setOnAction(event -> showSection(content, createPane));
        readButton.setOnAction(event -> showSection(content, readPane));
        updateButton.setOnAction(event -> showSection(content, updatePane));
        deleteButton.setOnAction(event -> showSection(content, deletePane));

        HBox navigation = new HBox(10, createButton, readButton, updateButton, deleteButton);
        navigation.setAlignment(Pos.CENTER_LEFT);
        navigation.setPadding(new Insets(0, 0, 14, 0));

        root.setTop(navigation);
        root.setCenter(content);

        Scene scene = new Scene(root, 1120, 760);
        stage.setScene(scene);
        stage.setTitle("User CRUD - JavaFX + JDBC");
        stage.show();

        showSection(content, createPane);
        refreshUsers(null);
    }

    private VBox buildCreatePane() {
        VBox container = buildSectionContainer("Create User");

        TextField emailField = new TextField();
        emailField.setPromptText("user@example.com");
        TextField rolesField = new TextField("[\"ROLE_USER\"]");
        rolesField.setPromptText("[\"ROLE_USER\", \"ROLE_ADMIN\"]");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last name (optional)");

        GridPane form = buildFormGrid();
        addFormRow(form, 0, "Email", emailField);
        addFormRow(form, 1, "Roles", rolesField);
        addFormRow(form, 2, "Password", passwordField);
        addFormRow(form, 3, "First Name", firstNameField);
        addFormRow(form, 4, "Last Name", lastNameField);

        Label hintLabel = new Label("Roles must be saved as JSON array text (example: [\"ROLE_USER\"]).");
        hintLabel.setStyle("-fx-text-fill: #334155;");

        Label statusLabel = new Label();
        Button addButton = buildPrimaryButton("Add User");
        addButton.setOnAction(event -> {
            String validationMessage = validateUserInputs(
                    emailField.getText(),
                    rolesField.getText(),
                    passwordField.getText(),
                    firstNameField.getText()
            );
            if (validationMessage != null) {
                setError(statusLabel, validationMessage);
                return;
            }

            User user = new User(
                    emailField.getText().trim(),
                    normalizeRoles(rolesField.getText()),
                    passwordField.getText(),
                    firstNameField.getText().trim(),
                    normalizeNullable(lastNameField.getText())
            );

            try {
                serviceUser.ajouter(user);
                emailField.clear();
                rolesField.setText("[\"ROLE_USER\"]");
                passwordField.clear();
                firstNameField.clear();
                lastNameField.clear();
                refreshUsers(null);
                setSuccess(statusLabel, "User created successfully.");
            } catch (SQLDataException e) {
                setError(statusLabel, e.getMessage());
            }
        });

        container.getChildren().addAll(form, hintLabel, addButton, statusLabel);
        return container;
    }

    private VBox buildReadPane() {
        VBox container = buildSectionContainer("Read Users");
        TableView<User> table = buildUserTable();
        Label statusLabel = new Label();

        Button refreshButton = buildPrimaryButton("Refresh");
        refreshButton.setOnAction(event -> refreshUsers(statusLabel));

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(table, refreshButton, statusLabel);
        return container;
    }

    private VBox buildUpdatePane() {
        VBox container = buildSectionContainer("Update User");

        TextField idField = new TextField();
        idField.setPromptText("ID (manual or from selection)");
        TextField emailField = new TextField();
        emailField.setPromptText("user@example.com");
        TextField rolesField = new TextField();
        rolesField.setPromptText("[\"ROLE_USER\"]");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last name (optional)");

        GridPane form = buildFormGrid();
        addFormRow(form, 0, "ID", idField);
        addFormRow(form, 1, "Email", emailField);
        addFormRow(form, 2, "Roles", rolesField);
        addFormRow(form, 3, "Password", passwordField);
        addFormRow(form, 4, "First Name", firstNameField);
        addFormRow(form, 5, "Last Name", lastNameField);

        TableView<User> table = buildUserTable();
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedUser) -> {
            if (selectedUser == null) {
                return;
            }
            fillUpdateFields(selectedUser, idField, emailField, rolesField, passwordField, firstNameField, lastNameField);
        });

        Label statusLabel = new Label();

        Button useSelectedButton = buildPrimaryButton("Use Selected Row");
        useSelectedButton.setOnAction(event -> {
            User selectedUser = table.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                setError(statusLabel, "Select a user from the table first.");
                return;
            }
            fillUpdateFields(selectedUser, idField, emailField, rolesField, passwordField, firstNameField, lastNameField);
            setSuccess(statusLabel, "Form filled from selected row.");
        });

        Button updateButton = buildPrimaryButton("Update User");
        updateButton.setOnAction(event -> {
            Integer resolvedId = resolveTargetId(idField.getText(), table.getSelectionModel().getSelectedItem());
            if (resolvedId == null) {
                setError(statusLabel, "Provide a valid id or select a row in the table.");
                return;
            }

            String validationMessage = validateUserInputs(
                    emailField.getText(),
                    rolesField.getText(),
                    passwordField.getText(),
                    firstNameField.getText()
            );
            if (validationMessage != null) {
                setError(statusLabel, validationMessage);
                return;
            }

            User user = new User(
                    resolvedId,
                    emailField.getText().trim(),
                    normalizeRoles(rolesField.getText()),
                    passwordField.getText(),
                    firstNameField.getText().trim(),
                    normalizeNullable(lastNameField.getText())
            );

            try {
                serviceUser.modifier(user);
                refreshUsers(null);
                setSuccess(statusLabel, "User updated successfully.");
            } catch (SQLDataException e) {
                setError(statusLabel, e.getMessage());
            }
        });

        Button refreshButton = buildPrimaryButton("Refresh Table");
        refreshButton.setOnAction(event -> refreshUsers(statusLabel));

        HBox actions = new HBox(10, useSelectedButton, updateButton, refreshButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(form, actions, table, statusLabel);
        return container;
    }

    private VBox buildDeletePane() {
        VBox container = buildSectionContainer("Delete User");

        TextField idField = new TextField();
        idField.setPromptText("ID (manual or from selection)");

        GridPane form = buildFormGrid();
        addFormRow(form, 0, "ID", idField);

        TableView<User> table = buildUserTable();
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedUser) -> {
            if (selectedUser != null) {
                idField.setText(String.valueOf(selectedUser.getId()));
            }
        });

        Label statusLabel = new Label();

        Button useSelectedButton = buildPrimaryButton("Use Selected Row");
        useSelectedButton.setOnAction(event -> {
            User selectedUser = table.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                setError(statusLabel, "Select a user from the table first.");
                return;
            }
            idField.setText(String.valueOf(selectedUser.getId()));
            setSuccess(statusLabel, "Delete id filled from selected row.");
        });

        Button deleteButton = buildPrimaryButton("Delete User");
        deleteButton.setOnAction(event -> {
            Integer resolvedId = resolveTargetId(idField.getText(), table.getSelectionModel().getSelectedItem());
            if (resolvedId == null) {
                setError(statusLabel, "Provide a valid id or select a row in the table.");
                return;
            }

            User user = new User();
            user.setId(resolvedId);

            try {
                serviceUser.supprimer(user);
                idField.clear();
                table.getSelectionModel().clearSelection();
                refreshUsers(null);
                setSuccess(statusLabel, "User deleted successfully.");
            } catch (SQLDataException e) {
                setError(statusLabel, e.getMessage());
            }
        });

        Button refreshButton = buildPrimaryButton("Refresh Table");
        refreshButton.setOnAction(event -> refreshUsers(statusLabel));

        HBox actions = new HBox(10, useSelectedButton, deleteButton, refreshButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(form, actions, table, statusLabel);
        return container;
    }

    private TableView<User> buildUserTable() {
        TableView<User> table = new TableView<>();
        table.setItems(userData);

        TableColumn<User, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));

        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));

        TableColumn<User, String> rolesColumn = new TableColumn<>("Roles");
        rolesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoles()));

        TableColumn<User, String> passwordColumn = new TableColumn<>("Password");
        passwordColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPassword()));

        TableColumn<User, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));

        TableColumn<User, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));

        table.getColumns().add(idColumn);
        table.getColumns().add(emailColumn);
        table.getColumns().add(rolesColumn);
        table.getColumns().add(passwordColumn);
        table.getColumns().add(firstNameColumn);
        table.getColumns().add(lastNameColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(420);
        return table;
    }

    private VBox buildSectionContainer(String title) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        VBox container = new VBox(12, titleLabel);
        container.setPadding(new Insets(14));
        container.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #cbd5e1;" +
                        "-fx-border-radius: 12;"
        );
        return container;
    }

    private GridPane buildFormGrid() {
        GridPane gridPane = new GridPane();
        gridPane.setVgap(8);
        gridPane.setHgap(12);
        return gridPane;
    }

    private void addFormRow(GridPane gridPane, int rowIndex, String labelText, TextField field) {
        Label label = new Label(labelText + " :");
        label.setMinWidth(120);
        gridPane.add(label, 0, rowIndex);
        field.setPrefWidth(380);
        gridPane.add(field, 1, rowIndex);
    }

    private Button buildPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #2563eb;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 14 8 14;"
        );
        return button;
    }

    private Button buildNavigationButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #0f172a;" +
                        "-fx-text-fill: #e2e8f0;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 16 8 16;" +
                        "-fx-font-weight: bold;"
        );
        return button;
    }

    private void showSection(StackPane stackPane, Node targetSection) {
        for (Node section : stackPane.getChildren()) {
            boolean active = section == targetSection;
            section.setVisible(active);
            section.setManaged(active);
        }
    }

    private void refreshUsers(Label statusLabel) {
        try {
            userData.setAll(serviceUser.recuperer());
            if (statusLabel != null) {
                setSuccess(statusLabel, "Loaded " + userData.size() + " users.");
            }
        } catch (SQLDataException e) {
            if (statusLabel != null) {
                setError(statusLabel, e.getMessage());
            }
        }
    }

    private String validateUserInputs(String email, String roles, String password, String firstName) {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required.";
        }
        if (roles == null || roles.trim().isEmpty()) {
            return "Roles is required.";
        }
        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            return "First name is required.";
        }
        return null;
    }

    private Integer resolveTargetId(String idText, User selectedUser) {
        if (idText != null && !idText.trim().isEmpty()) {
            try {
                int parsedId = Integer.parseInt(idText.trim());
                if (parsedId > 0) {
                    return parsedId;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (selectedUser != null && selectedUser.getId() > 0) {
            return selectedUser.getId();
        }

        return null;
    }

    private String normalizeRoles(String rawRoles) {
        String value = rawRoles == null ? "" : rawRoles.trim();
        if (value.isEmpty()) {
            return "[]";
        }

        if (value.startsWith("[") && value.endsWith("]")) {
            return value;
        }

        String[] tokens = value.split(",");
        StringBuilder builder = new StringBuilder("[");

        for (String token : tokens) {
            String role = token.trim();
            if (role.isEmpty()) {
                continue;
            }
            if (builder.length() > 1) {
                builder.append(", ");
            }
            builder.append('"').append(role.replace("\"", "\\\"")).append('"');
        }

        builder.append(']');
        return builder.toString();
    }

    private String normalizeNullable(String text) {
        if (text == null) {
            return null;
        }
        String value = text.trim();
        return value.isEmpty() ? null : value;
    }

    private void fillUpdateFields(
            User user,
            TextField idField,
            TextField emailField,
            TextField rolesField,
            PasswordField passwordField,
            TextField firstNameField,
            TextField lastNameField
    ) {
        idField.setText(String.valueOf(user.getId()));
        emailField.setText(user.getEmail());
        rolesField.setText(user.getRoles());
        passwordField.setText(user.getPassword());
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName() == null ? "" : user.getLastName());
    }

    private void setSuccess(Label label, String message) {
        label.setStyle("-fx-text-fill: #166534; -fx-font-weight: bold;");
        label.setText(message);
    }

    private void setError(Label label, String message) {
        label.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
        label.setText(message);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
