package controllers;

import Model.Reclamation;
import Service.ReclamationService;
import controllers.DetailReclamationController;
import controllers.ModifierStatusController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import controllers.ReponseFormController;

import java.util.Date;
import java.util.List;

public class HomeAdminController {

    @FXML
    private TextField searchField;
    @FXML private TableView<Reclamation> table;
    @FXML private TableColumn<Reclamation, String> colTitre;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private TableColumn<Reclamation, Date> colDate;
    @FXML private TableColumn<Reclamation, Void> colAction;

    private final ReclamationService service = new ReclamationService();


    @FXML
    public void initialize() {

        colTitre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));
        colDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDateCreation()));

        addButtons();
        loadTable(service.getAll(1));
    }

    private void loadTable(List<Reclamation> list) {
        table.getItems().setAll(list);
    }

    private void addButtons() {

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button voir = new Button("Voir");
            private final Button modif = new Button("Modifier");
            private final Button supp = new Button("Supprimer");

            {
                voir.setOnAction(e -> openDetail(getTableView().getItems().get(getIndex())));
                modif.setOnAction(e -> openModifier(getTableView().getItems().get(getIndex())));
                supp.setOnAction(e -> delete(getTableView().getItems().get(getIndex())));
            }

            private final HBox box = new HBox(5, voir, modif, supp);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }
    public void refresh() {
        loadTable(service.getAll(1));
    }
    private void openDetail(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailReclamationadmin.fxml"));
            Parent root = loader.load();

            DetailReclamationControlleradmin c = loader.getController();
            c.setData(r);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void openModifier(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modstatus.fxml"));
            Parent root = loader.load();

            ModifierStatusController c = loader.getController();
            c.setData(r);
            c.setHomeController(this); // 🔥 مهم

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void delete(Reclamation r) {
        service.delete(r.getId());
        loadTable(service.getAll(1));
    }

    @FXML
    public void search() {
        String txt = searchField.getText();
        loadTable(service.searchByTitre(txt));
    }
}