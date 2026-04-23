package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
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
        refresh();
    }

    public void refresh() {
        try {
            List<Livraison> livraisons = livraisonService.select();
            livraisonsTable.setItems(FXCollections.observableArrayList(livraisons));
            if (countLabel != null) {
                countLabel.setText(livraisons.size() + " livraison(s)");
            }
        } catch (SQLException e) {
            livraisonsTable.setItems(FXCollections.observableArrayList());
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Impossible de charger les livraisons.\n" + e.getMessage(),
                    ButtonType.OK);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }
}
