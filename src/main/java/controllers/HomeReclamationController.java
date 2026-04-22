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
import javafx.geometry.Insets;
import javafx.stage.Screen;
import javafx.stage.Stage;
import Model.Reclamation;
import Service.ReclamationService;

import java.text.SimpleDateFormat;
import java.util.List;

public class HomeReclamationController {

    @FXML private Pagination pagination;
    private final int ITEMS_PER_PAGE = 5;
    private List<Reclamation> currentFilteredList;

    @FXML
    private TextField searchField;

    @FXML
    private VBox rootVBox;

    @FXML private ComboBox<String> statutCombo;

    private final ReclamationService service = new ReclamationService();
    private List<Reclamation> allReclamations;

    @FXML
    public void initialize() {
        load();
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
        scrollPane.setStyle("-fx-border-color: transparent; -fx-background-color: transparent;");
        scrollPane.setFitToWidth(true);

        VBox box = new VBox(15);
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
//hathi partie design
    private VBox createReclamationCard(Reclamation r) {
        VBox card = new VBox(10);
        card.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8; -fx-background-color: white; -fx-padding: 15;");
        card.setPrefWidth(Double.MAX_VALUE);

        // Title and Status HBox
        HBox titleBox = new HBox(15);
        titleBox.setStyle("-fx-alignment: center-left;");
        
        Label titre = new Label(r.getTitre());
        titre.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #333333;");
        
        Label statut = new Label(r.getStatut());
        String statusColor = getStatusColor(r.getStatut());
        statut.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 5 10; -fx-border-radius: 3;");
        
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        titleBox.getChildren().addAll(titre, statut);
        
        // Description
        Label description = new Label(r.getDescription());
        description.setStyle("-fx-font-size: 12; -fx-text-fill: #7a7a7a; -fx-wrap-text: true;");
        description.setWrapText(true);
        
        // Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        Label date = new Label("📅 " + sdf.format(r.getDateCreation()));
        date.setStyle("-fx-font-size: 11; -fx-text-fill: #999999;");
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center-left;");
        
        Button voir = new Button("Voir");
        voir.setStyle("-fx-padding: 6 20; -fx-font-size: 11; -fx-border-radius: 4; -fx-border-color: #0066cc; -fx-text-fill: #0066cc; -fx-background-color: transparent; -fx-cursor: hand;");
        voir.setOnAction(e -> openDetail(r));
        
        Button editer = new Button("Éditer");
        editer.setStyle("-fx-padding: 6 20; -fx-font-size: 11; -fx-border-radius: 4; -fx-border-color: #0066cc; -fx-text-fill: #0066cc; -fx-background-color: transparent; -fx-cursor: hand;");
        editer.setOnAction(e -> openEdit(r));
        
        buttonBox.getChildren().addAll(voir, editer);
        
        card.getChildren().addAll(titleBox, description, date, buttonBox);
        return card;
    }

    private String getStatusColor(String statut) {
        switch (statut.toUpperCase()) {
            case "EN ATTENTE":
                return "#FFA500";
            case "RÉSOLUE":
                return "#28a745";
            case "EN COURS":
                return "#17a2b8";
            default:
                return "#999999";
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

        // Créer une liste mutable pour éviter UnsupportedOperationException
        List<Reclamation> filtered = new java.util.ArrayList<>(
            allReclamations.stream()
                .filter(r -> (searchText.isEmpty() || r.getTitre().toLowerCase().contains(searchText) || r.getDescription().toLowerCase().contains(searchText)))
                .filter(r -> statut.equals("Tous") || r.getStatut().equalsIgnoreCase(statut))
                .toList()
        );

        // Trier aussi les résultats de recherche par date décroissante
        filtered.sort((r1, r2) -> {
            if (r1.getDateCreation() == null || r2.getDateCreation() == null) return 0;
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
            c.setHomeController(this); // Pass reference bch najim na3mil refreach li page home
//creer nouvelle window
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
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
    @FXML
    public void goBackToHomeAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeAdmin.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rootVBox.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);

            // نفس الطريقة اللي تخدم عندك
            Rectangle2D screen = Screen.getPrimary().getVisualBounds();

            stage.setX(0);
            stage.setY(0);
            System.err.println("Width home reclamation  : " + screen.getWidth());
            System.err.println("Height home reclamation  : " + screen.getHeight());
            stage.setWidth(screen.getWidth());
            stage.setHeight(screen.getHeight());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
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