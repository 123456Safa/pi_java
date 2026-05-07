package controllers;

import com.pharmax.controller.AdminBlogController;
import com.pharmax.controller.AdminCommentController;
import com.pharmax.controller.FrontBlogController;
import com.pharmax.controller.ScraperController;
import com.pharmax.service.ArticleService;
import com.pharmax.service.CommentModerationService;
import com.pharmax.service.CommentValidationService;
import com.pharmax.service.CommentaireService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import models.Commandes;
import models.User;
import utils.SessionManager;

/**
 * MainController — Back-Office navigation hub.
 *
 * Manages 4 management modules:
 *   1. Commandes  (Dashboard + Orders list + Order details)
 *   2. Produits   (Dashboard + Products + Categories)
 *   3. Réclamations (Admin view)
 *   4. Blog       (Admin Articles + Admin Comments + Public Blog + News Scraper)
 */
public class MainController implements OrderBackofficeNavigator {

    // ── FXML injected ──────────────────────────────────────────────────────────
    @FXML private StackPane contentPane;
    @FXML private AppBarController appBarController;

    // Commandes sidebar buttons
    @FXML private Button dashboardButton;
    @FXML private Button commandesButton;

    // Produits sidebar buttons
    @FXML private Button gestionProduitsButton;
    @FXML private Button gestionCategoriesButton;
    @FXML private Button gestionDashboardButton;

    // Réclamations sidebar button
    @FXML private Button reclamationsAdminButton;

    // Utilisateurs sidebar buttons
    @FXML private Button usersDirectoryButton;
    @FXML private Button auditLogsButton;
    @FXML private Button monProfilAdminButton;

    // Blog sidebar buttons
    @FXML private Button blogArticlesButton;
    @FXML private Button blogCommentsButton;
    @FXML private Button blogPublicButton;
    @FXML private Button blogScraperButton;

    // ── Lazy-loaded Blog services ──────────────────────────────────────────────
    private ArticleService     blogArticleService;
    private CommentaireService blogCommentService;

    // ── Lazy-loaded Blog controllers (cached after first use) ──────────────────
    private AdminBlogController    adminBlogController;
    private AdminCommentController adminCommentController;
    private FrontBlogController    frontBlogController;
    private ScraperController      scraperController;

    // ── Cached blog views ──────────────────────────────────────────────────────
    private Region adminBlogView;
    private Region adminCommentView;
    private Region frontBlogView;
    private Region scraperView;

    // ── User module lazy-loaded instances ──────────────────────────────────────
    private controllers.UserController userController;
    private Views.AdminDashboardView   adminDashboardView;
    private Views.AdminLogsView        adminLogsView;
    private Views.UserProfileView      userProfileView;
    private Region                     usersDirectoryRegion;
    private Region                     auditLogsRegion;
    private Region                     userProfileRegion;

    // ══════════════════════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void initialize() {
        showDashboard();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MODULE 1 — COMMANDES
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void showDashboard() {
        loadFxmlView("/backoffice/dashboard.fxml",
                "Dashboard Commandes", "Tableau de bord / Vue générale",
                dashboardButton, controller -> {
                    if (controller instanceof DashboardController dc) dc.refresh();
                });
    }

    @FXML
    public void showCommandes() {
        loadFxmlView("/backoffice/orders-management.fxml",
                "Gestion des Commandes", "Tableau de bord / Commandes",
                commandesButton, controller -> {
                    if (controller instanceof OrdersManagementController oc) {
                        oc.setNavigator(this);
                        oc.refresh();
                    }
                });
    }

