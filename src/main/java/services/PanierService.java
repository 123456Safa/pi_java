package services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.PanierItem;

public class PanierService {
    private static final ObservableList<PanierItem> panier = FXCollections.observableArrayList();

    public static ObservableList<PanierItem> getPanier() {
        return panier;
    }

    public static void ajouterAuPanier(PanierItem item) {
        // Vérifier si le produit existe déjà
        for (PanierItem p : panier) {
            if (p.getProduitId() == item.getProduitId()) {
                p.setQuantite(p.getQuantite() + item.getQuantite());
                return;
            }
        }
        // Sinon ajouter le nouveau produit
        panier.add(item);
    }

    public static void retirerDuPanier(PanierItem item) {
        panier.remove(item);
    }

    public static void viderPanier() {
        panier.clear();
    }

    public static double getTotal() {
        return panier.stream()
                .mapToDouble(PanierItem::getSousTotal)
                .sum();
    }

    public static int getNombreProduits() {
        return panier.size();
    }

    public static int getQuantiteTotale() {
        return panier.stream()
                .mapToInt(PanierItem::getQuantite)
                .sum();
    }
}

