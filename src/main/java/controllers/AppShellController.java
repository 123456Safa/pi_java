package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.util.LinkedHashMap;
import java.util.Map;

public class AppShellController {
    private static final String FRONT_OFFICE = "frontoffice";
    private static final String BACK_OFFICE = "backoffice";

    // Référence statique pour accéder depuis les contrôleurs enfants
    private static AppShellController instance;

    // Map pour stocker les définitions des modules
    private final Map<String, ModuleDefinition> modules = new LinkedHashMap<>();

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        modules.put(FRONT_OFFICE, new ModuleDefinition("/frontoffice/main.fxml", "Front Office", null));
        instance = this; // Stocker la référence
        modules.put(BACK_OFFICE, new ModuleDefinition("/main.fxml", "Back Office", null));
        showFrontOffice();
    }

    @FXML
    public void showFrontOffice() {
        loadModule(FRONT_OFFICE);
    }

    @FXML
    public void showBackOffice() {
        loadModule(BACK_OFFICE);
    }

    private void loadModule(String key) {
        ModuleDefinition module = modules.get(key);
        if (module == null) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(module.fxmlPath()));
            Parent view = loader.load();
            contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static AppShellController getInstance() {
        return instance;
    }
    private record ModuleDefinition(String fxmlPath, String title, Button button) {
    }
}
