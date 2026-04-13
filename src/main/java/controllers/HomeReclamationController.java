package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Model.Reclamation;
import Service.ReclamationService;

public class HomeReclamationController {

    @FXML
    private VBox container;

    private final ReclamationService service = new ReclamationService();

    @FXML
    public void initialize() {
        load();
    }

    public void load() {

        container.getChildren().clear();

        for (Reclamation r : service.getAll()) {

            HBox box = new HBox(15);
            box.setStyle("-fx-border-color:black; -fx-padding:10;");

            Label titre = new Label(r.getTitre());
            Label date = new Label(String.valueOf(r.getDateCreation()));
            Label statut = new Label(r.getStatut());

            Button voir = new Button("Voir");
            Button modif = new Button("Modifier");
            Button supp = new Button("Supprimer");

            voir.setOnAction(e -> openDetail(r));
            modif.setOnAction(e -> openEdit(r));

            supp.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("Supprimer ?");

                alert.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.OK) {
                        service.delete(r.getId());
                        load();
                    }
                });
            });

            box.getChildren().addAll(titre, date, statut, voir, modif, supp);
            container.getChildren().add(box);
        }
    }

    private void openDetail(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailReclamation.fxml"));
            Parent root = loader.load();

            DetailReclamationController c = loader.getController();
            c.setData(r);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEdit(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditReclamation.fxml"));
            Parent root = loader.load();

            EditReclamationController c = loader.getController();
            c.setData(r);
            c.setHomeController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormReclamation.fxml"));
            Parent root = loader.load();

            FormReclamationController c = loader.getController();
            c.setHomeController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}