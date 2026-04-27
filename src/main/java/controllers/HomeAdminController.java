package controllers;

import Model.Reclamation;
import Model.Reponse;
import Service.ReclamationService;
import Service.ReponseService;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class HomeAdminController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private DatePicker dateFilter;
    @FXML
    private ComboBox<String> sortByField;
    @FXML
    private ComboBox<String> sortOrder;
    @FXML
    private Label totalReclamationsLabel;
    @FXML
    private Label enAttenteLabel;
    @FXML
    private Label enCoursLabel;
    @FXML
    private Label resolueLabel;

    @FXML
    private TableView<Reclamation> table;
    @FXML
    private TableColumn<Reclamation, String> colTitre;
    @FXML
    private TableColumn<Reclamation, String> colUser;
    @FXML
    private TableColumn<Reclamation, String> colStatut;
    @FXML
    private TableColumn<Reclamation, Date> colDate;
    @FXML
    private TableColumn<Reclamation, Integer> colReponses;
    @FXML
    private TableColumn<Reclamation, String> colMessage;
    @FXML
    private TableColumn<Reclamation, Void> colAction;

    private final ReclamationService service = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();
    private List<Reclamation> allReclamations;

    @FXML
    private Pagination pagination;
    private final int ITEMS_PER_PAGE = 5;
    private List<Reclamation> currentFilteredList = new ArrayList<>();
    private PauseTransition searchDebounce;

    @FXML
    public void initialize() {
        // Load all reclamations
        allReclamations = service.getAll();

        // Configuration des colonnes
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
            if (!reponses.isEmpty()) {
                return new SimpleStringProperty(reponses.get(reponses.size() - 1).getContenu());
            }
            return new SimpleStringProperty("-");
        });

        setupUserColumn();
        setupStatusColumn();
        setupReponsesColumn();
        setupDateColumn();

        // status filter
        statusFilter.getItems().addAll("-- Tous --", "EN ATTENTE", "EN COURS", "RÉSOLUE");
        statusFilter.setValue("-- Tous --");

        // sort bi options
        sortByField.getItems().addAll("Date (défaut)", "Titre", "Statut");

        // order options
        sortOrder.getItems().addAll("↓ DESC", "↑ ASC");
        sortOrder.setValue("↓ DESC");

        addButtons();
        // pagination.setPageFactory(this::createPage);
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateTablePage(newVal.intValue());
        });
        // Ajax-style live search with 300ms debounce
        searchDebounce = new PauseTransition(Duration.millis(300));
        searchDebounce.setOnFinished(e -> applyFiltersAndSort());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchDebounce.playFromStart();
        });
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        sortByField.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        sortOrder.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSort());

        updateStatistics();
        applyFiltersAndSort();
    }

    private void updateStatistics() {
        if (allReclamations == null)
            return;

        long total = allReclamations.size();
        long enAttente = allReclamations.stream().filter(r -> "EN ATTENTE".equals(r.getStatut())).count();
        long enCours = allReclamations.stream().filter(r -> "EN COURS".equals(r.getStatut())).count();
        long resolue = allReclamations.stream().filter(r -> "RÉSOLUE".equals(r.getStatut())).count();

        totalReclamationsLabel.setText(String.valueOf(total));
        enAttenteLabel.setText(String.valueOf(enAttente));
        enCoursLabel.setText(String.valueOf(enCours));
        resolueLabel.setText(String.valueOf(resolue));
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
                    if (status != null && !status.isEmpty() && !status.contains("Tous")
                            && !r.getStatut().equals(status)) {
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

        currentFilteredList = filtered;
        /*
         * int pageCount = (int) Math.ceil((double) filtered.size() / ITEMS_PER_PAGE);
         * pagination.setPageCount(pageCount == 0 ? 1 : pageCount);
         * pagination.setCurrentPageIndex(0);
         * updateTablePage(0);
         */
        int pageCount = (int) Math.ceil((double) filtered.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setCurrentPageIndex(0);
        updateTablePage(0);
    }

    private javafx.scene.Node createPage(int pageIndex) {
        updateTablePage(pageIndex);
        return table;
    }

    /*
     * private void updateTablePage(int pageIndex) {
     * if (currentFilteredList == null)
     * return;
     * int fromIndex = pageIndex * ITEMS_PER_PAGE;
     * int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE,
     * currentFilteredList.size());
     * if (fromIndex <= toIndex && fromIndex < currentFilteredList.size()) {
     * table.getItems().setAll(currentFilteredList.subList(fromIndex, toIndex));
     * } else {
     * table.getItems().clear();
     * }
     * }
     */
    private void updateTablePage(int pageIndex) {
        if (currentFilteredList == null || currentFilteredList.isEmpty()) {
            table.getItems().clear();
            return;
        }

        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, currentFilteredList.size());

        if (fromIndex >= currentFilteredList.size()) {
            table.getItems().clear();
            return;
        }

        table.getItems().setAll(currentFilteredList.subList(fromIndex, toIndex));
    }

     private void setupUserColumn() {
         colUser.setCellFactory(column -> new TableCell<>() {
             @Override
             protected void updateItem(String item, boolean empty) {
                 super.updateItem(item, empty);
                 if (empty || item == null || item.equals("-")) {
                     setGraphic(new Label("-"));
                     setStyle("-fx-padding: 0;");
                 } else {
                     HBox box = new HBox(12);
                     box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                     
                     // Avatar circle background
                     Label icon = new Label("👤");
                     icon.setStyle("-fx-font-size: 20;");
                     
                     VBox textVBox = new VBox(2);
                     textVBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                     
                     Label nameLabel = new Label("Utilisateur");
                     nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 12; -fx-text-fill: #1e293b;");
                     
                     Label emailLabel = new Label("user" + getIndex() + "@example.com");
                     emailLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11;");
                     
                     textVBox.getChildren().addAll(nameLabel, emailLabel);
                     box.getChildren().addAll(icon, textVBox);
                     setGraphic(box);
                     setStyle("-fx-padding: 8 0;");
                 }
             }
         });
     }

     private void setupDateColumn() {
         colDate.setCellFactory(column -> new TableCell<>() {
             private final java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

             @Override
             protected void updateItem(Date item, boolean empty) {
                 super.updateItem(item, empty);
                 if (empty || item == null) {
                     setText(null);
                     setStyle("-fx-padding: 0;");
                 } else {
                     setText("📅 " + format.format(item));
                     setStyle("-fx-text-fill: #64748b; -fx-font-size: 11; -fx-padding: 8 0;");
                 }
             }
         });
     }

     private void setupStatusColumn() {
         colStatut.setCellFactory(column -> new TableCell<>() {
             @Override
             protected void updateItem(String item, boolean empty) {
                 super.updateItem(item, empty);
                 if (empty || item == null) {
                     setGraphic(null);
                     setStyle("-fx-padding: 0;");
                 } else {
                     Label label = new Label(item);
                     label.getStyleClass().add("status-label");
                     String color = "#64748b";
                     String bgColor = "#f1f5f9";

                     if ("RÉSOLUE".equals(item)) {
                         color = "#15803d";
                         bgColor = "#dcfce7";
                     } else if ("EN ATTENTE".equals(item)) {
                         color = "#92400e";
                         bgColor = "#fef3c7";
                     } else if ("EN COURS".equals(item)) {
                         color = "#7c3aed";
                         bgColor = "#f3e8ff";
                     }

                     label.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: " + color
                             + "; -fx-padding: 6 14; -fx-border-radius: 20; -fx-background-radius: 20; -fx-font-weight: 600; -fx-font-size: 11;");
                     setGraphic(label);
                     setAlignment(javafx.geometry.Pos.CENTER);
                     setStyle("-fx-padding: 8 0;");
                 }
             }
         });
     }

     private void setupReponsesColumn() {
         colReponses.setCellFactory(column -> new TableCell<>() {
             @Override
             protected void updateItem(Integer item, boolean empty) {
                 super.updateItem(item, empty);
                 if (empty || item == null) {
                     setGraphic(null);
                     setStyle("-fx-padding: 0;");
                 } else {
                     Label label = new Label(String.valueOf(item));
                     label.setStyle(
                             "-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: 600; -fx-font-size: 11;");
                     setGraphic(label);
                     setAlignment(javafx.geometry.Pos.CENTER);
                     setStyle("-fx-padding: 8 0;");
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
                 voir.setStyle(
                         "-fx-background-color: white; -fx-border-color: #6366f1; -fx-border-width: 1.5; -fx-border-radius: 7; -fx-background-radius: 7; -fx-text-fill: #6366f1; -fx-font-size: 11; -fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 6 16;");
                 editer.setStyle(
                         "-fx-background-color: white; -fx-border-color: #f59e0b; -fx-border-width: 1.5; -fx-border-radius: 7; -fx-background-radius: 7; -fx-text-fill: #f59e0b; -fx-font-size: 11; -fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 6 16;");
                 supprimer.setStyle(
                         "-fx-background-color: white; -fx-border-color: #ef4444; -fx-border-width: 1.5; -fx-border-radius: 7; -fx-background-radius: 7; -fx-text-fill: #ef4444; -fx-font-size: 12; -fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 6 12;");

                 // Textes boutons
                 voir.setText("Détails");
                 editer.setText("Éditer");
                 supprimer.setText("🗑");

                 voir.setOnAction(e -> openDetail(getTableView().getItems().get(getIndex())));
                 editer.setOnAction(e -> openModifier(getTableView().getItems().get(getIndex())));
                 supprimer.setOnAction(e -> delete(getTableView().getItems().get(getIndex())));
             }

             private final HBox box = new HBox(6, voir, editer, supprimer);
             {
                 box.setAlignment(javafx.geometry.Pos.CENTER);
             }

             @Override
             protected void updateItem(Void item, boolean empty) {
                 super.updateItem(item, empty);
                 setGraphic(empty ? null : box);
                 setStyle("-fx-padding: 8 0;");
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
        if (statusFilter != null)
            statusFilter.getSelectionModel().selectFirst();
        if (dateFilter != null)
            dateFilter.setValue(null);
        if (sortByField != null)
            sortByField.setValue("Date (défaut)");
        if (sortOrder != null)
            sortOrder.setValue("↓ DESC");
        applyFiltersAndSort();
    }
    /*
     * @FXML
     * public void goToHomeReclamation() {
     * try {
     * FXMLLoader loader = new
     * FXMLLoader(getClass().getResource("/HomeReclamation.fxml"));
     * Parent root = loader.load();
     * 
     * Stage stage = (Stage) table.getScene().getWindow();
     * 
     * Scene scene = new Scene(root);
     * stage.setScene(scene);
     * System.err.println("Width home admin  : " +
     * Screen.getPrimary().getVisualBounds().getWidth());
     * System.err.println("Height home admin  X: " + 816.0);
     * stage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
     * stage.setHeight(816.0);
     * stage.setX(0);
     * stage.setY(0);
     */

    /*
     * FXMLLoader loader = new
     * FXMLLoader(getClass().getResource("/HomeReclamation.fxml"));
     * Parent root = loader.load();
     * 
     * Stage stage = (Stage) table.getScene().getWindow();
     * 
     * Scene scene = new Scene(root);
     * stage.setScene(scene);
     * 
     * // نفس الطريقة اللي تخدم عندك
     * Rectangle2D screen = Screen.getPrimary().getVisualBounds();
     * 
     * stage.setX(0);
     * stage.setY(0);
     * System.err.println("Width home admin  : " + screen.getWidth());
     * System.err.println("Height home admin  : " + screen.getHeight());
     * stage.setWidth(screen.getWidth());
     * stage.setHeight(screen.getHeight());
     * 
     * } catch (Exception e) {
     * e.printStackTrace();
     * }
     * }
     */
    @FXML
    public void goToHomeReclamation() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/HomeReclamation.fxml"));

            Platform.runLater(() -> {
                Stage stage = (Stage) table.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setMaximized(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
