package com.pharmax;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.pharmax.controller.AdminBlogController;
import com.pharmax.controller.AdminCommentController;
import com.pharmax.controller.FrontBlogController;
import com.pharmax.controller.ScraperController;
import com.pharmax.model.Article;
import com.pharmax.service.ArticleService;
import com.pharmax.service.CommentaireService;
import com.pharmax.ui.SplashScreen;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * MainApp — PharmaX Blog & Comment Management
 * Modern JavaFX app with asynchronous initialization to prevent UI freezing.
 * Uses lazy-loading for controllers and background threads for heavy operations.
 */
public class MainApp extends Application {

    private ArticleService articleService;
    private CommentaireService commentaireService;

    private StackPane contentArea;
    private Button btnFront, btnAdminBlog, btnAdminComment, btnScraper;
    // Note: activeBtn tracking removed - button styles managed via CSS classes

    // Lazy-loaded controllers
    private FrontBlogController frontBlogController;
    private AdminBlogController adminBlogController;
    private AdminCommentController adminCommentController;
    private ScraperController scraperController;

    // Cached views (built once, reused)
    private javafx.scene.layout.Region frontView;
    private javafx.scene.layout.Region adminBlogView;
    private javafx.scene.layout.Region adminCommentView;
    private javafx.scene.layout.Region scraperView;

    private SplashScreen splashScreen;
    private ExecutorService executorService;

