-- ============================================================
-- PharmaX — Blog Tables Migration for 'pharm' database
-- Run this once to add blog tables to your existing 'pharm' DB
-- ============================================================

USE pharm;

-- ── Articles (Blog posts) ──────────────────────────────────
CREATE TABLE IF NOT EXISTS articles (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    titre       VARCHAR(255)  NOT NULL,
    contenu     TEXT          NOT NULL,
    image_url   VARCHAR(500)  NULL,
    categorie   VARCHAR(100)  NULL,
    statut      VARCHAR(20)   NOT NULL DEFAULT 'brouillon',
    -- brouillon | publie
    likes       INT           NOT NULL DEFAULT 0,
    dislikes    INT           NOT NULL DEFAULT 0,
    sauvegardes INT           NOT NULL DEFAULT 0,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Commentaires (Blog comments) ──────────────────────────
CREATE TABLE IF NOT EXISTS commentaires (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    article_id  INT          NOT NULL,
    contenu     TEXT         NOT NULL,
    auteur      VARCHAR(100) NOT NULL DEFAULT 'Anonyme',
    statut      VARCHAR(20)  NOT NULL DEFAULT 'en_attente',
    -- valide | refuse | en_attente
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_article
        FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Commentaires Archives ──────────────────────────────────
CREATE TABLE IF NOT EXISTS commentaires_archives (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    article_id      INT          NOT NULL,
    contenu         TEXT         NOT NULL,
    auteur          VARCHAR(100) NOT NULL DEFAULT 'Anonyme',
    statut_original VARCHAR(20)  NOT NULL,
    archived_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Notifications (for order/product events) ───────────────
CREATE TABLE IF NOT EXISTS notifications (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT          NOT NULL DEFAULT 1,
    type        VARCHAR(50)  NOT NULL,
    message     TEXT         NOT NULL,
    is_read     TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ── Sample Blog data ───────────────────────────────────────
INSERT IGNORE INTO articles (id, titre, contenu, statut, created_at) VALUES
(1, 'Les bienfaits de la vitamine D en hiver',
 'La vitamine D joue un rôle crucial dans le maintien de la santé osseuse et du système immunitaire. Pendant les mois d''hiver, notre exposition au soleil diminue considérablement, ce qui peut entraîner une carence. Il est recommandé de consommer des aliments riches en vitamine D comme les poissons gras, les œufs et les produits laitiers enrichis.',
 'publie', NOW()),

(2, 'Comment bien gérer son traitement antibiotique',
 'Les antibiotiques sont des médicaments puissants qui combattent les infections bactériennes. Il est essentiel de respecter la durée du traitement prescrit, même si les symptômes s''améliorent. Prendre un antibiotique de manière incorrecte peut contribuer à la résistance bactérienne.',
 'publie', NOW()),

(3, 'Prévention de la grippe saisonnière : nos conseils',
 'La saison de la grippe approche et il est important de se préparer. La vaccination reste le moyen le plus efficace de se protéger. En complément, adoptez les gestes barrières : lavage fréquent des mains, port du masque dans les lieux publics, et aération régulière des espaces clos.',
 'publie', NOW());

INSERT IGNORE INTO commentaires (article_id, contenu, auteur, statut) VALUES
(1, 'Merci pour ces explications claires !', 'Fatma B.', 'valide'),
(1, 'Article très informatif et utile.', 'Ahmed K.', 'valide'),
(2, 'Information importante sur la résistance bactérienne.', 'Sarra M.', 'valide'),
(3, 'Merci pour ces conseils pratiques.', 'Mohamed A.', 'valide');

-- ── Verify ────────────────────────────────────────────────
SELECT 'articles' AS table_name, COUNT(*) AS rows FROM articles
UNION ALL
SELECT 'commentaires', COUNT(*) FROM commentaires
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications;
