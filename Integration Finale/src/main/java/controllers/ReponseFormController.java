package controllers;

import models.Reclamation;
import models.Reponse;
import services.ReponseService;
import services.OpenAI;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.Date;

public class ReponseFormController {

    @FXML private TextArea contenu;
    @FXML private Label lblHeader;
    @FXML private Label lblReclamation;

    private Reclamation reclamation;
    private DetailReclamationControlleradmin parentController;
    private final ReponseService service = new ReponseService();

    public void setReclamation(Reclamation r) {
        this.reclamation = r;
        if (lblReclamation != null) lblReclamation.setText(r.getTitre());
    }

    public void setParentController(DetailReclamationControlleradmin parent) {
        this.parentController = parent;
    }

    @FXML
    public void save() {
        if (reclamation == null) return;
        String contenuText = contenu.getText().trim();
        if (contenuText.isEmpty()) return;

        Reponse rep = new Reponse();
        rep.setContenu(contenuText);
        rep.setDateReponse(new Date());
        rep.setReclamationId(reclamation.getId());
        service.add(rep);

        if (parentController != null) parentController.refreshReponses();
        ((Stage) contenu.getScene().getWindow()).close();
    }

    @FXML
    public void cancel() {
        ((Stage) contenu.getScene().getWindow()).close();
    }

    @FXML
    public void genererAvecIA() {
        if (reclamation == null) {
            contenu.setText("Aucune réclamation sélectionnée.");
            return;
        }
        contenu.setText("🤖 Génération en cours...");
        contenu.setDisable(true);
        new Thread(() -> {
            try {
                String prompt = "Tu es un assistant client d'une entreprise.\n"
                        + "Réponds toujours en français, poliment et professionnellement.\n"
                        + "Sois court (2-3 phrases maximum).\n\n"
                        + "Titre : " + reclamation.getTitre() + "\n"
                        + "Description : " + reclamation.getDescription();
                String reponse = OpenAI.generateResponse(prompt);
                javafx.application.Platform.runLater(() -> {
                    contenu.setText(reponse);
                    contenu.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    contenu.setText("❌ Erreur IA.");
                    contenu.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }
}
