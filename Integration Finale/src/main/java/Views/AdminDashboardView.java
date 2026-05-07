package Views;

import controllers.UserController;
import models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.sql.SQLDataException;
import java.util.List;
import javafx.collections.FXCollections;

public class AdminDashboardView {

    private final UserController userController;
    private final User currentUser;
    private final Runnable onLogout;
    private final Runnable onShowProfile;
    private final Runnable onShowLogs;
    private final Runnable onShowUserCreate;
    private final java.util.function.Consumer<User> onShowUserEdit;
    private final Runnable onShowHome;

    private TableView<User> userTable;
    private ObservableList<User> userData = FXCollections.observableArrayList();
    
    private Label totalUsersLabel = new Label("0");
    private Label adminUsersLabel = new Label("0");
    private Label normalUsersLabel = new Label("0");

    private ComboBox<String> sortField = new ComboBox<>(FXCollections.observableArrayList("ID", "Email", "First Name", "Last Name", "Status"));
    private ToggleButton sortOrderBtn = new ToggleButton("\u2191"); // Up arrow

    public AdminDashboardView(UserController userController, User currentUser, Runnable onLogout, Runnable onShowProfile, Runnable onShowLogs, Runnable onShowUserCreate, java.util.function.Consumer<User> onShowUserEdit, Runnable onShowHome) {
        this.userController = userController;
        this.currentUser = currentUser;
        this.onLogout = onLogout;
        this.onShowProfile = onShowProfile;
        this.onShowLogs = onShowLogs;
        this.onShowUserCreate = onShowUserCreate;
        this.onShowUserEdit = onShowUserEdit;
        this.onShowHome = onShowHome;
        
        sortField.setValue("ID");
        sortField.getStyleClass().add("modern-input");
        sortOrderBtn.getStyleClass().add("btn-outline-sm");
        
        sortField.setOnAction(e -> refreshData());
        sortOrderBtn.setOnAction(e -> {
            sortOrderBtn.setText(sortOrderBtn.isSelected() ? "\u2193" : "\u2191");
            refreshData();
        });
    }

    public Region buildView() {
        // --- Main Content Area (no sidebar — unified dashboard provides it) ---
        VBox content = new VBox(24);
        content.setPadding(new Insets(30));
        
        // Header with stats
        HBox topBar = buildTopBar();
        HBox statsCards = buildStatsCards();
        
        // Table Section
        VBox tableSection = buildTableSection();
        
        content.getChildren().addAll(topBar, statsCards, tableSection);

        refreshData();
        return content;
    }

    // Removed buildSidebar and buildSidebarBtn as they are now in SidebarHelper

    private HBox buildTopBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Users Directory");
        title.getStyleClass().add("premium-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search users...");
        search.getStyleClass().add("modern-input");
        search.setPrefWidth(250);
        search.setOnKeyReleased(e -> refreshData(search.getText()));

        Button addBtn = new Button("+ New User");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setOnAction(e -> onShowUserCreate.run());

