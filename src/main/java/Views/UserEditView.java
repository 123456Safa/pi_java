package Views;

import Controllers.UserController;
import Models.User;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.layout.Region;

public class UserEditView {

    private final UserController userController;
    private final User targetUser;
    private final Runnable onBack;
    private final Runnable onSuccess;
    private Label statusLabel;

    public UserEditView(UserController userController, User targetUser, Runnable onBack, Runnable onSuccess) {
        this.userController = userController;
        this.targetUser = targetUser;
        this.onBack = onBack;
        this.onSuccess = onSuccess;
    }

    public VBox buildView() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(40, 60, 40, 60));
        root.getStyleClass().add("single-window-root");

        // --- Header ---
        HBox header = new HBox(20);
        header.getStyleClass().add("premium-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 30, 15, 30));

        Button backBtn = new Button("\u2190 Back to Dashboard");
        backBtn.getStyleClass().add("btn-back-header");
        backBtn.setOnAction(e -> onBack.run());

        Label title = new Label("Edit User: " + targetUser.getEmail());
        title.getStyleClass().add("premium-title");
        
        header.getChildren().addAll(backBtn, title);

        // --- Form Content ---
        VBox formCard = new VBox(30);
        formCard.getStyleClass().add("premium-card");
        formCard.setPadding(new Insets(40));
        formCard.setMaxWidth(800);
        formCard.setAlignment(Pos.TOP_LEFT);

        Label formTitle = new Label("Update Member Information");
        formTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        GridPane grid = new GridPane();
        grid.setVgap(25);
        grid.setHgap(40);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        TextField fname = new TextField(targetUser.getFirstName()); fname.getStyleClass().add("modern-input"); fname.setMinWidth(300);
        TextField lname = new TextField(targetUser.getLastName() != null ? targetUser.getLastName() : ""); lname.getStyleClass().add("modern-input"); lname.setMinWidth(300);
        TextField email = new TextField(targetUser.getEmail()); email.getStyleClass().add("modern-input"); email.setMinWidth(300);
        
        PasswordField pwd = new PasswordField(); 
        pwd.setPromptText("Leave blank to keep current password");
        pwd.getStyleClass().add("modern-input"); 
        pwd.setMinWidth(300);
        
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList(UserController.ROLE_NORMAL_USER, UserController.ROLE_ADMIN));
        role.setValue(userController.toRoleLabel(targetUser.getRoles()));
        role.getStyleClass().add("modern-input");
        role.setPrefWidth(Double.MAX_VALUE);

        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("BLOCKED", "UNBLOCKED"));
        status.setValue(targetUser.getStatus() != null ? targetUser.getStatus() : "UNBLOCKED");
        status.getStyleClass().add("modern-input");
        status.setPrefWidth(Double.MAX_VALUE);

        grid.add(createLabeledField("First Name", fname), 0, 0);
        grid.add(createLabeledField("Last Name", lname), 1, 0);
        grid.add(createLabeledField("Email Address", email), 0, 1);
        grid.add(createLabeledField("New Password", pwd), 1, 1);
        grid.add(createLabeledField("User Role", role), 0, 2);
        grid.add(createLabeledField("Account Status", status), 1, 2);

        statusLabel = new Label();
        statusLabel.setWrapText(true);

        Button saveBtn = new Button("Update User Account");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setPadding(new Insets(12, 30, 12, 30));
        saveBtn.setOnAction(e -> handleUpdate(email.getText(), pwd.getText(), fname.getText(), lname.getText(), role.getValue(), status.getValue()));

        formCard.getChildren().addAll(formTitle, grid, saveBtn, statusLabel);

        VBox container = new VBox(20, header, formCard);
        container.setAlignment(Pos.TOP_CENTER);
        
        return container;
    }

    private VBox createLabeledField(String labelText, Control field) {
        VBox group = new VBox(6);
        group.getStyleClass().add("input-group");
        Label label = new Label(labelText.toUpperCase());
        label.getStyleClass().add("modern-label");
        label.setMinWidth(Region.USE_PREF_SIZE);
        field.setMaxWidth(Double.MAX_VALUE);
        group.getChildren().addAll(label, field);
        return group;
    }

    private void handleUpdate(String email, String pwd, String fname, String lname, String roleLabel, String status) {
        targetUser.setEmail(email);
        targetUser.setFirstName(fname);
        targetUser.setLastName(lname);
        targetUser.setStatus(status);
        if (!pwd.isEmpty()) {
            targetUser.setPassword(pwd);
        }

        try {
            userController.updateUser(targetUser, roleLabel);
            onSuccess.run();
        } catch (Exception ex) {
            setError(ex.getMessage());
        }
    }

    private void setError(String msg) {
        statusLabel.getStyleClass().setAll("feedback-error");
        statusLabel.setText(msg);
    }
}
