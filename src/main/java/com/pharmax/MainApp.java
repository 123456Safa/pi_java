package com.pharmax;

import com.pharmax.controller.AdminBlogController;
import com.pharmax.controller.AdminCommentController;
import com.pharmax.controller.FrontBlogController;
import com.pharmax.model.Article;
import com.pharmax.model.Commentaire;
import com.pharmax.service.ArticleService;
import com.pharmax.service.CommentaireService;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * MainApp — PharmaX Blog & Comment Management
 * Design matching the original Symfony project (Pharmax green #5ea96b).
 */
public class MainApp extends Application {

    private ArticleService articleService;
    private CommentaireService commentaireService;

    private StackPane contentArea;
    private Button btnFront, btnAdminBlog, btnAdminComment;
    private Button activeBtn;

    private FrontBlogController frontBlogController;
    private AdminBlogController adminBlogController;
    private AdminCommentController adminCommentController;

    @Override
    public void start(Stage primaryStage) {
        articleService = new ArticleService();
        commentaireService = new CommentaireService(
                new com.pharmax.service.CommentValidationService(),
                new com.pharmax.service.CommentModerationService()
        );

        seedSampleData();

        // ─── Navigation Bar (matches Symfony sidebar style) ────
        HBox navbar = new HBox();
        navbar.getStyleClass().add("pharmax-navbar");
        navbar.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("⚕ PharmaX");
        logo.setStyle("-fx-text-fill: #5ea96b; -fx-font-size: 18; -fx-font-weight: bold; -fx-padding: 0 25 0 15;");

        Region spacer1 = new Region();
        spacer1.setPrefWidth(10);

        btnFront = createNavButton("📰 Blog");
        btnAdminBlog = createNavButton("📝 Gestion Articles");
        btnAdminComment = createNavButton("💬 Gestion Commentaires");

        navbar.getChildren().addAll(logo, spacer1, btnFront, btnAdminBlog, btnAdminComment);

        // ─── Content Area ──────────────────────────────────────
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f8f9fa;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // ─── Build Controllers ─────────────────────────────────
        frontBlogController = new FrontBlogController(articleService, commentaireService);
        adminBlogController = new AdminBlogController(articleService);
        adminCommentController = new AdminCommentController(commentaireService, articleService);

        // ─── Navigation Actions ────────────────────────────────
        btnFront.setOnAction(e -> switchView("front"));
        btnAdminBlog.setOnAction(e -> switchView("adminBlog"));
        btnAdminComment.setOnAction(e -> switchView("adminComment"));

        // ─── Layout ────────────────────────────────────────────
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #f8f9fa;");
        root.getChildren().addAll(navbar, contentArea);

        Scene scene = new Scene(root, 1100, 750);

        // Load CSS
        try {
            String css = Objects.requireNonNull(
                    getClass().getResource("/css/pharmax.css")
            ).toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("⚠ CSS not found, using defaults.");
        }

        primaryStage.setTitle("PharmaX — Blog & Comment Management");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Default view
        switchView("front");
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn");
        btn.setMinHeight(50);
        return btn;
    }

    private void switchView(String view) {
        contentArea.getChildren().clear();

        // Reset all nav buttons
        btnFront.getStyleClass().remove("nav-btn-active");
        btnAdminBlog.getStyleClass().remove("nav-btn-active");
        btnAdminComment.getStyleClass().remove("nav-btn-active");

        switch (view) {
            case "front":
                btnFront.getStyleClass().add("nav-btn-active");
                contentArea.getChildren().add(frontBlogController.buildView());
                frontBlogController.refresh();
                break;
            case "adminBlog":
                btnAdminBlog.getStyleClass().add("nav-btn-active");
                contentArea.getChildren().add(adminBlogController.buildView());
                adminBlogController.refresh();
                break;
            case "adminComment":
                btnAdminComment.getStyleClass().add("nav-btn-active");
                contentArea.getChildren().add(adminCommentController.buildView());
                adminCommentController.refresh();
                break;
        }
    }

    private void seedSampleData() {
        Article a1 = articleService.create(
                "Les bienfaits de la vitamine D en hiver",
                "La vitamine D joue un rôle crucial dans le maintien de la santé osseuse et du système immunitaire. " +
                "Pendant les mois d'hiver, notre exposition au soleil diminue considérablement, ce qui peut entraîner " +
                "une carence en vitamine D. Il est recommandé de consommer des aliments riches en vitamine D comme " +
                "les poissons gras, les œufs et les produits laitiers enrichis. Un complément alimentaire peut également " +
                "être envisagé après consultation avec votre pharmacien.",
                null
        );
        a1.publish();

        Article a2 = articleService.create(
                "Comment bien gérer son traitement antibiotique",
                "Les antibiotiques sont des médicaments puissants qui combattent les infections bactériennes. " +
                "Il est essentiel de respecter la durée du traitement prescrit, même si les symptômes s'améliorent. " +
                "Prendre un antibiotique de manière incorrecte peut contribuer à la résistance bactérienne. " +
                "Votre pharmacien PharmaX est là pour vous conseiller sur la prise optimale de vos médicaments.",
                null
        );
        a2.publish();

        Article a3 = articleService.create(
                "Prévention de la grippe saisonnière : nos conseils",
                "La saison de la grippe approche et il est important de se préparer. La vaccination reste le moyen " +
                "le plus efficace de se protéger. En complément, adoptez les gestes barrières : lavage fréquent des mains, " +
                "port du masque dans les lieux publics, et aération régulière des espaces clos. " +
                "Rendez-vous dans votre pharmacie PharmaX pour votre vaccin antigrippal.",
                null
        );
        a3.publish();

        // Sample comments
        commentaireService.createDirect("Merci pour ces explications claires.", a1, "valide");
        commentaireService.createDirect("Article très informatif !", a1, "valide");
        commentaireService.createDirect("Je ne savais pas pour la résistance bactérienne.", a2, "valide");
        commentaireService.createDirect("Commentaire en attente de validation.", a3, "en_attente");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