    @Override
    public void start(Stage primaryStage) {
        // ═══════════════════════════════════════════════════════════════
        // PHASE 1: SHOW SPLASH SCREEN IMMEDIATELY
        // ═══════════════════════════════════════════════════════════════

        splashScreen = new SplashScreen();
        splashScreen.show();
        splashScreen.updateMessage("Loading services...");

        executorService = Executors.newFixedThreadPool(2);

        // ═══════════════════════════════════════════════════════════════
        // PHASE 2: BUILD UI ON FX THREAD (lightweight, no data loading)
        // ═══════════════════════════════════════════════════════════════

        Platform.runLater(() -> {
            try {
                buildMainUI(primaryStage);
                splashScreen.updateMessage("Initializing database...");
            } catch (Exception e) {
                System.err.println("❌ Error building UI: " + e.getMessage());
            }
        });

        // ═══════════════════════════════════════════════════════════════
        // PHASE 3: LOAD DATA IN BACKGROUND (won't freeze UI)
        // ═══════════════════════════════════════════════════════════════

        executorService.execute(() -> {
            try {
                // Initialize services
                articleService = new ArticleService();
                commentaireService = new CommentaireService(
                        new com.pharmax.service.CommentValidationService(),
                        new com.pharmax.service.CommentModerationService(),
                        articleService
                );
                splashScreen.updateMessage("Seeding sample data...");

                // Seed data if needed
                seedSampleData();
                splashScreen.updateMessage("Almost ready...");

                // Small delay to ensure all is ready
                Thread.sleep(500);

                // ─── Show main window & hide splash ───
                Platform.runLater(() -> {
                    splashScreen.close();
                    primaryStage.show();
                    // Load initial view
                    switchView("front");
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("❌ Initialization interrupted: " + e.getMessage());
                Platform.runLater(() -> {
                    splashScreen.updateMessage("Initialization interrupted");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                    }
                    splashScreen.close();
                    primaryStage.show();
                });
            } catch (RuntimeException e) {
                System.err.println("❌ Runtime error during initialization: " + e.getMessage());
                Platform.runLater(() -> {
                    splashScreen.updateMessage("Database unavailable. UI loaded without data.");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                    }
                    splashScreen.close();
                    primaryStage.show();
                    switchView("front");
                });
            } catch (Exception e) {
                System.err.println("❌ Unexpected error: " + e.getMessage());
                Platform.runLater(() -> {
                    splashScreen.updateMessage("Error: " + e.getMessage());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                    }
                    splashScreen.close();
                    primaryStage.show();
                });
            }
        });
    }

    /**
     * Build the main UI structure (without loading data).
     * This runs immediately on the FX thread so the window appears responsive.
     */
    private void buildMainUI(Stage primaryStage) {
        // ─── Navigation Bar ────
        HBox navbar = new HBox();
        navbar.getStyleClass().add("pharmax-navbar");
        navbar.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("⚕ PharmaX");
        logo.setStyle("-fx-text-fill: white; -fx-font-size: 22; -fx-font-weight: bold; -fx-padding: 0 25 0 20;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnFront = createNavButton("📰 Blog");
        btnAdminBlog = createNavButton("📝 Articles");
        btnAdminComment = createNavButton("💬 Comments");
        btnScraper = createNavButton("🔍 Scraper");

        navbar.getChildren().addAll(logo, btnFront, btnAdminBlog, btnAdminComment, btnScraper, spacer);

        // ─── Content Area ──────
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f5f7fa;");
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Show loading message initially
        Label loadingLabel = new Label("⏳ Loading...");
        loadingLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #7f8c8d;");
        contentArea.getChildren().add(loadingLabel);

        // ─── Navigation Actions (lazy-load controllers) ────
        btnFront.setOnAction(e -> switchView("front"));
        btnAdminBlog.setOnAction(e -> switchView("adminBlog"));
        btnAdminComment.setOnAction(e -> switchView("adminComment"));
        btnScraper.setOnAction(e -> switchView("scraper"));

        // ─── Layout ────
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f5f7fa;");
        root.getChildren().addAll(navbar, contentArea);

        Scene scene = new Scene(root, 1200, 800);

        // Load modern CSS
        try {
            String css = Objects.requireNonNull(
                    getClass().getResource("/css/pharmax-modern.css")
            ).toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.out.println("⚠ Modern CSS not found, trying fallback...");
            try {
                String css = Objects.requireNonNull(
                        getClass().getResource("/css/pharmax.css")
                ).toExternalForm();
                scene.getStylesheets().add(css);
            } catch (Exception ex) {
                System.out.println("⚠ CSS not found, using defaults.");
            }
        }

        primaryStage.setTitle("PharmaX — Blog & Comment Management");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.centerOnScreen();
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-btn");
        btn.setMinHeight(50);
        return btn;
    }

    /**
     * Lazy-load and switch to a view.
     * Controllers are only instantiated when first accessed.
     */
    private void switchView(String view) {
        contentArea.getChildren().clear();

        // Reset all nav buttons
        btnFront.getStyleClass().remove("nav-btn-active");
        btnAdminBlog.getStyleClass().remove("nav-btn-active");
        btnAdminComment.getStyleClass().remove("nav-btn-active");
        btnScraper.getStyleClass().remove("nav-btn-active");

        try {
            switch (view) {
                case "front" -> {
                    btnFront.getStyleClass().add("nav-btn-active");
                    if (frontView == null) {
                        if (frontBlogController == null) {
                            frontBlogController = new FrontBlogController(articleService, commentaireService);
                        }
                        frontView = frontBlogController.buildView();
                    }
                    contentArea.getChildren().add(frontView);
                    if (frontBlogController != null) {
                        try {
                            frontBlogController.refresh();
                        } catch (RuntimeException e) {
                            System.err.println("⚠ Could not load articles: " + e.getMessage());
                        }
                    }
                }
                case "adminBlog" -> {
                    btnAdminBlog.getStyleClass().add("nav-btn-active");
                    if (adminBlogView == null) {
                        if (adminBlogController == null) {
                            adminBlogController = new AdminBlogController(articleService);
                        }
                        adminBlogView = adminBlogController.buildView();
                    }
                    contentArea.getChildren().add(adminBlogView);
                    if (adminBlogController != null) {
                        try {
                            adminBlogController.refresh();
                        } catch (RuntimeException e) {
                            System.err.println("⚠ Could not load articles: " + e.getMessage());
                        }
                    }
                }
                case "adminComment" -> {
                    btnAdminComment.getStyleClass().add("nav-btn-active");
                    if (adminCommentView == null) {
                        if (adminCommentController == null) {
                            adminCommentController = new AdminCommentController(commentaireService, articleService);
                        }
                        adminCommentView = adminCommentController.buildView();
                    }
                    contentArea.getChildren().add(adminCommentView);
                    if (adminCommentController != null) {
                        try {
                            adminCommentController.refresh();
                        } catch (RuntimeException e) {
                            System.err.println("⚠ Could not load comments: " + e.getMessage());
                        }
                    }
                }
                case "scraper" -> {
                    btnScraper.getStyleClass().add("nav-btn-active");
                    if (scraperView == null) {
                        if (scraperController == null) {
                            scraperController = new ScraperController();
                        }
                        scraperView = scraperController.buildView();
                    }
                    contentArea.getChildren().add(scraperView);
                }
                default -> {}
            }
        } catch (Exception e) {
            System.err.println("❌ Error switching view: " + e.getMessage());
            Label errorLabel = new Label("Error loading view: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14;");
            contentArea.getChildren().add(errorLabel);
        }
    }

    /**
     * Seed sample data on first run (if database is empty).
     */
    private void seedSampleData() {
        try {
            // Check if articles already exist
            int count = articleService.countPublished();
            if (count > 0) {
                System.out.println("✅ Data already seeded (" + count + " articles found)");
                return;
            }

            System.out.println("📦 Seeding sample data...");

            Article a1 = articleService.create(
                    "Les bienfaits de la vitamine D en hiver",
                    "La vitamine D joue un rôle crucial dans le maintien de la santé osseuse et du système immunitaire. " +
                    "Pendant les mois d'hiver, notre exposition au soleil diminue considérablement, ce qui peut entraîner " +
                    "une carence en vitamine D. Il est recommandé de consommer des aliments riches en vitamine D comme " +
                    "les poissons gras, les œufs et les produits laitiers enrichis. Un complément alimentaire peut également " +
                    "être envisagé après consultation avec votre pharmacien.",
                    null,
                    null
            );
            if (a1 != null && a1.getId() != null) articleService.togglePublish(a1.getId());

            Article a2 = articleService.create(
                    "Comment bien gérer son traitement antibiotique",
                    "Les antibiotiques sont des médicaments puissants qui combattent les infections bactériennes. " +
                    "Il est essentiel de respecter la durée du traitement prescrit, même si les symptômes s'améliorent. " +
                    "Prendre un antibiotique de manière incorrecte peut contribuer à la résistance bactérienne. " +
                    "Votre pharmacien PharmaX est là pour vous conseiller sur la prise optimale de vos médicaments.",
                    null,
                    null
            );
            if (a2 != null && a2.getId() != null) articleService.togglePublish(a2.getId());

            Article a3 = articleService.create(
                    "Prévention de la grippe saisonnière : nos conseils",
                    "La saison de la grippe approche et il est important de se préparer. La vaccination reste le moyen " +
                    "le plus efficace de se protéger. En complément, adoptez les gestes barrières : lavage fréquent des mains, " +
                    "port du masque dans les lieux publics, et aération régulière des espaces clos. " +
                    "Rendez-vous dans votre pharmacie PharmaX pour votre vaccin antigrippal.",
                    null,
                    null
            );
            if (a3 != null && a3.getId() != null) articleService.togglePublish(a3.getId());

            // Sample comments
            if (a1 != null && a1.getId() != null) {
                commentaireService.createDirect("Merci pour ces explications claires !", a1, "valide");
                commentaireService.createDirect("Article très informatif et utile.", a1, "valide");
            }
            if (a2 != null && a2.getId() != null) {
                commentaireService.createDirect("Information importante sur la résistance bactérienne.", a2, "valide");
            }
            if (a3 != null && a3.getId() != null) {
                commentaireService.createDirect("Merci pour ces conseils pratiques.", a3, "valide");
            }

            System.out.println("✅ Sample data seeded successfully");
        } catch (Exception e) {
            System.err.println("⚠ Error seeding data: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        if (executorService != null) {
            executorService.shutdown();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
