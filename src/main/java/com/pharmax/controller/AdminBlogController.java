package com.pharmax.controller;

import com.pharmax.model.Article;
import com.pharmax.service.ArticleService;

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
import java.util.Optional;

/**
 * AdminBlogController — Back Office CRUD for Articles.
 * Design matching Sneat Bootstrap admin template used in original Symfony project.
 */
public class AdminBlogController {

    private final ArticleService articleService;

    private TableView<Article> articleTable;

    // Form
    private VBox formPane;
    private TextField titreField;
    private TextArea contenuField;
    private ComboBox<String> statutCombo;
    private Label feedbackLabel;
    private Button btnSave;
    private Label formTitle;

    private Article editingArticle = null;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public AdminBlogController(ArticleService articleService) {
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

        Label headerTitle = new Label("📝 Gestion des Articles");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        headerTitle.setStyle("-fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNew = new Button("+ Nouvel Article");
        btnNew.setStyle("-fx-background-color: #5ea96b; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 12; -fx-padding: 8 20; -fx-cursor: hand; -fx-background-radius: 4;");
        btnNew.setOnAction(e -> onNewArticle());

        header.getChildren().addAll(headerTitle, spacer, btnNew);

        // Content container
        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 25, 20, 25));
        VBox.setVgrow(content, Priority.ALWAYS);

