package controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class FrontOfficeMainController {

    @FXML
    private BorderPane mainPane;

    @FXML
    public void initialize() {
        loadVueCatalogue();
    }

    @FXML
    public void showCatalogue() {
        loadVueCatalogue();
    }

    @FXML
    public void showPanier() {
        loadVue("panier");
    }

    @FXML
    public void showHistorique() {
        loadVue("historique");
    }

    private void loadVueCatalogue() {
        loadVue("catalogue");
    }

    private void loadVue(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontoffice/" + fxml + ".fxml"));
            Parent view = loader.load();
            mainPane.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

