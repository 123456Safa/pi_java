-- Créer la table user si elle n'existe pas
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `nom` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) UNIQUE NOT NULL,
    `mot_de_passe` VARCHAR(255),
    `role` VARCHAR(50) DEFAULT 'client'
);

-- Insérer un utilisateur par défaut
INSERT IGNORE INTO `user` (nom, email, mot_de_passe, role) VALUES
('Client Pharmax', 'client@pharmax.com', 'pass123', 'client'),
('Admin Pharmax', 'admin@pharmax.com', 'admin123', 'admin');

-- Ajouter la clé étrangère si elle n'existe pas
ALTER TABLE `commandes` ADD CONSTRAINT `FK_35D4282CFB88E14F`
FOREIGN KEY (`utilisateur_id`) REFERENCES `user` (`id`);

-- Vérification
SELECT * FROM `user`;

