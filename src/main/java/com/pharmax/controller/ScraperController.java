package com.pharmax.controller;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pharmax.model.ScrapedArticle;
import com.pharmax.service.ScraperService;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * ScraperController — programmatic JavaFX panel for the article scraper.
 *
 * Equivalent to:  GET /api/articles/import?keyword=xxx
 *
 * Behaviour:
 *  - User types a keyword and presses Search (or Enter)
 *  - Scraping runs on a background daemon thread (UI stays responsive)
 *  - Results appear in a styled ListView (max 10, preview-only, no DB save)
 *  - Status bar shows result count and cache info
 *  - "Clear Cache" forces fresh fetch on next search
 *
 * Pattern mirrors AdminBlogController / FrontBlogController:
 *  call buildView() to get the root pane, then add it to the content area.
 */
public class ScraperController {

    private static final Logger LOG = Logger.getLogger(ScraperController.class.getName());

    // ─── Service ─────────────────────────────────────────────────────────────
    private final ScraperService scraperService = ScraperService.getInstance();

    // ─── UI nodes (built once in buildView) ──────────────────────────────────
    private TextField         keywordField;
    private ComboBox<String>  languageComboBox;
    private Button            searchButton;
    private Button            clearCacheButton;
    private Label             statusLabel;
    private ProgressIndicator spinner;
    private ListView<ScrapedArticle> resultsList;

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Builds and returns the scraper panel. Call this once and add to your
     * StackPane/VBox content area. Matches the pattern used by other controllers.
     */
    public VBox buildView() {
        VBox root = new VBox(14);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #f0f2f5;");
        VBox.setVgrow(root, Priority.ALWAYS);

        // ── Header ─────────────────────────────────────────────────────────
        Label title = new Label("🔍 Article Scraper");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; " +
                       "-fx-text-fill: #1f5e42; -fx-font-family: 'Segoe UI';");

