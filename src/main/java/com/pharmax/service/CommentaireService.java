package com.pharmax.service;

import com.pharmax.model.Article;
import com.pharmax.model.Commentaire;
import com.pharmax.model.CommentaireArchive;
import com.pharmax.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * CommentaireService — CRUD operations for Commentaire and CommentaireArchive
 * entities via JDBC.
 * Corresponds to PHP CommentaireRepository + CommentaireArchiveRepository +
 * CommentaireController + CommentaireApiController.
 * Connected to the pharmax MySQL database (same as Symfony).
 *
 * Tables:
 *   - commentaire: id, contenu, created_at, statut, article_id, produit_id, user_id
 *   - archive_de_commentaire: id, contenu, date_publication, user_name, user_email, reason, article_id, archived_at
 */
public class CommentaireService {

    private final CommentValidationService validationService;
    private final CommentModerationService moderationService;
    private final ArticleService articleService;

    // ─── Constructor ───────────────────────────────────────────

    public CommentaireService(CommentValidationService validationService,
            CommentModerationService moderationService,
            ArticleService articleService) {
        this.validationService = validationService;
        this.moderationService = moderationService;
        this.articleService = articleService;
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
            CommentaireArchive archive = createArchive(contenu, article, userName, null, "inappropriate");

            result.put("success", false);
            result.put("warning", "Votre commentaire contient un langage inapproprié et n'a pas pu être publié.");
            result.put("status", "BLOQUE");
            result.put("message", "Commentaire bloqué pour contenu inapproprié");
            result.put("archive", archive);
            return result;
        }

        // ✅ Content is appropriate — create comment
        Commentaire commentaire = createDirect(contenu, article, "valide");

