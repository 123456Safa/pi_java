package controllers;

import model.Categorie;
import service.CategorieService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import utils.NavigationService;

public class CategorieController {
    private CategorieService categorieService = new CategorieService();
    @FXML private TableView<Categorie> tableCategories;
    @FXML private TableColumn<Categorie, String> colNom;
    @FXML private TableColumn<Categorie, String> colDescription;
    @FXML private TableColumn<Categorie, java.util.Date> colCreatedAt;
    @FXML private TableColumn<Categorie, Void> colActions;
    @FXML private Pagination pagination;
    @FXML private Button btnAjouter;

    @FXML private TextField tfRechercheCategorie;
    @FXML private Button btnRechercherCategorie;
    @FXML private Button btnResetCategorie;
    @FXML private ComboBox<String> cbTriCategorie;
    @FXML private ComboBox<String> cbOrdreCategorie;

    private ObservableList<Categorie> categoriesList = FXCollections.observableArrayList();
    private static final int ITEMS_PER_PAGE = 3;

    @FXML
    public void initialize() {
        try {
            // Configurer les colonnes
            colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNom()));
            colDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
            colCreatedAt.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCreatedAt()));

            // Configurer les ComboBox de tri
            cbTriCategorie.getItems().addAll("Date d'ajout", "Nom", "ID");
            cbTriCategorie.setValue("Date d'ajout");
            cbTriCategorie.setOnAction(e -> appliquerTri());

            cbOrdreCategorie.getItems().addAll("↓ Décroissant", "↑ Croissant");
            cbOrdreCategorie.setValue("↓ Décroissant");
            cbOrdreCategorie.setOnAction(e -> appliquerTri());

            // Ajouter les boutons d'actions
            addActionButtons();

            // Configurer la pagination
            pagination.setPageFactory(this::createPage);

            // Charger les données de la table
            rafraichirTable();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation", "Une erreur est survenue lors de l'initialisation : " + e.getMessage());
        }
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<Categorie, Void>() {
            private final Button btnModifier  = new Button("✏  Modifier");
            private final Button btnSupprimer = new Button("🗑  Supprimer");
            private final HBox box = new HBox(6);

            {
                // Styles
                btnModifier.getStyleClass().addAll("action-btn", "action-btn-edit");
                btnSupprimer.getStyleClass().addAll("action-btn", "action-btn-delete");

                // Tooltips
                btnModifier.setTooltip(new Tooltip("Modifier cette catégorie"));
                btnSupprimer.setTooltip(new Tooltip("Supprimer cette catégorie"));

                box.setAlignment(Pos.CENTER);
                box.getChildren().addAll(btnModifier, btnSupprimer);

                btnModifier.setOnAction(e -> modifierCategorie(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(e -> supprimerCategorie(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
        });
    }

    public void rafraichirTable() {
        try {
            categoriesList.setAll(categorieService.afficher());
            updatePagination();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePagination() {
        int count = categoriesList.size();
        int pageCount = (count / ITEMS_PER_PAGE) + (count % ITEMS_PER_PAGE > 0 ? 1 : 0);
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        updateTableForPage(0);
    }

    private javafx.scene.Node createPage(int pageIndex) {
        updateTableForPage(pageIndex);
        return new Label("");
    }

    private void updateTableForPage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, categoriesList.size());
        
        if (fromIndex < categoriesList.size()) {
            tableCategories.setItems(FXCollections.observableArrayList(categoriesList.subList(fromIndex, toIndex)));
        } else {
            tableCategories.setItems(FXCollections.observableArrayList());
        }
    }

    @FXML
    private void onAjouter(ActionEvent event) {
        try {
            // Charger le fichier FXML du formulaire
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CategorieForm.fxml"));
            BorderPane root = loader.load();

            // Obtenir le contrôleur du formulaire
            CategorieFormController formController = loader.getController();

            // Créer une nouvelle fenêtre (Stage)
            Stage stage = new Stage();
            stage.setTitle("Ajouter une nouvelle catégorie");
            stage.setScene(new Scene(root, 500, 400));
            stage.setResizable(true);

            // Passer la référence de la stage et du contrôleur parent
            formController.setStage(stage);
            formController.setParentController(this);

            // Afficher la fenêtre de manière modale
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la fenêtre du formulaire: " + e.getMessage());
        }
    }

    private void modifierCategorie(Categorie categorie) {
        if (categorie != null) {
            try {
                // Charger le fichier FXML du formulaire
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CategorieForm.fxml"));
                BorderPane root = loader.load();

                // Obtenir le contrôleur du formulaire
                CategorieFormController formController = loader.getController();

                // Passer la catégorie pour édition AVANT de créer la stage
                formController.setCategorieToEdit(categorie);

                // Créer une nouvelle fenêtre (Stage)
                Stage stage = new Stage();
                stage.setTitle("Modifier la catégorie");
                stage.setScene(new Scene(root, 500, 400));
                stage.setResizable(true);

                // Passer la référence de la stage et du contrôleur parent
                formController.setStage(stage);
                formController.setParentController(this);

                // Afficher la fenêtre de manière modale
                stage.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                showError("Erreur", "Impossible d'ouvrir la fenêtre du formulaire: " + e.getMessage());
            }
        }
    }

    private void supprimerCategorie(Categorie categorie) {
        if (categorie != null) {
            if (AlertUtil.showConfirmation("Confirmation", "Êtes-vous sûr de vouloir supprimer la catégorie \"" + categorie.getNom() + "\" ?")) {
                try {
                    categorieService.supprimer(categorie.getId());
                    rafraichirTable();
                    AlertUtil.showSuccess("Succès", "La catégorie a été supprimée avec succès.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    AlertUtil.showError("Erreur", "Une erreur est survenue: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void onRechercherCategorie(ActionEvent event) {
        try {
            String recherche = tfRechercheCategorie.getText().trim().toLowerCase();

            if (recherche.isEmpty()) {
                rafraichirTable();
                return;
            }

            List<Categorie> toutesCategories = categorieService.afficher();
            ObservableList<Categorie> resultatRecherche = FXCollections.observableArrayList();

            for (Categorie cat : toutesCategories) {
                if (cat.getNom().toLowerCase().contains(recherche) ||
                    cat.getDescription().toLowerCase().contains(recherche)) {
                    resultatRecherche.add(cat);
                }
            }

            categoriesList.setAll(resultatRecherche);
            updatePagination();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    @FXML
    private void onResetRechercheCategorie(ActionEvent event) {
        tfRechercheCategorie.clear();
        cbTriCategorie.setValue("Date d'ajout");
        cbOrdreCategorie.setValue("↓ Décroissant");
        rafraichirTable();
    }

    private void appliquerTri() {
        try {
            String triOption = cbTriCategorie.getValue();
            String ordreOption = cbOrdreCategorie.getValue();
            boolean estCroissant = ordreOption != null && ordreOption.startsWith("↑");
            
            List<Categorie> categories = new ArrayList<>(categoriesList);
            
            // Trier selon l'option sélectionnée
            if ("Date d'ajout".equals(triOption)) {
                if (estCroissant) {
                    categories.sort((c1, c2) -> {
                        if (c1.getCreatedAt() == null || c2.getCreatedAt() == null) return 0;
                        return c1.getCreatedAt().compareTo(c2.getCreatedAt());
                    });
                } else {
                    categories.sort((c1, c2) -> {
                        if (c1.getCreatedAt() == null || c2.getCreatedAt() == null) return 0;
                        return c2.getCreatedAt().compareTo(c1.getCreatedAt());
                    });
                }
            } else if ("Nom".equals(triOption)) {
                if (estCroissant) {
                    categories.sort((c1, c2) -> c1.getNom().compareTo(c2.getNom()));
                } else {
                    categories.sort((c1, c2) -> c2.getNom().compareTo(c1.getNom()));
                }
            } else if ("ID".equals(triOption)) {
                if (estCroissant) {
                    categories.sort((c1, c2) -> Integer.compare(c1.getId(), c2.getId()));
                } else {
                    categories.sort((c1, c2) -> Integer.compare(c2.getId(), c1.getId()));
                }
            }
            
            categoriesList.setAll(categories);
            updatePagination();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.show();
    }

    @FXML
    private void onLinkProduits(ActionEvent event) {
        // Navigation vers la vue Produits
        NavigationService.getInstance().navigateByName("Produits");
    }

    @FXML
    private void onLinkCategories(ActionEvent event) {
        // Navigation vers la vue Catégories
        NavigationService.getInstance().navigateByName("Catégories");
    }

    @FXML
    private void onLinkDashboard(ActionEvent event) {
        // Navigation vers la vue Dashboard
        NavigationService.getInstance().navigateByName("Dashboard");
    }
}
