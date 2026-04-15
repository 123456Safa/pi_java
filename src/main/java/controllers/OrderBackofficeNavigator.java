package controllers;

import models.Commandes;

public interface OrderBackofficeNavigator {
    void showDashboard();
    void showCommandes();
    void showOrderDetails(Commandes commande);
}
