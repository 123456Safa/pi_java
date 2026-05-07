package utils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Utility class to build a consistent sidebar for administrative views.
 */
public class SidebarHelper {

    public static VBox buildSidebar(String activeTab, 
                                    Runnable onShowUsers, 
                                    Runnable onShowLogs, 
                                    Runnable onShowProfile, 
                                    Runnable onLogout,
                                    Runnable onShowHome) {
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(30, 20, 30, 20));
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: #1e293b;"); // Dark sidebar

        Label logo = new Label("PharmaX");
        logo.setStyle("-fx-text-fill: #10b981; -fx-font-size: 24; -fx-font-weight: bold;");
        
        VBox navLinks = new VBox(8);
        navLinks.setPadding(new Insets(20, 0, 0, 0));

        Button usersBtn = buildSidebarBtn("\uD83D\uDC65 Users Management", "users".equals(activeTab));
        usersBtn.setOnAction(e -> onShowUsers.run());

        Button logsBtn = buildSidebarBtn("\uD83D\uDCDD Audit Logs", "logs".equals(activeTab));
        logsBtn.setOnAction(e -> onShowLogs.run());

        Button profileBtn = buildSidebarBtn("\uD83D\uDCAA My Profile", "profile".equals(activeTab));
        profileBtn.setOnAction(e -> onShowProfile.run());
        
        Button homeBtn = buildSidebarBtn("\uD83C\uDF10 Visit Front-end", "home".equals(activeTab));
        homeBtn.setOnAction(e -> onShowHome.run());

        navLinks.getChildren().addAll(usersBtn, logsBtn, profileBtn, homeBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = buildSidebarBtn("\uD83D\uDEAA Sign Out", false);
        logoutBtn.setStyle("-fx-text-fill: #f87171; -fx-background-color: transparent; -fx-font-weight: bold; -fx-cursor: hand; -fx-alignment: center-left; -fx-padding: 12 16;");
        logoutBtn.setOnAction(e -> onLogout.run());

        sidebar.getChildren().addAll(logo, navLinks, spacer, logoutBtn);
        return sidebar;
    }

    private static Button buildSidebarBtn(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(12, 16, 12, 16));
        btn.setStyle("-fx-background-color: " + (active ? "#334155" : "transparent") + "; " +
                     "-fx-text-fill: " + (active ? "#ffffff" : "#94a3b8") + "; " +
                     "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }
}
