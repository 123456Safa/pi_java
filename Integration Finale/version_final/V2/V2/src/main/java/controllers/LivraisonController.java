package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Livraison;
import services.LivraisonService;

import java.sql.SQLException;
import java.util.List;

public class LivraisonController {
    @FXML private TableView<Livraison> livraisonsTable;
    @FXML private TableColumn<Livraison, Integer> idColumn;
    @FXML private TableColumn<Livraison, Integer> commandeIdColumn;
    @FXML private TableColumn<Livraison, String> lastNameColumn;
    @FXML private TableColumn<Livraison, String> firstNameColumn;
    @FXML private TableColumn<Livraison, String> emailColumn;
    @FXML private TableColumn<Livraison, String> adresseColumn;
    @FXML private TableColumn<Livraison, String> telColumn;
    @FXML private TableColumn<Livraison, String> createdAtColumn;
    @FXML private Label countLabel;
    @FXML private Pagination pagination;

    private static final int ITEMS_PER_PAGE = 8;
    private List<Livraison> allLivraisons = FXCollections.observableArrayList();

    private final LivraisonService livraisonService = new LivraisonService();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        commandeIdColumn.setCellValueFactory(new PropertyValueFactory<>("commandeId"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        telColumn.setCellValueFactory(new PropertyValueFactory<>("tel"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        pagination.currentPageIndexProperty().addListener((obs, oldIdx, newIdx) -> updateTablePage(newIdx.intValue()));
        
        refresh();
    }

    public void refresh() {
        try {
            allLivraisons = livraisonService.select();
            updatePagination();
            if (countLabel != null) {
                countLabel.setText(allLivraisons.size() + " livraison(s)");
            }
        } catch (SQLException e) {
            allLivraisons = FXCollections.observableArrayList();
            updatePagination();
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Impossible de charger les livraisons.\n" + e.getMessage(),
                    ButtonType.OK);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    private void updatePagination() {
        int totalItems = allLivraisons.size();
        int pageCount = (totalItems + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        updateTablePage(0);
    }

    private void updateTablePage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allLivraisons.size());
        
        if (fromIndex >= allLivraisons.size()) {
            livraisonsTable.setItems(FXCollections.observableArrayList());
            return;
        }
        
        livraisonsTable.setItems(FXCollections.observableArrayList(allLivraisons.subList(fromIndex, toIndex)));
    }
}
