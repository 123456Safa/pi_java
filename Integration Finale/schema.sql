-- Création de la base de données pharm
CREATE DATABASE IF NOT EXISTS pharm;
USE pharm;

-- Table utilisateur
CREATE TABLE IF NOT EXISTS utilisateur (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'client'
);

-- Table produit
CREATE TABLE IF NOT EXISTS produit (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    prix DOUBLE NOT NULL,
    description TEXT,
    stock INT NOT NULL DEFAULT 0
);

-- Table commande
CREATE TABLE IF NOT EXISTS commande (
    id INT PRIMARY KEY AUTO_INCREMENT,
    produits TEXT, -- Peut-être changer en relation avec ligne_commande
    totales DOUBLE NOT NULL,
    statut VARCHAR(50) DEFAULT 'en_attente',
    date VARCHAR(50),
    utilisateur_id INT,
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
);

-- Table ligne_commande
CREATE TABLE IF NOT EXISTS ligne_commande (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    prix DOUBLE NOT NULL,
    quantite INT NOT NULL,
    sous_total DOUBLE NOT NULL,
    commande_id INT,
    FOREIGN KEY (commande_id) REFERENCES commande(id)
);

-- Insertion de données exemple
INSERT INTO utilisateur (nom, email, mot_de_passe) VALUES
('Admin', 'admin@pharmax.com', 'admin123'),
('Client1', 'client1@example.com', 'pass123');

INSERT INTO produit (nom, prix, description, stock) VALUES
('Paracétamol', 5.50, 'Médicament contre la douleur', 100),
('Ibuprofène', 7.20, 'Anti-inflammatoire', 80),
('Vitamine C', 10.00, 'Supplément vitaminé', 50);
