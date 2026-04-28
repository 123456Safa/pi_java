package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;
import javafx.stage.Screen;
import javafx.stage.Stage;
import Model.Reclamation;
import Service.ReclamationService;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.text.SimpleDateFormat;
import java.util.List;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HomeReclamationController {

    @FXML
    private Pagination pagination;
    private final int ITEMS_PER_PAGE = 5;
    private List<Reclamation> currentFilteredList;

    @FXML
    private TextField searchField;

    @FXML
    private VBox rootVBox;

    @FXML
    private ComboBox<String> statutCombo;

    private final ReclamationService service = new ReclamationService();
    private List<Reclamation> allReclamations;
    private PauseTransition searchDebounce;

    @FXML
    public void initialize() {
        load();

        // Ajax-style live search with 300ms debounce
        searchDebounce = new PauseTransition(Duration.millis(300));
        searchDebounce.setOnFinished(e -> handleSearch());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchDebounce.playFromStart();
        });

        if (statutCombo != null) {
            statutCombo.getSelectionModel().selectFirst();
            statutCombo.setOnAction(e -> handleSearch());
        }
    }

    public void load() {
        allReclamations = service.getAll();
        // Trier par date
        if (allReclamations != null && !allReclamations.isEmpty()) {
            allReclamations.sort((r1, r2) -> {
                if (r1.getDateCreation() == null && r2.getDateCreation() == null)
                    return 0;
                if (r1.getDateCreation() == null)
                    return 1;
                if (r2.getDateCreation() == null)
                    return -1;
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

    // hathi partie design - Enhanced Card Design
    private VBox createReclamationCard(Reclamation r) {
        VBox card = new VBox(12);
        card.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-background-color: white; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.08), 12, 0, 0, 4);");
        card.setPrefWidth(Double.MAX_VALUE);

        // Title and Status HBox
        HBox titleBox = new HBox(15);
        titleBox.setStyle("-fx-alignment: center-left;");

        Label titre = new Label(r.getTitre());
        titre.setStyle("-fx-font-size: 16; -fx-font-weight: 600; -fx-text-fill: #0f172a;");

        Label statut = new Label(r.getStatut());
        String statusColor = getStatusColor(r.getStatut());
        String statusBgColor = getStatusBgColor(r.getStatut());
        statut.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: " + statusColor + "; -fx-background-color: "
                + statusBgColor + "; -fx-padding: 6 14; -fx-border-radius: 20; -fx-background-radius: 20;");

        HBox.setHgrow(titleBox, Priority.ALWAYS);
        titleBox.getChildren().addAll(titre, statut);

        // Description
        Label description = new Label(r.getDescription());
        description.setStyle("-fx-font-size: 13; -fx-text-fill: #64748b; -fx-wrap-text: true; -fx-line-spacing: 2;");
        description.setWrapText(true);
        description.setMaxWidth(Double.MAX_VALUE);

        // Date and Metadata
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        Label date = new Label("📅 " + sdf.format(r.getDateCreation()));
        date.setStyle("-fx-font-size: 12; -fx-text-fill: #94a3b8; -fx-font-weight: 500;");

        // Buttons container
        HBox buttonBox = new HBox(12);
        buttonBox.setStyle(
                "-fx-alignment: center-left;" +
                        "-fx-padding: 12 0 0 0;"
        );

// Voir button
        Button voir = new Button("👁 Voir");
        voir.getStyleClass().add("pill-btn-blue");
        voir.setOnAction(e -> openDetail(r));
        voir.setPrefHeight(36);
        voir.setMinWidth(130);

// Edit button
        Button editer = new Button("✏️ Éditer");
        editer.getStyleClass().add("pill-btn-orange");
        editer.setOnAction(e -> openEdit(r));
        editer.setPrefHeight(36);
        editer.setMinWidth(110);

// Add buttons
        buttonBox.getChildren().addAll(voir, editer);

// CARD STYLE (clean + stable)
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-padding: 18;" +
                        "-fx-spacing: 8;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4);"
        );

// Add elements to card
        card.getChildren().addAll(titleBox, description, date, buttonBox);

        return card;
    }

    private String getStatusColor(String statut) {
        switch (statut.toUpperCase()) {
            case "EN ATTENTE":
                return "#92400e";
            case "RÉSOLUE":
                return "#15803d";
            case "EN COURS":
                return "#7c3aed";
            default:
                return "#64748b";
        }
    }

    private String getStatusBgColor(String statut) {
        switch (statut.toUpperCase()) {
            case "EN ATTENTE":
                return "#fef3c7";
            case "RÉSOLUE":
                return "#dcfce7";
            case "EN COURS":
                return "#f3e8ff";
            default:
                return "#f1f5f9";
        }
    }

    @FXML
    public void resetFilters() {
        searchField.setText("");
        if (statutCombo != null)
            statutCombo.getSelectionModel().selectFirst();
        load();
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        String statut = (statutCombo != null && statutCombo.getValue() != null) ? statutCombo.getValue() : "Tous";

        // Créer une liste mutable pour éviter UnsupportedOperationException
        List<Reclamation> filtered = new java.util.ArrayList<>(
                allReclamations.stream()
                        .filter(r -> (searchText.isEmpty() || r.getTitre().toLowerCase().contains(searchText)
                                || r.getDescription().toLowerCase().contains(searchText)))
                        .filter(r -> statut.equals("Tous") || r.getStatut().equalsIgnoreCase(statut))
                        .toList());

        // Trier aussi les résultats de recherche par date décroissante
        filtered.sort((r1, r2) -> {
            if (r1.getDateCreation() == null || r2.getDateCreation() == null)
                return 0;
            return r2.getDateCreation().compareTo(r1.getDateCreation());
        });

        displayReclamations(filtered);
    }

    private void openDetail(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailReclamation.fxml"));
            Parent root = loader.load();

            DetailReclamationController c = loader.getController();
            c.setData(r);
            c.setHomeController(this);

            Stage stage = new Stage();

            Scene scene = new Scene(root, 900, 650); // 👈 مهم جداً
            stage.setScene(scene);

            stage.setResizable(false);
            stage.setMaximized(false);
            stage.centerOnScreen();

            stage.showAndWait();

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
            stage.showAndWait();

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
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * @FXML
     * public void goBackToHomeAdmin() {
     * try {
     * FXMLLoader loader = new
     * FXMLLoader(getClass().getResource("/HomeAdmin.fxml"));
     * Parent root = loader.load();
     *
     * Stage stage = (Stage) rootVBox.getScene().getWindow();
     *
     * Scene scene = new Scene(root);
     * stage.setScene(scene);
     *
     * // نفس الطريقة اللي تخدم عندك
     * Rectangle2D screen = Screen.getPrimary().getVisualBounds();
     *
     * stage.setX(0);
     * stage.setY(0);
     * System.err.println("Width home reclamation  : " + screen.getWidth());
     * System.err.println("Height home reclamation  : " + screen.getHeight());
     * stage.setWidth(screen.getWidth());
     * stage.setHeight(screen.getHeight());
     *
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * }
     */
    @FXML
    public void goBackToHomeAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeAdmin.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rootVBox.getScene().getWindow();

            stage.setScene(new Scene(root));

            stage.setMaximized(true); // فقط هذا

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}