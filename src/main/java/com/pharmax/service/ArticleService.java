package com.pharmax.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.pharmax.model.Article;
import com.pharmax.util.DatabaseConnection;

/**
 * ArticleService — CRUD operations for Article entities via JDBC.
 * Corresponds to PHP ArticleRepository + parts of
 * BlogController/ArticleController.
 * Connected to the pharmax MySQL database (same as Symfony).
 *
 * Table: article
 * Columns: id, titre, contenu, contenu_en, image, created_at, updated_at, likes, is_draft
 */
public class ArticleService {

    // ─── CREATE ────────────────────────────────────────────────

    /**
     * Create and persist a new article in the database.
     */
    public Article create(String titre, String contenu, String contenuEn, String image) {
        String sql = "INSERT INTO article (titre, contenu, contenu_en, image, created_at, updated_at, likes, is_draft) VALUES (?, ?, ?, ?, ?, ?, 0, 1)";
        Connection cnx = DatabaseConnection.getInstance();
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            ps.setString(1, titre);
            ps.setString(2, contenu);
            ps.setString(3, contenuEn);
            ps.setString(4, image);
            ps.setTimestamp(5, now);
            ps.setTimestamp(6, now);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            Article article = new Article();
            if (keys.next()) {
                article.setId(keys.getInt(1));
            }
            article.setTitre(titre);
            article.setContenu(contenu);
            article.setContenuEn(contenuEn);
            article.setImage(image);
            article.setDateCreation(LocalDateTime.now());
            article.setDateModification(LocalDateTime.now());
            return article;

        } catch (SQLException e) {
            System.err.println("❌ Erreur création article: " + e.getMessage());
            return null;
        }
    }

    // ─── READ ───────────────────────────────  ───────────────────

    /**
     * Find an article by ID.
     */
    public Article find(int id) {
        String sql = "SELECT * FROM article WHERE id = ?";
        Connection cnx = DatabaseConnection.getInstance();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapArticle(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche article: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find all articles.
     */
    public List<Article> findAll() {
        String sql = "SELECT * FROM article ORDER BY created_at DESC";
        return executeQueryList(sql);
    }

    /**
     * Find all published (non-draft) articles.
     */
    public List<Article> findPublished() {
        String sql = "SELECT * FROM article WHERE is_draft = 0 ORDER BY created_at DESC";
        return executeQueryList(sql);
    }

    /**
     * Find published articles sorted by newest first, with pagination.
     */
    public List<Article> findPublishedPaginated(int page, int itemsPerPage) {
        String sql = "SELECT * FROM article WHERE is_draft = 0 ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Article> result = new ArrayList<>();
        Connection cnx = DatabaseConnection.getInstance();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, itemsPerPage);
            ps.setInt(2, (page - 1) * itemsPerPage);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapArticle(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur pagination articles: " + e.getMessage());
        }
        return result;
    }

    /**
     * Search articles by title or content.
     */
    public List<Article> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findPublished();
        }
        String sql = "SELECT * FROM article WHERE is_draft = 0 AND (titre LIKE ? OR contenu LIKE ?) ORDER BY created_at DESC";
        List<Article> result = new ArrayList<>();
        Connection cnx = DatabaseConnection.getInstance();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            String pattern = "%" + query + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapArticle(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche articles: " + e.getMessage());
        }
        return result;
    }

    /**
     * Get total count of published articles.
     */
    public int countPublished() {
        String sql = "SELECT COUNT(*) FROM article WHERE is_draft = 0";
        Connection cnx = DatabaseConnection.getInstance();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage articles: " + e.getMessage());
        }
        return 0;
    }

    // ─── UPDATE ────────────────────────────────────────────────

    /**
     * Update an existing article.
     */
    public Article update(int id, String titre, String contenu, String contenuEn, String image) {
        String sql = "UPDATE article SET titre = ?, contenu = ?, contenu_en = ?, image = ?, updated_at = ? WHERE id = ?";
        Connection cnx = DatabaseConnection.getInstance();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, titre);
            ps.setString(2, contenu);
            ps.setString(3, contenuEn);
            ps.setString(4, image);
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(6, id);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                return find(id);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour article: " + e.getMessage());
        }
        return null;
    }

    /**
     * Toggle publish/draft status.
     */
    public Article togglePublish(int id) {
        String sql = "UPDATE article SET is_draft = NOT is_draft, updated_at = ? WHERE id = ?";
        Connection cnx = DatabaseConnection.getInstance();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, id);
            ps.executeUpdate();
            return find(id);
        } catch (SQLException e) {
            System.err.println("❌ Erreur toggle publish: " + e.getMessage());
        }
        return null;
    }

    /**
     * Like an article.
     */
    public Article like(int id) {
        String sql = "UPDATE article SET likes = likes + 1 WHERE id = ?";
        Connection cnx = DatabaseConnection.getInstance();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return find(id);
        } catch (SQLException e) {
            System.err.println("❌ Erreur like article: " + e.getMessage());
        }
        return null;
    }

    /**
     * Unlike an article.
     */
    public Article unlike(int id) {
        String sql = "UPDATE article SET likes = GREATEST(likes - 1, 0) WHERE id = ?";
        Connection cnx = DatabaseConnection.getInstance();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            return find(id);
        } catch (SQLException e) {
            System.err.println("❌ Erreur unlike article: " + e.getMessage());
        }
        return null;
    }

    // ─── DELETE ─────────────────────────────────────────────────

    /**
     * Delete an article by ID.
     * Also deletes associated comments and archives to satisfy FK constraints.
     */
    public boolean delete(int id) {
        Connection cnx = DatabaseConnection.getInstance();

        // 1️⃣ Delete associated archives (archive_de_commentaire.article_id)
        String deleteArchives = "DELETE FROM archive_de_commentaire WHERE article_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(deleteArchives)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("⚠ Erreur suppression archives liées: " + e.getMessage());
        }

        // 2️⃣ Delete associated comments (commentaire.article_id)
        String deleteComments = "DELETE FROM commentaire WHERE article_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(deleteComments)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("⚠ Erreur suppression commentaires liés: " + e.getMessage());
        }

        // 3️⃣ Delete the article itself
        String sql = "DELETE FROM article WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression article: " + e.getMessage());
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Execute a simple query and return a list of articles.
     */
    private List<Article> executeQueryList(String sql) {
        List<Article> result = new ArrayList<>();
        Connection cnx = DatabaseConnection.getInstance();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                result.add(mapArticle(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur requête articles: " + e.getMessage());
        }
        return result;
    }

    /**
     * Map a ResultSet row to an Article object.
     * Column names match the Symfony/Doctrine schema.
     */
    private Article mapArticle(ResultSet rs) throws SQLException {
        Article article = new Article();
        article.setId(rs.getInt("id"));
        article.setTitre(rs.getString("titre"));
        article.setContenu(rs.getString("contenu"));
        article.setContenuEn(rs.getString("contenu_en"));
        article.setImage(rs.getString("image"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            article.setDateCreation(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            article.setDateModification(updatedAt.toLocalDateTime());
        }

        article.setLikes(rs.getInt("likes"));
        article.setIsDraft(rs.getBoolean("is_draft"));
        return article;
    }
}
