-- ===============================================
-- Vérification des produits en base de données
-- ===============================================

USE pharm;

echo "=== VÉRIFICATION DE LA TABLE PRODUITS ===";

-- 1. Lister les tables
SHOW TABLES;

-- 2. Structure de la table produits
DESCRIBE produits;

-- 3. Nombre de produits
SELECT COUNT(*) as 'Nombre total de produits' FROM produits;

-- 4. Afficher tous les produits
SELECT id, nom, prix, stock FROM produits ORDER BY id;

-- 5. Produits par catégorie de prix
echo "=== RÉSUMÉ PAR CATÉGORIE DE PRIX ===";
SELECT
    CASE
        WHEN prix < 5 THEN 'Moins de 5 DT'
        WHEN prix < 10 THEN '5 à 10 DT'
        WHEN prix < 15 THEN '10 à 15 DT'
        ELSE '15 DT et plus'
    END as 'Catégorie',
    COUNT(*) as 'Nombre de produits'
FROM produits
GROUP BY CASE
    WHEN prix < 5 THEN 'Moins de 5 DT'
    WHEN prix < 10 THEN '5 à 10 DT'
    WHEN prix < 15 THEN '10 à 15 DT'
    ELSE '15 DT et plus'
END;

