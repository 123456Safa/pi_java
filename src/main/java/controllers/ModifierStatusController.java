package controllers;
import Model.Reclamation;
import Service.ReclamationService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class ModifierStatusController {
    private HomeAdminController homeController;

    public void setHomeController(HomeAdminController homeController) {
        this.homeController = homeController;
    }
    @FXML private ComboBox<String> statusBox;

    private Reclamation r;
    private final ReclamationService service = new ReclamationService();

    @FXML
    public void initialize() {
        statusBox.getItems().addAll("EN_COURS", "EN_ATTENTE", "RESOLU");
    }

    public void setData(Reclamation r) {
        this.r = r;
        statusBox.setValue(r.getStatut());
    }

    @FXML
    public void save() {
        r.setStatut(statusBox.getValue());
        service.updateStatus(r);

        if (homeController != null) {
            homeController.refresh();
        }

        ((Stage) statusBox.getScene().getWindow()).close();
    }

    @FXML
    public void cancel() {
        ((Stage) statusBox.getScene().getWindow()).close();
    }
}