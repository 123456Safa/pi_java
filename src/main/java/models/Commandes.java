package models;

public class Commandes {
    private int id;
    private String produits;
    private double totales;
    private String statut;
    private String createdAt;
    private int utilisateurId;

    public Commandes() {}

    public Commandes(int id, String produits, double totales,
                     String statut, String createdAt, int utilisateurId) {
        this.id = id;
        this.produits = produits;
        this.totales = totales;
        this.statut = statut;
        this.createdAt = createdAt;
        this.utilisateurId = utilisateurId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProduits() {
        return produits;
    }

    public void setProduits(String produits) {
        this.produits = produits;
    }

    public double getTotales() {
        return totales;
    }

    public void setTotales(double totales) {
        this.totales = totales;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }
}
