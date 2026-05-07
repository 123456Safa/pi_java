-- ============================================
-- PHARMAX - Script de Configuration Complète
-- Base de données: pharmax (correspond à MyConnection.java)
-- ============================================

-- 1. Créer la base de données
CREATE DATABASE IF NOT EXISTS pharmax;
USE pharmax;

-- 2. Créer la table user
CREATE TABLE IF NOT EXISTS `user` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `nom` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) UNIQUE NOT NULL,
    `mot_de_passe` VARCHAR(255),
    `role` VARCHAR(50) DEFAULT 'client'
);

-- 3. Insérer utilisateurs par défaut
INSERT IGNORE INTO `user` (`nom`, `email`, `mot_de_passe`, `role`) VALUES
('Client Pharmax', 'client@pharmax.com', 'pass123', 'client'),
('Admin Pharmax', 'admin@pharmax.com', 'admin123', 'admin');

-- 4. Créer la table produits
CREATE TABLE IF NOT EXISTS produits (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    prix DOUBLE NOT NULL,
    description TEXT,
    stock INT DEFAULT 0
);

-- 5. Créer la table commandes
CREATE TABLE IF NOT EXISTS commandes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    produits VARCHAR(255),
    totales DOUBLE,
    statut VARCHAR(50),
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    utilisateur_id INT,
    FOREIGN KEY (utilisateur_id) REFERENCES `user` (id)
);

-- 6. Créer la table lignes_commandes
CREATE TABLE IF NOT EXISTS lignes_commandes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100),
    prix DOUBLE,
    quantite INT,
    sous_total DOUBLE,
    commande_id INT,
    FOREIGN KEY (commande_id) REFERENCES commandes(id)
);

-- 7. Créer la table livraisons
CREATE TABLE IF NOT EXISTS livraisons (
    id INT PRIMARY KEY AUTO_INCREMENT,
    last_name VARCHAR(100),
    first_name VARCHAR(100),
    email VARCHAR(255),
    adresse VARCHAR(255),
    tel VARCHAR(30),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commande_id INT,
    FOREIGN KEY (commande_id) REFERENCES commandes(id) ON DELETE CASCADE
);

-- 8. Insérer les produits
INSERT INTO produits (nom, prix, description, stock) VALUES
('Aspirine 500mg', 4.99, 'Analgésique et anti-inflammatoire - 20 comprimés', 50),
('Paracétamol 500mg', 3.49, 'Anti-douleur et anti-fièvre - 16 comprimés', 75),
('Ibuprofène 200mg', 5.99, 'Anti-inflammatoire non stéroïdien - 24 comprimés', 60),
('Sirop contre la toux', 8.50, 'Sirop expectorant - 200ml', 30),
('Spray nasal décongestionnant', 6.99, 'Décongestionnant pour sinus - 15ml', 45),
('Pastilles gorge menthe', 2.99, 'Pastilles pour maux de gorge - 20 pastilles', 100),
('Oméprazole 20mg', 7.50, 'Anti-reflux gastrique - 14 gélules', 40),
('Charbon actif', 5.49, 'Pour troubles digestifs - 30 comprimés', 55),
('Probiotiques', 12.99, 'Flore intestinale - 30 gélules', 25),
('Vitamines C 500mg', 6.99, 'Vitamine C - 30 comprimés', 80),
('Multivitamines', 9.99, 'Complément multivitaminé - 30 comprimés', 45),
('Calcium + Vitamine D', 10.99, 'Santé osseuse - 30 comprimés', 35),
('Magnésium', 8.49, 'Relaxation musculaire - 60 gélules', 50),
('Antihistaminique 10mg', 7.99, 'Anti-allergène - 30 comprimés', 65),
('Pommade anti-démangeaison', 5.99, 'Crème dermatologique - 50g', 40),
('Gel antibactérien', 3.99, 'Désinfectant pour mains - 100ml', 120),
('Pansements stériles', 4.99, 'Assortiment 30 pansements', 90),
('Thermomètre numérique', 15.99, 'Thermomètre sans contact - 1 unité', 20),
('Masques chirurgicaux', 6.99, 'Boîte de 50 masques', 75),
('Écran solaire SPF 50', 11.99, 'Protection UV - 200ml', 55),
('Crème hydratante', 9.49, 'Soin de la peau - 100ml', 40),
('Shampooing médical', 7.99, 'Traitement cuir chevelu - 250ml', 35),
('Mélatonine 2mg', 8.99, 'Pour meilleur sommeil - 60 comprimés', 50),
('Glucosamine', 14.99, 'Santé articulaire - 60 gélules', 25),
('Zinc 15mg', 6.49, 'Renforce immunité - 30 comprimés', 70);

-- 9. Vérification
SELECT '✅ Base de données PHARMAX configurée!' as Status;
SELECT COUNT(*) as 'Nombre de produits' FROM produits;
