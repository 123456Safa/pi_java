package Views;

import Models.AdminLog;
import Services.AdminLogService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;
import utils.SidebarHelper;

public class AdminLogsView {

    private final AdminLogService adminLogService;
    private final Runnable onShowUsers;
    private final Runnable onShowProfile;
    private final Runnable onLogout;
    private final Runnable onShowHome;
    private final Runnable onShowLogs;

    public AdminLogsView(Runnable onShowUsers, Runnable onShowProfile, Runnable onShowLogs, Runnable onLogout, Runnable onShowHome) {
        this.adminLogService = new AdminLogService();
        this.onShowUsers = onShowUsers;
        this.onShowProfile = onShowProfile;
        this.onShowLogs = onShowLogs;
        this.onLogout = onLogout;
        this.onShowHome = onShowHome;
    }

    public BorderPane buildView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("single-window-root");

        // --- Sidebar ---
        root.setLeft(SidebarHelper.buildSidebar("logs", onShowUsers, onShowLogs, onShowProfile, onLogout, onShowHome));

        // --- Main Content Area ---
        VBox content = new VBox(24);
        content.setPadding(new Insets(30));

        // --- Premium Header ---
        HBox header = new HBox(20);
        header.getStyleClass().add("premium-header");
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Administrative Audit Logs");
        title.getStyleClass().add("premium-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- Export Actions ---
        MenuButton exportBtn = new MenuButton("Export Logs");
        exportBtn.getStyleClass().add("btn-outline");
        MenuItem exportPdf = new MenuItem("Export as PDF");
        exportPdf.setOnAction(e -> handleExport("pdf"));
        MenuItem exportExcel = new MenuItem("Export as Excel");
        exportExcel.setOnAction(e -> handleExport("excel"));
        MenuItem exportJson = new MenuItem("Export as JSON");
        exportJson.setOnAction(e -> handleExport("json"));
        exportBtn.getItems().addAll(exportPdf, exportExcel, exportJson);

        header.getChildren().addAll(title, spacer, exportBtn);

        // --- Logs Table Section ---
        VBox tableContainer = new VBox(15);
        tableContainer.getStyleClass().add("premium-card");
        tableContainer.setPadding(new Insets(10));
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        TableView<AdminLog> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        
        TableColumn<AdminLog, Integer> logIdCol = new TableColumn<>("Log ID");
        logIdCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        logIdCol.setPrefWidth(70);

        TableColumn<AdminLog, Integer> adminIdCol = new TableColumn<>("Admin ID");
        adminIdCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("adminId"));
        adminIdCol.setPrefWidth(80);
        
        TableColumn<AdminLog, String> adminNameCol = new TableColumn<>("Admin Name");
        adminNameCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("adminName"));
        
        TableColumn<AdminLog, String> adminEmailCol = new TableColumn<>("Admin Email");
        adminEmailCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("adminEmail"));
        
        TableColumn<AdminLog, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("actionType"));
        actionCol.setCellFactory(column -> new TableCell<AdminLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.toUpperCase());
                    badge.getStyleClass().add("badge");
                    if (item.contains("DELETE") || item.contains("BLOCK")) {
                        badge.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;");
                    } else if (item.contains("CREATE")) {
                        badge.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #065f46;");
                    } else {
                        badge.setStyle("-fx-background-color: #e0f2fe; -fx-text-fill: #075985;");
                    }
                    setGraphic(badge);
                }
            }
        });
        
        TableColumn<AdminLog, String> targetEmailCol = new TableColumn<>("Target User Email");
        targetEmailCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("targetEmail"));
        
        TableColumn<AdminLog, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("details"));
        detailsCol.setPrefWidth(250);
        
        TableColumn<AdminLog, String> dateCol = new TableColumn<>("Timestamp");
        dateCol.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("createdAt"));

        table.getColumns().addAll(logIdCol, adminIdCol, adminNameCol, adminEmailCol, actionCol, targetEmailCol, detailsCol, dateCol);
        
        List<AdminLog> logs = adminLogService.getAllLogs();
        table.getItems().addAll(logs);

        VBox.setVgrow(table, Priority.ALWAYS);
        tableContainer.getChildren().add(table);
        
        content.getChildren().addAll(header, tableContainer);
        root.setCenter(content);

        return root;
    }

    private void handleExport(String format) {
        javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
        chooser.setTitle("Save Exported Logs");
        chooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter(format.toUpperCase() + " Files", "*." + format));
        
        java.io.File selectedFile = chooser.showSaveDialog(null);
        
        if (selectedFile != null) {
            try {
                List<AdminLog> logs = adminLogService.getAllLogs();
                if ("pdf".equals(format)) {
                    Services.ExportService.exportLogsToPdf(logs, selectedFile);
                } else if ("excel".equals(format)) {
                    Services.ExportService.exportLogsToExcel(logs, selectedFile);
                } else {
                    Services.ExportService.exportLogsToJson(logs, selectedFile);
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Logs exported successfully!");
                alert.show();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Export failed: " + e.getMessage());
                alert.show();
            }
        }
    }
}