    @Override
    public void showOrderDetails(Commandes commande) {
        loadFxmlView("/backoffice/order-details.fxml",
                "Détails de la Commande",
                "Tableau de bord / Commandes / #" + commande.getId(),
                null, controller -> {
                    if (controller instanceof OrderDetailsController dc) {
                        dc.setNavigator(this);
                        dc.setCommande(commande);
                    }
                });
        updateActiveMenu(commandesButton);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MODULE 2 — PRODUITS & CATÉGORIES
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void showGestionDashboard() {
        loadFxmlView("/gestion/dashboard.fxml",
                "Dashboard Produits", "Tableau de bord / Produits / Vue générale",
                gestionDashboardButton, controller -> {});
    }

    @FXML
    public void showGestionProduits() {
        loadFxmlView("/gestion/produits.fxml",
                "Gestion des Produits", "Tableau de bord / Produits",
                gestionProduitsButton, controller -> {});
    }

    @FXML
    public void showGestionCategories() {
        loadFxmlView("/gestion/categories.fxml",
                "Gestion des Catégories", "Tableau de bord / Catégories",
                gestionCategoriesButton, controller -> {});
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MODULE 3 — RÉCLAMATIONS
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void showReclamationsAdmin() {
        loadFxmlView("/reclamation/home-admin.fxml",
                "Réclamations", "Tableau de bord / Réclamations",
                reclamationsAdminButton, controller -> {
                    if (controller instanceof HomeAdminController c) c.refresh();
                });
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MODULE 5 — UTILISATEURS
    // ══════════════════════════════════════════════════════════════════════════

    private controllers.UserController getUserController() {
        if (userController == null) {
            userController = new controllers.UserController();
        }
        return userController;
    }

    @FXML
    public void showUsersDirectory() {
        updateActiveMenu(usersDirectoryButton);
        setAppBarSection("Users Directory", "Tableau de bord / Utilisateurs");
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            // Rebuild each time so data is always fresh
            adminDashboardView = new Views.AdminDashboardView(
                getUserController(), currentUser,
                this::logout, // onLogout
                this::showMonProfil,
                this::showAuditLogs,
                this::showUserCreate,
                this::showUserEdit,
                () -> {} // onShowHome — not needed in unified sidebar
            );
            usersDirectoryRegion = adminDashboardView.buildView();
            contentPane.getChildren().setAll(usersDirectoryRegion);
        } catch (Exception e) {
            showError("Erreur chargement Users Directory", e);
        }
    }

    @FXML
    public void showAuditLogs() {
        updateActiveMenu(auditLogsButton);
        setAppBarSection("Audit Logs", "Tableau de bord / Utilisateurs / Audit Logs");
        try {
            adminLogsView = new Views.AdminLogsView(
                this::showUsersDirectory,
                this::showMonProfil,
                this::showAuditLogs,
                this::logout, // onLogout
                () -> {}  // onShowHome
            );
            auditLogsRegion = adminLogsView.buildView();
            contentPane.getChildren().setAll(auditLogsRegion);
        } catch (Exception e) {
            showError("Erreur chargement Audit Logs", e);
        }
    }

    @FXML
    public void showMonProfil() {
        updateActiveMenu(monProfilAdminButton);
        setAppBarSection("Mon Profil", "Tableau de bord / Utilisateurs / Profil");
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            services.FaceAuthService faceAuthService = new services.FaceAuthService();
            userProfileView = new Views.UserProfileView(
                getUserController(), faceAuthService, currentUser,
                this::showUsersDirectory,
                this::showMonProfil,
                this::showAuditLogs,
                this::logout, // onLogout
                () -> {}  // onShowHome
            );
            userProfileRegion = userProfileView.buildView();
            contentPane.getChildren().setAll(userProfileRegion);
        } catch (Exception e) {
            showError("Erreur chargement Profil", e);
        }
    }

    private void showUserCreate() {
        setAppBarSection("Create User", "Tableau de bord / Utilisateurs / Nouveau");
        try {
            Views.UserCreateView createView = new Views.UserCreateView(
                getUserController(), this::showUsersDirectory, this::showUsersDirectory
            );
            contentPane.getChildren().setAll(createView.buildView());
        } catch (Exception e) {
            showError("Erreur chargement Create User", e);
        }
    }

    private void showUserEdit(User target) {
        setAppBarSection("Edit User", "Tableau de bord / Utilisateurs / Modifier");
        try {
            Views.UserEditView editView = new Views.UserEditView(
                getUserController(), target, this::showUsersDirectory, this::showUsersDirectory
            );
            contentPane.getChildren().setAll(editView.buildView());
        } catch (Exception e) {
            showError("Erreur chargement Edit User", e);
        }
    }

    private void logout() {
        org.example.Main mainApp = org.example.Main.getInstance();
        if (mainApp != null) {
            mainApp.logout();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MODULE 4 — BLOG & ARTICLES
    // ══════════════════════════════════════════════════════════════════════════

    @FXML
    public void showBlogArticles() {
        updateActiveMenu(blogArticlesButton);
        setAppBarSection("Gestion Articles Blog", "Tableau de bord / Blog / Articles");
        try {
            initBlogServices();
            if (adminBlogController == null) {
                adminBlogController = new AdminBlogController(blogArticleService);
                adminBlogView = adminBlogController.buildView();
            }
            contentPane.getChildren().setAll(wrapBlogView(adminBlogView));
            adminBlogController.refresh();
        } catch (Exception e) {
            showError("Erreur chargement Blog Articles", e);
        }
    }

    @FXML
    public void showBlogComments() {
        updateActiveMenu(blogCommentsButton);
        setAppBarSection("Gestion Commentaires Blog", "Tableau de bord / Blog / Commentaires");
        try {
            initBlogServices();
            if (adminCommentController == null) {
                adminCommentController = new AdminCommentController(blogCommentService, blogArticleService);
                adminCommentView = adminCommentController.buildView();
            }
            contentPane.getChildren().setAll(wrapBlogView(adminCommentView));
            adminCommentController.refresh();
        } catch (Exception e) {
            showError("Erreur chargement Blog Commentaires", e);
        }
    }

    @FXML
    public void showBlogPublic() {
        System.out.println("DEBUG: MainController.showBlogPublic() called - BACKOFFICE BLOG");
        updateActiveMenu(blogPublicButton);
        setAppBarSection("Blog Public PharmaX", "Tableau de bord / Blog / Vue publique");
        try {
            initBlogServices();
            if (frontBlogController == null) {
                frontBlogController = new FrontBlogController(blogArticleService, blogCommentService);
                frontBlogView = frontBlogController.buildView();
            }
            contentPane.getChildren().setAll(wrapBlogView(frontBlogView));
            frontBlogController.refresh();
        } catch (Exception e) {
            showError("Erreur chargement Blog Public", e);
        }
    }

    @FXML
    public void showBlogScraper() {
        updateActiveMenu(blogScraperButton);
        setAppBarSection("Scraper News", "Tableau de bord / Blog / Scraper");
        try {
            if (scraperController == null) {
                scraperController = new ScraperController();
                scraperView = scraperController.buildView();
            }
            contentPane.getChildren().setAll(wrapBlogView(scraperView));
        } catch (Exception e) {
            showError("Erreur chargement Scraper", e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /** Lazy-initialize blog services (only once). */
    private void initBlogServices() {
        if (blogArticleService == null) {
            blogArticleService = new ArticleService();
        }
        if (blogCommentService == null) {
            blogCommentService = new CommentaireService(
                    new CommentValidationService(),
                    new CommentModerationService(),
                    blogArticleService
            );
        }
    }

    /**
     * Wraps a programmatic blog view in a VBox so it fills the content pane properly.
     */
    private Region wrapBlogView(Region view) {
        VBox wrapper = new VBox(view);
        VBox.setVgrow(view, Priority.ALWAYS);
        wrapper.setFillWidth(true);
        return wrapper;
    }

    /**
     * Load an FXML view into the content pane.
     */
    private void loadFxmlView(String resource,
                              String title,
                              String subtitle,
                              Button activeButton,
                              ControllerInitializer initializer) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(resource));
            javafx.scene.Parent view = loader.load();
            Object controller = loader.getController();
            if (initializer != null) initializer.initialize(controller);
            contentPane.getChildren().setAll(view);
            setAppBarSection(title, subtitle);
            updateActiveMenu(activeButton);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur chargement vue: " + resource, e);
        }
    }

    private void setAppBarSection(String title, String subtitle) {
        if (appBarController != null) {
            appBarController.setSection(title, subtitle);
        }
    }

    private void updateActiveMenu(Button activeButton) {
        Button[] buttons = {
            dashboardButton, commandesButton,
            gestionProduitsButton, gestionCategoriesButton, gestionDashboardButton,
            reclamationsAdminButton,
            blogArticlesButton, blogCommentsButton, blogPublicButton, blogScraperButton,
            usersDirectoryButton, auditLogsButton, monProfilAdminButton
        };
        for (Button btn : buttons) {
            if (btn != null) btn.getStyleClass().remove("sidebar-button-active");
        }
        if (activeButton != null && !activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    private void showError(String message, Exception e) {
        javafx.scene.control.Label label = new javafx.scene.control.Label("⚠ " + message + "\n" + e.getMessage());
        label.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14; -fx-padding: 30;");
        contentPane.getChildren().setAll(label);
    }

    @FXML
    private void handlePharmaXLabelClick(MouseEvent event) {
        AppShellController shell = AppShellController.getInstance();
        if (shell != null) shell.showFrontOffice();
    }

    @FunctionalInterface
    private interface ControllerInitializer {
        void initialize(Object controller);
    }
}
