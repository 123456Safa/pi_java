package controllers.frontoffice;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Commandes;
import models.LigneCommandes;
import services.CommandeService;
import services.LigneCommandeService;

import java.util.List;

public class HistoriqueController {

    @FXML
    private TableView<Commandes> tableCommandes;
    @FXML
    private TableColumn<Commandes, Integer> colId;
    @FXML
    private TableColumn<Commandes, String> colStatut;
    @FXML
    private TableColumn<Commandes, String> colDate;
    @FXML
    private TableColumn<Commandes, Double> colTotal;

    @FXML
    private TableView<LigneCommandes> tableLignes;
    @FXML
    private TableColumn<LigneCommandes, String> colLigneNom;
    @FXML
    private TableColumn<LigneCommandes, Integer> colLigneQte;
    @FXML
    private TableColumn<LigneCommandes, Double> colLignePrix;
    @FXML
    private TableColumn<LigneCommandes, Double> colLigneSousTotal;

    private CommandeService commandeService = new CommandeService();
    private LigneCommandeService ligneCommandeService = new LigneCommandeService();

    @FXML
    public void initialize() {
        setupTableCommandes();
        setupTableLignes();
        loadCommandes();
    }

    private void setupTableCommandes() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totales"));

        tableCommandes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadLignesCommande(newVal.getId());
            }
        });
    }

    private void setupTableLignes() {
        colLigneNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colLigneQte.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colLignePrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colLigneSousTotal.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
    }

    private void loadCommandes() {
        try {
            List<Commandes> commandes = commandeService.select();
            ObservableList<Commandes> data = FXCollections.observableArrayList(commandes);
            tableCommandes.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLignesCommande(int commandeId) {
        try {
            List<LigneCommandes> lignes = ligneCommandeService.select();
            ObservableList<LigneCommandes> data = FXCollections.observableArrayList(
                lignes.stream()
                    .filter(l -> l.getCommandeId() == commandeId)
                    .toList()
            );
            tableLignes.setItems(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

