-- ===============================================
-- PHARMAX - Données d'exemple pour test
-- Base de données: pharm
-- Table: produits
-- ===============================================

USE pharm;

-- Vérifier la structure de la table
DESC produits;

-- ===============================================
-- Insérer des produits pharmaceutiques d'exemple
-- ===============================================

INSERT INTO produits (nom, prix, description, stock) VALUES
-- Douleurs et Fièvre
('Aspirine 500mg', 4.99, 'Analgésique et anti-inflammatoire - 20 comprimés', 50),
('Paracétamol 500mg', 3.49, 'Anti-douleur et anti-fièvre - 16 comprimés', 75),
('Ibuprofène 200mg', 5.99, 'Anti-inflammatoire non stéroïdien - 24 comprimés', 60),

-- Rhume et Grippe
('Sirop contre la toux', 8.50, 'Sirop expectorant - 200ml', 30),
('Spray nasal décongestionnant', 6.99, 'Décongestionnant pour sinus - 15ml', 45),
('Pastilles gorge menthe', 2.99, 'Pastilles pour maux de gorge - 20 pastilles', 100),

-- Digestion
('Oméprazole 20mg', 7.50, 'Anti-reflux gastrique - 14 gélules', 40),
('Charbon actif', 5.49, 'Pour troubles digestifs - 30 comprimés', 55),
('Probiotiques', 12.99, 'Flore intestinale - 30 gélules', 25),

-- Vitamines et Minéraux
('Vitamines C 500mg', 6.99, 'Vitamine C - 30 comprimés', 80),
('Multivitamines', 9.99, 'Complément multivitaminé - 30 comprimés', 45),
('Calcium + Vitamine D', 10.99, 'Santé osseuse - 30 comprimés', 35),
('Magnésium', 8.49, 'Relaxation musculaire - 60 gélules', 50),

-- Allergie
('Antihistaminique 10mg', 7.99, 'Anti-allergène - 30 comprimés', 65),
('Pommade anti-démangeaison', 5.99, 'Crème dermatologique - 50g', 40),

-- Hygiène et Soins
('Gel antibactérien', 3.99, 'Désinfectant pour mains - 100ml', 120),
('Pansements stériles', 4.99, 'Assortiment 30 pansements', 90),
('Thermomètre numérique', 15.99, 'Thermomètre sans contact - 1 unité', 20),
('Masques chirurgicaux', 6.99, 'Boîte de 50 masques', 75),

-- Beauté et Bien-être
('Écran solaire SPF 50', 11.99, 'Protection UV - 200ml', 55),
('Crème hydratante', 9.49, 'Soin de la peau - 100ml', 40),
('Shampooing médical', 7.99, 'Traitement cuir chevelu - 250ml', 35),

-- Sommeil
('Mélatonine 2mg', 8.99, 'Pour meilleur sommeil - 60 comprimés', 50),

-- Articulations
('Glucosamine', 14.99, 'Santé articulaire - 60 gélules', 25),

-- Cough et Rhume
('Zinc 15mg', 6.49, 'Renforce immunité - 30 comprimés', 70);

-- ===============================================
-- Vérifier les insertions
-- ===============================================

SELECT COUNT(*) as 'Nombre de produits' FROM produits;
SELECT * FROM produits LIMIT 10;

-- ===============================================
-- Résumé des catégories disponibles
-- ===============================================

/*
CATÉGORIES DE PRODUITS:
1. Douleurs et Fièvre (3 produits)
2. Rhume et Grippe (3 produits)
3. Digestion (3 produits)
4. Vitamines et Minéraux (4 produits)
5. Allergie (2 produits)
6. Hygiène et Soins (4 produits)
7. Beauté et Bien-être (3 produits)
8. Sommeil (1 produit)
9. Articulations (1 produit)
10. Immunité (1 produit)

TOTAL: 25 produits pour tester
*/

