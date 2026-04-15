package com.pharmax.service;

import com.pharmax.model.Article;
import com.pharmax.model.Commentaire;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * ArticleService — CRUD operations for Article entities.
 * Corresponds to PHP ArticleRepository + parts of
 * BlogController/ArticleController.
 * Uses in-memory storage (replace with JPA/Hibernate for DB persistence).
 */
public class ArticleService {

    private final Map<Integer, Article> articles = new LinkedHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    // ─── CREATE ────────────────────────────────────────────────

    /**
     * Create and persist a new article.
     */
    public Article create(String titre, String contenu, String image) {
        Article article = new Article();
        article.setId(idGenerator.getAndIncrement());
        article.setTitre(titre);
        article.setContenu(contenu);
        article.setImage(image);
        article.setDateCreation(LocalDateTime.now());
        article.setDateModification(LocalDateTime.now());
        articles.put(article.getId(), article);
        return article;
    }

    // ─── READ ──────────────────────────────────────────────────

    /**
     * Find an article by ID.
     */
    public Article find(int id) {
        return articles.get(id);
    }

    /**
     * Find all articles.
     */
    public List<Article> findAll() {
        return new ArrayList<>(articles.values());
    }

    /**
     * Find all published (non-draft) articles.
     */
    public List<Article> findPublished() {
        return articles.values().stream()
                .filter(a -> !a.isDraft())
                .collect(Collectors.toList());
    }

    /**
     * Find published articles sorted by newest first, with pagination.
     */
    public List<Article> findPublishedPaginated(int page, int itemsPerPage) {
        List<Article> published = findPublished();
        published.sort((a, b) -> {
            LocalDateTime dateA = a.getDateCreation() != null ? a.getDateCreation() : LocalDateTime.MIN;
            LocalDateTime dateB = b.getDateCreation() != null ? b.getDateCreation() : LocalDateTime.MIN;
            return dateB.compareTo(dateA);
        });

        int start = (page - 1) * itemsPerPage;
        if (start >= published.size())
            return Collections.emptyList();
        int end = Math.min(start + itemsPerPage, published.size());
        return published.subList(start, end);
    }

    /**
     * Search articles by title or content.
     */
    public List<Article> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findPublished();
        }
        String search = query.toLowerCase();
        return findPublished().stream()
                .filter(a -> (a.getTitre() != null && a.getTitre().toLowerCase().contains(search)) ||
                        (a.getContenu() != null && a.getContenu().toLowerCase().contains(search)))
                .collect(Collectors.toList());
    }

    /**
     * Get total count of published articles.
     */
    public int countPublished() {
        return (int) articles.values().stream().filter(a -> !a.isDraft()).count();
    }

    // ─── UPDATE ────────────────────────────────────────────────

    /**
     * Update an existing article.
     */
    public Article update(int id, String titre, String contenu, String image) {
        Article article = articles.get(id);
        if (article == null)
            return null;

        article.setTitre(titre);
        article.setContenu(contenu);
        article.setImage(image);
        article.setDateModification(LocalDateTime.now());
        return article;
    }

    /**
     * Toggle publish/draft status.
     */
    public Article togglePublish(int id) {
        Article article = articles.get(id);
        if (article == null)
            return null;

        if (article.isDraft()) {
            article.publish();
        } else {
            article.saveDraft();
        }
        return article;
    }

    /**
     * Like an article.
     */
    public Article like(int id) {
        Article article = articles.get(id);
        if (article != null) {
            article.incrementLikes();
        }
        return article;
    }

    /**
     * Unlike an article.
     */
    public Article unlike(int id) {
        Article article = articles.get(id);
        if (article != null) {
            article.decrementLikes();
        }
        return article;
    }

    // ─── DELETE ─────────────────────────────────────────────────

    /**
     * Delete an article by ID.
     */
    public boolean delete(int id) {
        return articles.remove(id) != null;
    }
}
