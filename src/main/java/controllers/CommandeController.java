package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Commandes;
import services.CommandeService;

import java.time.LocalDate;

public class CommandeController {

    @FXML
    private TextField tfProduits;

    @FXML
    private TextField tfTotales;

    @FXML
    private TextField tfStatut;

    @FXML
    private DatePicker dpDate;

    @FXML
    private TextField tfUtilisateurId;

    @FXML
    private TableView<Commandes> tableCommande;

    @FXML
    private TableColumn<Commandes, Integer> colId;

    @FXML
    private TableColumn<Commandes, String> colProduits;

    @FXML
    private TableColumn<Commandes, Double> colTotal;

    @FXML
    private TableColumn<Commandes, String> colStatut;

    @FXML
    private TableColumn<Commandes, String> colDate;

    @FXML
    private TableColumn<Commandes, Integer> colUtilisateurId;

    @FXML
    private Pagination pagination;

    private static final int ITEMS_PER_PAGE = 8;
    private ObservableList<Commandes> allCommandes = FXCollections.observableArrayList();

    private final CommandeService service = new CommandeService();

    private Commandes selectedCommandes;

    @FXML
    public void initialize() {
        // Initialize the columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProduits.setCellValueFactory(new PropertyValueFactory<>("produits"));
        
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totales"));
        colTotal.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.3f DT", item));
                }
            }
        });

        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("status-badge");
                    String s = item.toLowerCase();
                    if (s.contains("attente")) badge.getStyleClass().add("status-pending");
                    else if (s.contains("confirm")) badge.getStyleClass().add("status-confirmed");
                    else if (s.contains("livre") || s.contains("deliver")) badge.getStyleClass().add("status-delivered");
                    else if (s.contains("annul")) badge.getStyleClass().add("status-cancelled");
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colUtilisateurId.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));

        pagination.currentPageIndexProperty().addListener((obs, oldIdx, newIdx) -> updateTablePage(newIdx.intValue()));
        
        loadCommandes();
    }

    @FXML
    public void ajouterCommande() {
        try {
            String dateStr = dpDate.getValue() != null ? dpDate.getValue().toString() : "";

            Commandes c;
            if (selectedCommandes != null) {
                c = selectedCommandes;
                c.setProduits(tfProduits.getText());
                c.setTotales(Double.parseDouble(tfTotales.getText()));
                c.setStatut(tfStatut.getText());
                c.setCreatedAt(dateStr);
                c.setUtilisateurId(Integer.parseInt(tfUtilisateurId.getText()));

                service.update(c);
            } else {
                c = new Commandes();

                c.setProduits(tfProduits.getText());
                c.setTotales(Double.parseDouble(tfTotales.getText()));
                c.setStatut(tfStatut.getText());
                c.setCreatedAt(dateStr);
                c.setUtilisateurId(Integer.parseInt(tfUtilisateurId.getText()));

                service.add(c);
            }

            loadCommandes();
            clearFields();
            selectedCommandes = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadCommandes() {
        try {
            allCommandes = FXCollections.observableArrayList(service.select());
            updatePagination();
        } catch (Exception e) {
            allCommandes = FXCollections.observableArrayList();
            updatePagination();
            e.printStackTrace();
        }
    }

    private void updatePagination() {
        int totalItems = allCommandes.size();
        int pageCount = (totalItems + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        pagination.setPageCount(Math.max(1, pageCount));
        pagination.setCurrentPageIndex(0);
        updateTablePage(0);
    }

    private void updateTablePage(int pageIndex) {
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allCommandes.size());
        
        if (fromIndex >= allCommandes.size()) {
            tableCommande.setItems(FXCollections.observableArrayList());
            return;
        }
        
        tableCommande.setItems(FXCollections.observableArrayList(allCommandes.subList(fromIndex, toIndex)));
    }

    private void clearFields() {
        tfProduits.clear();
        tfTotales.clear();
        tfStatut.clear();
        tfUtilisateurId.clear();
        dpDate.setValue(null);
    }

    @FXML
    public void modifierCommande() {
        selectedCommandes = tableCommande.getSelectionModel().getSelectedItem();
        if (selectedCommandes != null) {
            tfProduits.setText(selectedCommandes.getProduits());
            tfTotales.setText(String.valueOf(selectedCommandes.getTotales()));
            tfStatut.setText(selectedCommandes.getStatut());
            if (selectedCommandes.getCreatedAt() != null && !selectedCommandes.getCreatedAt().isEmpty()) {
                try {
                    dpDate.setValue(LocalDate.parse(selectedCommandes.getCreatedAt().substring(0, 10)));
                } catch (Exception e) {
                    // Date format may not be parseable, skip
                }
            }
            tfUtilisateurId.setText(String.valueOf(selectedCommandes.getUtilisateurId()));
        }
    }

    @FXML
    public void supprimerCommande() {
        selectedCommandes = tableCommande.getSelectionModel().getSelectedItem();
        if (selectedCommandes != null) {
            try {
                service.delete(selectedCommandes.getId());
                loadCommandes();
                clearFields();
                selectedCommandes = null;
                System.out.println("commande supprimée");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
