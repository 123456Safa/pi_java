package models;

public class Produit {
    private int id;
    private String nom;
    private double prix;
    private String description;
    private int stock;

    public Produit() {}

    public Produit(int id, String nom, double prix, String description, int stock) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.description = description;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
