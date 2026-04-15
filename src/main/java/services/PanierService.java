package services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.PanierItem;

public class PanierService {
    private static PanierService instance;
    private final ObservableList<PanierItem> panier = FXCollections.observableArrayList();

    private PanierService() {}

    public static PanierService getInstance() {
        if (instance == null) {
            instance = new PanierService();
        }
        return instance;
    }

    public ObservableList<PanierItem> getPanier() {
        return panier;
    }

    public void ajouterProduit(PanierItem item) {
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

    public void supprimerProduit(PanierItem item) {
        panier.remove(item);
    }

    public void augmenterQuantite(PanierItem item) {
        item.setQuantite(item.getQuantite() + 1);
    }

    public void diminuerQuantite(PanierItem item) {
        if (item.getQuantite() > 1) {
            item.setQuantite(item.getQuantite() - 1);
        } else {
            supprimerProduit(item);
        }
    }

    public void viderPanier() {
        panier.clear();
    }

    public double getSousTotal() {
        return panier.stream()
                .mapToDouble(PanierItem::getSousTotal)
                .sum();
    }

    public static void ajouterAuPanier(PanierItem item) {
        getInstance().ajouterProduit(item);
    }

    public static void retirerDuPanier(PanierItem item) {
        getInstance().supprimerProduit(item);
    }

    public static void viderPanierStatic() {
        getInstance().viderPanier();
    }

    public static double getTotal() {
        return getInstance().getSousTotal();
    }

    public static int getNombreProduits() {
        return getInstance().getPanier().size();
    }

    public static int getQuantiteTotale() {
        return getInstance().getPanier().stream()
                .mapToInt(PanierItem::getQuantite)
                .sum();
    }

    public static ObservableList<PanierItem> getPanierStatic() {
        return getInstance().getPanier();
    }
}
