-- Créer la table ligne_commandes (corrigez le nom)
CREATE TABLE IF NOT EXISTS ligne_commandes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    prix DOUBLE NOT NULL,
    quantite INT NOT NULL,
    sous_total DOUBLE NOT NULL,
    commande_id INT,
    FOREIGN KEY (commande_id) REFERENCES commandes(id) ON DELETE CASCADE
);

-- Aussi créer lignes_commandes pour être compatible avec le service
CREATE TABLE IF NOT EXISTS lignes_commandes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    prix DOUBLE NOT NULL,
    quantite INT NOT NULL,
    sous_total DOUBLE NOT NULL,
    commande_id INT,
    FOREIGN KEY (commande_id) REFERENCES commandes(id) ON DELETE CASCADE
);

-- Index pour les performances
CREATE INDEX idx_lignes_commande_id ON lignes_commandes(commande_id);
CREATE INDEX idx_ligne_commande_id ON ligne_commandes(commande_id);

-- Vérification
SELECT '✅ Tables créées avec succès!' as Status;
SELECT COUNT(*) as 'Total lignes_commandes' FROM lignes_commandes;

