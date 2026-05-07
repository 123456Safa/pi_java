package models;

import java.util.Date;

public class Categorie {
    private int id;
    private String nom;
    private String description;
    private Date createdAt;

    public Categorie() {}

    public Categorie(int id, String nom, String description, Date createdAt) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return nom; }
}
