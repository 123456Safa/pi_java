package services;

import models.Client;
import models.Commandes;
import models.PanierItem;
import models.LigneCommandes;
import utils.MyConnection;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public class CommandeService implements IService<Commandes> {

    private Connection cnx;

    public CommandeService() {
        cnx = MyConnection.getInstance().getConnection();
    }

    @Override
    public void add(Commandes c) throws SQLException {
        String sql = "INSERT INTO commandes (produits, totales, statut, utilisateur_id, created_at) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, c.getProduits());
        ps.setDouble(2, c.getTotales());
        ps.setString(3, c.getStatut());
        ps.setInt(4, c.getUtilisateurId());
        ps.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));

        ps.executeUpdate();
        System.out.println("✅ Commande ajoutée");
    }

    @Override
    public void update(Commandes c) throws SQLException {
        String sql = "UPDATE commandes SET produits=?, totales=?, statut=?, utilisateur_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, c.getProduits());
        ps.setDouble(2, c.getTotales());
        ps.setString(3, c.getStatut());
        ps.setInt(4, c.getUtilisateurId());
        ps.setInt(5, c.getId());

        ps.executeUpdate();
        System.out.println("✏️ Commande modifiée");
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM commandes WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("🗑️ Commande supprimée");
    }

    @Override
    public List<Commandes> select() throws SQLException {
        List<Commandes> list = new ArrayList<>();

        String sql = "SELECT * FROM commandes";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Commandes c = new Commandes(
                    rs.getInt("id"),
                    rs.getString("produits"),
                    rs.getDouble("totales"),
                    rs.getString("statut"),
                    null, // pas de colonne date
                    rs.getInt("utilisateur_id")
            );

            list.add(c);
        }

        return list;
    }

    // Méthode spéciale pour front office - enregistre commande + lignes
    public void enregistrerCommande(Client client, Collection<PanierItem> panierItems) throws SQLException {
        // Créer la commande
        Commandes commande = new Commandes();
        commande.setProduits("Commande panier");
        commande.setTotales(panierItems.stream().mapToDouble(PanierItem::getSousTotal).sum());
        commande.setStatut("En attente");
        commande.setUtilisateurId(1); // Par défaut

        // Insérer la commande et récupérer l'ID
        String sqlCommande = "INSERT INTO commandes (produits, totales, statut, utilisateur_id, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = cnx.prepareStatement(sqlCommande, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, commande.getProduits());
            pstmt.setDouble(2, commande.getTotales());
            pstmt.setString(3, commande.getStatut());
            pstmt.setInt(4, commande.getUtilisateurId());
            pstmt.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int commandeId = rs.getInt(1);

                // Insérer les lignes de commande
                LigneCommandeService ligneService = new LigneCommandeService();
                for (PanierItem item : panierItems) {
                    LigneCommandes ligne = new LigneCommandes();
                    ligne.setNom(item.getNom());
                    ligne.setPrix(item.getPrix());
                    ligne.setQuantite(item.getQuantite());
                    ligne.setSousTotal(item.getSousTotal());
                    ligne.setCommandeId(commandeId);
                    ligneService.add(ligne);
                }
                System.out.println("✅ Commande enregistrée avec " + panierItems.size() + " articles");
            }
        }
    }
}