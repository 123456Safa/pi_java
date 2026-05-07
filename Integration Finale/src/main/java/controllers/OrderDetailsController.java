package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Commandes;
import models.LigneCommandes;
import services.LigneCommandeService;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class OrderDetailsController {
    @FXML
    private Label orderIdLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label userIdLabel;
    @FXML
    private Label createdAtLabel;
    @FXML
    private TableView<LigneCommandes> detailsTable;
    @FXML
    private TableColumn<LigneCommandes, String> productNameColumn;
    @FXML
    private TableColumn<LigneCommandes, Integer> quantityColumn;
    @FXML
    private TableColumn<LigneCommandes, Double> unitPriceColumn;
    @FXML
    private TableColumn<LigneCommandes, Double> subtotalColumn;

    private final LigneCommandeService ligneCommandeService = new LigneCommandeService();
    private OrderBackofficeNavigator navigator;

    @FXML
    public void initialize() {
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        subtotalColumn.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
    }

    public void setNavigator(OrderBackofficeNavigator navigator) {
        this.navigator = navigator;
    }

    public void setCommande(Commandes commande) {
        orderIdLabel.setText("#" + commande.getId());
        statusLabel.setText(commande.getStatut());
        totalLabel.setText(String.format("%.2f DT", commande.getTotales()));
        userIdLabel.setText(String.valueOf(commande.getUtilisateurId()));
        createdAtLabel.setText(commande.getCreatedAt());
        loadLignes(commande.getId());
    }

    @FXML
    public void goBack() {
        if (navigator != null) {
            navigator.showCommandes();
        }
    }

    private void loadLignes(int commandeId) {
        try {
            List<LigneCommandes> lignes = ligneCommandeService.select().stream()
                    .filter(ligne -> ligne.getCommandeId() == commandeId)
                    .collect(Collectors.toList());
            detailsTable.setItems(FXCollections.observableArrayList(lignes));
        } catch (SQLException e) {
            detailsTable.setItems(FXCollections.observableArrayList());
        }
    }
}
