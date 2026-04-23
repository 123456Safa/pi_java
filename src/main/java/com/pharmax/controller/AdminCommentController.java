package com.pharmax.controller;

import com.pharmax.model.Commentaire;
import com.pharmax.model.CommentaireArchive;
import com.pharmax.service.ArticleService;
import com.pharmax.service.CommentaireService;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * AdminCommentController — Back Office for Commentaires.
 * Admin can: View, Delete comments + view archives + see statistics.
 * Design matching Sneat Bootstrap admin template (Symfony project).
 */
public class AdminCommentController {

    private final CommentaireService commentaireService;
    private final ArticleService articleService;

    private TableView<Commentaire> commentTable;
    private TableView<CommentaireArchive> archiveTable;

    // Stats
    private Label lblApproved, lblPending, lblBlocked, lblTotal;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminCommentController(CommentaireService commentaireService, ArticleService articleService) {
        this.commentaireService = commentaireService;
        this.articleService = articleService;
    }

    public VBox buildView() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(18, 25, 18, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #e0e0e0 transparent; "
                + "-fx-border-width: 0 0 1 0;");

        Label headerTitle = new Label("💬 Gestion des Commentaires");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        headerTitle.setStyle("-fx-text-fill: #2c3e50;");

        header.getChildren().add(headerTitle);

        // Content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 25, 20, 25));
        VBox.setVgrow(content, Priority.ALWAYS);

        // Stats bar
        HBox statsBar = buildStatsBar();

