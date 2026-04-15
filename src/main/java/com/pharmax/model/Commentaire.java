package com.pharmax.model;

import java.time.LocalDateTime;

/**
 * Entity Commentaire — corresponds to PHP App\Entity\Commentaire
 * Represents a comment attached to an article or a product.
 * Validated by CommentValidationService before persistence.
 */
public class Commentaire {

    private Integer id;
    private String contenu;
    private LocalDateTime datePublication;
    private String statut;  // "valide", "bloque", "en_attente"
    private Article article;
    private String userName; // Simplified from User entity relationship

    // ─── Constructor ───────────────────────────────────────────
    public Commentaire() {
        this.datePublication = LocalDateTime.now();
        this.statut = "en_attente";
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

    public Commentaire setContenu(String contenu) {
        this.contenu = contenu;
        return this;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public Commentaire setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
        return this;
    }

    public String getStatut() {
        return statut;
    }

    public Commentaire setStatut(String statut) {
        this.statut = statut;
        return this;
    }

    public Article getArticle() {
        return article;
    }

    public Commentaire setArticle(Article article) {
        this.article = article;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public Commentaire setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @Override
    public String toString() {
        return "Commentaire #" + id + " [" + statut + "]";
    }
}
