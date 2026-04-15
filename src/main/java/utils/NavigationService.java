package utils;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * Service pour gérer la navigation entre les onglets
 */
public class NavigationService {
    private static NavigationService instance;
    private static TabPane tabPane;

    private NavigationService() {
    }

    public static NavigationService getInstance() {
        if (instance == null) {
            instance = new NavigationService();
        }
        return instance;
    }

    public static void setTabPane(TabPane tabs) {
        tabPane = tabs;
    }

    /**
     * Navigue vers l'onglet spécifié
     * @param tabIndex l'index de l'onglet (0 = Accueil, 1 = Produits, 2 = Catégories, 3 = Dashboard)
     */
    public void navigateTo(int tabIndex) {
        if (tabPane != null && tabIndex >= 0 && tabIndex < tabPane.getTabs().size()) {
            tabPane.getSelectionModel().select(tabIndex);
        }
    }

    /**
     * Navigue vers l'onglet par son nom
     * @param tabName le nom de l'onglet
     */
    public void navigateByName(String tabName) {
        if (tabPane != null) {
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getText().equals(tabName)) {
                    tabPane.getSelectionModel().select(tab);
                    return;
                }
            }
        }
    }
}