        // Toolbar
        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button btnDelete = new Button("🗑 Supprimer");
        btnDelete.setStyle("-fx-background-color: #8b7b6f; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 11; -fx-padding: 6 14; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> onDeleteComment());

        Button btnShow = new Button("👁 Détail");
        btnShow.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 11; -fx-padding: 6 14; -fx-cursor: hand;");
        btnShow.setOnAction(e -> onShowComment());

        toolbar.getChildren().addAll(btnDelete, btnShow);

        // Comment table
        Label lblComments = new Label("Commentaires actifs");
        lblComments.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblComments.setStyle("-fx-text-fill: #2c3e50;");

        commentTable = buildCommentTable();
        VBox.setVgrow(commentTable, Priority.ALWAYS);

        // Archive section
        Separator sep = new Separator();

        HBox archiveHeader = new HBox(10);
        archiveHeader.setAlignment(Pos.CENTER_LEFT);

        Label lblArchives = new Label("📦 Commentaires archivés (bloqués)");
        lblArchives.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblArchives.setStyle("-fx-text-fill: #2c3e50;");

        Region archiveSpacer = new Region();
        HBox.setHgrow(archiveSpacer, Priority.ALWAYS);

        Button btnDeleteArchive = new Button("🗑 Supprimer archive");
        btnDeleteArchive.setStyle("-fx-background-color: #8b7b6f; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 11; -fx-padding: 6 14; -fx-cursor: hand;");
        btnDeleteArchive.setOnAction(e -> onDeleteArchive());

        archiveHeader.getChildren().addAll(lblArchives, archiveSpacer, btnDeleteArchive);

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
        tv.setPlaceholder(new Label("Aucun commentaire"));
        tv.setPrefHeight(220);

        TableColumn<Commentaire, String> colContenu = new TableColumn<>("Contenu");
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colContenu.setPrefWidth(300);

        TableColumn<Commentaire, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(100);
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(item);
                    switch (item.toLowerCase()) {
                        case "valide":
                            setStyle("-fx-text-fill: #5ea96b; -fx-font-weight: bold;");
                            break;
                        case "en_attente":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "bloqué":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #656565;");
                    }
                }
            }
        });

        TableColumn<Commentaire, String> colArticle = new TableColumn<>("Article");
        colArticle.setPrefWidth(200);
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

        tv.getColumns().addAll(colContenu, colStatut, colArticle, colDate);
        return tv;
    }

    @SuppressWarnings("unchecked")
    private TableView<CommentaireArchive> buildArchiveTable() {
        TableView<CommentaireArchive> tv = new TableView<>();
        tv.setPlaceholder(new Label("Aucune archive"));
        tv.setPrefHeight(120);

        TableColumn<CommentaireArchive, String> colContenu = new TableColumn<>("Contenu");
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colContenu.setPrefWidth(300);

        TableColumn<CommentaireArchive, String> colUser = new TableColumn<>("Utilisateur");
        colUser.setCellValueFactory(new PropertyValueFactory<>("userName"));
        colUser.setPrefWidth(120);

        TableColumn<CommentaireArchive, String> colReason = new TableColumn<>("Raison");
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

        tv.getColumns().addAll(colContenu, colUser, colReason, colDate);
        return tv;
    }

    // ─── Stats Bar (matching Sneat cards) ──────────────────────

    private HBox buildStatsBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);

        lblApproved = new Label("0");
        VBox vApproved = createStatCard("Approuvés", lblApproved, "#5ea96b");

        lblPending = new Label("0");
        VBox vPending = createStatCard("En attente", lblPending, "#f39c12");

        lblBlocked = new Label("0");
        VBox vBlocked = createStatCard("Bloqués", lblBlocked, "#e74c3c");

        lblTotal = new Label("0");
        VBox vTotal = createStatCard("Total", lblTotal, "#2c3e50");

        bar.getChildren().addAll(vApproved, vPending, vBlocked, vTotal);
        HBox.setHgrow(vApproved, Priority.ALWAYS);
        HBox.setHgrow(vPending, Priority.ALWAYS);
        HBox.setHgrow(vBlocked, Priority.ALWAYS);
        HBox.setHgrow(vTotal, Priority.ALWAYS);

        return bar;
    }

    private VBox createStatCard(String title, Label numberLabel, String borderColor) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent transparent " + borderColor + "; "
                + "-fx-border-width: 0 0 0 4; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 3, 0, 0, 1);");

        Label lbl = new Label(title.toUpperCase());
        lbl.setStyle("-fx-text-fill: #999; -fx-font-size: 10; -fx-font-weight: bold;");

        numberLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        numberLabel.setStyle("-fx-text-fill: " + borderColor + ";");

        card.getChildren().addAll(lbl, numberLabel);
        return card;
    }

    // ─── Actions (view + delete only) ────────────────────────

    private void onDeleteComment() {
        Commentaire selected = commentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélectionnez un commentaire à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le commentaire #" + selected.getId() + " ?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            commentaireService.delete(selected.getId());
            refresh();
        }
    }

    private void onShowComment() {
        Commentaire selected = commentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélectionnez un commentaire.");
            return;
        }
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Détail Commentaire #" + selected.getId());
        info.setHeaderText("Commentaire");
        info.setContentText(
                "Contenu: " + selected.getContenu() + "\n\n" +
                "Statut: " + selected.getStatut() + "\n" +
                "Article: " + (selected.getArticle() != null ? selected.getArticle().getTitre() : "N/A") + "\n" +
                "Date: " + (selected.getDatePublication() != null ? selected.getDatePublication().format(DATE_FMT) : "N/A"));
        info.showAndWait();
    }

    private void onDeleteArchive() {
        CommentaireArchive selected = archiveTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélectionnez une archive à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'archive #" + selected.getId() + " ?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            commentaireService.deleteArchive(selected.getId());
            refresh();
        }
    }

    // ─── Helpers ────────────────────────────────────────────────

    public void refresh() {
        if (commentTable != null) {
            commentTable.setItems(FXCollections.observableArrayList(commentaireService.findAll()));
            commentTable.refresh();
        }
        if (archiveTable != null) {
            archiveTable.setItems(FXCollections.observableArrayList(commentaireService.findAllArchives()));
            archiveTable.refresh();
        }
        updateStats();
    }

    private void updateStats() {
        Map<String, Integer> stats = commentaireService.getStatistics();
        if (lblApproved != null) lblApproved.setText(String.valueOf(stats.getOrDefault("approved", 0)));
        if (lblPending != null) lblPending.setText(String.valueOf(stats.getOrDefault("pending", 0)));
        if (lblBlocked != null) lblBlocked.setText(String.valueOf(stats.getOrDefault("blocked", 0)));
        if (lblTotal != null) lblTotal.setText(String.valueOf(stats.getOrDefault("total", 0)));
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.showAndWait();
    }
}
