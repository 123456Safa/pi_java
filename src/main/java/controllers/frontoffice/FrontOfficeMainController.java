package controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import models.PanierItem;
import services.PanierService;
import controllers.AppShellController;

public class FrontOfficeMainController {

    @FXML
    private BorderPane mainPane;
    @FXML
    private Button catalogueButton;
    @FXML
    private Button panierButton;
    @FXML
    private Button historiqueButton;
    @FXML
    private Label panierBadge;
    @FXML
    private Label pharmaxLabel;

    @FXML
    public void initialize() {
        updatePanierBadge();
        PanierService.getInstance().getPanier().addListener((javafx.collections.ListChangeListener<PanierItem>) change -> {
            updatePanierBadge();
        });
        loadVueCatalogue();
    }

    private void updatePanierBadge() {
        if (panierBadge != null) {
            int count = PanierService.getInstance().getPanier().size();
            panierBadge.setText(String.valueOf(count));
            panierBadge.setVisible(count > 0);
        }
    }

    @FXML
    public void showCatalogue() {
        loadVueCatalogue();
    }

    @FXML
    public void showPanier() {
        loadVue("panier", panierButton);
    }

    @FXML
    public void showHistorique() {
        loadVue("historique", historiqueButton);
    }

    private void loadVueCatalogue() {
        loadVue("catalogue", catalogueButton);
    }

    private void loadVue(String fxml, Button activeButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/" + fxml + ".fxml"));
            Parent view = loader.load();
            Object controller = loader.getController();
            
            // Appel optionnel à setOnContinuerAchats si c'est le PanierController
            if (controller != null && "PanierController".equals(controller.getClass().getSimpleName())) {
                try {
                    controller.getClass().getMethod("setOnContinuerAchats", Runnable.class)
                            .invoke(controller, (Runnable) this::showCatalogue);
                } catch (Exception ex) {
                    // Ignorer silencieusement
                }
            }
            
            mainPane.setCenter(view);
            updateActiveButton(activeButton);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERREUR CHARGEMENT VUE: " + fxml);
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur de chargement");
            alert.setHeaderText("Impossible de charger la vue : " + fxml);
            alert.setContentText(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            alert.show();
        }
    }

    private void updateActiveButton(Button activeButton) {
        Button[] buttons = {catalogueButton, panierButton, historiqueButton};
        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("fo-nav-button-active");
            }
        }
        if (activeButton != null && !activeButton.getStyleClass().contains("fo-nav-button-active")) {
            activeButton.getStyleClass().add("fo-nav-button-active");
        }
    }

    @FXML
    private void handlePharmaxLabelClick(MouseEvent event) {
        // Aller au backoffice en cliquant sur le logo PHARMAX
        AppShellController shellController = AppShellController.getInstance();
        if (shellController != null) {
            shellController.showBackOffice();
        }
    }
}
