-- =====================================================
-- SCRIPT DE CORRECTION BASE DE DONNÉES PHARMAX
-- Exécutez ce script pour corriger les erreurs
-- =====================================================

USE pharm;

-- Étape 1: Créer la table `user` si elle n'existe pas
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `nom` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) UNIQUE NOT NULL,
    `mot_de_passe` VARCHAR(255),
    `role` VARCHAR(50) DEFAULT 'client'
);

-- Étape 2: Vérifier et corriger la colonne date → created_date dans la table commandes
-- Première, vérifier si la colonne 'date' existe et la renommer en 'created_date'
ALTER TABLE `commandes`
MODIFY COLUMN `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Étape 3: Insérer les utilisateurs par défaut
INSERT IGNORE INTO `user` (`nom`, `email`, `mot_de_passe`, `role`) VALUES
('Client Pharmax', 'client@pharmax.com', 'pass123', 'client'),
('Admin Pharmax', 'admin@pharmax.com', 'admin123', 'admin');

-- Étape 4: Ajouter la contrainte de clé étrangère si elle n'existe pas
ALTER TABLE `commandes` ADD CONSTRAINT `FK_commandes_user`
FOREIGN KEY (`utilisateur_id`) REFERENCES `user` (`id`)
ON DELETE SET NULL
ON UPDATE CASCADE;

-- Étape 5: Vérification finale
SELECT '✅ Correction terminée!' as Status;
SELECT COUNT(*) as 'Utilisateurs en BD' FROM `user`;
SELECT * FROM `user`;

