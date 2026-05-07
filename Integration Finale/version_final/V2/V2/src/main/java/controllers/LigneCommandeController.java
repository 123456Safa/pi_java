package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.LigneCommandes;
import services.LigneCommandeService;

public class LigneCommandeController {

    @FXML
    private TextField tfNom;

    @FXML
    private TextField tfPrix;

    @FXML
    private TextField tfQuantite;

    @FXML
    private TextField tfSousTotal;

    @FXML
    private TextField tfCommandeId;

    @FXML
    private TableView<LigneCommandes> tableLigne;

    @FXML
    private TableColumn<LigneCommandes, Integer> colId;

    @FXML
    private TableColumn<LigneCommandes, String> colNom;

    @FXML
    private TableColumn<LigneCommandes, Double> colPrix;

    @FXML
    private TableColumn<LigneCommandes, Integer> colQuantite;

    @FXML
    private TableColumn<LigneCommandes, Double> colSousTotal;

    @FXML
    private TableColumn<LigneCommandes, Integer> colCommandeId;

    private final LigneCommandeService service = new LigneCommandeService();

    private LigneCommandes selectedLigne;

    @FXML
    public void initialize() {
        // Initialize the columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colSousTotal.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
        colCommandeId.setCellValueFactory(new PropertyValueFactory<>("commandeId"));

        loadLignes();
    }

    @FXML
    public void ajouterLigne() {
        try {
            LigneCommandes l;
            if (selectedLigne != null) {
                l = selectedLigne;
                l.setNom(tfNom.getText());
                l.setPrix(Double.parseDouble(tfPrix.getText()));
                l.setQuantite(Integer.parseInt(tfQuantite.getText()));
                l.setSousTotal(Double.parseDouble(tfSousTotal.getText()));
                l.setCommandeId(Integer.parseInt(tfCommandeId.getText()));

                service.update(l);
                System.out.println("✏️ Ligne modifiée");
            } else {
                l = new LigneCommandes();

                l.setNom(tfNom.getText());
                l.setPrix(Double.parseDouble(tfPrix.getText()));
                l.setQuantite(Integer.parseInt(tfQuantite.getText()));
                l.setSousTotal(Double.parseDouble(tfSousTotal.getText()));
                l.setCommandeId(Integer.parseInt(tfCommandeId.getText()));

                service.add(l);
                System.out.println("✅ Ligne ajoutée");
            }

            loadLignes();
            clearFields();
            selectedLigne = null;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadLignes() {
        try {
            ObservableList<LigneCommandes> data =
                    FXCollections.observableArrayList(service.select());

            tableLigne.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        tfNom.clear();
        tfPrix.clear();
        tfQuantite.clear();
        tfSousTotal.clear();
        tfCommandeId.clear();
    }

    @FXML
    public void modifierLigne() {
        selectedLigne = tableLigne.getSelectionModel().getSelectedItem();
        if (selectedLigne != null) {
            tfNom.setText(selectedLigne.getNom());
            tfPrix.setText(String.valueOf(selectedLigne.getPrix()));
            tfQuantite.setText(String.valueOf(selectedLigne.getQuantite()));
            tfSousTotal.setText(String.valueOf(selectedLigne.getSousTotal()));
            tfCommandeId.setText(String.valueOf(selectedLigne.getCommandeId()));
        }
    }

    @FXML
    public void supprimerLigne() {
        selectedLigne = tableLigne.getSelectionModel().getSelectedItem();
        if (selectedLigne != null) {
            try {
                service.delete(selectedLigne.getId());
                loadLignes();
                clearFields();
                selectedLigne = null;
                System.out.println("🗑️ Ligne supprimée");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
