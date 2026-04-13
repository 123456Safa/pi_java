package model;

import java.util.Date;

public class Produit {
    private int id;
    private String nom;
    private String description;
    private double prix;
    private String image;
    private Date dateExpiration;
    private String statut;
    private Date createdAt;
    private int quantite;
    private int categorieId;

    public Produit() {}

    public Produit(int id, String nom, String description, double prix, String image, Date dateExpiration, String statut, Date createdAt, int quantite, int categorieId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.image = image;
        this.dateExpiration = dateExpiration;
        this.statut = statut;
        this.createdAt = createdAt;
        this.quantite = quantite;
        this.categorieId = categorieId;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public Date getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(Date dateExpiration) { this.dateExpiration = dateExpiration; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public int getCategorieId() { return categorieId; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }
}

