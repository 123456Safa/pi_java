package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import service.ProduitService;
import service.CategorieService;
import model.Produit;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {
    @FXML private Label lblTotalProduits;
    @FXML private Label lblProduitsStock;
    @FXML private Label lblTotalCategories;
    @FXML private Label lblCategoriesActives;
    @FXML private Label lblProduitsExpires;
    @FXML private Label lblAretirer;
    @FXML private PieChart pieChartStock;
    @FXML private BarChart<String, Number> barChartPlusChers;
    @FXML private BarChart<String, Number> barChartMoinsChers;

    private ProduitService produitService = new ProduitService();
    private CategorieService categorieService = new CategorieService();

    @FXML
    public void initialize() {
        try {
            List<Produit> produits = produitService.afficher();
            int totalProduits = produits.size();
            int produitsStock = (int) produits.stream().filter(p -> p.getQuantite() > 0).count();
            int produitsExpires = (int) produits.stream().filter(p -> p.getStatut() != null && p.getStatut().toLowerCase().contains("expir")).count();

            lblTotalProduits.setText(String.valueOf(totalProduits));
            lblProduitsStock.setText(produitsStock + " Produits en stock");
            lblProduitsExpires.setText(String.valueOf(produitsExpires));
            lblAretirer.setText(produitsExpires + " À retirer du stock");

            int totalCategories = categorieService.afficher().size();
            lblTotalCategories.setText(String.valueOf(totalCategories));
            lblCategoriesActives.setText(totalCategories + " Catégories actives");

            // PieChart
            PieChart.Data valables = new PieChart.Data("Produits Valables", produitsStock);
            PieChart.Data horsStock = new PieChart.Data("Produits Hors Stock", totalProduits - produitsStock);
            pieChartStock.getData().addAll(valables, horsStock);
            valables.getNode().setStyle("-fx-pie-color: #22c55e;");
            horsStock.getNode().setStyle("-fx-pie-color: #ef4444;");

            // BarChart Plus Chers
            List<Produit> plusChers = produits.stream().sorted(Comparator.comparingDouble(Produit::getPrix).reversed()).limit(5).collect(Collectors.toList());
            XYChart.Series<String, Number> seriesPlus = new XYChart.Series<>();
            for (Produit p : plusChers) {
                seriesPlus.getData().add(new XYChart.Data<>(p.getNom(), p.getPrix()));
            }
            barChartPlusChers.getData().add(seriesPlus);
            barChartPlusChers.setLegendVisible(false);
            barChartPlusChers.lookupAll(".default-color0.chart-bar").forEach(n -> n.setStyle("-fx-bar-fill: #818cf8;"));

            // BarChart Moins Chers
            List<Produit> moinsChers = produits.stream().sorted(Comparator.comparingDouble(Produit::getPrix)).limit(5).collect(Collectors.toList());
            XYChart.Series<String, Number> seriesMoins = new XYChart.Series<>();
            for (Produit p : moinsChers) {
                seriesMoins.getData().add(new XYChart.Data<>(p.getNom(), p.getPrix()));
            }
            barChartMoinsChers.getData().add(seriesMoins);
            barChartMoinsChers.setLegendVisible(false);
            barChartMoinsChers.lookupAll(".default-color0.chart-bar").forEach(n -> n.setStyle("-fx-bar-fill: #22c55e;"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


