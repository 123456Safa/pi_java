package com.pharmax.controller;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.pharmax.model.Article;
import com.pharmax.service.AIService;
import com.pharmax.service.ArticleService;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

public class AdminBlogController {

    private final ArticleService articleService;

    // Core layout
    private StackPane pageContainer;
    private VBox tablePage;
    private ScrollPane formPage;

    // Table
    private TableView<Article> articleTable;

    // Form fields
    private TextField titreField;
    private TextArea contenuField;
    private ComboBox<String> statutCombo;
    private Label feedbackLabel;
    private Button btnSave;
    private Label formTitle;

    // Image Upload
    private File selectedImageFile = null;
    private ImageView imagePreview;
    private Label imageLabelInfo;

    private Article editingArticle = null;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final java.util.Map<Integer, SimpleBooleanProperty> articleSelectionMap = new java.util.HashMap<>();

    public AdminBlogController(ArticleService articleService) {
        this.articleService = articleService;
    }

    public StackPane buildView() {
        pageContainer = new StackPane();
        pageContainer.setStyle("-fx-background-color: #f8f9fa;");

        tablePage = buildTablePage();
        formPage = buildFormPage();
        formPage.setVisible(false);

        pageContainer.getChildren().addAll(tablePage, formPage);
        return pageContainer;
    }

    // ─── Table Page ──────────────────────────────────────────────

