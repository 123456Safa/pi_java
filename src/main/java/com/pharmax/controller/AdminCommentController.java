package com.pharmax.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.pharmax.model.Commentaire;
import com.pharmax.model.CommentaireArchive;
import com.pharmax.service.ArticleService;
import com.pharmax.service.CommentaireService;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * AdminCommentController — Back Office for Commentaires.
 * Features: multi-select delete, modern stat cards, animated tables.
 */
public class AdminCommentController {

    private final CommentaireService commentaireService;

    private TableView<Commentaire> commentTable;
    private TableView<CommentaireArchive> archiveTable;
    private final Map<Integer, SimpleBooleanProperty> commentSelectionMap = new HashMap<>();
    private final Map<Integer, SimpleBooleanProperty> archiveSelectionMap = new HashMap<>();

    // Stats
    private Label lblApproved, lblPending, lblBlocked, lblTotal;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminCommentController(CommentaireService commentaireService, ArticleService articleService) {
        this.commentaireService = commentaireService;
    }

    public VBox buildView() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f0f2f5;");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(18, 25, 18, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #e2e8f0 transparent; "
                + "-fx-border-width: 0 0 1 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 3, 0, 0, 1);");

        Label headerTitle = new Label("💬 Comment Management");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        headerTitle.setStyle("-fx-text-fill: #1a202c;");

        header.getChildren().add(headerTitle);

        // Content
        VBox content = new VBox(16);
        content.setPadding(new Insets(20, 25, 20, 25));
        VBox.setVgrow(content, Priority.ALWAYS);

        // Stats bar
        HBox statsBar = buildStatsBar();

        // Comment toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 16, 10, 16));
        toolbar.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 3, 0, 0, 1);");

        Button btnDeleteSelected = new Button("🗑 Delete Selected");
        btnDeleteSelected.getStyleClass().add("btn-danger");
        btnDeleteSelected.setOnAction(e -> onDeleteSelectedComments());

        Button btnShow = new Button("👁 Details");
        btnShow.setStyle("-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 8 16; -fx-cursor: hand; -fx-background-radius: 8;");
        btnShow.setOnAction(e -> onShowComment());

        Region toolSpacer = new Region();
        HBox.setHgrow(toolSpacer, Priority.ALWAYS);

        CheckBox selectAllComments = new CheckBox("Select All");
        selectAllComments.setStyle("-fx-font-size: 12; -fx-text-fill: #718096;");
        selectAllComments.setOnAction(e -> {
            boolean selected = selectAllComments.isSelected();
            for (SimpleBooleanProperty prop : commentSelectionMap.values()) {
                prop.set(selected);
            }
        });

        toolbar.getChildren().addAll(btnDeleteSelected, btnShow, toolSpacer, selectAllComments);

        // Comment table
        Label lblComments = new Label("Active Comments");
        lblComments.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblComments.setStyle("-fx-text-fill: #1a202c;");

        commentTable = buildCommentTable();
        VBox.setVgrow(commentTable, Priority.ALWAYS);

        // Archive section
        Separator sep = new Separator();
        sep.setStyle("-fx-padding: 5 0;");

        HBox archiveHeader = new HBox(10);
        archiveHeader.setAlignment(Pos.CENTER_LEFT);
        archiveHeader.setPadding(new Insets(10, 16, 10, 16));
        archiveHeader.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 3, 0, 0, 1);");

        Label lblArchives = new Label("📦 Blocked Comments Archive");
        lblArchives.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblArchives.setStyle("-fx-text-fill: #1a202c;");

        Region archiveSpacer = new Region();
        HBox.setHgrow(archiveSpacer, Priority.ALWAYS);

        Button btnDeleteSelectedArchive = new Button("🗑 Delete Selected");
        btnDeleteSelectedArchive.getStyleClass().add("btn-danger");
        btnDeleteSelectedArchive.setOnAction(e -> onDeleteSelectedArchives());

        CheckBox selectAllArchives = new CheckBox("Select All");
        selectAllArchives.setStyle("-fx-font-size: 12; -fx-text-fill: #718096;");
        selectAllArchives.setOnAction(e -> {
            boolean selected = selectAllArchives.isSelected();
            for (SimpleBooleanProperty prop : archiveSelectionMap.values()) {
                prop.set(selected);
            }
        });

        archiveHeader.getChildren().addAll(lblArchives, archiveSpacer, btnDeleteSelectedArchive, selectAllArchives);

        archiveTable = buildArchiveTable();

        content.getChildren().addAll(statsBar, toolbar, lblComments, commentTable,
                sep, archiveHeader, archiveTable);

        root.getChildren().addAll(header, content);
        return root;
    }

    // ─── Table Builders ────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private TableView<Commentaire> buildCommentTable() {
        TableView<Commentaire> tv = new TableView<>();
        tv.setPlaceholder(new Label("No comments"));
        tv.setPrefHeight(220);
        tv.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        // Checkbox column
        TableColumn<Commentaire, Boolean> colSelect = new TableColumn<>("");
        colSelect.setPrefWidth(40);
        colSelect.setSortable(false);
        colSelect.setCellFactory(col -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();
            private javafx.beans.property.SimpleBooleanProperty boundProperty = null;
            
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                
                if (boundProperty != null) {
                    cb.selectedProperty().unbindBidirectional(boundProperty);
                    boundProperty = null;
                }
                
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Commentaire c = getTableRow().getItem();
                    if (c.getId() != null) {
                        commentSelectionMap.putIfAbsent(c.getId(), new SimpleBooleanProperty(false));
                        boundProperty = commentSelectionMap.get(c.getId());
                        cb.selectedProperty().bindBidirectional(boundProperty);
                    }
                    setGraphic(cb);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Commentaire, String> colContenu = new TableColumn<>("Content");
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colContenu.setPrefWidth(280);

        TableColumn<Commentaire, String> colStatut = new TableColumn<>("Status");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(100);
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    String style;
                    switch (item.toLowerCase()) {
                        case "valide" -> style = "-fx-background-color: rgba(39,174,96,0.15); -fx-text-fill: #27ae60;";
                        case "en_attente" -> style = "-fx-background-color: rgba(243,156,18,0.15); -fx-text-fill: #d68910;";
                        default -> style = "-fx-background-color: rgba(231,76,60,0.15); -fx-text-fill: #e74c3c;";
                    }
                    badge.setStyle(style + " -fx-padding: 3 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11;");
                    setGraphic(badge);
                    setText("");
                }
            }
        });

        TableColumn<Commentaire, String> colArticle = new TableColumn<>("Article");
        colArticle.setPrefWidth(180);
        colArticle.setCellValueFactory(cellData -> {
            Commentaire c = cellData.getValue();
            String name = c.getArticle() != null ? c.getArticle().getTitre() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(name);
        });

        TableColumn<Commentaire, LocalDateTime> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("datePublication"));
        colDate.setPrefWidth(130);
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(DATE_FMT));
            }
        });

        tv.getColumns().addAll(colSelect, colContenu, colStatut, colArticle, colDate);
        return tv;
    }

    @SuppressWarnings("unchecked")
    private TableView<CommentaireArchive> buildArchiveTable() {
        TableView<CommentaireArchive> tv = new TableView<>();
        tv.setPlaceholder(new Label("No archives"));
        tv.setPrefHeight(140);
        tv.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        // Checkbox column
        TableColumn<CommentaireArchive, Boolean> colSelect = new TableColumn<>("");
        colSelect.setPrefWidth(40);
        colSelect.setSortable(false);
        colSelect.setCellFactory(col -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();
            private javafx.beans.property.SimpleBooleanProperty boundProperty = null;
            
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                
                if (boundProperty != null) {
                    cb.selectedProperty().unbindBidirectional(boundProperty);
                    boundProperty = null;
                }
                
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    CommentaireArchive a = getTableRow().getItem();
                    if (a.getId() != null) {
                        archiveSelectionMap.putIfAbsent(a.getId(), new SimpleBooleanProperty(false));
                        boundProperty = archiveSelectionMap.get(a.getId());
                        cb.selectedProperty().bindBidirectional(boundProperty);
                    }
                    setGraphic(cb);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<CommentaireArchive, String> colContenu = new TableColumn<>("Content");
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colContenu.setPrefWidth(280);

        TableColumn<CommentaireArchive, String> colUser = new TableColumn<>("User");
        colUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colUser.setPrefWidth(120);

        TableColumn<CommentaireArchive, String> colReason = new TableColumn<>("Reason");
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colReason.setPrefWidth(100);

        TableColumn<CommentaireArchive, LocalDateTime> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("datePublication"));
        colDate.setPrefWidth(130);
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(DATE_FMT));
            }
        });

        tv.getColumns().addAll(colSelect, colContenu, colUser, colReason, colDate);
        return tv;
    }

    // ─── Stats Bar ─────────────────────────────────────────────

    private HBox buildStatsBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);

        lblApproved = new Label("0");
        VBox vApproved = createStatCard("APPROVED", lblApproved, "#27ae60", "✅");

        lblPending = new Label("0");
        VBox vPending = createStatCard("PENDING", lblPending, "#f39c12", "⏳");

        lblBlocked = new Label("0");
        VBox vBlocked = createStatCard("BLOCKED", lblBlocked, "#e74c3c", "🚫");

        lblTotal = new Label("0");
        VBox vTotal = createStatCard("TOTAL", lblTotal, "#2d8659", "📊");

        bar.getChildren().addAll(vApproved, vPending, vBlocked, vTotal);
        HBox.setHgrow(vApproved, Priority.ALWAYS);
        HBox.setHgrow(vPending, Priority.ALWAYS);
        HBox.setHgrow(vBlocked, Priority.ALWAYS);
        HBox.setHgrow(vTotal, Priority.ALWAYS);

        return bar;
    }

    private VBox createStatCard(String title, Label numberLabel, String color, String icon) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 2);");

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18;");
        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill: #718096; -fx-font-size: 10; -fx-font-weight: bold;");
        topRow.getChildren().addAll(iconLabel, lbl);

        numberLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        numberLabel.setStyle("-fx-text-fill: " + color + ";");

        card.getChildren().addAll(topRow, numberLabel);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 2);"));

        return card;
    }

    // ─── Actions ────────────────────────────────────────────────

    private void onDeleteSelectedComments() {
        List<Integer> toDelete = new ArrayList<>();
        for (Map.Entry<Integer, SimpleBooleanProperty> entry : commentSelectionMap.entrySet()) {
            if (entry.getValue().get()) {
                toDelete.add(entry.getKey());
            }
        }

        if (toDelete.isEmpty()) {
            Commentaire selected = commentTable.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getId() != null) {
                toDelete.add(selected.getId());
            }
        }

        if (toDelete.isEmpty()) {
            showAlert("Please select comment(s) to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + toDelete.size() + " comment(s)?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            new Thread(() -> {
                commentaireService.deleteMultiple(toDelete);
                Platform.runLater(() -> {
                    commentSelectionMap.clear();
                    commentTable.getItems().removeIf(c -> toDelete.contains(c.getId()));
                    updateStats();
                });
            }).start();
        }
    }

    private void onDeleteSelectedArchives() {
        List<Integer> toDelete = new ArrayList<>();
        for (Map.Entry<Integer, SimpleBooleanProperty> entry : archiveSelectionMap.entrySet()) {
            if (entry.getValue().get()) {
                toDelete.add(entry.getKey());
            }
        }

        if (toDelete.isEmpty()) {
            CommentaireArchive selected = archiveTable.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getId() != null) {
                toDelete.add(selected.getId());
            }
        }

        if (toDelete.isEmpty()) {
            showAlert("Please select archive(s) to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete " + toDelete.size() + " archive(s)?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            new Thread(() -> {
                commentaireService.deleteMultipleArchives(toDelete);
                Platform.runLater(() -> {
                    archiveSelectionMap.clear();
                    archiveTable.getItems().removeIf(a -> toDelete.contains(a.getId()));
                    updateStats();
                });
            }).start();
        }
    }

    private void onShowComment() {
        Commentaire selected = commentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a comment.");
            return;
        }
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Comment #" + selected.getId());
        info.setHeaderText("Comment Details");
        info.setContentText(
                "Content: " + selected.getContenu() + "\n\n" +
                "Status: " + selected.getStatut() + "\n" +
                "Article: " + (selected.getArticle() != null ? selected.getArticle().getTitre() : "N/A") + "\n" +
                "Date: " + (selected.getDatePublication() != null ? selected.getDatePublication().format(DATE_FMT) : "N/A"));
        info.showAndWait();
    }

    // ─── Helpers ────────────────────────────────────────────────

    public void refresh() {
        new Thread(() -> {
            try {
                var allComments = commentaireService.findAll();
                var allArchives = commentaireService.findAllArchives();
                
                Platform.runLater(() -> {
                    if (commentTable != null) {
                        commentTable.setItems(FXCollections.observableArrayList(allComments));
                        commentTable.refresh();
                    }
                    if (archiveTable != null) {
                        archiveTable.setItems(FXCollections.observableArrayList(allArchives));
                        archiveTable.refresh();
                    }
                    updateStats();
                });
            } catch (Exception e) {
                Platform.runLater(() -> System.err.println("⚠ Could not refresh comments: " + e.getMessage()));
            }
        }).start();
    }

    private void updateStats() {
        new Thread(() -> {
            try {
                Map<String, Integer> stats = commentaireService.getStatistics();
                Platform.runLater(() -> {
                    if (lblApproved != null) lblApproved.setText(String.valueOf(stats.getOrDefault("approved", 0)));
                    if (lblPending != null) lblPending.setText(String.valueOf(stats.getOrDefault("pending", 0)));
                    if (lblBlocked != null) lblBlocked.setText(String.valueOf(stats.getOrDefault("blocked", 0)));
                    if (lblTotal != null) lblTotal.setText(String.valueOf(stats.getOrDefault("total", 0)));
                });
            } catch (Exception e) {
                Platform.runLater(() -> System.err.println("⚠ Could not update stats: " + e.getMessage()));
            }
        }).start();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.showAndWait();
    }
}
