package models;

public class Commandes {
    private int id;
    private String produits;
    private double totales;
    private String statut;
    private String date;
    private int utilisateurId;

    public Commandes() {}

    public Commandes(int id, String produits, double totales,
                    String statut, String date, int utilisateurId) {
        this.id = id;
        this.produits = produits;
        this.totales = totales;
        this.statut = statut;
        this.date = date;
        this.utilisateurId = utilisateurId;
    }

    // getters setters
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }
}