package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Commandes;
import services.CommandeService;

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

    private final CommandeService service = new CommandeService();

    private Commandes selectedCommandes;

    @FXML
    public void initialize() {
        // Initialize the columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProduits.setCellValueFactory(new PropertyValueFactory<>("produits"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totales"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colUtilisateurId.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));

        loadCommandes();
    }

    @FXML
    public void ajouterCommande() {
        try {
            Commandes c;
            if (selectedCommandes != null) {
                c = selectedCommandes;
                c.setProduits(tfProduits.getText());
                c.setTotales(Double.parseDouble(tfTotales.getText()));
                c.setStatut(tfStatut.getText());
                c.setDate(dpDate.getValue().toString());
                c.setUtilisateurId(Integer.parseInt(tfUtilisateurId.getText()));

                service.update(c);
                System.out.println("✏️ commande modifiée");
            } else {
                c = new Commandes();

                c.setProduits(tfProduits.getText());
                c.setTotales(Double.parseDouble(tfTotales.getText()));
                c.setStatut(tfStatut.getText());
                c.setDate(dpDate.getValue().toString());
                c.setUtilisateurId(Integer.parseInt(tfUtilisateurId.getText()));

                service.add(c);
                System.out.println("✅ commande ajoutée");
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
            ObservableList<Commandes> data =
                    FXCollections.observableArrayList(service.select());

            tableCommande.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            // Assuming date is string, but DatePicker needs LocalDate
            // For simplicity, skip date for now
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
                System.out.println("🗑️ commande supprimée");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}