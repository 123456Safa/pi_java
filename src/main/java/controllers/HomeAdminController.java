package controllers;

import Model.Reclamation;
import Service.ReclamationService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class HomeAdminController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<String> sortByField;
    @FXML private ComboBox<String> sortOrder;
    
    @FXML private TableView<Reclamation> table;
    @FXML private TableColumn<Reclamation, String> colTitre;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private TableColumn<Reclamation, Date> colDate;
    @FXML private TableColumn<Reclamation, Void> colAction;

    private final ReclamationService service = new ReclamationService();
    private List<Reclamation> allReclamations;

    @FXML
    public void initialize() {
        // Load all reclamations
        allReclamations = service.getAll();

        // yorbit tab bi  reclamation
        colTitre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));
        colDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDateCreation()));

        //  status filter
        statusFilter.getItems().addAll("Tous", "EN ATTENTE", "EN COURS", "RÉSOLUE");

        // sort bi options
        sortByField.getItems().addAll("Date (défaut)", "Titre", "Statut");

        //  order options
        sortOrder.getItems().addAll("↓ DESC", "↑ ASC");
        sortOrder.setValue("↓ DESC");

        addButtons();
        loadTable(allReclamations);

        // Add listeners for real-time search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        sortByField.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        sortOrder.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
    }

    private void applyFiltersAndSort() {
        List<Reclamation> filtered = allReclamations.stream()
                .filter(r -> {
                    // Search by title
                    String searchText = searchField.getText().toLowerCase();
                    if (!searchText.isEmpty() && !r.getTitre().toLowerCase().contains(searchText)) {
                        return false;
                    }

                    // Filter by status - ignore if "Tous" or null
                    String status = statusFilter.getValue();
                    if (status != null && !status.isEmpty() && !status.equals("Tous") && !r.getStatut().equals(status)) {
                        return false;
                    }

                    // Filter by date
                    LocalDate selectedDate = dateFilter.getValue();
                    if (selectedDate != null) {
                        LocalDate reclamationDate = ((java.sql.Date) r.getDateCreation()).toLocalDate();
                        if (!reclamationDate.equals(selectedDate)) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // Apply sorting
        String sortBy = sortByField.getValue();
        boolean isAsc = sortOrder.getValue() != null && sortOrder.getValue().contains("ASC");

        if (sortBy == null || sortBy.contains("Date")) {
            filtered.sort((r1, r2) -> {
                int comparison = r2.getDateCreation().compareTo(r1.getDateCreation());
                return isAsc ? -comparison : comparison;
            });
        } else if (sortBy.contains("Titre")) {
            filtered.sort((r1, r2) -> {
                int comparison = r1.getTitre().compareTo(r2.getTitre());
                return isAsc ? comparison : -comparison;
            });
        } else if (sortBy.contains("Statut")) {
            filtered.sort((r1, r2) -> {
                int comparison = r1.getStatut().compareTo(r2.getStatut());
                return isAsc ? comparison : -comparison;
            });
        }

        loadTable(filtered);
    }

    private void loadTable(List<Reclamation> list) {
        table.getItems().setAll(list);
    }

    private void addButtons() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button voir = new Button("👁️ Voir");
            private final Button editer = new Button("✏️ Éditer");
            private final Button supprimer = new Button("🗑️ Supprimer");

            {
                voir.setStyle("-fx-font-size: 11; -fx-padding: 6 12; -fx-background-color: #00bcd4; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");
                editer.setStyle("-fx-font-size: 11; -fx-padding: 6 12; -fx-background-color: #FFA500; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");
                supprimer.setStyle("-fx-font-size: 11; -fx-padding: 6 12; -fx-background-color: #FF4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");

                voir.setOnAction(e -> openDetail(getTableView().getItems().get(getIndex())));
                editer.setOnAction(e -> openModifier(getTableView().getItems().get(getIndex())));
                supprimer.setOnAction(e -> delete(getTableView().getItems().get(getIndex())));
            }

            private final HBox box = new HBox(8, voir, editer, supprimer);

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    public void refresh() {
        allReclamations = service.getAll();
        applyFiltersAndSort();
    }

    private void openDetail(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DetailReclamationadmin.fxml"));
            Parent root = loader.load();

            DetailReclamationControlleradmin c = loader.getController();
            c.setData(r);
            c.setHomeController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openModifier(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modstatus.fxml"));
            Parent root = loader.load();

            ModifierStatusController c = loader.getController();
            c.setData(r);
            c.setHomeController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void delete(Reclamation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la réclamation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                service.delete(r.getId());
                refresh();
            }
        });
    }

    @FXML
    public void search() {
        applyFiltersAndSort();
    }

    @FXML
    public void resetFilters() {
        searchField.setText("");
        if (statusFilter != null) statusFilter.getSelectionModel().selectFirst();
        if (dateFilter != null) dateFilter.setValue(null);
        if (sortByField != null) sortByField.setValue("Date (défaut)");
        if (sortOrder != null) sortOrder.setValue("↓ DESC");
        loadTable(allReclamations);
    }

    @FXML
    public void goToHomeReclamation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/HomeReclamation.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) table.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
