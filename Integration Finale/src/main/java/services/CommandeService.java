package services;

import models.Client;
import models.CommandeConfirmation;
import models.Commandes;
import models.LigneCommandes;
import models.PanierItem;
import services.IService;
import services.LigneCommandeService;
import utils.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class CommandeService implements IService<Commandes> {
    private static final double TVA_RATE = 0.19;

    private final Connection cnx;
    private final LigneCommandeService ligneCommandeService;
    private final LivraisonService livraisonService;
    private final OrderQrCodeService qrCodeService = new OrderQrCodeService();

    public CommandeService() {
        cnx = MyConnection.getInstance().getConnection();
        ensureCoreTablesExist();
        ligneCommandeService = new LigneCommandeService();
        livraisonService = new LivraisonService();
    }

    @Override
    public void add(Commandes commande) throws SQLException {
        String sql = "INSERT INTO commandes (produits, totales, statut, created_date, utilisateur_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, commande.getProduits());
            ps.setDouble(2, commande.getTotales());
            ps.setString(3, commande.getStatut());
            ps.setString(4, commande.getCreatedAt());
            ps.setInt(5, commande.getUtilisateurId());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Commandes commande) throws SQLException {
        String sql = "UPDATE commandes SET produits=?, totales=?, statut=?, created_date=?, utilisateur_id=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, commande.getProduits());
            ps.setDouble(2, commande.getTotales());
            ps.setString(3, commande.getStatut());
            ps.setString(4, commande.getCreatedAt());
            ps.setInt(5, commande.getUtilisateurId());
            ps.setInt(6, commande.getId());
            ps.executeUpdate();
            
            // Envoyer une notification après la mise à jour complète de la commande (ex: statut modifié via backoffice)
            sendNotificationForStatusUpdate(commande.getId(), commande.getStatut());
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        boolean autoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);

        try {
            livraisonService.deleteByCommandeId(id);
            ligneCommandeService.deleteByCommandeId(id);

            String sql = "DELETE FROM commandes WHERE id=?";
            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            cnx.commit();
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(autoCommit);
        }
    }

    @Override
    public List<Commandes> select() throws SQLException {
        List<Commandes> commandes = new ArrayList<>();
        String sql = "SELECT * FROM commandes";

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                commandes.add(new Commandes(
                        rs.getInt("id"),
                        rs.getString("produits"),
                        rs.getDouble("totales"),
                        rs.getString("statut"),
                        rs.getString("created_date"),
                        rs.getInt("utilisateur_id")
                ));
            }
        }

        return commandes;
    }

    public Commandes findById(int id) throws SQLException {
        String sql = "SELECT * FROM commandes WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Commandes(
                            rs.getInt("id"),
                            rs.getString("produits"),
                            rs.getDouble("totales"),
                            rs.getString("statut"),
                            rs.getString("created_date"),
                            rs.getInt("utilisateur_id")
                    );
                }
            }
        }
        return null;
    }

    public Commandes findByQrToken(String token) throws SQLException {
        String sql = "SELECT * FROM commandes WHERE qr_token=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Commandes(
                            rs.getInt("id"),
                            rs.getString("produits"),
                            rs.getDouble("totales"),
                            rs.getString("statut"),
                            rs.getString("created_date"),
                            rs.getInt("utilisateur_id")
                    );
                }
            }
        }
        return null;
    }

    public String getOrCreateQrToken(int commandeId) throws SQLException {
        String current = getQrToken(commandeId);
        if (current != null && !current.isBlank()) {
            return current;
        }

        String token = qrCodeService.generateToken();
        String sql = "UPDATE commandes SET qr_token=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setInt(2, commandeId);
            ps.executeUpdate();
        }
        return token;
    }

    private String getQrToken(int commandeId) throws SQLException {
        String sql = "SELECT qr_token FROM commandes WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("qr_token");
                }
            }
        }
        return null;
    }

    public void updateStatus(int commandeId, String statut) throws SQLException {
        String sql = "UPDATE commandes SET statut=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, commandeId);
            ps.executeUpdate();
            
            // Envoyer une notification après la mise à jour
            sendNotificationForStatusUpdate(commandeId, statut);
        }
    }

    private void sendNotificationForStatusUpdate(int commandeId, String statut) {
        String query = "SELECT c.utilisateur_id, u.email FROM commandes c JOIN user u ON c.utilisateur_id = u.id WHERE c.id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, commandeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int actualUserId = rs.getInt("utilisateur_id");
                    String email = rs.getString("email");
                    
                    String title = "Mise à jour de commande";
                    String message = "Votre commande #" + commandeId + " est maintenant : " + statut;
                    String type = "ORDER_" + statut.toUpperCase().replace(" ", "_");
                    
                    NotificationService notificationService = new NotificationService();
                    notificationService.sendNotification(actualUserId, title, message, type, email);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'envoi de la notification de statut : " + e.getMessage());
        }
    }

    public CommandeConfirmation enregistrerCommande(Client client, Collection<PanierItem> panierItems, String modePaiement) throws SQLException {
        return enregistrerCommande(client, panierItems, modePaiement, 0.0);
    }

    public CommandeConfirmation enregistrerCommande(Client client, Collection<PanierItem> panierItems, String modePaiement, double fraisLivraison) throws SQLException {
        return enregistrerCommande(client, panierItems, modePaiement, fraisLivraison, 0.0);
    }

    public CommandeConfirmation enregistrerCommande(Client client, Collection<PanierItem> panierItems, String modePaiement, double fraisLivraison, double reduction) throws SQLException {
        if (panierItems == null || panierItems.isEmpty()) {
            throw new SQLException("Le panier est vide.");
        }

        double sousTotal = panierItems.stream().mapToDouble(PanierItem::getSousTotal).sum();
        double tva = sousTotal * TVA_RATE;
        double totalTtc = Math.max(0.0, sousTotal + tva + Math.max(0.0, fraisLivraison) - Math.max(0.0, reduction));
        String createdAt = new Timestamp(System.currentTimeMillis()).toString();

        Commandes commande = new Commandes();
        commande.setProduits(construireProduitsDepuisPanier(panierItems));
        commande.setTotales(totalTtc);
        commande.setStatut("En attente");
        commande.setCreatedAt(createdAt);
        // Get or create user ID for this client
        commande.setUtilisateurId(getOrCreateUserId(client));

        boolean autoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);

        try {
            String qrToken = qrCodeService.generateToken();
            int commandeId = insertCommande(commande, qrToken);
            List<LigneCommandes> lignes = insererLignesCommande(commandeId, panierItems);
            
            // Insert Livraison Details
            models.Livraison livraison = new models.Livraison();
            // Try splitting nom to firstName and lastName
            String[] parts = client.getNom().trim().split(" ", 2);
            if(parts.length > 1) {
                livraison.setFirstName(parts[0]);
                livraison.setLastName(parts[1]);
            } else {
                livraison.setFirstName(client.getNom());
                livraison.setLastName(client.getNom());
            }
            livraison.setEmail(client.getEmail());
            livraison.setAdresse(client.getAdresse());
            livraison.setTel(client.getTelephone());
            livraison.setCreatedAt(createdAt);
            livraison.setCommandeId(commandeId);
            
            livraisonService.add(livraison);
            
            cnx.commit();

            return new CommandeConfirmation(
                    commandeId,
                    client,
                    lignes,
                    modePaiement,
                    createdAt,
                    sousTotal,
                    tva,
                    totalTtc
            );
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(autoCommit);
        }
    }

    private int insertCommande(Commandes commande, String qrToken) throws SQLException {
        String sql = "INSERT INTO commandes (produits, totales, statut, created_date, utilisateur_id, qr_token) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, commande.getProduits());
            ps.setDouble(2, commande.getTotales());
            ps.setString(3, commande.getStatut());
            ps.setString(4, commande.getCreatedAt());
            ps.setInt(5, commande.getUtilisateurId());
            ps.setString(6, qrToken);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Impossible de recuperer l'identifiant de la commande.");
    }

    private List<LigneCommandes> insererLignesCommande(int commandeId, Collection<PanierItem> panierItems) throws SQLException {
        List<LigneCommandes> lignes = new ArrayList<>();

        for (PanierItem item : panierItems) {
            LigneCommandes ligne = new LigneCommandes();
            ligne.setNom(item.getNom());
            ligne.setPrix(item.getPrix());
            ligne.setQuantite(item.getQuantite());
            ligne.setSousTotal(item.getSousTotal());
            ligne.setCommandeId(commandeId);
            ligneCommandeService.add(ligne);
            lignes.add(ligne);
        }

        return lignes;
    }

    private String construireProduitsDepuisPanier(Collection<PanierItem> panierItems) {
        return panierItems.stream()
                .map(item -> String.format(
                        Locale.US,
                        "{\"nom\":\"%s\",\"quantite\":%d,\"prix\":%.2f,\"sousTotal\":%.2f}",
                        escapeJson(item.getNom()),
                        item.getQuantite(),
                        item.getPrix(),
                        item.getSousTotal()
                ))
                .reduce((left, right) -> left + "," + right)
                .map(value -> "[" + value + "]")
                .orElse("[]");
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    /**
     * Récupère ou crée un utilisateur basé sur l'email du client
     */
    private int getOrCreateUserId(Client client) throws SQLException {
        // First, try to use the currently logged-in user's ID from SessionManager
        models.User sessionUser = utils.SessionManager.getInstance().getCurrentUser();
        if (sessionUser != null && sessionUser.getId() > 0) {
            return sessionUser.getId();
        }

        if (client == null || client.getEmail() == null) {
            // Retourne l'ID du client par défaut
            return 1;
        }

        // Vérifie si l'utilisateur existe déjà
        String selectSql = "SELECT id FROM `user` WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(selectSql)) {
            ps.setString(1, client.getEmail());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        // Si l'utilisateur n'existe pas, le créer avec le schema simple de ce module.
        // Si la table user appartient deja a un autre module, on rattache la commande a un user existant.
        String insertSql = "INSERT INTO `user` (nom, email, role) VALUES (?, ?, 'client')";
        try (PreparedStatement ps = cnx.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getNom() != null ? client.getNom() : "Client");
            ps.setString(2, client.getEmail());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            int existingUserId = getFirstExistingUserId();
            if (existingUserId > 0) {
                System.err.println("Creation client ignoree, schema user incompatible: " + e.getMessage());
                return existingUserId;
            }
            throw e;
        }

        // Fallback: retourne l'ID du client par défaut
        return 1;
    }

    private int getFirstExistingUserId() throws SQLException {
        String sql = "SELECT id FROM `user` ORDER BY id LIMIT 1";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return 0;
    }

    private void ensureCoreTablesExist() {
        executeUpdateIgnoringErrors("""
                CREATE TABLE IF NOT EXISTS `user` (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    nom VARCHAR(255) NOT NULL,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    mot_de_passe VARCHAR(255),
                    role VARCHAR(50) DEFAULT 'client'
                )
                """);

        executeUpdateIgnoringErrors("""
                CREATE TABLE IF NOT EXISTS commandes (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    produits TEXT,
                    totales DOUBLE,
                    statut VARCHAR(50),
                    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    utilisateur_id INT
                )
                """);

        executeUpdateIgnoringErrors("ALTER TABLE commandes ADD COLUMN produits TEXT");
        executeUpdateIgnoringErrors("ALTER TABLE commandes MODIFY COLUMN produits TEXT");
        executeUpdateIgnoringErrors("ALTER TABLE commandes ADD COLUMN totales DOUBLE");
        executeUpdateIgnoringErrors("ALTER TABLE commandes ADD COLUMN statut VARCHAR(50)");
        executeUpdateIgnoringErrors("ALTER TABLE commandes ADD COLUMN created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP");
        executeUpdateIgnoringErrors("ALTER TABLE commandes ADD COLUMN utilisateur_id INT");
        executeUpdateIgnoringErrors("ALTER TABLE commandes ADD COLUMN qr_token VARCHAR(64)");
        executeUpdateIgnoringErrors("CREATE INDEX idx_commandes_qr_token ON commandes(qr_token)");
        executeUpdateIgnoringErrors("""
                INSERT IGNORE INTO `user` (nom, email, mot_de_passe, role)
                VALUES ('Client Pharmax', 'client@pharmax.com', 'pass123', 'client')
                """);
    }

    private void executeUpdateIgnoringErrors(String sql) {
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Verification schema ignoree: " + e.getMessage());
        }
    }
}
