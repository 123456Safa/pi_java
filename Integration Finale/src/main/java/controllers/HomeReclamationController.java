package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import models.Reclamation;
import services.ReclamationService;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class HomeReclamationController {

    @FXML private Pagination pagination;
    private final int ITEMS_PER_PAGE = 5;
    private List<Reclamation> currentFilteredList;

    @FXML private TextField searchField;
    @FXML private VBox rootVBox;
    @FXML private ComboBox<String> statutCombo;

    private final ReclamationService service = new ReclamationService();
    private List<Reclamation> allReclamations;
    private PauseTransition searchDebounce;

    @FXML
    public void initialize() {
        load();
        searchDebounce = new PauseTransition(Duration.millis(300));
        searchDebounce.setOnFinished(e -> handleSearch());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> searchDebounce.playFromStart());
        if (statutCombo != null) {
            statutCombo.getSelectionModel().selectFirst();
            statutCombo.setOnAction(e -> handleSearch());
        }
    }

    public void load() {
        models.User currentUser = utils.SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.isAdmin()) {
            // Admins see all reclamations
            allReclamations = service.getAll();
        } else if (currentUser != null) {
            // Regular users see only their own reclamations
            allReclamations = service.getAll(currentUser.getId());
        } else {
            allReclamations = new java.util.ArrayList<>();
        }
        if (allReclamations != null && !allReclamations.isEmpty()) {
            allReclamations.sort((r1, r2) -> {
                if (r1.getDateCreation() == null && r2.getDateCreation() == null) return 0;
                if (r1.getDateCreation() == null) return 1;
                if (r2.getDateCreation() == null) return -1;
                return r2.getDateCreation().compareTo(r1.getDateCreation());
            });
        }
        displayReclamations(allReclamations);
    }

    private void displayReclamations(List<Reclamation> reclamations) {
        this.currentFilteredList = reclamations;
        int pageCount = (int) Math.ceil((double) reclamations.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(this::createPage);
    }

    private javafx.scene.Node createPage(int pageIndex) {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-border-color: transparent; -fx-background-color: transparent; -fx-padding: 20 26;");
        scrollPane.setFitToWidth(true);

        VBox box = new VBox(16);
        box.setStyle("-fx-padding: 0; -fx-background-color: transparent;");

        if (currentFilteredList != null) {
            int fromIndex = pageIndex * ITEMS_PER_PAGE;
            int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, currentFilteredList.size());
            for (int i = fromIndex; i < toIndex; i++) {
                box.getChildren().add(createReclamationCard(currentFilteredList.get(i)));
            }
        }
        scrollPane.setContent(box);
        return scrollPane;
    }

    private VBox createReclamationCard(Reclamation r) {
        VBox card = new VBox(15);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-padding: 24;" +
            "-fx-background-radius: 12;" +
            "-fx-border-radius: 12;" +
            "-fx-border-color: #e2ece8 #e2ece8 #e2ece8 #006B45;" +
            "-fx-border-width: 1 1 1 6;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,80,40,0.08), 12, 0, 0, 4);"
        );
        card.setPrefWidth(Double.MAX_VALUE);

        HBox titleBox = new HBox(15);
        titleBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titre = new Label(r.getTitre().toUpperCase());
        titre.setStyle("-fx-font-size: 16; -fx-font-weight: 800; -fx-text-fill: #0d2b1e;");
        HBox.setHgrow(titre, Priority.ALWAYS);

        Label statut = new Label(r.getStatut().toUpperCase());
        String statusColor = getStatusColor(r.getStatut());
        String statusBgColor = getStatusBgColor(r.getStatut());
        statut.setStyle("-fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: " + statusColor +
                       "; -fx-background-color: " + statusBgColor +
                       "; -fx-padding: 5 12; -fx-background-radius: 6; -fx-border-radius: 6;");
        titleBox.getChildren().addAll(titre, statut);

        Label description = new Label(r.getDescription());
        description.setStyle("-fx-font-size: 14; -fx-text-fill: #4a6158; -fx-wrap-text: true;");
        description.setWrapText(true);
        description.setMaxHeight(60);

        HBox bottomBox = new HBox(15);
        bottomBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        bottomBox.setStyle("-fx-padding: 10 0 0 0;");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", java.util.Locale.FRENCH);
        Label date = new Label("Créée le " + sdf.format(r.getDateCreation()));
        date.setStyle("-fx-font-size: 12; -fx-text-fill: #90a8a0;");
        HBox.setHgrow(date, Priority.ALWAYS);

        Button voir = new Button("Détails");
        voir.getStyleClass().add("btn-header-action");
        voir.setOnAction(e -> openDetail(r));

        Button editer = new Button("Modifier");
        editer.getStyleClass().add("btn-header-action");
        editer.setOnAction(e -> openEdit(r));

        bottomBox.getChildren().addAll(date, editer, voir);
        card.getChildren().addAll(titleBox, description, bottomBox);
        return card;
    }

    private String getStatusColor(String statut) {
        switch (statut.toUpperCase()) {
            case "EN ATTENTE": return "#92400e";
            case "RÉSOLUE": return "#15803d";
            case "EN COURS": return "#7c3aed";
            default: return "#64748b";
        }
    }

    private String getStatusBgColor(String statut) {
        switch (statut.toUpperCase()) {
            case "EN ATTENTE": return "#fef3c7";
            case "RÉSOLUE": return "#dcfce7";
            case "EN COURS": return "#f3e8ff";
            default: return "#f1f5f9";
        }
    }

    @FXML
    public void resetFilters() {
        searchField.setText("");
        if (statutCombo != null) statutCombo.getSelectionModel().selectFirst();
        load();
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        String statut = (statutCombo != null && statutCombo.getValue() != null) ? statutCombo.getValue() : "Tous";
        List<Reclamation> filtered = new java.util.ArrayList<>(
                allReclamations.stream()
                        .filter(r -> (searchText.isEmpty() || r.getTitre().toLowerCase().contains(searchText)
                                || r.getDescription().toLowerCase().contains(searchText)))
                        .filter(r -> statut.equals("Tous") || r.getStatut().equalsIgnoreCase(statut))
                        .toList());
        filtered.sort((r1, r2) -> {
            if (r1.getDateCreation() == null || r2.getDateCreation() == null) return 0;
            return r2.getDateCreation().compareTo(r1.getDateCreation());
        });
        displayReclamations(filtered);
    }

    private void openDetail(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reclamation/detail.fxml"));
            Parent root = loader.load();
            DetailReclamationController c = loader.getController();
            c.setData(r);
            c.setHomeController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 650));
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEdit(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reclamation/edit.fxml"));
            Parent root = loader.load();
            EditReclamationController c = loader.getController();
            c.setData(r);
            c.setHomeController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 650));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reclamation/form.fxml"));
            Parent root = loader.load();
            FormReclamationController c = loader.getController();
            c.setHomeController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 650));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goBackToHomeAdmin() {
        AppShellController shellController = AppShellController.getInstance();
        if (shellController != null) {
            shellController.showBackOffice();
        }
    }

    @FXML
    public void goBackToFront() {
        AppShellController shellController = AppShellController.getInstance();
        if (shellController != null) {
            shellController.showFrontOffice();
        }
    }
}