    private VBox buildTablePage() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: transparent;");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(18, 25, 18, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #e0e0e0 transparent; -fx-border-width: 0 0 1 0;");

        Label headerTitle = new Label("📝 Gestion des Articles");
        headerTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        headerTitle.setStyle("-fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnNew = new Button("+ Nouvel Article");
        btnNew.setStyle("-fx-background-color: #2ea043; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13; -fx-padding: 8 20; -fx-cursor: hand; -fx-background-radius: 6;");
        btnNew.setOnAction(e -> showFormPage(null));

        header.getChildren().addAll(headerTitle, spacer, btnNew);

        // Content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 25, 20, 25));
        VBox.setVgrow(content, Priority.ALWAYS);

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button btnEdit = new Button("✏ Modifier");
        btnEdit.setStyle("-fx-background-color: #f1f8f5; -fx-text-fill: #2ea043; -fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 8 16; -fx-cursor: hand; -fx-border-color: #2ea043; -fx-border-radius: 4;");
        btnEdit.setOnAction(e -> {
            Article selected = articleTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showFormPage(selected);
            } else {
                showAlert("Sélectionnez un article à modifier.");
            }
        });

        Button btnDelete = new Button("🗑 Supprimer Sélection");
        btnDelete.setStyle("-fx-background-color: #fff0f0; -fx-text-fill: #d73a49; -fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 8 16; -fx-cursor: hand; -fx-border-color: #d73a49; -fx-border-radius: 4;");
        btnDelete.setOnAction(e -> onDeleteArticle());

        Button btnToggle = new Button("🔄 Statut Sélection");
        btnToggle.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #4b5563; -fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 8 16; -fx-cursor: hand; -fx-border-color: #d1d5db; -fx-border-radius: 4;");
        btnToggle.setOnAction(e -> onToggleStatus());

        Region toolSpacer = new Region();
        HBox.setHgrow(toolSpacer, Priority.ALWAYS);

        CheckBox selectAllArticles = new CheckBox("Sélectionner Tout");
        selectAllArticles.setStyle("-fx-font-size: 12; -fx-text-fill: #718096;");
        selectAllArticles.setOnAction(e -> {
            boolean selected = selectAllArticles.isSelected();
            for (SimpleBooleanProperty prop : articleSelectionMap.values()) {
                prop.set(selected);
            }
        });

        toolbar.getChildren().addAll(btnEdit, btnDelete, btnToggle, toolSpacer, selectAllArticles);

        articleTable = buildArticleTable();
        VBox.setVgrow(articleTable, Priority.ALWAYS);

        ScrollPane scrollWrapper = new ScrollPane(articleTable);
        scrollWrapper.setFitToWidth(true);
        scrollWrapper.setFitToHeight(true);
        scrollWrapper.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        VBox.setVgrow(scrollWrapper, Priority.ALWAYS);

        content.getChildren().addAll(toolbar, scrollWrapper);
        root.getChildren().addAll(header, content);

        return root;
    }

    @SuppressWarnings("unchecked")
    private TableView<Article> buildArticleTable() {
        TableView<Article> tv = new TableView<>();
        tv.setPlaceholder(new Label("Aucun article disponible"));
        tv.setStyle("-fx-font-size: 14px; -fx-border-color: #e2e8f0; -fx-border-radius: 4;");

        TableColumn<Article, Boolean> colSelect = new TableColumn<>("");
        colSelect.setPrefWidth(40);
        colSelect.setSortable(false);
        colSelect.setCellFactory(col -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();
            private SimpleBooleanProperty boundProperty = null;
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
                    Article a = getTableRow().getItem();
                    articleSelectionMap.putIfAbsent(a.getId(), new SimpleBooleanProperty(false));
                    boundProperty = articleSelectionMap.get(a.getId());
                    cb.selectedProperty().bindBidirectional(boundProperty);
                    setGraphic(cb);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Article, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);

        TableColumn<Article, String> colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colTitre.setPrefWidth(350);

        TableColumn<Article, Boolean> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isDraft()));
        colStatut.setPrefWidth(120);
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean isDraft, boolean empty) {
                super.updateItem(isDraft, empty);
                if (empty || isDraft == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(isDraft ? "Brouillon" : "Publié");
                    badge.setStyle(isDraft
                            ? "-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-padding: 4 8; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;"
                            : "-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 4 8; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Article, Integer> colLikes = new TableColumn<>("Likes");
        colLikes.setCellValueFactory(new PropertyValueFactory<>("likes"));
        colLikes.setPrefWidth(80);

        TableColumn<Article, LocalDateTime> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        colDate.setPrefWidth(150);
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(DATE_FMT));
            }
        });

        tv.getColumns().addAll(colSelect, colId, colTitre, colStatut, colLikes, colDate);
        return tv;
    }

    // ─── Form Page ───────────────────────────────────────────────

    private ScrollPane buildFormPage() {
        VBox page = new VBox(0);
        page.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = new HBox(15);
        header.setPadding(new Insets(18, 25, 18, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #e0e0e0 transparent; -fx-border-width: 0 0 1 0;");

        Button btnBack = new Button("← Retour");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #4b5563; -fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showTablePage());

        formTitle = new Label("Nouvel Article");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        formTitle.setStyle("-fx-text-fill: #2c3e50;");

        header.getChildren().addAll(btnBack, formTitle);

        // Content
        VBox formContent = new VBox(20);
        formContent.setPadding(new Insets(30));
        formContent.setAlignment(Pos.TOP_CENTER);

        VBox formCard = new VBox(15);
        formCard.setMaxWidth(800);
        formCard.setPadding(new Insets(30));
        formCard.setStyle("-fx-background-color: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // AI Generate Section
        HBox aiBox = new HBox(10);
        aiBox.setAlignment(Pos.CENTER_LEFT);
        aiBox.setPadding(new Insets(12, 16, 12, 16));
        aiBox.setStyle("-fx-background-color: linear-gradient(to right, #f0f9ff, #e8f5e9); -fx-background-radius: 8; -fx-border-color: #b2dfdb; -fx-border-radius: 8; -fx-border-width: 1;");

        Label aiIcon = new Label("🤖");
        aiIcon.setStyle("-fx-font-size: 20;");
        Label aiLabel = new Label("Générer avec AI:");
        aiLabel.setStyle("-fx-text-fill: #2d8659; -fx-font-size: 14; -fx-font-weight: bold;");

        TextField aiKeywordField = new TextField();
        aiKeywordField.setPromptText("Ex: Vitamine C, Santé mentale...");
        aiKeywordField.setPrefWidth(220);
        HBox.setHgrow(aiKeywordField, Priority.ALWAYS);
        aiKeywordField.setStyle("-fx-padding: 8; -fx-font-size: 13;");

        Button btnGenerate = new Button("✨ Générer");
        btnGenerate.setStyle("-fx-background-color: linear-gradient(to bottom, #6c63ff, #5a52d5); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13; -fx-padding: 8 20; -fx-cursor: hand; -fx-background-radius: 6;");

        ProgressIndicator aiSpinner = new ProgressIndicator();
        aiSpinner.setPrefSize(20, 20);
        aiSpinner.setVisible(false);

        btnGenerate.setOnAction(e -> {
            String kw = aiKeywordField.getText().trim();
            if (kw.isEmpty()) {
                showFeedback("⚠ Entrez un mot-clé pour l'AI.", true);
                return;
            }
            btnGenerate.setDisable(true);
            aiSpinner.setVisible(true);
            showFeedback("🤖 Génération de l'article en cours...", false);

            new Thread(() -> {
                try {
                    String generated = AIService.getInstance().generateArticle(kw);
                    Platform.runLater(() -> {
                        contenuField.setText(generated);
                        showFeedback("✅ Article généré avec succès ! Ajoutez un titre.", false);
                        btnGenerate.setDisable(false);
                        aiSpinner.setVisible(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        showFeedback("❌ Erreur AI: " + ex.getMessage(), true);
                        btnGenerate.setDisable(false);
                        aiSpinner.setVisible(false);
                    });
                }
            }).start();
        });

        aiBox.getChildren().addAll(aiIcon, aiLabel, aiKeywordField, btnGenerate, aiSpinner);

        // Standard fields
        Label lblTitre = new Label("Titre:");
        lblTitre.setStyle("-fx-text-fill: #4a5568; -fx-font-weight: bold; -fx-font-size: 14;");
        titreField = new TextField();
        titreField.setPromptText("Titre de l'article");
        titreField.setStyle("-fx-padding: 10; -fx-font-size: 14; -fx-border-color: #cbd5e0; -fx-border-radius: 4;");

        Label lblContenu = new Label("Contenu:");
        lblContenu.setStyle("-fx-text-fill: #4a5568; -fx-font-weight: bold; -fx-font-size: 14;");
        contenuField = new TextArea();
        contenuField.setPromptText("Contenu de l'article...");
        contenuField.setPrefRowCount(10);
        contenuField.setWrapText(true);
        contenuField.setStyle("-fx-padding: 10; -fx-font-size: 14; -fx-border-color: #cbd5e0; -fx-border-radius: 4;");

        // Image Upload Area
        Label lblImage = new Label("Image de couverture:");
        lblImage.setStyle("-fx-text-fill: #4a5568; -fx-font-weight: bold; -fx-font-size: 14;");
        
        VBox imageUploadArea = new VBox(10);
        imageUploadArea.setAlignment(Pos.CENTER);
        imageUploadArea.setPadding(new Insets(20));
        imageUploadArea.setStyle("-fx-border-color: #cbd5e0; -fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 8; -fx-background-color: #f8fafc;");
        
        imagePreview = new ImageView();
        imagePreview.setFitWidth(150);
        imagePreview.setFitHeight(100);
        imagePreview.setPreserveRatio(true);
        imagePreview.setVisible(false);
        imagePreview.setManaged(false);

        imageLabelInfo = new Label("Aucune image sélectionnée");
        imageLabelInfo.setStyle("-fx-text-fill: #718096; -fx-font-size: 13;");

        Button btnUpload = new Button("📁 Parcourir...");
        btnUpload.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-weight: bold; -fx-cursor: hand;");
        btnUpload.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir une image");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                selectedImageFile = file;
                imageLabelInfo.setText(file.getName());
                try {
                    imagePreview.setImage(new Image(file.toURI().toString()));
                    imagePreview.setVisible(true);
                    imagePreview.setManaged(true);
                } catch (Exception ex) {
                    System.err.println("Could not load image preview: " + ex.getMessage());
                }
            }
        });

        imageUploadArea.getChildren().addAll(imagePreview, imageLabelInfo, btnUpload);

        Label lblStatut = new Label("Statut:");
        lblStatut.setStyle("-fx-text-fill: #4a5568; -fx-font-weight: bold; -fx-font-size: 14;");
        statutCombo = new ComboBox<>(FXCollections.observableArrayList("Brouillon", "Publié"));
        statutCombo.setValue("Brouillon");
        statutCombo.setStyle("-fx-font-size: 14;");

        feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);

        HBox buttons = new HBox(12);
        buttons.setPadding(new Insets(15, 0, 0, 0));
        btnSave = new Button("💾 Créer");
        btnSave.setStyle("-fx-background-color: #2ea043; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 10 25; -fx-cursor: hand; -fx-background-radius: 6;");
        btnSave.setOnAction(e -> onSave());

        buttons.getChildren().add(btnSave);

        formCard.getChildren().addAll(aiBox, lblTitre, titreField, lblContenu, contenuField, lblImage, imageUploadArea, lblStatut, statutCombo, feedbackLabel, buttons);
        formContent.getChildren().add(formCard);

        page.getChildren().addAll(header, formContent);

        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");
        return scrollPane;
    }

    // ─── Navigation ──────────────────────────────────────────────

    private void showFormPage(Article article) {
        editingArticle = article;
        feedbackLabel.setVisible(false);
        feedbackLabel.setManaged(false);
        selectedImageFile = null;
        imagePreview.setVisible(false);
        imagePreview.setManaged(false);

        if (article == null) {
            formTitle.setText("Nouvel Article");
            titreField.clear();
            contenuField.clear();
            statutCombo.setValue("Brouillon");
            btnSave.setText("💾 Créer");
            imageLabelInfo.setText("Aucune image sélectionnée");
        } else {
            formTitle.setText("Modifier l'article #" + article.getId());
            titreField.setText(article.getTitre());
            contenuField.setText(article.getContenu());
            statutCombo.setValue(article.isDraft() ? "Brouillon" : "Publié");
            btnSave.setText("💾 Mettre à jour");
            
            if (article.getImage() != null && !article.getImage().isEmpty()) {
                imageLabelInfo.setText(article.getImage());
            } else {
                imageLabelInfo.setText("Aucune image sélectionnée");
            }
        }

        tablePage.setVisible(false);
        formPage.setVisible(true);
    }

    private void showTablePage() {
        formPage.setVisible(false);
        tablePage.setVisible(true);
        refresh();
    }

    // ─── CRUD Actions ──────────────────────────────────────────

    private void onSave() {
        String titre = titreField.getText().trim();
        String contenu = contenuField.getText().trim();
        String statut = statutCombo.getValue();
        
        // Image handling (store absolute path for now if file selected)
        String imagePath = null;
        if (selectedImageFile != null) {
            imagePath = selectedImageFile.getAbsolutePath();
        } else if (editingArticle != null) {
            imagePath = editingArticle.getImage(); // keep existing
        }

        if (titre.isEmpty() || contenu.isEmpty()) {
            showFeedback("Le titre et le contenu sont obligatoires.", true);
            return;
        }

        final String finalImagePath = imagePath;

        new Thread(() -> {
            try {
                if (editingArticle == null) {
                    Article created = articleService.create(titre, contenu, null, finalImagePath);
                    if (created != null && "Publié".equals(statut)) {
                        created.publish();
                        articleService.update(created.getId(), created.getTitre(), created.getContenu(), created.getContenuEn(), finalImagePath); // dummy update to trigger publish or use toggle
                        articleService.togglePublish(created.getId()); // ensure published state if default was draft
                    }
                    Platform.runLater(() -> {
                        showTablePage();
                    });
                } else {
                    articleService.update(editingArticle.getId(), titre, contenu, editingArticle.getContenuEn(), finalImagePath);
                    if ("Publié".equals(statut) && editingArticle.isDraft()) {
                        articleService.togglePublish(editingArticle.getId());
                    } else if ("Brouillon".equals(statut) && !editingArticle.isDraft()) {
                        articleService.togglePublish(editingArticle.getId());
                    }
                    Platform.runLater(() -> {
                        showTablePage();
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> showFeedback("❌ Erreur: " + e.getMessage(), true));
            }
        }).start();
    }

    private void onDeleteArticle() {
        java.util.List<Integer> toDelete = new java.util.ArrayList<>();
        for (java.util.Map.Entry<Integer, SimpleBooleanProperty> entry : articleSelectionMap.entrySet()) {
            if (entry.getValue().get()) {
                toDelete.add(entry.getKey());
            }
        }

        if (toDelete.isEmpty()) {
            Article selected = articleTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                toDelete.add(selected.getId());
            }
        }

        if (toDelete.isEmpty()) {
            showAlert("Sélectionnez au moins un article à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer " + toDelete.size() + " article(s) ?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            new Thread(() -> {
                try {
                    for (int id : toDelete) {
                        articleService.delete(id);
                    }
                    Platform.runLater(() -> {
                        articleSelectionMap.clear();
                        articleTable.getItems().removeIf(a -> toDelete.contains(a.getId()));
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("❌ Erreur suppression: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void onToggleStatus() {
        java.util.List<Article> toToggle = new java.util.ArrayList<>();
        for (Article a : articleTable.getItems()) {
            SimpleBooleanProperty prop = articleSelectionMap.get(a.getId());
            if (prop != null && prop.get()) {
                toToggle.add(a);
            }
        }

        if (toToggle.isEmpty()) {
            Article selected = articleTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                toToggle.add(selected);
            }
        }

        if (toToggle.isEmpty()) {
            showAlert("Sélectionnez au moins un article.");
            return;
        }

        new Thread(() -> {
            try {
                for (Article a : toToggle) {
                    articleService.togglePublish(a.getId());
                }
                Platform.runLater(() -> {
                    for (Article a : toToggle) {
                        a.setIsDraft(!a.isDraft());
                    }
                    articleTable.refresh();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("❌ Erreur: " + e.getMessage()));
            }
        }).start();
    }

    public void refresh() {
        if (articleTable != null) {
            new Thread(() -> {
                try {
                    var articles = articleService.findAll();
                    Platform.runLater(() -> {
                        articleTable.setItems(FXCollections.observableArrayList(articles));
                        articleTable.refresh();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> System.err.println("⚠ Could not refresh articles: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void showFeedback(String message, boolean isError) {
        feedbackLabel.setVisible(true);
        feedbackLabel.setManaged(true);
        feedbackLabel.setText(message);
        if (isError) {
            feedbackLabel.setStyle("-fx-text-fill: #721c24; -fx-background-color: #f8d7da; -fx-padding: 10; -fx-border-color: #f5c6cb; -fx-border-width: 1; -fx-border-radius: 4;");
        } else {
            feedbackLabel.setStyle("-fx-text-fill: #155724; -fx-background-color: #d4edda; -fx-padding: 10; -fx-border-color: #c3e6cb; -fx-border-width: 1; -fx-border-radius: 4;");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        alert.showAndWait();
    }
}
