package com.pharmax.service;

import com.pharmax.model.Article;
import com.pharmax.model.Commentaire;
import com.pharmax.model.CommentaireArchive;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * CommentaireService — CRUD operations for Commentaire and CommentaireArchive
 * entities.
 * Corresponds to PHP CommentaireRepository + CommentaireArchiveRepository +
 * CommentaireController + CommentaireApiController.
 * Uses in-memory storage (replace with JPA/Hibernate for DB persistence).
 */
public class CommentaireService {

    private final Map<Integer, Commentaire> commentaires = new LinkedHashMap<>();
    private final Map<Integer, CommentaireArchive> archives = new LinkedHashMap<>();
    private final AtomicInteger commentIdGen = new AtomicInteger(1);
    private final AtomicInteger archiveIdGen = new AtomicInteger(1);

    private final CommentValidationService validationService;
    private final CommentModerationService moderationService;

    // ─── Constructor ───────────────────────────────────────────

    public CommentaireService(CommentValidationService validationService,
            CommentModerationService moderationService) {
        this.validationService = validationService;
        this.moderationService = moderationService;
    }

    // ═══════════════════════════════════════════════════════════
    // COMMENTAIRE CRUD
    // ═══════════════════════════════════════════════════════════

    // ─── CREATE ────────────────────────────────────────────────

    /**
     * Create a new comment with validation and AI moderation.
     *
     * @param contenu  the comment text
     * @param article  the article being commented on
     * @param userName the author name
     * @return a map with result keys: "success", "message", "warning", "status",
     *         "comment", "archive"
     */
    public Map<String, Object> createWithModeration(String contenu, Article article, String userName) {
        Map<String, Object> result = new HashMap<>();

        // 1️⃣ Validate content
        List<String> errors = validationService.validateContent(contenu);
        if (!errors.isEmpty()) {
            result.put("success", false);
            result.put("message", errors.get(0));
            return result;
        }

        // 2️⃣ AI Moderation — analyze for inappropriate content
        boolean isToxic = moderationService.analyze(contenu);

        if (isToxic) {
            // ❌ Content is inappropriate — archive it
            CommentaireArchive archive = new CommentaireArchive();
            archive.setId(archiveIdGen.getAndIncrement());
            archive.setContenu(contenu);
            archive.setArticle(article);
            archive.setDatePublication(LocalDateTime.now());
            archive.setUserName(userName != null ? userName : "Anonymous");
            archive.setReason("inappropriate");
            archives.put(archive.getId(), archive);

            result.put("success", false);
            result.put("warning", "Votre commentaire contient un langage inapproprié et n'a pas pu être publié.");
            result.put("status", "BLOQUE");
            result.put("message", "Commentaire bloqué pour contenu inapproprié");
            result.put("archive", archive);
            return result;
        }

        // ✅ Content is appropriate — create comment
        Commentaire commentaire = new Commentaire();
        commentaire.setId(commentIdGen.getAndIncrement());
        commentaire.setContenu(contenu);
        commentaire.setArticle(article);
        commentaire.setUserName(userName);
        commentaire.setStatut("valide");
        commentaire.setDatePublication(LocalDateTime.now());
        commentaires.put(commentaire.getId(), commentaire);

        // Also add to article's comment list
        article.addCommentaire(commentaire);

        result.put("success", true);
        result.put("message", "Votre commentaire a été publié avec succès");
        result.put("status", "VALIDE");
        result.put("comment", commentaire);
        return result;
    }

    /**
     * Create a comment directly (admin-created, bypasses moderation).
     */
    public Commentaire createDirect(String contenu, Article article, String statut) {
        Commentaire commentaire = new Commentaire();
        commentaire.setId(commentIdGen.getAndIncrement());
        commentaire.setContenu(contenu);
        commentaire.setArticle(article);
        commentaire.setStatut(statut != null ? statut : "valide");
        commentaire.setDatePublication(LocalDateTime.now());
        commentaires.put(commentaire.getId(), commentaire);

        if (article != null) {
            article.addCommentaire(commentaire);
        }
        return commentaire;
    }

    // ─── READ ──────────────────────────────────────────────────

    public Commentaire find(int id) {
        return commentaires.get(id);
    }

    public List<Commentaire> findAll() {
        return new ArrayList<>(commentaires.values());
    }

    public List<Commentaire> findByArticle(Article article) {
        return commentaires.values().stream()
                .filter(c -> c.getArticle() != null && c.getArticle().getId().equals(article.getId()))
                .collect(Collectors.toList());
    }

    public List<Commentaire> findByStatut(String statut) {
        return commentaires.values().stream()
                .filter(c -> statut.equalsIgnoreCase(c.getStatut()))
                .collect(Collectors.toList());
    }

    // ─── UPDATE ────────────────────────────────────────────────

    public Commentaire update(int id, String contenu, String statut) {
        Commentaire commentaire = commentaires.get(id);
        if (commentaire == null)
            return null;

        if (contenu != null)
            commentaire.setContenu(contenu);
        if (statut != null && validationService.isValidStatus(statut)) {
            commentaire.setStatut(statut);
        }
        return commentaire;
    }

    // ─── DELETE ─────────────────────────────────────────────────

    public boolean delete(int id) {
        Commentaire c = commentaires.remove(id);
        if (c != null && c.getArticle() != null) {
            c.getArticle().removeCommentaire(c);
        }
        return c != null;
    }

    /**
     * Delete multiple comments by IDs.
     *
     * @return number of deleted comments
     */
    public int deleteMultiple(List<Integer> ids) {
        int count = 0;
        for (int id : ids) {
            if (delete(id))
                count++;
        }
        return count;
    }

    // ═══════════════════════════════════════════════════════════
    // ARCHIVE CRUD
    // ═══════════════════════════════════════════════════════════

    public CommentaireArchive findArchive(int id) {
        return archives.get(id);
    }

    public List<CommentaireArchive> findAllArchives() {
        return new ArrayList<>(archives.values());
    }

    public boolean deleteArchive(int id) {
        return archives.remove(id) != null;
    }

    public int deleteMultipleArchives(List<Integer> ids) {
        int count = 0;
        for (int id : ids) {
            if (deleteArchive(id))
                count++;
        }
        return count;
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTICS (corresponds to CommentaireApiController::getStatistics)
    // ═══════════════════════════════════════════════════════════

    /**
     * Get comment statistics by status.
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        int valide = 0, bloque = 0, enAttente = 0;

        for (Commentaire c : commentaires.values()) {
            switch (c.getStatut().toLowerCase()) {
                case "valide":
                    valide++;
                    break;
                case "bloque":
                    bloque++;
                    break;
                case "en_attente":
                    enAttente++;
                    break;
            }
        }

        bloque += archives.size(); // Count archived as blocked

        stats.put("approved", valide);
        stats.put("pending", enAttente);
        stats.put("blocked", bloque);
        stats.put("archived", archives.size());
        stats.put("total", commentaires.size() + archives.size());
        return stats;
    }
}
