package controllers;

import models.Reponse;
import services.ReponseService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class EditReponseController {

    @FXML private TextArea contenu;
    @FXML private Label lblReclamation;
    @FXML private Label lblDate;

    private Reponse reponse;
    private DetailReclamationControlleradmin parentController;
    private final ReponseService service = new ReponseService();

    public void setReponse(Reponse rep, String reclamationTitle) {
        this.reponse = rep;
        if (lblReclamation != null) lblReclamation.setText(reclamationTitle);
        if (lblDate != null) lblDate.setText(rep.getDateReponse().toString());
        if (contenu != null) contenu.setText(rep.getContenu());
    }

    public void setParentController(DetailReclamationControlleradmin parent) {
        this.parentController = parent;
    }

    @FXML
    public void save() {
        if (reponse == null) return;
        String contenuText = contenu.getText().trim();
        if (contenuText.isEmpty()) return;
        reponse.setContenu(contenuText);
        service.update(reponse);
        if (parentController != null) parentController.refreshReponses();
        ((Stage) contenu.getScene().getWindow()).close();
    }

    @FXML
    public void cancel() {
        ((Stage) contenu.getScene().getWindow()).close();
    }
}
