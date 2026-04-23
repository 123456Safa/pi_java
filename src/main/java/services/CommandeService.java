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

    public CommandeService() {
        cnx = MyConnection.getInstance().getConnection();
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

    public CommandeConfirmation enregistrerCommande(Client client, Collection<PanierItem> panierItems, String modePaiement) throws SQLException {
        if (panierItems == null || panierItems.isEmpty()) {
            throw new SQLException("Le panier est vide.");
        }

        double sousTotal = panierItems.stream().mapToDouble(PanierItem::getSousTotal).sum();
        double tva = sousTotal * TVA_RATE;
        double totalTtc = sousTotal + tva;
        String createdAt = new Timestamp(System.currentTimeMillis()).toString();

        Commandes commande = new Commandes();
        commande.setProduits(construireProduitsDepuisPanier(panierItems));
        commande.setTotales(totalTtc);
        commande.setStatut("En attente");
        commande.setCreatedAt(createdAt);
        commande.setUtilisateurId(1);

        boolean autoCommit = cnx.getAutoCommit();
        cnx.setAutoCommit(false);

        try {
            int commandeId = insertCommande(commande);
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

    private int insertCommande(Commandes commande) throws SQLException {
        String sql = "INSERT INTO commandes (produits, totales, statut, created_date, utilisateur_id) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, commande.getProduits());
            ps.setDouble(2, commande.getTotales());
            ps.setString(3, commande.getStatut());
            ps.setString(4, commande.getCreatedAt());
            ps.setInt(5, commande.getUtilisateurId());
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
}
