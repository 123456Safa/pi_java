package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import Model.Reclamation;
import Service.ReclamationService;

public class EditReclamationController {

    @FXML
    private TextField titre;

    @FXML
    private TextField description;

    private Reclamation reclamation;
    private final ReclamationService service = new ReclamationService();

    // باش نستقبل البيانات من Home
    public void setData(Reclamation r) {
        this.reclamation = r;
        titre.setText(r.getTitre());
        description.setText(r.getDescription());
    }

    @FXML
    public void save() {
        reclamation.setTitre(titre.getText());
        reclamation.setDescription(description.getText());

        service.update(reclamation); // 👈 أهم سطر

        close();
    }

    @FXML
    public void cancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) titre.getScene().getWindow();
        stage.close();
    }
}