package Views;

import Models.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class UserHomeView {

    private final User currentUser;
    private final Runnable onShowProfile;
    private final Runnable onLogout;

    public UserHomeView(User currentUser, Runnable onShowProfile, Runnable onLogout) {
        this.currentUser = currentUser;
        this.onShowProfile = onShowProfile;
        this.onLogout = onLogout;
    }

    public VBox buildView() {
        VBox root = new VBox(0);
        root.getStyleClass().add("single-window-root");
        root.setAlignment(Pos.TOP_CENTER);

        // --- Navbar ---
        HBox navbar = new HBox(20);
        navbar.getStyleClass().add("premium-header");
        navbar.setAlignment(Pos.CENTER_LEFT);
        
        Label logo = new Label("PharmaX");
        logo.setStyle("-fx-text-fill: #10b981; -fx-font-size: 24; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button profileBtn = new Button("\uD83D\uDC64 My Account");
        profileBtn.getStyleClass().add("btn-primary");
        profileBtn.setPrefWidth(120);
        profileBtn.setOnAction(e -> onShowProfile.run());

        Button logoutBtn = new Button("Sign Out");
        logoutBtn.getStyleClass().add("btn-danger");
        logoutBtn.setPrefWidth(120);
        logoutBtn.setOnAction(e -> onLogout.run());

        navbar.getChildren().addAll(logo, spacer, profileBtn, logoutBtn);

        // --- Hero Section ---
        VBox hero = new VBox(20);
        hero.setPadding(new Insets(80, 40, 80, 40));
        hero.setAlignment(Pos.CENTER);
        hero.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #f1f5f9);");

        Label welcome = new Label("Welcome back, " + currentUser.getFirstName() + "!");
        welcome.setStyle("-fx-font-size: 42; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtext = new Label("Manage your health profile and access premium pharmaceutical tools.");
        subtext.setStyle("-fx-font-size: 18; -fx-text-fill: #64748b;");

        hero.getChildren().addAll(welcome, subtext);

        // --- Quick Actions / Features ---
        HBox cards = new HBox(30);
        cards.setAlignment(Pos.CENTER);
        cards.setPadding(new Insets(40));

        cards.getChildren().addAll(
            createFeatureCard("\uD83D\uDD12 Security", "Update your 2FA and Face ID settings to keep your account safe."),
            createFeatureCard("\uD83D\uDCC4 My Data", "View and export your activity logs and medical history."),
            createFeatureCard("\uD83D\uDCAC Support", "Chat with our AI health assistant for immediate guidance.")
        );

        root.getChildren().addAll(navbar, hero, cards);
        return root;
    }

    private VBox createFeatureCard(String title, String desc) {
        VBox card = new VBox(15);
        card.getStyleClass().add("premium-card");
        card.setPadding(new Insets(30));
        card.setPrefWidth(300);
        card.setAlignment(Pos.TOP_LEFT);

        Label t = new Label(title);
        t.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label d = new Label(desc);
        d.setWrapText(true);
        d.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14; -fx-line-spacing: 5;");
        
        Button action = new Button("Explore \u2192");
        action.getStyleClass().add("btn-outline");
        action.setStyle("-fx-padding: 8 16;");

        card.getChildren().addAll(t, d, action);
        return card;
    }
}
