-- ═══════════════════════════════════════════════════════════════
-- PharmaX Database Setup Script
-- Creates fresh database with all entities
-- ═══════════════════════════════════════════════════════════════

-- Drop existing database if it exists
DROP DATABASE IF EXISTS pharmax;

-- Create new database
CREATE DATABASE pharmax CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the new database
USE pharmax;

-- ─────────────────────────────────────────────────────────────
-- Table: article
-- Represents a blog article with draft/publish support
-- ─────────────────────────────────────────────────────────────
CREATE TABLE article (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    contenu LONGTEXT,
    contenu_en LONGTEXT,
    image VARCHAR(255),
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    likes INT DEFAULT 0,
    is_draft BOOLEAN DEFAULT true,
    INDEX idx_date_creation (date_creation),
    INDEX idx_is_draft (is_draft)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────
-- Table: commentaire
-- Represents a comment attached to an article
-- ─────────────────────────────────────────────────────────────
CREATE TABLE commentaire (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contenu LONGTEXT NOT NULL,
    date_publication DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(20) DEFAULT 'en_attente',
    user_name VARCHAR(100),
    article_id INT NOT NULL,
    FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
    INDEX idx_statut (statut),
    INDEX idx_article_id (article_id),
    INDEX idx_date_publication (date_publication)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ─────────────────────────────────────────────────────────────
-- Table: commentaire_archive
-- Stores blocked/inappropriate comments for audit purposes
-- ─────────────────────────────────────────────────────────────
CREATE TABLE commentaire_archive (
    id INT AUTO_INCREMENT PRIMARY KEY,
    contenu LONGTEXT NOT NULL,
    date_publication DATETIME,
    user_name VARCHAR(100),
    user_email VARCHAR(255),
    reason VARCHAR(255) DEFAULT 'inappropriate',
    article_id INT,
    archived_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE SET NULL,
    INDEX idx_archived_at (archived_at),
    INDEX idx_article_id (article_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ═══════════════════════════════════════════════════════════════
-- Database setup complete
-- ═══════════════════════════════════════════════════════════════
