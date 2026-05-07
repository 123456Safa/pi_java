-- ============================================
-- PHARMAX - Ajout des tables manquantes
-- Tables: reclamation et reponse
-- ============================================

USE pharmaxfinal;

-- Créer la table reclamation
CREATE TABLE IF NOT EXISTS reclamation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(50) DEFAULT 'en_attente',
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE
);

-- Créer la table reponse
CREATE TABLE IF NOT EXISTS reponse (
    id INT PRIMARY KEY AUTO_INCREMENT,
    contenu TEXT NOT NULL,
    date_reponse DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reclamation_id INT,
    FOREIGN KEY (reclamation_id) REFERENCES reclamation (id) ON DELETE CASCADE
);

-- Vérification
SELECT '✅ Tables reclamation et reponse créées!' as Status;
