package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import models.User;
import utils.SessionManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AppShellController — Top-level router between Front Office and Back Office.
 *
 * Front Office: /gestion/accueil-front.fxml  (pharmacy landing + blog)
 * Back  Office: /main.fxml                   (4-module admin dashboard)
 */
public class AppShellController {

    private static final String FRONT_OFFICE = "frontoffice";
    private static final String BACK_OFFICE  = "backoffice";

    private static AppShellController instance;

    private final Map<String, ModuleDefinition> modules = new LinkedHashMap<>();

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        // Front office: beautiful pharmacy landing with products + blog link
        modules.put(FRONT_OFFICE, new ModuleDefinition("/gestion/accueil-front.fxml", "Front Office"));
        // Back office: sidebar with Commandes + Produits + Réclamations + Blog
        modules.put(BACK_OFFICE, new ModuleDefinition("/main.fxml", "Back Office"));

        instance = this;
        showFrontOffice();
    }

    @FXML
    public void showFrontOffice() {
        loadModule(FRONT_OFFICE);
    }

    /**
     * RBAC-guarded: only users with ROLE_ADMIN can access the Back Office.
     */
    @FXML
    public void showBackOffice() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isAdmin()) {
            loadModule(BACK_OFFICE);
        } else {
            System.err.println("⛔ Access Denied: User '" 
                + (currentUser != null ? currentUser.getEmail() : "null") 
                + "' is not an admin.");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Accès Refusé");
            alert.setHeaderText("Espace Réservé aux Administrateurs");
            alert.setContentText("Vous n'avez pas les permissions nécessaires pour accéder au Back-Office.");
            alert.showAndWait();
        }
    }

    private void loadModule(String key) {
        ModuleDefinition module = modules.get(key);
        if (module == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(module.fxmlPath()));
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ AppShell: Error loading module '" + key + "': " + e.getMessage());
        }
    }

    public static AppShellController getInstance() {
        return instance;
    }

    private record ModuleDefinition(String fxmlPath, String title) {}
}
