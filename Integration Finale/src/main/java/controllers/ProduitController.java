package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Produit;
import services.ProduitService;

public class ProduitController {

    @FXML
    private TextField tfNom;

    @FXML
    private TextField tfPrix;

    @FXML
    private TextField tfDescription;

    @FXML
    private TextField tfStock;

    @FXML
    private TableView<Produit> tableProduit;

    @FXML
    private TableColumn<Produit, Integer> colId;

    @FXML
    private TableColumn<Produit, String> colNom;

    @FXML
    private TableColumn<Produit, Double> colPrix;

    @FXML
    private TableColumn<Produit, String> colDescription;

    @FXML
    private TableColumn<Produit, Integer> colStock;

    @FXML
    private Pagination pagination;

    private static final int ITEMS_PER_PAGE = 8;
    private ObservableList<Produit> allProduits = FXCollections.observableArrayList();

    private final ProduitService service = new ProduitService();

    private Produit selectedProduit;

    @FXML
    public void initialize() {
        // Initialize the columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        pagination.currentPageIndexProperty().addListener((obs, oldIdx, newIdx) -> updateTablePage(newIdx.intValue()));
        
        loadProduits();
    }

    @FXML
    public void ajouterProduit() {
        try {
            Produit p;
            if (selectedProduit != null) {
                p = selectedProduit;
                p.setNom(tfNom.getText());
                p.setPrix(Double.parseDouble(tfPrix.getText()));
                p.setDescription(tfDescription.getText());
                p.setStock(Integer.parseInt(tfStock.getText()));

                service.update(p);
                System.out.println("✏️ Produit modifié");
            } else {
                p = new Produit();

                p.setNom(tfNom.getText());
                p.setPrix(Double.parseDouble(tfPrix.getText()));
                p.setDescription(tfDescription.getText());
                p.setStock(Integer.parseInt(tfStock.getText()));

                service.add(p);
                System.out.println("✅ Produit ajouté");
            }

            loadProduits();
            clearFields();
            selectedProduit = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadProduits() {
        try {
            allProduits = FXCollections.observableArrayList(service.select());
            updatePagination();
        } catch (Exception e) {
            allProduits = FXCollections.observableArrayList();
            updatePagination();
            e.printStackTrace();
        }
    }

    private void updatePagination() {
        int totalItems = allProduits.size();
        int pageCount = (totalItems + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        updateTablePage(0);
    }

    private void updateTablePage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allProduits.size());
        
        if (fromIndex >= allProduits.size()) {
            tableProduit.setItems(FXCollections.observableArrayList());
            return;
        }
        
        tableProduit.setItems(FXCollections.observableArrayList(allProduits.subList(fromIndex, toIndex)));
    }

    private void clearFields() {
        tfNom.clear();
        tfPrix.clear();
        tfDescription.clear();
        tfStock.clear();
    }

    @FXML
    public void modifierProduit() {
        selectedProduit = tableProduit.getSelectionModel().getSelectedItem();
        if (selectedProduit != null) {
            tfNom.setText(selectedProduit.getNom());
            tfPrix.setText(String.valueOf(selectedProduit.getPrix()));
            tfDescription.setText(selectedProduit.getDescription());
            tfStock.setText(String.valueOf(selectedProduit.getStock()));
        }
    }

    @FXML
    public void supprimerProduit() {
        selectedProduit = tableProduit.getSelectionModel().getSelectedItem();
        if (selectedProduit != null) {
            try {
                service.delete(selectedProduit.getId());
                loadProduits();
                clearFields();
                selectedProduit = null;
                System.out.println("🗑️ Produit supprimé");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
