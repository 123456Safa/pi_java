package controllers;

import Model.Reponse;
import Service.ReponseService;
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

    public EditReponseController() {}

    public void setReponse(Reponse rep, String reclamationTitle) {
        this.reponse = rep;
        
        // Display reclamation title
        if (lblReclamation != null) {
            lblReclamation.setText(reclamationTitle);
        }
        
        // Display date
        if (lblDate != null) {
            lblDate.setText(rep.getDateReponse().toString());
        }
        
        // Display content
        if (contenu != null) {
            contenu.setText(rep.getContenu());
        }
    }

    public void setParentController(DetailReclamationControlleradmin parent) {
        this.parentController = parent;
    }

    @FXML
    public void save() {

        if (reponse == null) {
            System.out.println("Reponse NULL ❌");
            return;
        }

        String contenuText = contenu.getText().trim();
        if (contenuText.isEmpty()) {
            System.out.println("Contenu vide ❌");
            return;
        }

        reponse.setContenu(contenuText);
        service.update(reponse);

        // Refresh the parent controller's responses
        if (parentController != null) {
            parentController.refreshReponses();
        }

        ((Stage) contenu.getScene().getWindow()).close();
    }

    @FXML
    public void cancel() {
        ((Stage) contenu.getScene().getWindow()).close();
    }
}