        Label subtitle = new Label("Search health & medical articles by keyword  ·  powered by The Guardian Health & Santé Magazine");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096; " +
                          "-fx-font-family: 'Segoe UI';");

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e2e8f0;");

        // ── Search bar ─────────────────────────────────────────────────────
        keywordField = new TextField();
        keywordField.setPromptText("Enter keyword (e.g. cancer, treatment, médicament)");
        keywordField.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1a202c; " +
                              "-fx-prompt-text-fill: #a0aec0; -fx-border-color: #cbd5e0; " +
                              "-fx-border-radius: 6; -fx-background-radius: 6; " +
                              "-fx-font-size: 14px; -fx-padding: 8 12 8 12; " +
                              "-fx-font-family: 'Segoe UI';");
        HBox.setHgrow(keywordField, Priority.ALWAYS);
        keywordField.setOnAction(e -> handleSearch());

        languageComboBox = new ComboBox<>();
        languageComboBox.getItems().addAll("English", "Français");
        languageComboBox.setValue("English");
        languageComboBox.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #1a202c; " +
                                  "-fx-border-color: #cbd5e0; -fx-border-radius: 6; " +
                                  "-fx-background-radius: 6; -fx-font-size: 13px; " +
                                  "-fx-padding: 8 10 8 10; -fx-font-family: 'Segoe UI';");
        languageComboBox.setPrefWidth(120);

        searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: linear-gradient(to bottom, #2d8659, #1f5e42); -fx-text-fill: white; " +
                              "-fx-font-weight: bold; -fx-font-size: 14px; " +
                              "-fx-padding: 8 20 8 20; -fx-background-radius: 6; " +
                              "-fx-cursor: hand; -fx-font-family: 'Segoe UI';");
        searchButton.setOnAction(e -> handleSearch());

        clearCacheButton = new Button("Clear Cache");
        clearCacheButton.setStyle("-fx-background-color: #fff0f0; -fx-text-fill: #d73a49; -fx-border-color: #d73a49; -fx-border-radius: 6; " +
                                  "-fx-font-weight: bold; -fx-font-size: 13px; " +
                                  "-fx-padding: 7 13 7 13; -fx-background-radius: 6; " +
                                  "-fx-cursor: hand; -fx-font-family: 'Segoe UI';");
        clearCacheButton.setOnAction(e -> handleClearCache());

        spinner = new ProgressIndicator();
        spinner.setPrefSize(28, 28);
        spinner.setStyle("-fx-progress-color: #2d8659;");
        spinner.setVisible(false);

        HBox searchBar = new HBox(10, keywordField, languageComboBox, searchButton, clearCacheButton, spinner);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        // ── Status bar ─────────────────────────────────────────────────────
        statusLabel = new Label("Enter a keyword and press Search.");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #718096; " +
                             "-fx-font-family: 'Segoe UI';");

        // ── Results list ───────────────────────────────────────────────────
        resultsList = new ListView<>();
        resultsList.setCellFactory(lv -> new ArticleCell());
        resultsList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; " +
                             "-fx-control-inner-background: #f0f2f5;");
        VBox.setVgrow(resultsList, Priority.ALWAYS);

        // ── Footer ─────────────────────────────────────────────────────────
        Label footer = new Label(
                "Results are preview-only · Max 10 per search · " +
                "Repeated searches for the same keyword are served from cache.");
        footer.setStyle("-fx-font-size: 12px; -fx-text-fill: #a0aec0; " +
                        "-fx-font-family: 'Segoe UI';");

        root.getChildren().addAll(
                title, subtitle, sep,
                searchBar, statusLabel,
                resultsList, footer
        );

        LOG.info("ScraperController: view built.");
        return root;
    }

    // ─── Handlers ────────────────────────────────────────────────────────────

    /**
     * Fetches articles for the entered keyword.
     * Runs on a background thread — never blocks the JavaFX Application Thread.
     */
    private void handleSearch() {
        String keyword = keywordField.getText();
        String selectedLanguage = languageComboBox.getValue();
        String languageCode = "fr".equalsIgnoreCase(selectedLanguage) ? "fr" : "en";

        if (keyword == null || keyword.trim().isEmpty()) {
            statusLabel.setText("⚠ Please enter a keyword before searching.");
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e74c3c; " +
                                 "-fx-font-family: 'Segoe UI';");
            return;
        }

        setLoading(true);
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #3498db; " +
                             "-fx-font-family: 'Segoe UI';");
        statusLabel.setText("Searching for \"" + keyword.trim() + "\" in " + selectedLanguage + "…");
        resultsList.setItems(FXCollections.observableArrayList());

        Task<List<ScrapedArticle>> task = new Task<>() {
            @Override
            protected List<ScrapedArticle> call() {
                return scraperService.scrapeArticles(keyword.trim(), languageCode);
            }
        };

        task.setOnSucceeded(e -> {
            List<ScrapedArticle> articles = task.getValue();
            resultsList.setItems(FXCollections.observableArrayList(articles));

            String cacheInfo = " (cache: " + scraperService.getCacheSize() + " keyword(s) stored)";
            if (articles.isEmpty()) {
                statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e74c3c; " +
                                     "-fx-font-family: 'Segoe UI';");
                statusLabel.setText("No results found for \"" + keyword.trim() + "\" in " + selectedLanguage + "." + cacheInfo);
            } else {
                statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #27ae60; " +
                                     "-fx-font-family: 'Segoe UI';");
                statusLabel.setText("✔ Found " + articles.size() +
                        " article(s) for \"" + keyword.trim() + "\" in " + selectedLanguage + "." + cacheInfo);
            }
            setLoading(false);
            LOG.log(Level.INFO, String.format(
                "ScraperController: displayed %d result(s) for language: %s",
                articles.size(), languageCode));
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            LOG.log(Level.SEVERE, "ScraperController: background task failed.", ex);
            statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #e74c3c; " +
                                 "-fx-font-family: 'Segoe UI';");
            statusLabel.setText("❌ Error: " + (ex != null ? ex.getMessage() : "Unknown error"));
            setLoading(false);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);  // Daemon: won't block JVM shutdown
        thread.start();
    }

    /**
     * Clears the in-memory cache so the next search forces a fresh HTTP fetch.
     */
    private void handleClearCache() {
        scraperService.clearCache();
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #27ae60; " +
                             "-fx-font-family: 'Segoe UI';");
        statusLabel.setText("✔ Cache cleared — next search will fetch fresh results.");
        LOG.info("ScraperController: cache cleared by user.");
    }

    // ─── UI state helpers ────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        spinner.setVisible(loading);
        searchButton.setDisable(loading);
        keywordField.setDisable(loading);
        clearCacheButton.setDisable(loading);
    }

    // ─── Custom ListCell ─────────────────────────────────────────────────────

    /**
     * Renders each {@link ScrapedArticle} as a styled card inside the ListView.
     */
    private static class ArticleCell extends ListCell<ScrapedArticle> {

        private final Label titleLabel;
        private final Label descLabel;
        private final Label sourceLabel;
        private final Label urlLabel;
        private final VBox  card;

        ArticleCell() {
            titleLabel = new Label();
            titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; " +
                                "-fx-text-fill: #1f5e42; -fx-font-family: 'Segoe UI';");
            titleLabel.setWrapText(true);

            descLabel = new Label();
            descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4a5568; " +
                               "-fx-font-family: 'Segoe UI';");
            descLabel.setWrapText(true);
            descLabel.setMaxHeight(56);

            sourceLabel = new Label();
            sourceLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2d8659; " +
                                 "-fx-font-family: 'Segoe UI';");

            urlLabel = new Label();
            urlLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #a0aec0; " +
                              "-fx-font-family: 'Segoe UI';");
            urlLabel.setWrapText(true);

            Separator divider = new Separator();
            divider.setStyle("-fx-background-color: transparent;");

            card = new VBox(8, titleLabel, descLabel, sourceLabel, urlLabel, divider);
            card.setPadding(new Insets(16));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #e2e8f0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 2);");
        }

        @Override
        protected void updateItem(ScrapedArticle article, boolean empty) {
            super.updateItem(article, empty);
            if (empty || article == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                titleLabel.setText(article.getTitle());

                descLabel.setText(
                        article.getDescription() == null || article.getDescription().isEmpty()
                                ? "(No description available)"
                                : article.getDescription());

                sourceLabel.setText("📰  " + article.getSourceName());

                urlLabel.setText(
                        article.getSourceUrl() == null || article.getSourceUrl().isEmpty()
                                ? ""
                                : "🔗  " + article.getSourceUrl());

                card.setPrefWidth(getListView().getWidth() - 20);
                setGraphic(card);
                setText(null);
                setStyle("-fx-background-color: #181825; -fx-border-color: transparent;");
            }
        }
    }
}