        if (commentaire != null) {
            result.put("success", true);
            result.put("message", "Votre commentaire a été publié avec succès");
            result.put("status", "VALIDE");
            result.put("comment", commentaire);
        } else {
            result.put("success", false);
            result.put("message", "Erreur lors de la création du commentaire");
        }
        return result;
    }

    /**
     * Create a comment directly (admin-created, bypasses moderation).
     */
    public Commentaire createDirect(String contenu, Article article, String statut) {
        String sql = "INSERT INTO commentaire (contenu, created_at, statut, article_id) VALUES (?, ?, ?, ?)";
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setString(1, contenu);
            ps.setTimestamp(2, now);
            ps.setString(3, statut != null ? statut : "valide");
            if (article != null && article.getId() != null) {
                ps.setInt(4, article.getId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            Commentaire commentaire = new Commentaire();
            if (keys.next()) {
                commentaire.setId(keys.getInt(1));
            }
            commentaire.setContenu(contenu);
            commentaire.setArticle(article);
            commentaire.setStatut(statut != null ? statut : "valide");
            commentaire.setDatePublication(now.toLocalDateTime());
            return commentaire;

        } catch (SQLException e) {
            System.err.println("❌ Erreur création commentaire: " + e.getMessage());
            return null;
        }
    }

    // ─── READ ──────────────────────────────────────────────────

    public Commentaire find(int id) {
        String sql = "SELECT * FROM commentaire WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapCommentaire(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche commentaire: " + e.getMessage());
        }
        return null;
    }

    public List<Commentaire> findAll() {
        String sql = "SELECT * FROM commentaire ORDER BY created_at DESC";
        return executeCommentQueryList(sql);
    }

    public List<Commentaire> findByArticle(Article article) {
        String sql = "SELECT * FROM commentaire WHERE article_id = ? ORDER BY created_at DESC";
        List<Commentaire> result = new ArrayList<>();
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, article.getId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Commentaire c = mapCommentaire(rs);
                c.setArticle(article);
                result.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche par article: " + e.getMessage());
        }
        return result;
    }

    public List<Commentaire> findByStatut(String statut) {
        String sql = "SELECT * FROM commentaire WHERE statut = ? ORDER BY created_at DESC";
        List<Commentaire> result = new ArrayList<>();
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, statut);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapCommentaire(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche par statut: " + e.getMessage());
        }
        return result;
    }

    // ─── UPDATE ────────────────────────────────────────────────

    public Commentaire update(int id, String contenu, String statut) {
        StringBuilder sql = new StringBuilder("UPDATE commentaire SET ");
        List<Object> params = new ArrayList<>();
        boolean first = true;

        if (contenu != null) {
            sql.append("contenu = ?");
            params.add(contenu);
            first = false;
        }
        if (statut != null && validationService.isValidStatus(statut)) {
            if (!first) sql.append(", ");
            sql.append("statut = ?");
            params.add(statut);
        }

        if (params.isEmpty()) return find(id);

        sql.append(" WHERE id = ?");
        params.add(id);

        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    ps.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                }
            }
            ps.executeUpdate();
            return find(id);

        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour commentaire: " + e.getMessage());
        }
        return null;
    }

    // ─── DELETE ─────────────────────────────────────────────────

    public boolean delete(int id) {
        String sql = "DELETE FROM commentaire WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression commentaire: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete multiple comments by IDs.
     *
     * @return number of deleted comments
     */
    public int deleteMultiple(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return 0;

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "DELETE FROM commentaire WHERE id IN (" + placeholders + ")";
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression multiple: " + e.getMessage());
        }
        return 0;
    }

    // ═══════════════════════════════════════════════════════════
    // ARCHIVE CRUD (table: archive_de_commentaire)
    // ═══════════════════════════════════════════════════════════

    /**
     * Create an archive entry for a blocked comment.
     */
    private CommentaireArchive createArchive(String contenu, Article article,
                                              String userName, String userEmail, String reason) {
        String sql = "INSERT INTO archive_de_commentaire (contenu, date_publication, user_name, user_email, reason, article_id, archived_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setString(1, contenu);
            ps.setTimestamp(2, now);
            ps.setString(3, userName != null ? userName : "Anonymous");
            ps.setString(4, userEmail);
            ps.setString(5, reason != null ? reason : "inappropriate");
            if (article != null && article.getId() != null) {
                ps.setInt(6, article.getId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setTimestamp(7, now);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            CommentaireArchive archive = new CommentaireArchive();
            if (keys.next()) {
                archive.setId(keys.getInt(1));
            }
            archive.setContenu(contenu);
            archive.setArticle(article);
            archive.setDatePublication(now.toLocalDateTime());
            archive.setUserName(userName);
            archive.setUserEmail(userEmail);
            archive.setReason(reason != null ? reason : "inappropriate");
            archive.setArchivedAt(now.toLocalDateTime());
            return archive;

        } catch (SQLException e) {
            System.err.println("❌ Erreur création archive: " + e.getMessage());
            return null;
        }
    }

    public CommentaireArchive findArchive(int id) {
        String sql = "SELECT * FROM archive_de_commentaire WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapArchive(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche archive: " + e.getMessage());
        }
        return null;
    }

    public List<CommentaireArchive> findAllArchives() {
        String sql = "SELECT * FROM archive_de_commentaire ORDER BY archived_at DESC";
        List<CommentaireArchive> result = new ArrayList<>();
        try (Connection cnx = DatabaseConnection.getInstance();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                result.add(mapArchive(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur liste archives: " + e.getMessage());
        }
        return result;
    }

    public boolean deleteArchive(int id) {
        String sql = "DELETE FROM archive_de_commentaire WHERE id = ?";
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression archive: " + e.getMessage());
        }
        return false;
    }

    public int deleteMultipleArchives(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return 0;

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "DELETE FROM archive_de_commentaire WHERE id IN (" + placeholders + ")";
        try (Connection cnx = DatabaseConnection.getInstance();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            for (int i = 0; i < ids.size(); i++) {
                ps.setInt(i + 1, ids.get(i));
            }
            return ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression multiple archives: " + e.getMessage());
        }
        return 0;
    }

    // ═══════════════════════════════════════════════════════════
    // STATISTICS (corresponds to CommentaireApiController::getStatistics)
    // ═══════════════════════════════════════════════════════════

    /**
     * Get comment statistics by status.
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new LinkedHashMap<>();
        int valide = 0, bloque = 0, enAttente = 0, total = 0;

        String sql = "SELECT statut, COUNT(*) as cnt FROM commentaire GROUP BY statut";
        try (Connection cnx = DatabaseConnection.getInstance();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String statut = rs.getString("statut").toLowerCase();
                int count = rs.getInt("cnt");
                total += count;
                switch (statut) {
                    case "valide": valide = count; break;
                    case "bloque": bloque = count; break;
                    case "en_attente": enAttente = count; break;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur statistiques commentaires: " + e.getMessage());
        }

        // Count archives
        int archiveCount = 0;
        String archiveSql = "SELECT COUNT(*) FROM archive_de_commentaire";
        try (Connection cnx = DatabaseConnection.getInstance();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(archiveSql)) {

            if (rs.next()) {
                archiveCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage archives: " + e.getMessage());
        }

        bloque += archiveCount;

        stats.put("approved", valide);
        stats.put("pending", enAttente);
        stats.put("blocked", bloque);
        stats.put("archived", archiveCount);
        stats.put("total", total + archiveCount);
        return stats;
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Execute a simple query and return a list of commentaires.
     */
    private List<Commentaire> executeCommentQueryList(String sql) {
        List<Commentaire> result = new ArrayList<>();
        try (Connection cnx = DatabaseConnection.getInstance();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                result.add(mapCommentaire(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur requête commentaires: " + e.getMessage());
        }
        return result;
    }

    /**
     * Map a ResultSet row to a Commentaire object.
     * Column names match the Symfony/Doctrine schema.
     */
    private Commentaire mapCommentaire(ResultSet rs) throws SQLException {
        Commentaire c = new Commentaire();
        c.setId(rs.getInt("id"));
        c.setContenu(rs.getString("contenu"));
        c.setStatut(rs.getString("statut"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            c.setDatePublication(createdAt.toLocalDateTime());
        }

        // Load associated article if article_id is present
        int articleId = rs.getInt("article_id");
        if (!rs.wasNull() && articleService != null) {
            Article article = articleService.find(articleId);
            c.setArticle(article);
        }

        return c;
    }

    /**
     * Map a ResultSet row to a CommentaireArchive object.
     * Column names match the Symfony/Doctrine schema (table: archive_de_commentaire).
     */
    private CommentaireArchive mapArchive(ResultSet rs) throws SQLException {
        CommentaireArchive archive = new CommentaireArchive();
        archive.setId(rs.getInt("id"));
        archive.setContenu(rs.getString("contenu"));
        archive.setUserName(rs.getString("user_name"));
        archive.setUserEmail(rs.getString("user_email"));
        archive.setReason(rs.getString("reason"));

        Timestamp datePub = rs.getTimestamp("date_publication");
        if (datePub != null) {
            archive.setDatePublication(datePub.toLocalDateTime());
        }

        Timestamp archivedAt = rs.getTimestamp("archived_at");
        if (archivedAt != null) {
            archive.setArchivedAt(archivedAt.toLocalDateTime());
        }

        // Load associated article
        int articleId = rs.getInt("article_id");
        if (!rs.wasNull() && articleService != null) {
            Article article = articleService.find(articleId);
            archive.setArticle(article);
        }

        return archive;
    }
}
