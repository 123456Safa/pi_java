package models;

import javafx.beans.property.*;

public class PanierItem {
    private final IntegerProperty produitId;
    private final StringProperty nom;
    private final DoubleProperty prix;
    private final IntegerProperty quantite;
    private final DoubleProperty sousTotal;
    private final StringProperty description;
    private final StringProperty image;

    public PanierItem(int produitId, String nom, double prix, String description, String image) {
        this.produitId = new SimpleIntegerProperty(produitId);
        this.nom = new SimpleStringProperty(nom);
        this.prix = new SimpleDoubleProperty(prix);
        this.quantite = new SimpleIntegerProperty(1);
        this.sousTotal = new SimpleDoubleProperty(prix);
        this.description = new SimpleStringProperty(description);
        this.image = new SimpleStringProperty(image);

        // Bind sousTotal to quantite and prix
        this.sousTotal.bind(
            this.quantite.multiply(this.prix)
        );
    }

    // Getters and setters
    public int getProduitId() {
        return produitId.get();
    }

    public void setProduitId(int value) {
        produitId.set(value);
    }

    public IntegerProperty produitIdProperty() {
        return produitId;
    }

    public String getNom() {
        return nom.get();
    }

    public void setNom(String value) {
        nom.set(value);
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public double getPrix() {
        return prix.get();
    }

    public void setPrix(double value) {
        prix.set(value);
    }

    public DoubleProperty prixProperty() {
        return prix;
    }

    public int getQuantite() {
        return quantite.get();
    }

    public void setQuantite(int value) {
        quantite.set(value);
    }

    public IntegerProperty quantiteProperty() {
        return quantite;
    }

    public double getSousTotal() {
        return sousTotal.get();
    }

    public DoubleProperty sousTotalProperty() {
        return sousTotal;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String value) {
        description.set(value);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public String getImage() {
        return image.get();
    }

    public void setImage(String value) {
        image.set(value);
    }

    public StringProperty imageProperty() {
        return image;
    }
}

