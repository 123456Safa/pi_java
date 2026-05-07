package controllers;

import models.Produit;
import models.Categorie;
import services.ProduitService;
import services.CategorieService;
import java.io.IOException;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class GestionProduitController {
    private ProduitService produitService = new ProduitService();
    private CategorieService categorieService = new CategorieService();
    private java.util.Map<Integer, String> categorieMap = new java.util.HashMap<>();

    @FXML private TableView<Produit> tableProduits;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, String> colDescription;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, String> colImage;
    @FXML private TableColumn<Produit, java.util.Date> colDateExpiration;
    @FXML private TableColumn<Produit, String> colStatut;
    @FXML private TableColumn<Produit, Integer> colQuantite;
    @FXML private TableColumn<Produit, String> colCategorieId;
    @FXML private TableColumn<Produit, Double> colPrixFinal;
    @FXML private TableColumn<Produit, String> colPromoCode;
    @FXML private TableColumn<Produit, Void> colActions;
    @FXML private Pagination pagination;
    @FXML private Button btnAjouter;
    @FXML private TextField tfRechercheProduit;
    @FXML private ComboBox<Categorie> cbCategorieProduit;
    @FXML private Button btnRechercherProduit;
    @FXML private Button btnResetProduit;
    @FXML private ComboBox<String> cbTriProduit;
    @FXML private ComboBox<String> cbOrdreProduit;

    private ObservableList<Produit> produitsList = FXCollections.observableArrayList();
    private static final int ITEMS_PER_PAGE = 3;

    @FXML
    public void initialize() {
        try {
            colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNom()));
            colDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
            colPrix.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrix()).asObject());
            colDateExpiration.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDateExpiration()));
            colStatut.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut()));
            colQuantite.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQuantite()).asObject());
            colPrixFinal.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrixFinal()).asObject());
            colPromoCode.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPromoCode() != null ? data.getValue().getPromoCode() : "-"
            ));
            colCategorieId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                categorieMap.getOrDefault(data.getValue().getCategorieId(), "N/A")
            ));

            colImage.setCellFactory(col -> new TableCell<Produit, String>() {
                private final ImageView imageView = new ImageView();
                {
                    imageView.setFitHeight(60);
                    imageView.setFitWidth(60);
                    imageView.setPreserveRatio(true);
                }
                @Override
                protected void updateItem(String imagePath, boolean empty) {
                    super.updateItem(imagePath, empty);
                    if (empty || imagePath == null || imagePath.isEmpty()) {
                        setGraphic(null);
                    } else {
                        try {
                            File file = new File(imagePath);
                            if (file.exists()) {
                                imageView.setImage(new Image(file.toURI().toString(), 60, 60, true, true));
                                setGraphic(imageView);
                            } else {
                                setGraphic(null);
                                setText("N/A");
                            }
                        } catch (Exception e) {
                            setGraphic(null);
                            setText("N/A");
                        }
                    }
                }
            });
            colImage.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getImage()));

            addActionButtons();
            chargerCategories();

            cbTriProduit.getItems().addAll("Date d'ajout", "Nom", "Prix", "ID");
            cbTriProduit.setValue("Date d'ajout");
            cbTriProduit.setOnAction(e -> appliquerTri());

            cbOrdreProduit.getItems().addAll("↓ Décroissant", "↑ Croissant");
            cbOrdreProduit.setValue("↓ Décroissant");
            cbOrdreProduit.setOnAction(e -> appliquerTri());

            pagination.setPageFactory(this::createPage);
            rafraichirTable();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation", e.getMessage());
        }
    }

    private void addActionButtons() {
        colActions.setCellFactory(col -> new TableCell<Produit, Void>() {
            private final Button btnDetail   = new Button("👁  Voir");
            private final Button btnModifier  = new Button("✏  Modifier");
            private final Button btnSupprimer = new Button("🗑  Supprimer");
            private final HBox box = new HBox(6);

            {
                btnDetail.getStyleClass().addAll("action-btn", "action-btn-detail");
                btnModifier.getStyleClass().addAll("action-btn", "action-btn-edit");
                btnSupprimer.getStyleClass().addAll("action-btn", "action-btn-delete");
                box.setAlignment(Pos.CENTER);
                box.getChildren().addAll(btnDetail, btnModifier, btnSupprimer);

                btnDetail.setOnAction(e -> afficherDetailProduit(getTableView().getItems().get(getIndex())));
                btnModifier.setOnAction(e -> modifierProduit(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(e -> supprimerProduit(getTableView().getItems().get(getIndex())));
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
            produitsList.setAll(produitService.afficher());
            updatePagination();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePagination() {
        int count = produitsList.size();
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
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, produitsList.size());
        if (fromIndex < produitsList.size()) {
            tableProduits.setItems(FXCollections.observableArrayList(produitsList.subList(fromIndex, toIndex)));
        } else {
            tableProduits.setItems(FXCollections.observableArrayList());
        }
    }

    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieService.afficher();
            cbCategorieProduit.getItems().clear();
            cbCategorieProduit.getItems().add(null);
            cbCategorieProduit.getItems().addAll(categories);
            categorieMap.clear();
            for (Categorie cat : categories) {
                categorieMap.put(cat.getId(), cat.getNom());
            }
            cbCategorieProduit.setCellFactory(param -> new ListCell<Categorie>() {
                @Override
                protected void updateItem(Categorie item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "Toutes les catégories" : item.getNom());
                }
            });
            cbCategorieProduit.setButtonCell(new ListCell<Categorie>() {
                @Override
                protected void updateItem(Categorie item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "Toutes les catégories" : item.getNom());
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/produit-form.fxml"));
            BorderPane root = loader.load();
            GestionProduitFormController formController = loader.getController();
            Stage stage = new Stage();
            stage.setTitle("Ajouter un nouveau produit");
            stage.setScene(new Scene(root, 600, 800));
            stage.setResizable(true);
            formController.setStage(stage);
            formController.setParentController(this);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void modifierProduit(Produit produit) {
        if (produit == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/produit-form.fxml"));
            BorderPane root = loader.load();
            GestionProduitFormController formController = loader.getController();
            formController.setProduitToEdit(produit);
            Stage stage = new Stage();
            stage.setTitle("Modifier le produit");
            stage.setScene(new Scene(root, 600, 800));
            stage.setResizable(true);
            formController.setStage(stage);
            formController.setParentController(this);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void supprimerProduit(Produit produit) {
        if (produit == null) {
            AlertUtil.showError("Erreur", "Veuillez sélectionner un produit à supprimer");
            return;
        }
        if (AlertUtil.showConfirmation("Confirmation", "Êtes-vous sûr de vouloir supprimer \"" + produit.getNom() + "\" ?")) {
            try {
                produitService.supprimer(produit.getId());
                rafraichirTable();
                AlertUtil.showSuccess("Succès", "Le produit a été supprimé avec succès.");
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur", "Erreur: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onRechercherProduit(ActionEvent event) {
        try {
            String recherche = tfRechercheProduit.getText().trim().toLowerCase();
            Categorie categorieSelectionnee = cbCategorieProduit.getValue();
            List<Produit> tousLesProduits = produitService.afficher();
            ObservableList<Produit> resultatRecherche = FXCollections.observableArrayList();
            for (Produit prod : tousLesProduits) {
                boolean matchTexte = recherche.isEmpty() ||
                    prod.getNom().toLowerCase().contains(recherche) ||
                    prod.getDescription().toLowerCase().contains(recherche);
                boolean matchCategorie = categorieSelectionnee == null ||
                    prod.getCategorieId() == categorieSelectionnee.getId();
                if (matchTexte && matchCategorie) {
                    resultatRecherche.add(prod);
                }
            }
            produitsList.setAll(resultatRecherche);
            updatePagination();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    @FXML
    private void onResetRechercheProduit(ActionEvent event) {
        tfRechercheProduit.clear();
        cbCategorieProduit.setValue(null);
        cbTriProduit.setValue("Date d'ajout");
        cbOrdreProduit.setValue("↓ Décroissant");
        rafraichirTable();
    }

    private void appliquerTri() {
        String triOption = cbTriProduit.getValue();
        String ordreOption = cbOrdreProduit.getValue();
        if (triOption == null) return;
        boolean estCroissant = ordreOption != null && ordreOption.startsWith("↑");
        List<Produit> produits = new ArrayList<>(produitsList);
        if ("Date d'ajout".equals(triOption)) {
            produits.sort((p1, p2) -> {
                if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) return 0;
                return estCroissant ? p1.getCreatedAt().compareTo(p2.getCreatedAt()) : p2.getCreatedAt().compareTo(p1.getCreatedAt());
            });
        } else if ("Nom".equals(triOption)) {
            produits.sort((p1, p2) -> estCroissant ? p1.getNom().compareTo(p2.getNom()) : p2.getNom().compareTo(p1.getNom()));
        } else if ("Prix".equals(triOption)) {
            produits.sort((p1, p2) -> estCroissant ? Double.compare(p1.getPrix(), p2.getPrix()) : Double.compare(p2.getPrix(), p1.getPrix()));
        } else if ("ID".equals(triOption)) {
            produits.sort((p1, p2) -> estCroissant ? Integer.compare(p1.getId(), p2.getId()) : Integer.compare(p2.getId(), p1.getId()));
        }
        produitsList.setAll(produits);
        updatePagination();
    }

    private void afficherDetailProduit(Produit produit) {
        if (produit == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/produit-detail.fxml"));
            BorderPane root = loader.load();
            GestionProduitDetailController detailController = loader.getController();
            detailController.setProduit(produit);
            Stage stage = new Stage();
            stage.setTitle("Détail du produit - " + produit.getNom());
            stage.setScene(new Scene(root, 800, 600));
            stage.setResizable(true);
            detailController.setStage(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le détail: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.show();
    }
}
