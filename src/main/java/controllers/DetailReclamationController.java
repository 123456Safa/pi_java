package controllers;

import Model.Reclamation;
import Model.Reponse;
import Service.ReponseService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class DetailReclamationController {

    @FXML private Label lblTitre;
    @FXML private Label lblDate;
    @FXML private Label lblStatut;
    @FXML private TextArea txtDescription;

    @FXML private VBox reponseContainer;

    private final ReponseService service = new ReponseService();

    public void setData(Reclamation r) {

        lblTitre.setText(r.getTitre());
        lblDate.setText(String.valueOf(r.getDateCreation()));
        lblStatut.setText(r.getStatut());
        txtDescription.setText(r.getDescription());

        loadReponses(r.getId());
    }

    private void loadReponses(int reclamationId) {

        reponseContainer.getChildren().clear();

        List<Reponse> list = service.getByReclamationId(reclamationId);

        if (list == null || list.isEmpty()) {
            Label empty = new Label("Aucune réponse pour cette réclamation.");
            reponseContainer.getChildren().add(empty);
            return;
        }

        for (Reponse rep : list) {

            VBox box = new VBox(5);
            box.setStyle("-fx-border-color:lightgray; -fx-padding:8;");

            Label contenu = new Label(rep.getContenu());
            Label date = new Label(String.valueOf(rep.getDateReponse()));

            box.getChildren().addAll(contenu, date);
            reponseContainer.getChildren().add(box);
        }
    }

    @FXML
    private void onBack() {
        Stage stage = (Stage) lblTitre.getScene().getWindow();
        stage.close();
    }
}