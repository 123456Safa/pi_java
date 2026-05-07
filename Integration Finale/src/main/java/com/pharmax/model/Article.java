package com.pharmax.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity Article — corresponds to PHP App\Entity\Article
 * Represents a blog article with draft/publish support and comments.
 */
public class Article {

    private Integer id;
    private String titre;
    private String contenu;
    private String contenuEn;
    private String image;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private int likes;
    private boolean isDraft;
    private final List<Commentaire> commentaires;

    // ─── Constructor ───────────────────────────────────────────
    public Article() {
        this.commentaires = new ArrayList<>();
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
        this.isDraft = true;
        this.likes = 0;
    }

    // ─── Getters & Setters ─────────────────────────────────────

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public Article setTitre(String titre) {
        this.titre = titre;
        return this;
    }

    public String getContenu() {
        return contenu;
    }

    public Article setContenu(String contenu) {
        this.contenu = contenu;
        return this;
    }

    public String getContenuEn() {
        return contenuEn;
    }

    public Article setContenuEn(String contenuEn) {
        this.contenuEn = contenuEn;
        return this;
    }

    public String getImage() {
        return image;
    }

    public Article setImage(String image) {
        this.image = image;
        return this;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public Article setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
        return this;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public Article setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
        return this;
    }

    public int getLikes() {
        return likes;
    }

    public Article setLikes(int likes) {
        this.likes = likes;
        return this;
    }

    public Article incrementLikes() {
        this.likes++;
        return this;
    }

    public Article decrementLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
        return this;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public Article setIsDraft(boolean isDraft) {
        this.isDraft = isDraft;
        return this;
    }

    public Article publish() {
        this.isDraft = false;
        return this;
    }

    public Article saveDraft() {
        this.isDraft = true;
        return this;
    }

    // ─── Comment Collection Management ─────────────────────────

    public List<Commentaire> getCommentaires() {
        return commentaires;
    }

    public Article addCommentaire(Commentaire commentaire) {
        if (!this.commentaires.contains(commentaire)) {
            this.commentaires.add(commentaire);
            commentaire.setArticle(this);
        }
        return this;
    }

    public Article removeCommentaire(Commentaire commentaire) {
        if (this.commentaires.remove(commentaire)) {
            if (commentaire.getArticle() == this) {
                commentaire.setArticle(null);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return titre != null ? titre : "Article #" + id;
    }
}
