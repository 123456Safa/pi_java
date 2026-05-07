package models;

import java.util.List;

public class CommandeConfirmation {
    private final int commandeId;
    private final Client client;
    private final List<LigneCommandes> lignes;
    private final String modePaiement;
    private final String dateCommande;
    private final double sousTotal;
    private final double tva;
    private final double totalTtc;

    public CommandeConfirmation(int commandeId, Client client, List<LigneCommandes> lignes,
                                String modePaiement, String dateCommande,
                                double sousTotal, double tva, double totalTtc) {
        this.commandeId = commandeId;
        this.client = client;
        this.lignes = lignes;
        this.modePaiement = modePaiement;
        this.dateCommande = dateCommande;
        this.sousTotal = sousTotal;
        this.tva = tva;
        this.totalTtc = totalTtc;
    }

    public int getCommandeId() {
        return commandeId;
    }

    public Client getClient() {
        return client;
    }

    public List<LigneCommandes> getLignes() {
        return lignes;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public String getDateCommande() {
        return dateCommande;
    }

    public double getSousTotal() {
        return sousTotal;
    }

    public double getTva() {
        return tva;
    }

    public double getTotalTtc() {
        return totalTtc;
    }
}
