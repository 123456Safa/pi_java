package com.pharmax.model;

/**
 * Lightweight DTO representing a scraped article preview.
 * This object is NEVER persisted — it is preview-only.
 *
 * Fields:
 *  - title      : article headline
 *  - description: short excerpt / teaser text
 *  - imageUrl   : URL of the article thumbnail
 *  - sourceName : name of the source website
 *  - sourceUrl  : direct link to the original article
 */
public class ScrapedArticle {

    private String title;
    private String description;
    private String imageUrl;
    private String sourceName;
    private String sourceUrl;

    // ─── Constructor ────────────────────────────────────────────

    public ScrapedArticle() {}

    public ScrapedArticle(String title, String description,
                          String imageUrl, String sourceName, String sourceUrl) {
        this.title       = title;
        this.description = description;
        this.imageUrl    = imageUrl;
        this.sourceName  = sourceName;
        this.sourceUrl   = sourceUrl;
    }

    // ─── Getters & Setters ──────────────────────────────────────

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    // ─── Display helper ─────────────────────────────────────────

    @Override
    public String toString() {
        return "[" + sourceName + "] " + title;
    }
}
