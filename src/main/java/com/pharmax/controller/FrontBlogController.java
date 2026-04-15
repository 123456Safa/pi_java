package com.pharmax.controller;

import com.pharmax.model.Article;
import com.pharmax.model.Commentaire;
import com.pharmax.service.ArticleService;
import com.pharmax.service.CommentaireService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * FrontBlogController — Front Office (User view).
 * Design matches the original Symfony blog templates.
 * Color: #5ea96b (Pharmax green), #2c3e50 (dark), #656565 (text).
 */
public class FrontBlogController {

    private final ArticleService articleService;
    private final CommentaireService commentaireService;

    private VBox articleListPane;
    private VBox articleDetailPane;
    private VBox rootView;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter DATE_FMT_FULL = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public FrontBlogController(ArticleService articleService, CommentaireService commentaireService) {
        this.articleService = articleService;
        this.commentaireService = commentaireService;
    }

    public VBox buildView() {
        rootView = new VBox();
        rootView.setStyle("-fx-background-color: #f8f9fa;");

        // Hero Header (gradient banner like original)
        VBox header = new VBox(5);
        header.setPadding(new Insets(35, 30, 30, 30));
        header.setStyle("-fx-background-color: linear-gradient(to right, #5ea96b, #3498db);");
        header.getStyleClass().add("front-header");

        Label title = new Label("Blog PharmaX");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: white;");

        Label subtitle = new Label("Découvrez nos articles sur la santé et la pharmacie");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.85); -fx-font-size: 14;");

        header.getChildren().addAll(title, subtitle);

        // ScrollPane content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #f8f9fa;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        articleListPane = new VBox(0);
        articleListPane.setPadding(new Insets(25, 40, 25, 40));
        articleListPane.setStyle("-fx-background-color: #f8f9fa;");

        articleDetailPane = new VBox(0);
        articleDetailPane.setPadding(new Insets(25, 40, 25, 40));
        articleDetailPane.setStyle("-fx-background-color: #f8f9fa;");

        scrollPane.setContent(articleListPane);

        rootView.getChildren().addAll(header, scrollPane);
        rootView.setUserData(scrollPane);

