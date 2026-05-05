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
    private Button frontOfficeButton;
    @FXML
    private Button backOfficeButton;

    @FXML
    public void initialize() {
        modules.put(FRONT_OFFICE, new ModuleDefinition("/frontoffice/main.fxml", "Front Office", frontOfficeButton));
        modules.put(BACK_OFFICE, new ModuleDefinition("/main.fxml", "Back Office", backOfficeButton));
        instance = this; // Stocker la référence
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
            updateActiveButton(module.button());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateActiveButton(Button activeButton) {
        Button[] buttons = {frontOfficeButton, backOfficeButton};
        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("module-button-active");
            }
        }
        if (activeButton != null && !activeButton.getStyleClass().contains("module-button-active")) {
            activeButton.getStyleClass().add("module-button-active");
        }
    }

    public static AppShellController getInstance() {
        return instance;
    }
    private record ModuleDefinition(String fxmlPath, String title, Button button) {
    }
}
