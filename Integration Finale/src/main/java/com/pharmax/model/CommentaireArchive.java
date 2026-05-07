package com.pharmax.model;

import java.time.LocalDateTime;

/**
 * Entity CommentaireArchive — corresponds to PHP App\Entity\CommentaireArchive
 * Stores blocked/inappropriate comments for audit purposes.
 */
public class  CommentaireArchive {

    private Integer id;
    private String contenu;
    private LocalDateTime datePublication;
    private String userName;
    private String userEmail;
    private String reason; // Why it was blocked (default: "inappropriate")
    private Article article;
    private LocalDateTime archivedAt;

    // ─── Constructor ───────────────────────────────────────────
    public CommentaireArchive() {
        this.archivedAt = LocalDateTime.now();
        this.reason = "inappropriate";
    }

    // ─── Getters & Setters ─────────────────────────────────────

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public CommentaireArchive setContenu(String contenu) {
        this.contenu = contenu;
        return this;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public CommentaireArchive setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public CommentaireArchive setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public CommentaireArchive setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        return this;
    }

    public String getReason() {
        return reason;
    }

    public CommentaireArchive setReason(String reason) {
        this.reason = reason;
        return this;
    }

    public Article getArticle() {
        return article;
    }

    public CommentaireArchive setArticle(Article article) {
        this.article = article;
        return this;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public CommentaireArchive setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
        return this;
    }

    @Override
    public String toString() {
        return "Archive #" + id + " [" + reason + "]";
    }
}
