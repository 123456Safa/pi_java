package com.pharmax.controller;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.pharmax.model.Article;
import com.pharmax.model.Commentaire;
import com.pharmax.service.ArticleService;
import com.pharmax.service.CommentaireService;
import com.pharmax.service.TranslationService;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressIndicator;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * FrontBlogController — Front Office (User view).
 * Premium card-grid layout with flip-on-hover animation.
 * Icons: like, view, save for later.
 */
public class FrontBlogController {

    private final ArticleService articleService;
    private final CommentaireService commentaireService;

    private FlowPane articleGridPane;
    private VBox rootView;
    private ScrollPane scrollPane;

    // Track liked & saved articles in-memory (per session)
    private final Set<Integer> likedArticles = new HashSet<>();
    private final Set<Integer> savedArticles = new HashSet<>();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_FMT_FULL = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public FrontBlogController(ArticleService articleService, CommentaireService commentaireService) {
        this.articleService = articleService;
        this.commentaireService = commentaireService;
    }

    public VBox buildView() {
        rootView = new VBox();
        rootView.setStyle("-fx-background-color: #f0f2f5;");

        // Hero Header
        VBox header = new VBox(6);
        header.setPadding(new Insets(35, 30, 35, 30));
        header.getStyleClass().add("front-header");

        Label title = new Label("PharmaX Blog");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        title.setStyle("-fx-text-fill: white;");

        Label subtitle = new Label("Conseils de santé et actualités pharmaceutiques");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 14;");

        header.getChildren().addAll(title, subtitle);

        // ScrollPane content
        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f0f2f5; -fx-padding: 0;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Card grid (FlowPane for responsive layout)
        articleGridPane = new FlowPane();
        articleGridPane.setHgap(20);
        articleGridPane.setVgap(20);
        articleGridPane.setPadding(new Insets(25, 30, 25, 30));
        articleGridPane.setStyle("-fx-background-color: #f0f2f5;");

        scrollPane.setContent(articleGridPane);

        rootView.getChildren().addAll(header, scrollPane);

        return rootView;
    }

    // ─── Article Grid ──────────────────────────────────────────

    public void refresh() {
        showArticleGrid();
    }