        return rootView;
    }

    // ─── Article List (index) ──────────────────────────────────

    public void refresh() {
        showArticleList();
    }

    private void showArticleList() {
        articleListPane.getChildren().clear();

        List<Article> published = articleService.findPublished();

        if (published.isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(60, 20, 60, 20));
            Label icon = new Label("📭");
            icon.setStyle("-fx-font-size: 60;");
            Label msg = new Label("Aucun article publié pour le moment.");
            msg.setStyle("-fx-font-size: 14; -fx-text-fill: #999;");
            emptyState.getChildren().addAll(icon, msg);
            articleListPane.getChildren().add(emptyState);
        } else {
            for (Article article : published) {
                VBox card = createArticleCard(article);
                articleListPane.getChildren().add(card);
            }
        }

        ScrollPane sp = (ScrollPane) rootView.getUserData();
        sp.setContent(articleListPane);
    }

    private VBox createArticleCard(Article article) {
        // Outer container with bottom border (like original article-item)
        VBox card = new VBox(10);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #e0e0e0 transparent; "
                + "-fx-border-width: 0 0 1 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 3, 0, 0, 1);");

        // Title
        Label titre = new Label(article.getTitre());
        titre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titre.setStyle("-fx-text-fill: #2c3e50;");
        titre.setWrapText(true);

        // Category badge
        Label category = new Label("BLOG");
        category.setStyle("-fx-background-color: #5ea96b; -fx-text-fill: white; -fx-padding: 3 10; "
                + "-fx-font-size: 10; -fx-font-weight: bold;");

        // Excerpt
        String preview = article.getContenu() != null
                ? (article.getContenu().length() > 120 ? article.getContenu().substring(0, 120) + "..." : article.getContenu())
                : "";
        Label content = new Label(preview);
        content.setStyle("-fx-text-fill: #656565; -fx-font-size: 14;");
        content.setWrapText(true);

        // Meta line (like original post-meta)
        HBox meta = new HBox(20);
        meta.setAlignment(Pos.CENTER_LEFT);

        Label date = new Label("📅 " + (article.getDateCreation() != null
                ? article.getDateCreation().format(DATE_FMT) : ""));
        date.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");

        int commentCount = commentaireService.findByArticle(article).size();
        Label comments = new Label("💬 " + commentCount);
        comments.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");

        Label likes = new Label("❤ " + article.getLikes());
        likes.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12;");

        meta.getChildren().addAll(date, comments, likes);

        // Actions row
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(10, 0, 0, 0));

        Button btnRead = new Button("Lire l'article →");
        btnRead.getStyleClass().add("btn-read");
        btnRead.setStyle("-fx-background-color: #5ea96b; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 12; -fx-padding: 8 16; -fx-cursor: hand;");
        btnRead.setOnAction(e -> showArticleDetail(article));

        Button btnLike = new Button("♡ J'aime");
        btnLike.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd; -fx-border-width: 1; "
                + "-fx-text-fill: #656565; -fx-font-size: 11; -fx-padding: 6 12; -fx-cursor: hand;");
        btnLike.setOnAction(e -> {
            articleService.like(article.getId());
            likes.setText("❤ " + article.getLikes());
        });

        actions.getChildren().addAll(btnRead, btnLike);

        card.getChildren().addAll(category, titre, content, meta, actions);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: white; -fx-border-color: #5ea96b transparent #e0e0e0 transparent; "
                + "-fx-border-width: 0 0 1 4; -fx-effect: dropshadow(three-pass-box, rgba(94,169,107,0.12), 8, 0, 0, 2);"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-border-color: transparent transparent #e0e0e0 transparent; "
                + "-fx-border-width: 0 0 1 0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 3, 0, 0, 1);"));

        return card;
    }

    // ─── Article Detail (show) ─────────────────────────────────

    private void showArticleDetail(Article article) {
        articleDetailPane.getChildren().clear();

        // Back link (like original)
        Button btnBack = new Button("← Retour");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #5ea96b; -fx-font-weight: bold; "
                + "-fx-font-size: 13; -fx-cursor: hand; -fx-padding: 5 0 15 0;");
        btnBack.setOnAction(e -> showArticleList());

        // Article card (matching show.html.twig)
        VBox articleCard = new VBox(0);
        articleCard.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 4, 0, 0, 2);");

        // Article inner content
        VBox innerContent = new VBox(15);
        innerContent.setPadding(new Insets(30));

        // Title
        Label titre = new Label(article.getTitre());
        titre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titre.setStyle("-fx-text-fill: #2c3e50;");
        titre.setWrapText(true);

        // Meta bar with green border-bottom (like original)
        HBox metaBox = new HBox(20);
        metaBox.setAlignment(Pos.CENTER_LEFT);
        metaBox.setPadding(new Insets(0, 0, 15, 0));
        metaBox.setStyle("-fx-border-color: transparent transparent #5ea96b transparent; -fx-border-width: 0 0 2 0;");

        Label authorLabel = new Label("👤 Admin");
        authorLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 13;");

        Label dateLabel = new Label("📅 " + (article.getDateCreation() != null
                ? article.getDateCreation().format(DATE_FMT) : ""));
        dateLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 13;");

        int cmtCount = commentaireService.findByArticle(article).size();
        Label commentsLabel = new Label("💬 " + cmtCount + " Comments");
        commentsLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 13;");

        metaBox.getChildren().addAll(authorLabel, dateLabel, commentsLabel);

        // Full content (matching article.contenu|nl2br)
        Label contenu = new Label(article.getContenu());
        contenu.setStyle("-fx-text-fill: #656565; -fx-font-size: 15; -fx-line-spacing: 5;");
        contenu.setWrapText(true);

        // Article actions
        HBox articleActions = new HBox(15);
        articleActions.setAlignment(Pos.CENTER_LEFT);
        articleActions.setPadding(new Insets(20, 0, 0, 0));
        articleActions.setStyle("-fx-border-color: #f0f0f0 transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        Label likesLabel = new Label("❤ " + article.getLikes());
        likesLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14; -fx-font-weight: bold;");

        Button btnLike = new Button("♡ J'aime");
        btnLike.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd; -fx-border-width: 1; "
                + "-fx-text-fill: #656565; -fx-padding: 6 12; -fx-cursor: hand;");
        btnLike.setOnAction(e -> {
            articleService.like(article.getId());
            likesLabel.setText("❤ " + article.getLikes());
        });

        // Category tag
        Label catTag = new Label("BLOG");
        catTag.setStyle("-fx-background-color: #5ea96b; -fx-text-fill: white; -fx-padding: 5 12; "
                + "-fx-font-size: 11; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        articleActions.getChildren().addAll(likesLabel, btnLike, spacer, catTag);

        innerContent.getChildren().addAll(titre, metaBox, contenu, articleActions);
        articleCard.getChildren().add(innerContent);

        // ─── Comments Section ──────────────────────────────────
        VBox commentSection = buildCommentSection(article);

        articleDetailPane.getChildren().addAll(btnBack, articleCard, commentSection);

        ScrollPane sp = (ScrollPane) rootView.getUserData();
        sp.setContent(articleDetailPane);
        sp.setVvalue(0);
    }

    private VBox buildCommentSection(Article article) {
        VBox section = new VBox(0);
        section.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 4, 0, 0, 2);");
        section.setPadding(new Insets(30));
        VBox.setMargin(section, new Insets(25, 0, 0, 0));

        List<Commentaire> comments = commentaireService.findByArticle(article);
        long validCount = comments.stream().filter(c -> "valide".equalsIgnoreCase(c.getStatut())).count();

        // Section header (matching original style)
        Label sectionTitle = new Label("💬 Comments (" + validCount + ")");
        sectionTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sectionTitle.setStyle("-fx-text-fill: #2c3e50; -fx-border-color: transparent transparent #5ea96b transparent; "
                + "-fx-border-width: 0 0 3 0; -fx-padding: 0 0 12 0;");

        section.getChildren().add(sectionTitle);

        // Comment list
        VBox commentsContainer = new VBox(15);
        commentsContainer.setPadding(new Insets(20, 0, 0, 0));

        boolean hasValidComments = false;
        for (Commentaire c : comments) {
            if (!"valide".equalsIgnoreCase(c.getStatut())) continue;
            hasValidComments = true;

            // Comment card (matching original: border-left: 4px solid #5ea96b, bg: #f9f9f9)
            VBox commentCard = new VBox(8);
            commentCard.setPadding(new Insets(15, 20, 15, 20));
            commentCard.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: transparent transparent transparent #5ea96b; "
                    + "-fx-border-width: 0 0 0 4;");

            // User + Date row
            HBox userRow = new HBox(15);
            userRow.setAlignment(Pos.CENTER_LEFT);

            Label userName = new Label("👤 " + (c.getUserName() != null ? c.getUserName() : "Utilisateur"));
            userName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            userName.setStyle("-fx-text-fill: #2c3e50;");

            Label commentDate = new Label(c.getDatePublication() != null
                    ? c.getDatePublication().format(DATE_FMT_FULL) : "");
            commentDate.setStyle("-fx-font-size: 11; -fx-text-fill: #999;");

            userRow.getChildren().addAll(userName, commentDate);

            // Comment text
            Label commentText = new Label(c.getContenu());
            commentText.setWrapText(true);
            commentText.setStyle("-fx-font-size: 13; -fx-text-fill: #656565; -fx-line-spacing: 2;");

            commentCard.getChildren().addAll(userRow, commentText);
            commentsContainer.getChildren().add(commentCard);
        }

        if (!hasValidComments) {
            Label noComments = new Label("Aucun commentaire. Soyez le premier à commenter !");
            noComments.setStyle("-fx-text-fill: #999; -fx-font-size: 14; -fx-padding: 30 0;");
            noComments.setAlignment(Pos.CENTER);
            commentsContainer.getChildren().add(noComments);
        }

        section.getChildren().add(commentsContainer);

        // ─── Add Comment Form (matching original) ──────────────
        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(20, 0, 20, 0));
        section.getChildren().add(sep);

        Label formTitle = new Label("✏ Ajouter un commentaire");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        formTitle.setStyle("-fx-text-fill: #2c3e50;");

        VBox.setMargin(formTitle, new Insets(0, 0, 10, 0));

        TextArea commentInput = new TextArea();
        commentInput.setPromptText("Écrire votre commentaire...");
        commentInput.setPrefRowCount(4);
        commentInput.setStyle("-fx-border-color: #ddd; -fx-border-width: 2; -fx-font-size: 14; -fx-padding: 10;");

        // Focus styling
        commentInput.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                commentInput.setStyle("-fx-border-color: #5ea96b; -fx-border-width: 2; -fx-font-size: 14; -fx-padding: 10;");
            } else {
                commentInput.setStyle("-fx-border-color: #ddd; -fx-border-width: 2; -fx-font-size: 14; -fx-padding: 10;");
            }
        });

        Label feedback = new Label();
        feedback.setWrapText(true);

        Button btnSubmit = new Button("📤 Publier le commentaire");
        btnSubmit.setStyle("-fx-background-color: #5ea96b; -fx-text-fill: white; -fx-font-weight: bold; "
                + "-fx-font-size: 13; -fx-padding: 10 25; -fx-cursor: hand;");
        btnSubmit.setOnAction(e -> {
            String text = commentInput.getText().trim();
            if (text.isEmpty()) {
                showFeedback(feedback, "Le commentaire ne peut pas être vide.", true);
                return;
            }
            if (text.length() < 2) {
                showFeedback(feedback, "Le commentaire doit contenir au moins 2 caractères.", true);
                return;
            }

            Map<String, Object> result = commentaireService.createWithModeration(text, article, "Utilisateur");
            boolean success = (boolean) result.get("success");

            if (success) {
                showFeedback(feedback, (String) result.get("message"), false);
                commentInput.clear();
                showArticleDetail(article); // Refresh
            } else {
                String msg = result.containsKey("warning") ? (String) result.get("warning") : (String) result.get("message");
                showFeedback(feedback, msg, true);
            }
        });

        HBox submitRow = new HBox(15, btnSubmit);
        submitRow.setAlignment(Pos.CENTER_LEFT);
        submitRow.setPadding(new Insets(10, 0, 0, 0));

        section.getChildren().addAll(formTitle, commentInput, feedback, submitRow);

        return section;
    }

    private void showFeedback(Label label, String message, boolean isError) {
        label.setText(message);
        if (isError) {
            label.setStyle("-fx-text-fill: #721c24; -fx-background-color: #f8d7da; -fx-padding: 8 12; "
                    + "-fx-border-color: #f5c6cb; -fx-border-width: 1; -fx-font-size: 13;");
        } else {
            label.setStyle("-fx-text-fill: #155724; -fx-background-color: #d4edda; -fx-padding: 8 12; "
                    + "-fx-border-color: #c3e6cb; -fx-border-width: 1; -fx-font-size: 13;");
        }
    }
}
