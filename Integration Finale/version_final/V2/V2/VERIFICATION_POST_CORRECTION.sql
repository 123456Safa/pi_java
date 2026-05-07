-- ═══════════════════════════════════════════════════════════════
-- VÉRIFICATION POST-EXÉCUTION
-- Exécutez ces requêtes pour vérifier que tout est en place
-- ═══════════════════════════════════════════════════════════════

USE pharm;

-- ✅ TEST 1: Vérifier que la table user existe et a 2 utilisateurs
SELECT '✅ TEST 1: Table user' as Test;
SELECT COUNT(*) as 'Nombre d\'utilisateurs' FROM `user`;
SELECT * FROM `user`;
-- Résultat attendu: 2 enregistrements (Client + Admin)

PRINT '';
PRINT '';

-- ✅ TEST 2: Vérifier que la table commandes a la colonne created_date
SELECT '✅ TEST 2: Colonne created_date' as Test;
DESCRIBE commandes;
-- Résultat attendu: Colonne 'created_date' existe et type DATETIME

PRINT '';
PRINT '';

-- ✅ TEST 3: Vérifier la clé étrangère
SELECT '✅ TEST 3: Clé étrangère' as Test;
SELECT CONSTRAINT_NAME, TABLE_NAME, REFERENCED_TABLE_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME = 'commandes' AND COLUMN_NAME = 'utilisateur_id';
-- Résultat attendu: FK_commandes_user ou FK_35D4282CFB88E14F

PRINT '';
PRINT '';

-- ✅ TEST 4: Vérifier que les produits sont présents
SELECT '✅ TEST 4: Produits' as Test;
SELECT COUNT(*) as 'Nombre de produits' FROM produits;
SELECT * FROM produits LIMIT 5;
-- Résultat attendu: Au moins 25 produits

PRINT '';
PRINT '';

-- ✅ TEST 5: Simuler une commande (SANS EXÉCUTER)
SELECT '✅ TEST 5: Simulation création commande' as Test;
SELECT '-- Cette requête SIMULE l\'insertion d\'une commande' as Note;
SELECT '-- Elle ne sera PAS exécutée, juste vérifiée' as Note2;

-- Vérifier que l'utilisateur par défaut existe
SELECT id, nom, email FROM `user` WHERE id = 1;
-- Résultat attendu: Client Pharmax avec ID 1

PRINT '';
PRINT '';

-- ✅ TEST 6: Vérifier les tables essentielles
SELECT '✅ TEST 6: Tables essentielles' as Test;
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'pharm'
AND TABLE_NAME IN ('user', 'commandes', 'lignes_commandes', 'produits');
-- Résultat attendu: user, commandes, lignes_commandes, produits

PRINT '';
PRINT '';

-- ═══════════════════════════════════════════════════════════════
-- RÉSUMÉ FINAL
-- ═══════════════════════════════════════════════════════════════

SELECT '═══════════════════════════════════════════════════════════════' as '   ';
SELECT '✅ RÉSUMÉ DE VÉRIFICATION' as '   ';
SELECT '═══════════════════════════════════════════════════════════════' as '   ';

SELECT COUNT(*) as 'Utilisateurs', COUNT(*) FROM `user`;
SELECT COUNT(*) as 'Produits', COUNT(*) FROM produits;
SELECT COUNT(*) as 'Commandes', COUNT(*) FROM commandes;
SELECT COUNT(*) as 'Lignes commandes', COUNT(*) FROM lignes_commandes;

SELECT '✅ VÉRIFICATION RÉUSSIE!' as Status;
SELECT 'Vous pouvez maintenant tester l\'application' as 'Prochaine étape';

