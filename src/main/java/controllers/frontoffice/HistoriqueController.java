package controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import models.Commandes;
import models.LigneCommandes;
import services.CommandeService;
import services.LigneCommandeService;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HistoriqueController {
    private static final int CURRENT_USER_ID = 1;

    @FXML
    private TilePane commandesContainer;
    @FXML
    private TextField searchField;
    @FXML
    private Label resultsLabel;

    private final CommandeService commandeService = new CommandeService();
    private final LigneCommandeService ligneCommandeService = new LigneCommandeService();
    private List<Commandes> allCommandes = new ArrayList<>();

    @FXML
    public void initialize() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> refreshCommandes());
        loadCommandes();
    }

    private void loadCommandes() {
        try {
            allCommandes = commandeService.select().stream()
                    .filter(commande -> commande.getUtilisateurId() == CURRENT_USER_ID)
                    .collect(Collectors.toList());
            refreshCommandes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshCommandes() {
        commandesContainer.getChildren().clear();

        String query = searchField == null ? "" : searchField.getText();
        List<Commandes> filteredCommandes = allCommandes.stream()
                .filter(commande -> matchesSearch(commande, query))
                .collect(Collectors.toList());

        for (Commandes commande : filteredCommandes) {
            commandesContainer.getChildren().add(createCommandeCard(commande));
        }

        if (resultsLabel != null) {
            int count = filteredCommandes.size();
            resultsLabel.setText(count + (count > 1 ? " commandes" : " commande"));
        }
    }

    private VBox createCommandeCard(Commandes commande) {
        VBox card = new VBox(14);
        card.setPrefWidth(520);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; "
                + "-fx-border-color: #e7edf6; -fx-border-radius: 20; "
                + "-fx-effect: dropshadow(gaussian, rgba(36,50,72,0.08), 20, 0, 0, 5);");

        HBox header = new HBox();
        Label title = new Label("Commande #" + commande.getId());
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #243248;");

        Label badge = new Label(commande.getStatut());
        badge.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #5d4df1; -fx-font-size: 13; "
                + "-fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 7 14;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, badge);

        Label date = new Label(formatDate(commande.getCreatedAt()));
        date.setStyle("-fx-font-size: 14; -fx-text-fill: #6b7280;");

        int articleCount = countArticles(commande.getId());

        VBox leftBlock = new VBox(4);
        Label leftCaption = new Label("Nombre d'articles");
        leftCaption.setStyle("-fx-font-size: 14; -fx-text-fill: #6b7280;");
        Label leftValue = new Label(String.valueOf(articleCount));
        leftValue.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #111827;");
        leftBlock.getChildren().addAll(leftCaption, leftValue);

        VBox rightBlock = new VBox(4);
        Label rightCaption = new Label("Montant total");
        rightCaption.setStyle("-fx-font-size: 14; -fx-text-fill: #6b7280;");
        Label rightValue = new Label(String.format("%.2f DT", commande.getTotales()));
        rightValue.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-text-fill: #0f9f67;");
        rightBlock.getChildren().addAll(rightCaption, rightValue);

        HBox stats = new HBox(120, leftBlock, rightBlock);

        Button voirDetails = new Button("Voir details");
        voirDetails.setMaxWidth(Double.MAX_VALUE);
        voirDetails.setStyle("-fx-background-color: linear-gradient(to right, #6d5dfc, #5b4be9); -fx-text-fill: white; -fx-font-size: 14; "
                + "-fx-font-weight: bold; -fx-background-radius: 14; -fx-padding: 11 16; -fx-cursor: hand;");
        voirDetails.setOnAction(event -> showCommandeDetails(commande));

        card.getChildren().addAll(header, date, stats, voirDetails);
        return card;
    }

    private boolean matchesSearch(Commandes commande, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String normalizedQuery = query.trim().toLowerCase();
        String searchableText = String.join(" ",
                String.valueOf(commande.getId()),
                safeLower(commande.getStatut()),
                safeLower(formatDate(commande.getCreatedAt())),
                safeLower(commande.getProduits()),
                String.format("%.2f", commande.getTotales())
        ).toLowerCase();

        return searchableText.contains(normalizedQuery);
    }

    private int countArticles(int commandeId) {
        try {
            Commandes commande = commandeService.select().stream()
                    .filter(c -> c.getId() == commandeId)
                    .findFirst()
                    .orElse(null);

            List<LigneCommandes> lignes = getLignesForCommande(commande);
            if (!lignes.isEmpty()) {
                return lignes.stream()
                        .mapToInt(LigneCommandes::getQuantite)
                        .sum();
            }

            return countArticlesFromProduits(commande != null ? commande.getProduits() : null);
        } catch (Exception e) {
            return 0;
        }
    }

    private void showCommandeDetails(Commandes commande) {
        try {
            List<LigneCommandes> lignes = getLignesForCommande(commande);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.OK);
            alert.setTitle("Details de la commande");
            alert.setHeaderText("Commandes / #" + commande.getId());
            alert.getDialogPane().setPrefWidth(760);
            alert.getDialogPane().setPrefHeight(560);
            alert.getDialogPane().setContent(buildDetailsContent(commande, lignes));
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Impossible d'afficher les details de la commande.", ButtonType.OK);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }

    private List<LigneCommandes> getLignesForCommande(Commandes commande) {
        if (commande == null) {
            return Collections.emptyList();
        }

        try {
            List<LigneCommandes> lignes = ligneCommandeService.select().stream()
                    .filter(ligne -> ligne.getCommandeId() == commande.getId())
                    .collect(Collectors.toList());
            if (!lignes.isEmpty()) {
                return lignes;
            }
        } catch (SQLException ignored) {
            // Falls back to the serialized produits field when the detail table is unavailable.
        }

        return buildFallbackLignes(commande);
    }

    private ScrollPane buildDetailsContent(Commandes commande, List<LigneCommandes> lignes) {
        VBox root = new VBox(18);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #f6f7fb;");

        VBox infoCard = new VBox(18);
        infoCard.setPadding(new Insets(18));
        infoCard.setStyle("-fx-background-color: white; -fx-background-radius: 14; "
                + "-fx-border-color: #e5e7eb; -fx-border-radius: 14;");

        Label infoTitle = new Label("Informations de la Commande");
        infoTitle.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #374151;");

        HBox infoGrid = new HBox(90,
                createInfoBlock("Numero de commande", "#" + commande.getId()),
                createInfoBlock("Date de creation", formatDate(commande.getCreatedAt())),
                createInfoBlock("Client", "Client Anonyme")
        );

        VBox statutBlock = new VBox(8);
        Label statutLabel = new Label("Statut");
        statutLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #6b7280;");
        Label statutBadge = new Label(commande.getStatut().toUpperCase());
        statutBadge.setStyle("-fx-background-color: #fbbf24; -fx-text-fill: white; -fx-font-size: 13; "
                + "-fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10;");
        statutBlock.getChildren().addAll(statutLabel, statutBadge);

        infoCard.getChildren().addAll(infoTitle, infoGrid, statutBlock);

        VBox produitsCard = new VBox(14);
        produitsCard.setPadding(new Insets(18));
        produitsCard.setStyle("-fx-background-color: white; -fx-background-radius: 14; "
                + "-fx-border-color: #e5e7eb; -fx-border-radius: 14;");

        Label produitsTitle = new Label("Detail des Produits");
        produitsTitle.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #374151;");

        GridPane table = new GridPane();
        table.setHgap(18);
        table.setVgap(10);

        addHeader(table, "PRODUIT", 0, 0);
        addHeader(table, "PRIX (TND)", 1, 0);
        addHeader(table, "QUANTITE", 2, 0);
        addHeader(table, "SOUS-TOTAL (TND)", 3, 0);

        int row = 1;
        if (lignes.isEmpty()) {
            Label emptyLabel = new Label("Aucun detail produit disponible pour cette commande.");
            emptyLabel.setStyle("-fx-font-size: 15; -fx-text-fill: #6b7280;");
            table.add(emptyLabel, 0, row, 4, 1);
        } else {
            for (LigneCommandes ligne : lignes) {
                addCell(table, ligne.getNom(), 0, row, "-fx-font-size: 16; -fx-text-fill: #374151;");
                addCell(table, String.format("%.2f", ligne.getPrix()), 1, row, "-fx-font-size: 16; -fx-text-fill: #374151;");
                addCell(table, String.valueOf(ligne.getQuantite()), 2, row, "-fx-font-size: 16; -fx-text-fill: #374151;");
                addCell(table, String.format("%.2f", ligne.getSousTotal()), 3, row, "-fx-font-size: 16; -fx-text-fill: #374151;");
                row++;
            }
        }

        HBox totalRow = new HBox();
        totalRow.setPadding(new Insets(8, 0, 0, 0));
        Label totalLabel = new Label("Total:");
        totalLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #374151;");
        Label totalValue = new Label(String.format("%.2f TND", commande.getTotales()));
        totalValue.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #374151;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        totalRow.getChildren().addAll(spacer, totalLabel, new Label("   "), totalValue);

        produitsCard.getChildren().addAll(produitsTitle, table, totalRow);

        root.getChildren().addAll(infoCard, produitsCard);

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }

    private VBox createInfoBlock(String title, String value) {
        VBox block = new VBox(6);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #6b7280;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        block.getChildren().addAll(titleLabel, valueLabel);
        return block;
    }

    private void addHeader(GridPane table, String text, int column, int row) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #6b7280;");
        table.add(label, column, row);
    }

    private void addCell(GridPane table, String text, int column, int row, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        table.add(label, column, row);
    }

    private String formatDate(String createdAt) {
        if (createdAt == null) {
            return "";
        }
        return createdAt.replace('T', ' ');
    }

    private int countArticlesFromProduits(String produits) {
        return buildFallbackLignesFromProduits(produits).stream()
                .mapToInt(LigneCommandes::getQuantite)
                .sum();
    }

    private List<LigneCommandes> buildFallbackLignes(Commandes commande) {
        List<LigneCommandes> lignes = buildFallbackLignesFromProduits(commande.getProduits());
        if (lignes.isEmpty()) {
            return lignes;
        }

        int totalQuantite = lignes.stream().mapToInt(LigneCommandes::getQuantite).sum();
        if (totalQuantite > 0) {
            double prixUnitaireEstime = commande.getTotales() / totalQuantite;
            for (LigneCommandes ligne : lignes) {
                ligne.setPrix(prixUnitaireEstime);
                ligne.setSousTotal(prixUnitaireEstime * ligne.getQuantite());
            }
        }
        return lignes;
    }

    private List<LigneCommandes> buildFallbackLignesFromProduits(String produits) {
        List<LigneCommandes> lignes = new ArrayList<>();
        if (produits == null || produits.isBlank()) {
            return lignes;
        }

        String trimmed = produits.trim();
        if (trimmed.startsWith("[")) {
            List<LigneCommandes> jsonLignes = parseJsonProduits(trimmed);
            if (!jsonLignes.isEmpty()) {
                return jsonLignes;
            }
        }

        String cleaned = trimmed.replace("[", "").replace("]", "").replace("\"", "").trim();
        String[] items = cleaned.split("\\s*,\\s*");
        for (String item : items) {
            String[] parts = item.split("\\s+x");
            String nom = parts[0].trim();
            int quantite = 1;
            if (parts.length > 1) {
                try {
                    quantite = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ignored) {
                    quantite = 1;
                }
            }

            LigneCommandes ligne = new LigneCommandes();
            ligne.setNom(nom);
            ligne.setQuantite(quantite);
            ligne.setPrix(0);
            ligne.setSousTotal(0);
            lignes.add(ligne);
        }

        return lignes;
    }

    private List<LigneCommandes> parseJsonProduits(String produitsJson) {
        List<LigneCommandes> lignes = new ArrayList<>();

        Pattern objectPattern = Pattern.compile("\\{\\s*\"nom\"\\s*:\\s*\"([^\"]*)\"\\s*,\\s*\"quantite\"\\s*:\\s*(\\d+)\\s*,\\s*\"prix\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)\\s*,\\s*\"sousTotal\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)\\s*\\}");
        Matcher objectMatcher = objectPattern.matcher(produitsJson);
        while (objectMatcher.find()) {
            LigneCommandes ligne = new LigneCommandes();
            ligne.setNom(objectMatcher.group(1).replace("\\\"", "\"").replace("\\\\", "\\"));
            ligne.setQuantite(Integer.parseInt(objectMatcher.group(2)));
            ligne.setPrix(Double.parseDouble(objectMatcher.group(3)));
            ligne.setSousTotal(Double.parseDouble(objectMatcher.group(4)));
            lignes.add(ligne);
        }

        if (!lignes.isEmpty()) {
            return lignes;
        }

        Pattern stringPattern = Pattern.compile("\"([^\"]+)\"");
        Matcher stringMatcher = stringPattern.matcher(produitsJson);
        while (stringMatcher.find()) {
            String value = stringMatcher.group(1);
            String[] parts = value.split("\\s+x");

            LigneCommandes ligne = new LigneCommandes();
            ligne.setNom(parts[0].trim());
            ligne.setQuantite(parts.length > 1 ? parseQuantite(parts[1].trim()) : 1);
            ligne.setPrix(0);
            ligne.setSousTotal(0);
            lignes.add(ligne);
        }

        return lignes;
    }

    private int parseQuantite(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
