package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import models.Commandes;

public class MainController implements OrderBackofficeNavigator {

    @FXML
    private StackPane contentPane;
    @FXML
    private AppBarController appBarController;
    @FXML
    private Button dashboardButton;
    @FXML
    private Button commandesButton;
    @FXML
    private Button gestionProduitsButton;
    @FXML
    private Button gestionCategoriesButton;
    @FXML
    private Button gestionDashboardButton;
    @FXML
    private Button reclamationsAdminButton;

    @FXML
    public void initialize() {
        showDashboard();
    }

    @FXML
    public void showDashboard() {
        loadView("/backoffice/dashboard.fxml", "Dashboard Commandes", "Tableau de bord / Vue generale", dashboardButton, controller -> {
            if (controller instanceof DashboardController dashboardController) {
                dashboardController.refresh();
            }
        });
    }

    @FXML
    public void showCommandes() {
        loadView("/backoffice/orders-management.fxml", "Gestion des Commandes", "Tableau de bord / Commandes", commandesButton, controller -> {
            if (controller instanceof OrdersManagementController ordersController) {
                ordersController.setNavigator(this);
                ordersController.refresh();
            }
        });
    }

    @FXML
    public void showGestionProduits() {
        loadView("/gestion/produits.fxml", "Gestion des Produits", "Tableau de bord / Produits", gestionProduitsButton, controller -> {});
    }

    @FXML
    public void showGestionCategories() {
        loadView("/gestion/categories.fxml", "Gestion des Catégories", "Tableau de bord / Catégories", gestionCategoriesButton, controller -> {});
    }

    @FXML
    public void showGestionDashboard() {
        loadView("/gestion/dashboard.fxml", "Dashboard Produits", "Tableau de bord / Produits / Vue générale", gestionDashboardButton, controller -> {});
    }

    @FXML
    public void showReclamationsAdmin() {
        loadView("/reclamation/home-admin.fxml", "Réclamations", "Tableau de bord / Réclamations", reclamationsAdminButton, controller -> {
            if (controller instanceof HomeAdminController c) c.refresh();
        });
    }

    @Override
    public void showOrderDetails(Commandes commande) {
        loadView("/backoffice/order-details.fxml",
                "Details de la Commande",
                "Tableau de bord / Commandes / #" + commande.getId(),
                null,
                controller -> {
                    if (controller instanceof OrderDetailsController detailsController) {
                        detailsController.setNavigator(this);
                        detailsController.setCommande(commande);
                    }
                });
        updateActiveMenu(commandesButton);
    }

    private void loadView(String resource,
                          String title,
                          String subtitle,
                          Button activeButton,
                          ControllerInitializer initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Parent view = loader.load();
            Object controller = loader.getController();
            if (initializer != null) {
                initializer.initialize(controller);
            }
            contentPane.getChildren().setAll(view);
            appBarController.setSection(title, subtitle);
            updateActiveMenu(activeButton);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateActiveMenu(Button activeButton) {
        Button[] buttons = {dashboardButton, commandesButton, gestionProduitsButton, gestionCategoriesButton, gestionDashboardButton, reclamationsAdminButton};
        for (Button button : buttons) {
            if (button != null) {
                button.getStyleClass().remove("sidebar-button-active");
            }
        }
        if (activeButton != null && !activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    @FXML
    private void handlePharmaXLabelClick(MouseEvent event) {
        // Aller au frontoffice en cliquant sur le logo pharmaX
        AppShellController shellController = AppShellController.getInstance();
        if (shellController != null) {
            shellController.showFrontOffice();
        }
    }

    @FunctionalInterface
    private interface ControllerInitializer {
        void initialize(Object controller);
    }
}