        bar.getChildren().addAll(title, spacer, new Label("Sort By:"), sortField, sortOrderBtn, search, addBtn);
        return bar;
    }

    private HBox buildStatsCards() {
        HBox box = new HBox(20);
        
        box.getChildren().addAll(
            createStatCard("Total Users", totalUsersLabel, "#3b82f6"),
            createStatCard("Administrators", adminUsersLabel, "#f59e0b"),
            createStatCard("Standard Users", normalUsersLabel, "#10b981")
        );
        return box;
    }

    private VBox createStatCard(String title, Label val, String color) {
        VBox card = new VBox(8);
        card.getStyleClass().add("premium-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(250);

        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
        
        val.setStyle("-fx-font-size: 28; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(t, val);
        return card;
    }

    private VBox buildTableSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("premium-card");
        section.setPadding(new Insets(10));
        VBox.setVgrow(section, Priority.ALWAYS);

        // --- Table Actions (Export) ---
        HBox tableActions = new HBox(10);
        tableActions.setAlignment(Pos.CENTER_RIGHT);
        
        MenuButton exportBtn = new MenuButton("Export Users");
        exportBtn.getStyleClass().add("btn-outline");
        MenuItem exportPdf = new MenuItem("Export as PDF");
        exportPdf.setOnAction(e -> handleExport("pdf"));
        MenuItem exportExcel = new MenuItem("Export as Excel");
        exportExcel.setOnAction(e -> handleExport("excel"));
        MenuItem exportJson = new MenuItem("Export as JSON");
        exportJson.setOnAction(e -> handleExport("json"));
        exportBtn.getItems().addAll(exportPdf, exportExcel, exportJson);
        
        tableActions.getChildren().add(exportBtn);

        userTable = new TableView<>(userData);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getId()));
        idCol.setPrefWidth(50);

        TableColumn<User, String> nameCol = new TableColumn<>("User");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getFirstName() + " " + (d.getValue().getLastName() != null ? d.getValue().getLastName() : "")));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getEmail()));

        TableColumn<User, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getPhoneNumber() != null ? d.getValue().getPhoneNumber() : "N/A"));

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(userController.toRoleLabel(d.getValue().getRoles())));

        TableColumn<User, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getStatus()));
        statusCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("badge");
                    if ("BLOCKED".equals(item)) {
                        badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
                    } else {
                        badge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #065f46;");
                    }
                    setGraphic(badge);
                }
            }
        });

        TableColumn<User, String> lockoutCol = new TableColumn<>("Lockout Until");
        lockoutCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(getLockoutDisplay(d.getValue())));

        TableColumn<User, String> createdCol = new TableColumn<>("Created At");
        createdCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getCreatedAt() != null ? d.getValue().getCreatedAt().toString() : "N/A"));

        TableColumn<User, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final MenuButton actionsBtn = new MenuButton("Actions");
            {
                actionsBtn.getStyleClass().add("btn-outline-sm");
                actionsBtn.setStyle("-fx-background-radius: 20; -fx-padding: 4 12;");
                
                MenuItem edit = new MenuItem("\u270F Edit");
                MenuItem block = new MenuItem("\uD83D\uDEAB Block/Unblock");
                MenuItem delete = new MenuItem("\uD83D\uDDD1 Delete");
                
                actionsBtn.getItems().addAll(edit, block, delete);
                
                edit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                block.setOnAction(e -> handleToggleBlock(getTableView().getItems().get(getIndex())));
                delete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionsBtn);
            }
        });

        userTable.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, roleCol, statusCol, lockoutCol, createdCol, actionsCol);
        
        userTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem editItem = new MenuItem("\u270F Edit User");
            editItem.setOnAction(event -> handleEdit(row.getItem()));
            
            MenuItem blockItem = new MenuItem("\uD83D\uDEAB Toggle Block");
            blockItem.setOnAction(event -> handleToggleBlock(row.getItem()));
            
            MenuItem deleteItem = new MenuItem("\uD83D\uDDD1 Delete User");
            deleteItem.setStyle("-fx-text-fill: #ef4444;");
            deleteItem.setOnAction(event -> handleDelete(row.getItem()));
            
            contextMenu.getItems().addAll(editItem, blockItem, new SeparatorMenuItem(), deleteItem);
            
            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                .then((ContextMenu)null)
                .otherwise(contextMenu)
            );
            return row;
        });

        VBox.setVgrow(userTable, Priority.ALWAYS);
        section.getChildren().addAll(tableActions, userTable);
        return section;
    }

    private String getLockoutDisplay(User u) {
        if (u.getLockoutTime() == null) return "N/A";
        long diff = (System.currentTimeMillis() - u.getLockoutTime().getTime()) / (60 * 1000);
        if (diff < 15) return (15 - diff) + " mins";
        return "Expired";
    }

    private void handleExport(String format) {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Save Exported Users");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter(format.toUpperCase() + " Files", "*." + format));
        java.io.File file = chooser.showSaveDialog(userTable.getScene().getWindow());
        
        if (file != null) {
            try {
                if ("pdf".equals(format)) {
                    services.ExportService.exportToPdf(userData, file);
                } else if ("excel".equals(format)) {
                    services.ExportService.exportToExcel(userData, file);
                } else {
                    services.ExportService.exportToJson(userData, file);
                }
                showAlert("Success", "User list exported successfully!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Export failed: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void handleEdit(User user) {
        onShowUserEdit.accept(user);
    }

    private void handleToggleBlock(User user) {
        try {
            String newStatus = "BLOCKED".equals(user.getStatus()) ? "UNBLOCKED" : "BLOCKED";
            userController.updateStatus(user.getId(), newStatus);
            refreshData();
        } catch (Exception e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDelete(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + user.getEmail() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try {
                    userController.deleteUserById(user.getId());
                    refreshData();
                } catch (Exception e) {
                    showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void handleAddUser() {
        // Show a simplified registration dialog
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Create New User");
        dialog.setHeaderText("Add a new member to PharmaX");
        
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField fname = new TextField(); fname.setPromptText("First Name");
        TextField lname = new TextField(); lname.setPromptText("Last Name");
        TextField email = new TextField(); email.setPromptText("Email");
        PasswordField pwd = new PasswordField(); pwd.setPromptText("Password");
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList("USER", "ADMIN"));
        role.setValue("USER");
        
        grid.add(new Label("First Name:"), 0, 0); grid.add(fname, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1); grid.add(lname, 1, 1);
        grid.add(new Label("Email:"), 0, 2); grid.add(email, 1, 2);
        grid.add(new Label("Password:"), 0, 3); grid.add(pwd, 1, 3);
        grid.add(new Label("Role:"), 0, 4); grid.add(role, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new User(0, email.getText(), role.getValue(), pwd.getText(), fname.getText(), lname.getText());
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(u -> {
            try {
                userController.createUser(u, role.getValue());
                refreshData();
                showAlert("Success", "User created successfully!", Alert.AlertType.INFORMATION);
            } catch (Exception ex) {
                showAlert("Error", "Registration failed: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.show();
    }

    public void refreshData() {
        refreshData("");
    }

    public void refreshData(String search) {
        try {
            String field = sortField.getValue();
            if ("First Name".equals(field)) field = "FIRST_NAME";
            else if ("Last Name".equals(field)) field = "LAST_NAME";
            
            List<User> users = userController.searchAndSortUsers(search, field, !sortOrderBtn.isSelected());
            userData.setAll(users);
            
            int[] stats = userController.getStatistics();
            totalUsersLabel.setText(String.valueOf(stats[0]));
            adminUsersLabel.setText(String.valueOf(stats[1]));
            normalUsersLabel.setText(String.valueOf(stats[2]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
