package controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
public class SidebarController {
    @FXML
    private Button btnProduits;
    @FXML
    private Button btnCategories;
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnVoirProduits;
    private static BorderPane mainLayout;
    private static VBox sidebar;

    public static void setMainLayout(BorderPane layout) {
        mainLayout = layout;
    }

    public static void setSidebar(VBox sidebarPane) {
        sidebar = sidebarPane;
    }

    private void loadContent(String fxmlPath) {
        if (mainLayout != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Node content = loader.load();
                mainLayout.setCenter(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadContentFullscreen(String fxmlPath) {
        if (mainLayout != null && sidebar != null) {
            try {
                // Masquer la sidebar
                sidebar.setManaged(false);
                sidebar.setVisible(false);

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Node content = loader.load();
                mainLayout.setCenter(content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showSidebar() {
        if (sidebar != null) {
            sidebar.setManaged(true);
            sidebar.setVisible(true);
        }
    }

    @FXML
    public void onProduits(ActionEvent event) {
        showSidebar();
        loadContent("/Produit.fxml");
    }
    @FXML
    public void onCategories(ActionEvent event) {
        showSidebar();
        loadContent("/Categorie.fxml");
    }
    @FXML
    public void onDashboard(ActionEvent event) {
        showSidebar();
        loadContent("/Dashboard.fxml");
    }
    @FXML
    public void onVoirProduits(ActionEvent event) {
        loadContentFullscreen("/AccueilFront.fxml");
    }
}