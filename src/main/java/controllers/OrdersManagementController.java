package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import models.Commandes;
import services.CommandeService;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrdersManagementController {
    private static final String ALL_STATUSES = "Tous les statuts";
    private static final Pattern PRODUCT_NAME_PATTERN = Pattern.compile("\"nom\"\\s*:\\s*\"([^\"]+)\"");

    @FXML
    private TableView<Commandes> ordersTable;
    @FXML
    private TableColumn<Commandes, Integer> idColumn;
    @FXML
    private TableColumn<Commandes, String> produitsColumn;
    @FXML
    private TableColumn<Commandes, Double> totalColumn;
    @FXML
    private TableColumn<Commandes, String> statutColumn;
    @FXML
    private TableColumn<Commandes, String> createdAtColumn;
    @FXML
    private TableColumn<Commandes, Integer> utilisateurIdColumn;
    @FXML
    private TableColumn<Commandes, Void> actionsColumn;
    @FXML
    private Label ordersCountLabel;
    @FXML
    private Label pendingStatLabel;
    @FXML
    private Label confirmedStatLabel;
    @FXML
    private Label deliveredStatLabel;
    @FXML
    private Label cancelledStatLabel;
    @FXML
    private Label statusSummaryLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilterBox;
    @FXML
    private ComboBox<String> sortBox;

    private final CommandeService commandeService = new CommandeService();
    private final ObservableList<Commandes> masterOrders = FXCollections.observableArrayList();
    private final FilteredList<Commandes> filteredOrders = new FilteredList<>(masterOrders, commande -> true);
    private final SortedList<Commandes> sortedOrders = new SortedList<>(filteredOrders);
    private OrderBackofficeNavigator navigator;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        produitsColumn.setCellValueFactory(new PropertyValueFactory<>("produits"));
        produitsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : extractProductNames(item));
            }
        });
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totales"));
        totalColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.3f DT", item));
                }
            }
        });
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statutColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item);
                String normalizedStatus = normalizeStatus(item);
                if (normalizedStatus.contains("attente")) {
                    setStyle("-fx-text-fill: #c27a00; -fx-font-weight: bold;");
                } else if (normalizedStatus.contains("confirm")) {
                    setStyle("-fx-text-fill: #5d4df1; -fx-font-weight: bold;");
                } else if (normalizedStatus.contains("livre") || normalizedStatus.contains("deliver")) {
                    setStyle("-fx-text-fill: #0b8f78; -fx-font-weight: bold;");
                } else if (normalizedStatus.contains("annul")) {
                    setStyle("-fx-text-fill: #d13b4f; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #243248; -fx-font-weight: bold;");
                }
            }
        });
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        utilisateurIdColumn.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));
        setupFilters();
        configureActionsColumn();
        ordersTable.setItems(sortedOrders);
    }

    public void setNavigator(OrderBackofficeNavigator navigator) {
        this.navigator = navigator;
    }

    public void refresh() {
        try {
            masterOrders.setAll(commandeService.select());
            updateStatusFilterOptions();
            applyFilters();
        } catch (SQLException e) {
            masterOrders.clear();
            updateStatusFilterOptions();
            applyFilters();
        }
    }

    private void setupFilters() {
        statusFilterBox.setItems(FXCollections.observableArrayList(ALL_STATUSES));
        statusFilterBox.setValue(ALL_STATUSES);
        statusFilterBox.setOnAction(event -> applyFilters());

        sortBox.setItems(FXCollections.observableArrayList(
                "Plus recentes",
                "Plus anciennes",
                "Total croissant",
                "Total decroissant",
                "ID croissant",
                "ID decroissant"
        ));
        sortBox.setValue("Plus recentes");
        sortBox.setOnAction(event -> applySort());

        searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    private void updateStatusFilterOptions() {
        String currentValue = statusFilterBox.getValue();
        ObservableList<String> statuses = FXCollections.observableArrayList();
        statuses.add(ALL_STATUSES);
        masterOrders.stream()
                .map(Commandes::getStatut)
                .filter(status -> status != null && !status.isBlank())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .forEach(statuses::add);

        statusFilterBox.setItems(statuses);
        if (currentValue != null && statuses.contains(currentValue)) {
            statusFilterBox.setValue(currentValue);
        } else {
            statusFilterBox.setValue(ALL_STATUSES);
        }
    }

    private void applyFilters() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStatus = statusFilterBox.getValue();

        filteredOrders.setPredicate(commande -> matchesSearch(commande, keyword) && matchesStatus(commande, selectedStatus));
        applySort();
        updateOrdersCount();
        updateStatusStats();
    }

    private boolean matchesSearch(Commandes commande, String keyword) {
        if (keyword.isBlank()) {
            return true;
        }

        return String.valueOf(commande.getId()).contains(keyword)
                || safeLower(commande.getProduits()).contains(keyword)
                || safeLower(commande.getStatut()).contains(keyword)
                || String.valueOf(commande.getUtilisateurId()).contains(keyword)
                || safeLower(commande.getCreatedAt()).contains(keyword)
                || String.format(Locale.ROOT, "%.2f", commande.getTotales()).contains(keyword);
    }

    private boolean matchesStatus(Commandes commande, String selectedStatus) {
        return selectedStatus == null
                || ALL_STATUSES.equals(selectedStatus)
                || selectedStatus.equalsIgnoreCase(commande.getStatut());
    }

    private void applySort() {
        Comparator<Commandes> comparator = switch (sortBox.getValue() == null ? "" : sortBox.getValue()) {
            case "Plus anciennes" -> Comparator.comparing(Commandes::getCreatedAt, Comparator.nullsLast(String::compareToIgnoreCase)).thenComparingInt(Commandes::getId);
            case "Total croissant" -> Comparator.comparingDouble(Commandes::getTotales).thenComparingInt(Commandes::getId);
            case "Total decroissant" -> Comparator.comparingDouble(Commandes::getTotales).reversed().thenComparingInt(Commandes::getId);
            case "ID croissant" -> Comparator.comparingInt(Commandes::getId);
            case "ID decroissant" -> Comparator.comparingInt(Commandes::getId).reversed();
            default -> Comparator.comparing(Commandes::getCreatedAt, Comparator.nullsLast(String::compareToIgnoreCase)).reversed()
                    .thenComparing(Comparator.comparingInt(Commandes::getId).reversed());
        };
        sortedOrders.setComparator(comparator);
    }

    private void updateOrdersCount() {
        int count = filteredOrders.size();
        ordersCountLabel.setText(count + (count > 1 ? " commandes" : " commande"));
    }

    private void updateStatusStats() {
        long pendingCount = filteredOrders.stream()
                .filter(commande -> normalizeStatus(commande.getStatut()).contains("attente"))
                .count();
        long confirmedCount = filteredOrders.stream()
                .filter(commande -> normalizeStatus(commande.getStatut()).contains("confirm"))
                .count();
        long deliveredCount = filteredOrders.stream()
                .filter(commande -> normalizeStatus(commande.getStatut()).contains("livre") || normalizeStatus(commande.getStatut()).contains("deliver"))
                .count();
        long cancelledCount = filteredOrders.stream()
                .filter(commande -> normalizeStatus(commande.getStatut()).contains("annul"))
                .count();

        pendingStatLabel.setText(String.valueOf(pendingCount));
        confirmedStatLabel.setText(String.valueOf(confirmedCount));
        deliveredStatLabel.setText(String.valueOf(deliveredCount));
        cancelledStatLabel.setText(String.valueOf(cancelledCount));

        String dominantStatus = "Aucun statut dominant";
        long maxCount = Math.max(Math.max(pendingCount, confirmedCount), Math.max(deliveredCount, cancelledCount));
        if (maxCount > 0) {
            if (maxCount == pendingCount) {
                dominantStatus = "Statut dominant : En attente (" + pendingCount + ")";
            } else if (maxCount == confirmedCount) {
                dominantStatus = "Statut dominant : Confirmee (" + confirmedCount + ")";
            } else if (maxCount == deliveredCount) {
                dominantStatus = "Statut dominant : Livree (" + deliveredCount + ")";
            } else {
                dominantStatus = "Statut dominant : Annulee (" + cancelledCount + ")";
            }
        }
        statusSummaryLabel.setText(dominantStatus);
    }

    private void configureActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button detailsButton = new Button("👁");
            private final ComboBox<String> statusBox = new ComboBox<>();
            private final Button deleteButton = new Button("🗑");
            private final HBox container = new HBox(5);

            {
                detailsButton.getStyleClass().addAll("action-button", "secondary-action");
                deleteButton.getStyleClass().addAll("action-button", "danger-action");
                
                // Add tooltips since we removed text
                detailsButton.setTooltip(new javafx.scene.control.Tooltip("Voir détails"));
                deleteButton.setTooltip(new javafx.scene.control.Tooltip("Supprimer"));
                
                statusBox.getItems().setAll("En attente", "Confirmee", "Livree", "Annulee");
                statusBox.setPromptText("Statut");
                statusBox.getStyleClass().add("status-combo");
                statusBox.setPrefWidth(120);

                detailsButton.setOnAction(event -> {
                    Commandes commande = getTableView().getItems().get(getIndex());
                    if (navigator != null) {
                        navigator.showOrderDetails(commande);
                    }
                });

                statusBox.setOnAction(event -> {
                    Commandes commande = getTableView().getItems().get(getIndex());
                    String newStatus = statusBox.getValue();
                    if (commande != null && newStatus != null && !newStatus.equals(commande.getStatut())) {
                        updateOrderStatus(commande, newStatus);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Commandes commande = getTableView().getItems().get(getIndex());
                    if (commande != null) {
                        deleteOrder(commande);
                    }
                });

                container.getChildren().addAll(detailsButton, statusBox, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Commandes commande = getTableView().getItems().get(getIndex());
                statusBox.setValue(commande.getStatut());
                setGraphic(container);
            }
        });
    }

    private void updateOrderStatus(Commandes commande, String newStatus) {
        try {
            commande.setStatut(newStatus);
            commandeService.update(commande);
            refresh();
        } catch (SQLException e) {
            showError("Statut non modifie", "La mise a jour du statut a echoue.");
        }
    }

    private void deleteOrder(Commandes commande) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la commande #" + commande.getId() + " ?",
                ButtonType.YES,
                ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.YES) {
                try {
                    commandeService.delete(commande.getId());
                    refresh();
                } catch (SQLException e) {
                    showError("Suppression impossible", "La commande n'a pas pu etre supprimee.");
                }
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String normalizeStatus(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String extractProductNames(String produits) {
        if (produits == null || produits.isBlank()) {
            return "";
        }

        Matcher matcher = PRODUCT_NAME_PATTERN.matcher(produits);
        StringBuilder names = new StringBuilder();
        while (matcher.find()) {
            if (names.length() > 0) {
                names.append(", ");
            }
            names.append(matcher.group(1));
        }

        return names.length() > 0 ? names.toString() : produits;
    }
}