        // Toolbar
        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button btnEdit = new Button("✏ Modifier");
        btnEdit.setStyle("-fx-background-color: #5ea96b; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 11; -fx-padding: 6 14; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> onEditArticle());

        Button btnDelete = new Button("🗑 Supprimer");
        btnDelete.setStyle("-fx-background-color: #8b7b6f; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 11; -fx-padding: 6 14; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> onDeleteArticle());

        Button btnToggle = new Button("🔄 Publier/Brouillon");
        btnToggle.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 11; -fx-padding: 6 14; -fx-cursor: hand;");
        btnToggle.setOnAction(e -> onToggleStatus());

        toolbar.getChildren().addAll(btnEdit, btnDelete, btnToggle);

        // Table
        articleTable = buildArticleTable();
        VBox.setVgrow(articleTable, Priority.ALWAYS);

        // Form (hidden by default)
        formPane = buildForm();
        formPane.setVisible(false);
        formPane.setManaged(false);

        content.getChildren().addAll(toolbar, articleTable, formPane);
        root.getChildren().addAll(header, content);
        return root;
    }

    // ─── Table Builder ─────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private TableView<Article> buildArticleTable() {
        TableView<Article> tv = new TableView<>();
        tv.setPlaceholder(new Label("Aucun article"));

        TableColumn<Article, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Article, String> colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colTitre.setPrefWidth(300);

        TableColumn<Article, Boolean> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("draft"));
        colStatut.setPrefWidth(100);
        // Color the status cell
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean isDraft, boolean empty) {
                super.updateItem(isDraft, empty);
                if (empty || isDraft == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(isDraft ? "Brouillon" : "Publié");
                    setStyle(isDraft
                            ? "-fx-text-fill: #f39c12; -fx-font-weight: bold;"
                            : "-fx-text-fill: #5ea96b; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Article, Integer> colLikes = new TableColumn<>("Likes");
        colLikes.setCellValueFactory(new PropertyValueFactory<>("likes"));
        colLikes.setPrefWidth(60);

        TableColumn<Article, LocalDateTime> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colDate.setPrefWidth(130);
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(DATE_FMT));
            }
        });

        tv.getColumns().addAll(colId, colTitre, colStatut, colLikes, colDate);
        return tv;
    }

    // ─── Form ──────────────────────────────────────────────────

    private VBox buildForm() {
        VBox form = new VBox(12);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 3, 0, 0, 1);");

        formTitle = new Label("Nouvel Article");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        formTitle.setStyle("-fx-text-fill: #2c3e50;");

        // Title field
        Label lblTitre = new Label("Titre:");
        lblTitre.setStyle("-fx-text-fill: #656565; -fx-font-weight: bold; -fx-font-size: 13;");
        titreField = new TextField();
        titreField.setPromptText("Titre de l'article...");
        titreField.setStyle("-fx-border-color: #ddd; -fx-border-width: 2; -fx-padding: 8; -fx-font-size: 13;");
        titreField.focusedProperty().addListener((obs, old, focused) -> {
            titreField.setStyle(focused
                    ? "-fx-border-color: #5ea96b; -fx-border-width: 2; -fx-padding: 8; -fx-font-size: 13;"
                    : "-fx-border-color: #ddd; -fx-border-width: 2; -fx-padding: 8; -fx-font-size: 13;");
        });

        // Content field
        Label lblContenu = new Label("Contenu:");
        lblContenu.setStyle("-fx-text-fill: #656565; -fx-font-weight: bold; -fx-font-size: 13;");
        contenuField = new TextArea();
        contenuField.setPromptText("Contenu de l'article...");
        contenuField.setPrefRowCount(6);
        contenuField.setStyle("-fx-border-color: #ddd; -fx-border-width: 2; -fx-padding: 8; -fx-font-size: 13;");
        contenuField.focusedProperty().addListener((obs, old, focused) -> {
            contenuField.setStyle(focused
                    ? "-fx-border-color: #5ea96b; -fx-border-width: 2; -fx-padding: 8; -fx-font-size: 13;"
                    : "-fx-border-color: #ddd; -fx-border-width: 2; -fx-padding: 8; -fx-font-size: 13;");
        });

        // Status combo
        Label lblStatut = new Label("Statut:");
        lblStatut.setStyle("-fx-text-fill: #656565; -fx-font-weight: bold; -fx-font-size: 13;");
        statutCombo = new ComboBox<>(FXCollections.observableArrayList("brouillon", "publié"));
        statutCombo.setValue("brouillon");

        feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);

        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(10, 0, 0, 0));
        btnSave = new Button("💾 Créer");
        btnSave.setStyle("-fx-background-color: #5ea96b; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13; -fx-padding: 10 25; -fx-cursor: hand;");
        btnSave.setOnAction(e -> onSave());

        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle("-fx-background-color: #d3d3d3; -fx-text-fill: #333; -fx-font-size: 13; "
                + "-fx-padding: 10 20; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> clearForm());

        buttons.getChildren().addAll(btnSave, btnCancel);

        form.getChildren().addAll(formTitle, lblTitre, titreField, lblContenu, contenuField,
                lblStatut, statutCombo, feedbackLabel, buttons);
        return form;
    }

    // ─── CRUD Actions ──────────────────────────────────────────

    private void onNewArticle() {
        editingArticle = null;
        titreField.clear();
        contenuField.clear();
        statutCombo.setValue("brouillon");
        feedbackLabel.setText("");
        formTitle.setText("Nouvel Article");
        btnSave.setText("💾 Créer");
        formPane.setVisible(true);
        formPane.setManaged(true);
    }

    private void onEditArticle() {
        Article selected = articleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélectionnez un article à modifier.");
            return;
        }
        editingArticle = selected;
        titreField.setText(selected.getTitre());
        contenuField.setText(selected.getContenu());
        statutCombo.setValue(selected.isDraft() ? "brouillon" : "publié");
        feedbackLabel.setText("");
        formTitle.setText("Modifier l'article #" + selected.getId());
        btnSave.setText("💾 Mettre à jour");
        formPane.setVisible(true);
        formPane.setManaged(true);
    }

    private void onSave() {
        String titre = titreField.getText().trim();
        String contenu = contenuField.getText().trim();

        // Validation
        if (titre.isEmpty()) {
            showFeedback("Le titre est obligatoire.", true);
            return;
        }
        if (titre.length() < 5) {
            showFeedback("Le titre doit contenir au moins 5 caractères.", true);
            return;
        }
        if (contenu.isEmpty()) {
            showFeedback("Le contenu est obligatoire.", true);
            return;
        }
        if (contenu.length() < 10) {
            showFeedback("Le contenu doit contenir au moins 10 caractères.", true);
            return;
        }

        String statut = statutCombo.getValue();

        if (editingArticle == null) {
            Article created = articleService.create(titre, contenu, null);
            if ("publié".equals(statut)) {
                created.publish();
            }
            showFeedback("Article créé avec succès !", false);
        } else {
            articleService.update(editingArticle.getId(), titre, contenu, null);
            if ("publié".equals(statut)) {
                editingArticle.publish();
            } else {
                editingArticle.saveDraft();
            }
            showFeedback("Article mis à jour !", false);
        }
        clearForm();
        refresh();
    }

    private void onDeleteArticle() {
        Article selected = articleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélectionnez un article à supprimer.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer l'article \"" + selected.getTitre() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            articleService.delete(selected.getId());
            refresh();
        }
    }

    private void onToggleStatus() {
        Article selected = articleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Sélectionnez un article.");
            return;
        }
        articleService.togglePublish(selected.getId());
        refresh();
    }

    // ─── Helpers ────────────────────────────────────────────────

    public void refresh() {
        if (articleTable != null) {
            articleTable.setItems(FXCollections.observableArrayList(articleService.findAll()));
            articleTable.refresh();
        }
    }

    private void clearForm() {
        editingArticle = null;
        titreField.clear();
        contenuField.clear();
        statutCombo.setValue("brouillon");
        feedbackLabel.setText("");
        formPane.setVisible(false);
        formPane.setManaged(false);
    }

    private void showFeedback(String message, boolean isError) {
        feedbackLabel.setText(message);
        if (isError) {
            feedbackLabel.setStyle("-fx-text-fill: #721c24; -fx-background-color: #f8d7da; -fx-padding: 8 12; "
                    + "-fx-border-color: #f5c6cb; -fx-border-width: 1; -fx-font-size: 13;");
        } else {
            feedbackLabel.setStyle("-fx-text-fill: #155724; -fx-background-color: #d4edda; -fx-padding: 8 12; "
                    + "-fx-border-color: #c3e6cb; -fx-border-width: 1; -fx-font-size: 13;");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.showAndWait();
    }
}
