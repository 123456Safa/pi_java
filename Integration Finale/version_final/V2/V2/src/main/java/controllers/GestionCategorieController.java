package controllers;

import models.Categorie;
import services.CategorieService;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class GestionCategorieController {
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
            colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNom()));
            colDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
            colCreatedAt.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCreatedAt()));

            cbTriCategorie.getItems().addAll("Date d'ajout", "Nom", "ID");
            cbTriCategorie.setValue("Date d'ajout");
            cbTriCategorie.setOnAction(e -> appliquerTri());

            cbOrdreCategorie.getItems().addAll("Décroissant", "Croissant");
            cbOrdreCategorie.setValue("Décroissant");
            cbOrdreCategorie.setOnAction(e -> appliquerTri());

            addActionButtons();
            rafraichirTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<Categorie, Void>() {
            private final Button btnModifier  = new Button("✏  Modifier");
            private final Button btnSupprimer = new Button("🗑  Supprimer");
            private final HBox box = new HBox(6);

            {
                btnModifier.getStyleClass().addAll("action-btn", "action-btn-edit");
                btnSupprimer.getStyleClass().addAll("action-btn", "action-btn-delete");
                box.setAlignment(Pos.CENTER);
                box.getChildren().addAll(btnModifier, btnSupprimer);

                btnModifier.setOnAction(e -> modifierCategorie(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(e -> supprimerCategorie(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
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
        pagination.setPageFactory(pageIndex -> {
            updateTableForPage(pageIndex);
            return new Label("");
        });
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/categorie-form.fxml"));
            javafx.scene.layout.BorderPane root = loader.load();
            GestionCategorieFormController formController = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Ajouter une catégorie");
            stage.setScene(new Scene(root, 500, 400));
            formController.setStage(stage);
            formController.setParentController(this);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void modifierCategorie(Categorie categorie) {
        if (categorie == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/categorie-form.fxml"));
            javafx.scene.layout.BorderPane root = loader.load();
            GestionCategorieFormController formController = loader.getController();
            formController.setCategorieToEdit(categorie);
            Stage stage = new Stage();
            stage.setTitle("Modifier la catégorie");
            stage.setScene(new Scene(root, 500, 400));
            formController.setStage(stage);
            formController.setParentController(this);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void supprimerCategorie(Categorie categorie) {
        if (categorie == null) return;
        if (AlertUtil.showConfirmation("Confirmation", "Supprimer la catégorie \"" + categorie.getNom() + "\" ?")) {
            try {
                categorieService.supprimer(categorie.getId());
                rafraichirTable();
                AlertUtil.showSuccess("Succès", "Catégorie supprimée avec succès.");
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onRechercherCategorie(ActionEvent event) {
        try {
            String recherche = tfRechercheCategorie.getText().trim().toLowerCase();
            List<Categorie> toutes = categorieService.afficher();
            ObservableList<Categorie> resultats = FXCollections.observableArrayList();
            for (Categorie cat : toutes) {
                if (recherche.isEmpty() ||
                    cat.getNom().toLowerCase().contains(recherche) ||
                    (cat.getDescription() != null && cat.getDescription().toLowerCase().contains(recherche))) {
                    resultats.add(cat);
                }
            }
            categoriesList.setAll(resultats);
            updatePagination();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onResetRechercheCategorie(ActionEvent event) {
        tfRechercheCategorie.clear();
        cbTriCategorie.setValue("Date d'ajout");
        cbOrdreCategorie.setValue("Décroissant");
        rafraichirTable();
    }

    private void appliquerTri() {
        String triOption = cbTriCategorie.getValue();
        String ordreOption = cbOrdreCategorie.getValue();
        if (triOption == null) return;
        boolean estCroissant = ordreOption != null && ordreOption.startsWith("Croi");

        List<Categorie> cats = new ArrayList<>(categoriesList);
        if ("Nom".equals(triOption)) {
            cats.sort((c1, c2) -> estCroissant ? c1.getNom().compareTo(c2.getNom()) : c2.getNom().compareTo(c1.getNom()));
        } else if ("ID".equals(triOption)) {
            cats.sort((c1, c2) -> estCroissant ? Integer.compare(c1.getId(), c2.getId()) : Integer.compare(c2.getId(), c1.getId()));
        }
        categoriesList.setAll(cats);
        updatePagination();
    }
}
