package controllers;

import models.Reclamation;
import models.Reponse;
import services.ReclamationService;
import services.ReponseService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class HomeAdminController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private DatePicker dateFilter;
    @FXML private ComboBox<String> sortByField;
    @FXML private ComboBox<String> sortOrder;
    @FXML private Label totalReclamationsLabel;
    @FXML private Label enAttenteLabel;
    @FXML private Label enCoursLabel;
    @FXML private Label resolueLabel;
    @FXML private Label sansReponse2JoursLabel;

    @FXML private TableView<Reclamation> table;
    @FXML private TableColumn<Reclamation, String> colTitre;
    @FXML private TableColumn<Reclamation, String> colUser;
    @FXML private TableColumn<Reclamation, String> colStatut;
    @FXML private TableColumn<Reclamation, Date> colDate;
    @FXML private TableColumn<Reclamation, Integer> colReponses;
    @FXML private TableColumn<Reclamation, String> colMessage;
    @FXML private TableColumn<Reclamation, Void> colAction;

    private final ReclamationService service = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();
    private List<Reclamation> allReclamations;

    @FXML private Pagination pagination;
    private final int ITEMS_PER_PAGE = 5;
    private List<Reclamation> currentFilteredList = new ArrayList<>();
    private PauseTransition searchDebounce;

    @FXML
    public void initialize() {
        allReclamations = service.getAll();

        colTitre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));
        colUser.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUserId() == 0 ? "-" : "User #" + data.getValue().getUserId()));
        colStatut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatut()));
        colDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDateCreation()));
        colReponses.setCellValueFactory(data -> {
            int count = reponseService.getByReclamationId(data.getValue().getId()).size();
            return new SimpleObjectProperty<>(count);
        });
        colMessage.setCellValueFactory(data -> {
            List<Reponse> reponses = reponseService.getByReclamationId(data.getValue().getId());
            if (!reponses.isEmpty()) return new SimpleStringProperty(reponses.get(reponses.size() - 1).getContenu());
            return new SimpleStringProperty("-");
        });

        setupUserColumn();
        setupStatusColumn();
        setupReponsesColumn();
        setupDateColumn();

        statusFilter.getItems().addAll("-- Tous --", "EN ATTENTE", "EN COURS", "RÉSOLUE");
        statusFilter.setValue("-- Tous --");
        sortByField.getItems().addAll("Date (défaut)", "Titre", "Statut");
        sortOrder.getItems().addAll("↓ DESC", "↑ ASC");
        sortOrder.setValue("↓ DESC");

        addButtons();
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> updateTablePage(newVal.intValue()));
        searchDebounce = new PauseTransition(Duration.millis(300));
        searchDebounce.setOnFinished(e -> applyFiltersAndSort());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> searchDebounce.playFromStart());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        sortByField.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        sortOrder.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());

        updateStatistics();
        applyFiltersAndSort();
    }

    private void updateStatistics() {
        if (allReclamations == null) return;
        long total = allReclamations.size();
        long enAttente = allReclamations.stream().filter(r -> "EN ATTENTE".equals(r.getStatut())).count();
        long enCours = allReclamations.stream().filter(r -> "EN COURS".equals(r.getStatut())).count();
        long resolue = allReclamations.stream().filter(r -> "RÉSOLUE".equals(r.getStatut())).count();
        long sansReponse2Jours = allReclamations.stream().filter(r -> {
            List<Reponse> reponses = reponseService.getByReclamationId(r.getId());
            if (reponses.isEmpty()) {
                long daysDiff = ChronoUnit.DAYS.between(r.getDateCreation().toLocalDate(), LocalDate.now());
                return daysDiff >= 2;
            }
            return false;
        }).count();
        totalReclamationsLabel.setText(String.valueOf(total));
        enAttenteLabel.setText(String.valueOf(enAttente));
        enCoursLabel.setText(String.valueOf(enCours));
        resolueLabel.setText(String.valueOf(resolue));
        sansReponse2JoursLabel.setText(String.valueOf(sansReponse2Jours));
    }

    private void applyFiltersAndSort() {
        List<Reclamation> filtered = allReclamations.stream()
                .filter(r -> {
                    String searchText = searchField.getText().toLowerCase();
                    if (!searchText.isEmpty()) {
                        String titre = r.getTitre() != null ? r.getTitre().toLowerCase() : "";
                        String desc = r.getDescription() != null ? r.getDescription().toLowerCase() : "";
                        int reponseCount = reponseService.getByReclamationId(r.getId()).size();
                        if (!titre.contains(searchText) && !desc.contains(searchText) &&
                            !String.valueOf(reponseCount).contains(searchText)) return false;
                    }
                    String status = statusFilter.getValue();
                    if (status != null && !status.isEmpty() && !status.contains("Tous") && !r.getStatut().equals(status)) return false;
                    LocalDate selectedDate = dateFilter.getValue();
                    if (selectedDate != null) {
                        LocalDate recDate = ((java.sql.Date) r.getDateCreation()).toLocalDate();
                        if (!recDate.equals(selectedDate)) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        String sortBy = sortByField.getValue();
        boolean isAsc = sortOrder.getValue() != null && sortOrder.getValue().contains("ASC");
        if (sortBy == null || sortBy.contains("Date")) {
            filtered.sort((r1, r2) -> {
                int c = r2.getDateCreation().compareTo(r1.getDateCreation());
                return isAsc ? -c : c;
            });
        } else if (sortBy.contains("Titre")) {
            filtered.sort((r1, r2) -> { int c = r1.getTitre().compareTo(r2.getTitre()); return isAsc ? c : -c; });
        } else if (sortBy.contains("Statut")) {
            filtered.sort((r1, r2) -> { int c = r1.getStatut().compareTo(r2.getStatut()); return isAsc ? c : -c; });
        }

        currentFilteredList = filtered;
        int pageCount = (int) Math.ceil((double) filtered.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setCurrentPageIndex(0);
        updateTablePage(0);
    }

    private void updateTablePage(int pageIndex) {
        if (currentFilteredList == null || currentFilteredList.isEmpty()) { table.getItems().clear(); return; }
        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, currentFilteredList.size());
        if (fromIndex >= currentFilteredList.size()) { table.getItems().clear(); return; }
        table.getItems().setAll(currentFilteredList.subList(fromIndex, toIndex));
    }

    private void setupUserColumn() {
        colUser.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.equals("-")) {
                    setGraphic(new Label("-"));
                } else {
                    HBox box = new HBox(12);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label icon = new Label("👤");
                    icon.setStyle("-fx-font-size: 20;");
                    VBox textVBox = new VBox(2);
                    Label nameLabel = new Label("Utilisateur");
                    nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 12;");
                    textVBox.getChildren().add(nameLabel);
                    box.getChildren().addAll(icon, textVBox);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupDateColumn() {
        colDate.setCellFactory(column -> new TableCell<>() {
            private final java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd/MM/yyyy");
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); }
                else { setText("📅 " + format.format(item)); setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;"); }
            }
        });
    }

    private void setupStatusColumn() {
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); }
                else {
                    Label label = new Label(item);
                    String color = "#64748b", bgColor = "#f1f5f9";
                    if ("RÉSOLUE".equals(item)) { color = "#15803d"; bgColor = "#dcfce7"; }
                    else if ("EN ATTENTE".equals(item)) { color = "#92400e"; bgColor = "#fef3c7"; }
                    else if ("EN COURS".equals(item)) { color = "#7c3aed"; bgColor = "#f3e8ff"; }
                    label.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + color +
                            "; -fx-padding: 6 14; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-weight: 600; -fx-font-size: 11;");
                    setGraphic(label);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
    }

    private void setupReponsesColumn() {
        colReponses.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); }
                else {
                    Label label = new Label(String.valueOf(item));
                    label.setStyle("-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: 600; -fx-font-size: 11;");
                    setGraphic(label);
                    setAlignment(javafx.geometry.Pos.CENTER);
                }
            }
        });
    }

    private void addButtons() {
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button voir = new Button("Détails");
            private final Button editer = new Button("Éditer");
            private final Button supprimer = new Button("🗑");
            {
                voir.setStyle("-fx-background-color: white; -fx-border-color: #6366f1; -fx-border-width: 1.5; -fx-border-radius: 7; -fx-background-radius: 7; -fx-text-fill: #6366f1; -fx-font-size: 11; -fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 6 16;");
                editer.setStyle("-fx-background-color: white; -fx-border-color: #f59e0b; -fx-border-width: 1.5; -fx-border-radius: 7; -fx-background-radius: 7; -fx-text-fill: #f59e0b; -fx-font-size: 11; -fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 6 16;");
                supprimer.setStyle("-fx-background-color: white; -fx-border-color: #ef4444; -fx-border-width: 1.5; -fx-border-radius: 7; -fx-background-radius: 7; -fx-text-fill: #ef4444; -fx-font-size: 12; -fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 6 12;");
                voir.setOnAction(e -> openDetail(getTableView().getItems().get(getIndex())));
                editer.setOnAction(e -> openModifier(getTableView().getItems().get(getIndex())));
                supprimer.setOnAction(e -> delete(getTableView().getItems().get(getIndex())));
            }
            private final HBox box = new HBox(6, voir, editer, supprimer);
            { box.setAlignment(javafx.geometry.Pos.CENTER); }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    public void refresh() {
        allReclamations = service.getAll();
        updateStatistics();
        applyFiltersAndSort();
    }

    private void openDetail(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reclamation/detail-admin.fxml"));
            Parent root = loader.load();
            DetailReclamationControlleradmin c = loader.getController();
            c.setData(r);
            c.setHomeController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 700));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openModifier(Reclamation r) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reclamation/modstatus.fxml"));
            Parent root = loader.load();
            ModifierStatusController c = loader.getController();
            c.setData(r);
            c.setHomeController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 600, 500));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void delete(Reclamation r) {
        if (showDeleteConfirmation("Supprimer la réclamation",
            "Êtes-vous sûr de vouloir supprimer \"" + r.getTitre() + "\" ?")) {
            service.delete(r.getId());
            refresh();
        }
    }

    private boolean showDeleteConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(content);
        try {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/reclamation/style3.css").toExternalForm());
            dialogPane.getStyleClass().add("custom-confirm-dialog");
            Label iconLabel = new Label("?");
            iconLabel.setStyle("-fx-font-size: 26px; -fx-text-fill: white; -fx-background-color: #5856d6; -fx-background-radius: 50; -fx-min-width: 45; -fx-min-height: 45; -fx-alignment: center;");
            alert.setGraphic(iconLabel);
            Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
            Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
            if (okButton != null) { okButton.setText("OK"); okButton.getStyleClass().add("ok-button"); }
            if (cancelButton != null) { cancelButton.setText("Annuler"); cancelButton.getStyleClass().add("cancel-button"); }
        } catch (Exception ignored) {}
        return alert.showAndWait().filter(res -> res == ButtonType.OK).isPresent();
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
        applyFiltersAndSort();
    }

    @FXML
    public void goToHomeReclamation() {
        AppShellController shellController = AppShellController.getInstance();
        if (shellController != null) {
            shellController.showFrontOffice();
        }
    }
}