    private void showArticleGrid() {
        new Thread(() -> {
            try {
                List<Article> published = articleService.findPublished();
                Platform.runLater(() -> {
                    articleGridPane.getChildren().clear();

                    if (published.isEmpty()) {
                        VBox emptyState = new VBox(10);
                        emptyState.setAlignment(Pos.CENTER);
                        emptyState.setPadding(new Insets(80, 20, 80, 20));
                        emptyState.setPrefWidth(800);
                        Label icon = new Label("📭");
                        icon.setStyle("-fx-font-size: 70;");
                        Label msg = new Label("No articles published yet");
                        msg.setStyle("-fx-font-size: 16; -fx-text-fill: #718096; -fx-font-weight: bold;");
                        Label desc = new Label("Check back soon for health and pharmacy insights");
                        desc.setStyle("-fx-font-size: 13; -fx-text-fill: #a0aec0;");
                        emptyState.getChildren().addAll(icon, msg, desc);
                        articleGridPane.getChildren().add(emptyState);
                    } else {
                        for (int i = 0; i < published.size(); i++) {
                            StackPane card = createFlipCard(published.get(i));
                            // Staggered fade-in animation
                            card.setOpacity(0);
                            FadeTransition ft = new FadeTransition(Duration.millis(400), card);
                            ft.setFromValue(0);
                            ft.setToValue(1);
                            ft.setDelay(Duration.millis(i * 80));
                            ft.play();
                            articleGridPane.getChildren().add(card);
                        }
                    }

                    scrollPane.setContent(articleGridPane);
                });
            } catch (Exception e) {
                Platform.runLater(() -> System.err.println("⚠ Could not load articles: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Creates a flip card with FRONT (image+title+meta) and BACK (excerpt+actions).
     * Hover flips between them.
     */
    private StackPane createFlipCard(Article article) {
        double cardW = 340;
        double cardH = 360;

        // ════════════════ FRONT SIDE ════════════════
        VBox front = new VBox(0);
        front.setPrefSize(cardW, cardH);
        front.setMaxSize(cardW, cardH);
        front.setMinSize(cardW, cardH);
        front.setStyle("-fx-background-color: white; -fx-background-radius: 14;");
        front.setEffect(new javafx.scene.effect.DropShadow(10, 0, 4, javafx.scene.paint.Color.rgb(0,0,0,0.08)));

        // Image area
        StackPane imageArea = new StackPane();
        imageArea.setPrefHeight(180);
        imageArea.setMinHeight(180);
        imageArea.setMaxHeight(180);
        imageArea.setStyle("-fx-background-color: linear-gradient(to bottom right, #e8f5e9, #c8e6c9); -fx-background-radius: 14 14 0 0;");

        if (article.getImage() != null && !article.getImage().isEmpty()) {
            try {
                Image img;
                if (article.getImage().startsWith("http")) {
                    img = new Image(article.getImage(), cardW, 180, false, true, true);
                } else {
                    img = new Image("file:" + article.getImage(), cardW, 180, false, true, true);
                }
                ImageView iv = new ImageView(img);
                iv.setFitWidth(cardW);
                iv.setFitHeight(180);
                iv.setPreserveRatio(false);
                iv.setStyle("-fx-background-radius: 14 14 0 0;");
                // Clip for rounded top corners
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(cardW, 180);
                clip.setArcWidth(28);
                clip.setArcHeight(28);
                iv.setClip(clip);
                imageArea.getChildren().add(iv);
            } catch (Exception e) {
                Label placeholder = new Label("🖼");
                placeholder.setStyle("-fx-font-size: 50; -fx-text-fill: #a0aec0;");
                imageArea.getChildren().add(placeholder);
            }
        } else {
            Label placeholder = new Label("📰");
            placeholder.setStyle("-fx-font-size: 50; -fx-text-fill: #a0aec0;");
            imageArea.getChildren().add(placeholder);
        }

        // Content below image
        VBox cardContent = new VBox(8);
        cardContent.setPadding(new Insets(14, 16, 14, 16));

        // Author & date row
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label author = new Label("👤 PharmaX Team");
        author.setStyle("-fx-text-fill: #718096; -fx-font-size: 11;");
        Label dot = new Label("·");
        dot.setStyle("-fx-text-fill: #a0aec0;");
        Label date = new Label(article.getDateCreation() != null ? article.getDateCreation().format(DATE_FMT) : "");
        date.setStyle("-fx-text-fill: #718096; -fx-font-size: 11;");
        metaRow.getChildren().addAll(author, dot, date);

        // Title
        Label titre = new Label(article.getTitre());
        titre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        titre.setStyle("-fx-text-fill: #1a202c;");
        titre.setWrapText(true);
        titre.setMaxHeight(50);

        // Action icons row
        HBox actionsRow = new HBox(4);
        actionsRow.setAlignment(Pos.CENTER_LEFT);
        actionsRow.setPadding(new Insets(6, 0, 0, 0));

        Button btnLike = new Button(likedArticles.contains(article.getId()) ? "❤" : "🤍");
        btnLike.setStyle("-fx-background-color: transparent; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 4 8;");
        Label likeCount = new Label(String.valueOf(article.getLikes()));
        likeCount.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12; -fx-font-weight: bold;");

        Button btnView = new Button("👁");
        btnView.setStyle("-fx-background-color: transparent; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 4 8;");

        Button btnSave = new Button(savedArticles.contains(article.getId()) ? "🔖" : "📑");
        btnSave.setStyle("-fx-background-color: transparent; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 4 8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actionsRow.getChildren().addAll(btnLike, likeCount, btnView, btnSave, spacer);

        // Like action
        btnLike.setOnAction(e -> {
            if (article.getId() != null) {
                if (likedArticles.contains(article.getId())) {
                    likedArticles.remove(article.getId());
                    btnLike.setText("🤍");
                    new Thread(() -> {
                        articleService.unlike(article.getId());
                        Article updated = articleService.find(article.getId());
                        if (updated != null) Platform.runLater(() -> likeCount.setText(String.valueOf(updated.getLikes())));
                    }).start();
                } else {
                    likedArticles.add(article.getId());
                    btnLike.setText("❤");
                    // Bounce animation
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), btnLike);
                    st.setFromX(1); st.setFromY(1); st.setToX(1.4); st.setToY(1.4);
                    st.setAutoReverse(true); st.setCycleCount(2); st.play();
                    new Thread(() -> {
                        articleService.like(article.getId());
                        Article updated = articleService.find(article.getId());
                        if (updated != null) Platform.runLater(() -> likeCount.setText(String.valueOf(updated.getLikes())));
                    }).start();
                }
            }
        });

        // Save action
        btnSave.setOnAction(e -> {
            if (article.getId() != null) {
                if (savedArticles.contains(article.getId())) {
                    savedArticles.remove(article.getId());
                    btnSave.setText("📑");
                } else {
                    savedArticles.add(article.getId());
                    btnSave.setText("🔖");
                    ScaleTransition st = new ScaleTransition(Duration.millis(200), btnSave);
                    st.setFromX(1); st.setFromY(1); st.setToX(1.3); st.setToY(1.3);
                    st.setAutoReverse(true); st.setCycleCount(2); st.play();
                }
            }
        });

        // View action
        btnView.setOnAction(e -> showArticleDetail(article));

        cardContent.getChildren().addAll(metaRow, titre, actionsRow);
        front.getChildren().addAll(imageArea, cardContent);

        // ════════════════ BACK SIDE ════════════════
        VBox back = new VBox(12);
        back.setPrefSize(cardW, cardH);
        back.setMaxSize(cardW, cardH);
        back.setMinSize(cardW, cardH);
        back.setPadding(new Insets(22));
        back.setStyle("-fx-background-color: linear-gradient(to bottom, #f0fdf4, #ffffff); "
                + "-fx-background-radius: 14;");
        back.setEffect(new javafx.scene.effect.DropShadow(14, 0, 5, javafx.scene.paint.Color.rgb(45,134,89,0.18)));
        back.setVisible(false);

        Label backTitle = new Label(article.getTitre());
        backTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        backTitle.setStyle("-fx-text-fill: #1f5e42;");
        backTitle.setWrapText(true);
        backTitle.setMaxHeight(44);

        String preview = article.getContenu() != null
                ? (article.getContenu().length() > 250 ? article.getContenu().substring(0, 250) + "..." : article.getContenu())
                : "";
        Label excerpt = new Label(preview);
        excerpt.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13; -fx-line-spacing: 1.5;");
        excerpt.setWrapText(true);
        VBox.setVgrow(excerpt, Priority.ALWAYS);

        Button btnReadMore = new Button("Read Article →");
        btnReadMore.getStyleClass().add("btn-primary");
        btnReadMore.setStyle("-fx-background-color: linear-gradient(to right, #2d8659, #4a9f6f); -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-font-size: 13; -fx-padding: 10 24; -fx-cursor: hand; -fx-background-radius: 8;");
        btnReadMore.setOnAction(e -> showArticleDetail(article));

        back.getChildren().addAll(backTitle, excerpt, btnReadMore);

        // ════════════════ STACK (flip container) ════════════════
        StackPane flipCard = new StackPane(front, back);
        flipCard.setPrefSize(cardW, cardH);
        flipCard.setMaxSize(cardW, cardH);

        // Hover → flip
        flipCard.setOnMouseEntered(e -> {
            front.setVisible(false);
            back.setVisible(true);
            // Scale up slightly
            ScaleTransition st = new ScaleTransition(Duration.millis(200), flipCard);
            st.setToX(1.03); st.setToY(1.03); st.play();
        });
        flipCard.setOnMouseExited(e -> {
            back.setVisible(false);
            front.setVisible(true);
            ScaleTransition st = new ScaleTransition(Duration.millis(200), flipCard);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });

        return flipCard;
    }

    // ─── Article Detail (show) ─────────────────────────────────

    private void showArticleDetail(Article article) {
        VBox detailContent = new VBox(20);
        detailContent.setPadding(new Insets(25, 40, 25, 40));
        detailContent.setStyle("-fx-background-color: #f0f2f5;");

        // Back button
        Button btnBack = new Button("← Back to Articles");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #2d8659; -fx-font-weight: bold; "
                + "-fx-font-size: 13; -fx-cursor: hand; -fx-padding: 5 0;");
        btnBack.setOnAction(e -> {
            scrollPane.setContent(articleGridPane);
            showArticleGrid();
        });

        // Article container
        VBox articleCard = new VBox(20);
        articleCard.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 30; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 3);");

        // Image
        if (article.getImage() != null && !article.getImage().isEmpty()) {
            try {
                Image img;
                if (article.getImage().startsWith("http")) {
                    img = new Image(article.getImage(), 800, 300, true, true, true);
                } else {
                    img = new Image("file:" + article.getImage(), 800, 300, true, true, true);
                }
                ImageView iv = new ImageView(img);
                iv.setFitWidth(800);
                iv.setPreserveRatio(true);
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(800, 300);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                iv.setClip(clip);
                articleCard.getChildren().add(iv);
            } catch (Exception ignored) {}
        }

        // Title
        Label titre = new Label(article.getTitre());
        titre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titre.setStyle("-fx-text-fill: #1f5e42;");
        titre.setWrapText(true);

        // Meta bar
        HBox metaBox = new HBox(20);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        metaBox.setPadding(new Insets(0, 0, 15, 0));
        metaBox.setStyle("-fx-border-color: transparent transparent #e2e8f0 transparent; -fx-border-width: 0 0 1 0;");

        Label authorLabel = new Label("👤 PharmaX Team");
        authorLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 12;");

        Label dateLabel = new Label("📅 " + (article.getDateCreation() != null
                ? article.getDateCreation().format(DATE_FMT_FULL) : ""));
        dateLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 12;");

        metaBox.getChildren().addAll(authorLabel, dateLabel);

        // Full content
        Label contenu = new Label(article.getContenu());
        contenu.setStyle("-fx-text-fill: #2d3748; -fx-font-size: 14; -fx-line-spacing: 1.6;");
        contenu.setWrapText(true);

        // Translation Tools (Front End)
        HBox translateBox = new HBox(10);
        translateBox.setAlignment(Pos.CENTER_LEFT);
        translateBox.setPadding(new Insets(15, 15, 15, 15));
        translateBox.setStyle("-fx-background-color: #f7fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        
        Label translateLabel = new Label("🌐 Translate Article:");
        translateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        
        ComboBox<String> langCombo = new ComboBox<>(FXCollections.observableArrayList(
                "English", "Español", "Deutsch", "Italiano", "العربية", "Français (Original)"
        ));
        langCombo.setValue("English");
        
        Button btnTranslate = new Button("Translate");
        btnTranslate.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #4a5568; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 6;");
        
        ProgressIndicator translateSpinner = new ProgressIndicator();
        translateSpinner.setPrefSize(20, 20);
        translateSpinner.setVisible(false);

        btnTranslate.setOnAction(e -> {
            String targetLang = langCombo.getValue();
            if ("Français (Original)".equals(targetLang)) {
                contenu.setText(article.getContenu());
                return;
            }
            
            btnTranslate.setDisable(true);
            translateSpinner.setVisible(true);
            
            new Thread(() -> {
                try {
                    String translated = TranslationService.getInstance().translateText(article.getContenu(), targetLang);
                    Platform.runLater(() -> {
                        contenu.setText(translated);
                        btnTranslate.setDisable(false);
                        translateSpinner.setVisible(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        System.err.println("Translation Error: " + ex.getMessage());
                        btnTranslate.setDisable(false);
                        translateSpinner.setVisible(false);
                    });
                }
            }).start();
        });
        
        translateBox.getChildren().addAll(translateLabel, langCombo, btnTranslate, translateSpinner);

        articleCard.getChildren().addAll(titre, metaBox, translateBox, contenu);

        // Comments section (without name field)
        VBox commentsSection = buildCommentsSection(article);

        detailContent.getChildren().addAll(btnBack, articleCard, commentsSection);
        scrollPane.setContent(detailContent);
    }

    private VBox buildCommentsSection(Article article) {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 24; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        Label title = new Label("💬 Comments");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setStyle("-fx-text-fill: #1a202c;");

        List<Commentaire> comments = commentaireService.findByArticle(article);
        
        if (comments.isEmpty()) {
            Label empty = new Label("No comments yet. Be the first!");
            empty.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 13;");
            section.getChildren().addAll(title, empty);
        } else {
            VBox commentsList = new VBox(10);
            for (Commentaire comment : comments) {
                if ("valide".equals(comment.getStatut())) {
                    VBox commentItem = buildCommentItem(comment);
                    commentsList.getChildren().add(commentItem);
                }
            }
            section.getChildren().addAll(title, commentsList);
        }

        // Add comment form (NO name field — user system handles auth)
        VBox formSection = buildCommentForm(article);
        section.getChildren().add(formSection);

        return section;
    }

    private VBox buildCommentItem(Commentaire comment) {
        VBox item = new VBox(6);
        item.setStyle("-fx-background-color: #f7fafc; -fx-background-radius: 10; -fx-padding: 14;");

        Label date = new Label(comment.getDatePublication() != null 
                ? comment.getDatePublication().format(DATE_FMT_FULL) 
                : "");
        date.setStyle("-fx-text-fill: #a0aec0; -fx-font-size: 11;");

        Label text = new Label(comment.getContenu());
        text.setStyle("-fx-text-fill: #2d3748; -fx-font-size: 13; -fx-line-spacing: 1.4;");
        text.setWrapText(true);

        item.getChildren().addAll(date, text);
        return item;
    }

    private VBox buildCommentForm(Article article) {
        VBox form = new VBox(10);
        form.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0; -fx-padding: 20 0 0 0;");

        Label formTitle = new Label("Leave a Comment");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        formTitle.setStyle("-fx-text-fill: #1a202c;");

        Label commentLabel = new Label("Comment");
        commentLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 12; -fx-font-weight: bold;");

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Share your thoughts...");
        commentArea.setPrefRowCount(3);
        commentArea.setWrapText(true);

        Label feedbackLabel = new Label("");
        feedbackLabel.setStyle("-fx-font-size: 12;");

        Button btnSubmit = new Button("Post Comment");
        btnSubmit.getStyleClass().add("btn-primary");
        btnSubmit.setStyle("-fx-background-color: linear-gradient(to right, #2d8659, #4a9f6f); -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-padding: 10 24; -fx-cursor: hand; -fx-background-radius: 8;");

        btnSubmit.setOnAction(e -> {
            String content = commentArea.getText().trim();

            if (content.isEmpty()) {
                feedbackLabel.setText("⚠ Please enter a comment");
                feedbackLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
            } else {
                new Thread(() -> {
                    try {
                        java.util.Map<String, Object> result = commentaireService.createWithModeration(content, article, "User");
                        Platform.runLater(() -> {
                            if ((boolean) result.get("success")) {
                                feedbackLabel.setText("✅ Comment posted!");
                                feedbackLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12;");
                                commentArea.clear();
                            } else {
                                feedbackLabel.setText("❌ " + result.get("message"));
                                feedbackLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
                            }
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            feedbackLabel.setText("❌ " + ex.getMessage());
                            feedbackLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");
                        });
                    }
                }).start();
            }
        });

        form.getChildren().addAll(formTitle, commentLabel, commentArea, feedbackLabel, btnSubmit);
        return form;
    }
}
