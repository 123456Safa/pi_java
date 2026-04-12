package controllers;

import Model.Reclamation;
import Model.Reponse;
import Service.ReponseService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.Date;
public class ReponseFormController {

    @FXML private TextArea contenu;

    private Reclamation reclamation;
    private final ReponseService service = new ReponseService();

    // ✅ constructor vide (اختياري)
    public ReponseFormController() {}

    public void setReclamation(Reclamation r) {
        this.reclamation = r;
    }

    @FXML
    public void save() {

        if (reclamation == null) {
            System.out.println("Reclamation NULL ❌");
            return;
        }

        Reponse rep = new Reponse();
        rep.setContenu(contenu.getText());
        rep.setDateReponse(new Date());
        rep.setReclamationId(reclamation.getId());

        service.add(rep);

        ((Stage) contenu.getScene().getWindow()).close();
    }

    @FXML
    public void cancel() {
        ((Stage) contenu.getScene().getWindow()).close();
    }
}