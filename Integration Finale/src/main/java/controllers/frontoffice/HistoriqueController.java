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
import models.User;
import services.ServiceUser;

import javafx.scene.Cursor;
import java.sql.SQLException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HistoriqueController {
    @FXML
    private VBox commandesContainer;
    @FXML
    private TextField searchField;
    @FXML
    private Label resultsLabel;
    @FXML
    private Button prevButton;
    @FXML
    private Button nextButton;
    @FXML
    private HBox pageNumbersContainer;

    private final CommandeService commandeService = new CommandeService();
    private final LigneCommandeService ligneCommandeService = new LigneCommandeService();
    private final ServiceUser utilisateurService = new ServiceUser();
    private List<Commandes> allCommandes = new ArrayList<>();
    
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 8;

    @FXML
    public void initialize() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentPage = 0;
            refreshCommandes();
        });
        loadCommandes();
    }

    private void loadCommandes() {
        try {
            allCommandes = commandeService.select().stream()
                    .sorted((left, right) -> Integer.compare(right.getId(), left.getId()))
                    .collect(Collectors.toList());
            refreshCommandes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refresh() {
        loadCommandes();
    }

    private void refreshCommandes() {
        commandesContainer.getChildren().clear();

        String query = searchField == null ? "" : searchField.getText();
        List<Commandes> filteredCommandes = allCommandes.stream()
                .filter(commande -> matchesSearch(commande, query))
                .collect(Collectors.toList());

        int totalItems = filteredCommandes.size();
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);

        if (currentPage >= totalPages && totalPages > 0) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }

        int fromIndex = currentPage * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, totalItems);

        if (fromIndex < totalItems) {
            List<Commandes> pagedCommandes = filteredCommandes.subList(fromIndex, toIndex);
            for (Commandes commande : pagedCommandes) {
                commandesContainer.getChildren().add(createCommandeCard(commande));
            }
        }

        updatePaginationUI(totalPages);

        if (resultsLabel != null) {
            resultsLabel.setText(totalItems + (totalItems > 1 ? " commandes" : " commande"));
        }
    }

    private void updatePaginationUI(int totalPages) {
        if (prevButton == null || nextButton == null || pageNumbersContainer == null) return;

        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage >= totalPages - 1 || totalPages == 0);

        pageNumbersContainer.getChildren().clear();
        
        // Only show pagination if there's more than 1 page
        if (totalPages <= 1) return;

        for (int i = 0; i < totalPages; i++) {
            final int pageIndex = i;
            Button pageBtn = new Button(String.valueOf(i + 1));
            pageBtn.setCursor(Cursor.HAND);
            
            if (i == currentPage) {
                pageBtn.setStyle("-fx-background-color: #6d5dfc; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-min-width: 35; -fx-padding: 8;");
            } else {
                pageBtn.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-text-fill: #374151; -fx-background-radius: 8; -fx-border-radius: 8; -fx-min-width: 35; -fx-padding: 8;");
            }
            
            pageBtn.setOnAction(e -> {
                currentPage = pageIndex;
                refreshCommandes();
            });
            
            pageNumbersContainer.getChildren().add(pageBtn);
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            refreshCommandes();
        }
    }

    @FXML
    private void handleNextPage() {
        currentPage++;
        refreshCommandes();
    }

    private HBox createCommandeCard(Commandes commande) {
        HBox row = new HBox(20);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-border-color: #f1f5f9; -fx-border-width: 1; -fx-border-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.02), 8, 0, 0, 2);");

        // 1. Order Info Column (ID & Date)
        VBox idBlock = new VBox(2);
        idBlock.setMinWidth(120);
        idBlock.setPrefWidth(120);
        Label idLabel = new Label("#ORD-" + commande.getId());
        idLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label dateLabel = new Label(formatDate(commande.getCreatedAt()).substring(0, 10)); // Keep only date part
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #94a3b8;");
        idBlock.getChildren().addAll(idLabel, dateLabel);

        // 2. Status Badge Column
        HBox statusContainer = new HBox();
        statusContainer.setMinWidth(100);
        statusContainer.setPrefWidth(100);
        statusContainer.setAlignment(javafx.geometry.Pos.CENTER);
        Label statusBadge = new Label(commande.getStatut().toUpperCase());
        String baseStatusStyle = "-fx-font-size: 11; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10;";
        String normalizedStatus = normalizeStatus(commande.getStatut());
        if (normalizedStatus.contains("livre") || "paye".equals(normalizedStatus)) {
            statusBadge.setStyle(baseStatusStyle + "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;");
        } else if (normalizedStatus.contains("annul")) {
            statusBadge.setStyle(baseStatusStyle + "-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c;");
        } else {
            statusBadge.setStyle(baseStatusStyle + "-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1;");
        }
        statusContainer.getChildren().add(statusBadge);

        // 3. Articles Count Column
        Label artValue = new Label(countArticles(commande.getId()) + " articles");
        artValue.setMinWidth(100);
        artValue.setPrefWidth(100);
        artValue.setStyle("-fx-font-size: 14; -fx-text-fill: #475569;");

        // 4. Total Price Column
        Label totalValue = new Label(String.format("%.2f DT", commande.getTotales()));
        totalValue.setMinWidth(120);
        totalValue.setPrefWidth(120);
        totalValue.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 5. Actions Column
        Button voirDetails = new Button("Détails");
        voirDetails.setCursor(Cursor.HAND);
        voirDetails.setMinWidth(100);
        voirDetails.setPrefWidth(100);
        voirDetails.setStyle("-fx-background-color: transparent; -fx-border-color: #6d5dfc; -fx-border-radius: 6; "
                + "-fx-text-fill: #6d5dfc; -fx-font-weight: bold; -fx-padding: 6 0;");
        voirDetails.setOnAction(event -> showCommandeDetails(commande));
        
        // Button hover
        voirDetails.setOnMouseEntered(e -> voirDetails.setStyle("-fx-background-color: #6d5dfc; -fx-text-fill: white; -fx-background-radius: 6; -fx-font-weight: bold; -fx-padding: 6 0;"));
        voirDetails.setOnMouseExited(e -> voirDetails.setStyle("-fx-background-color: transparent; -fx-border-color: #6d5dfc; -fx-border-radius: 6; -fx-text-fill: #6d5dfc; -fx-font-weight: bold; -fx-padding: 6 0;"));

        row.getChildren().addAll(idBlock, statusContainer, artValue, totalValue, spacer, voirDetails);
        
        // Row hover effect
        row.setOnMouseEntered(e -> {
            row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10; "
                + "-fx-border-color: #cbd5e1; -fx-border-width: 1; -fx-border-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        });
        row.setOnMouseExited(e -> {
            row.setStyle("-fx-background-color: white; -fx-background-radius: 10; "
                + "-fx-border-color: #f1f5f9; -fx-border-width: 1; -fx-border-radius: 10; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.02), 8, 0, 0, 2);");
        });

        return row;
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

            javafx.scene.Scene scene = new javafx.scene.Scene(buildDetailsContent(commande, lignes), 1200, 800);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Détails de la commande #" + commande.getId());
            stage.setScene(scene);
            
            // On le met en plein écran / maximisé
            stage.setMaximized(true);
            
            stage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Impossible d'afficher les détails de la commande.", ButtonType.OK);
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
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Barre d'actions supérieure
        HBox actionBar = new HBox(15);
        actionBar.setPadding(new Insets(0, 0, 10, 0));
        actionBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button btnRetour = new Button("← Retour à la liste");
        btnRetour.setCursor(Cursor.HAND);
        btnRetour.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 8 16; -fx-font-weight: bold;");
        btnRetour.setOnAction(e -> ((javafx.stage.Stage) root.getScene().getWindow()).close());

        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);

        Button btnPdf = new Button("Télécharger PDF");
        btnPdf.setCursor(Cursor.HAND);
        btnPdf.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 20; -fx-font-weight: bold;");
        btnPdf.setOnAction(e -> handleDownloadPdf(commande, lignes));

        actionBar.getChildren().addAll(btnRetour, actionSpacer, btnPdf);

        VBox infoCard = new VBox(20);
        infoCard.setPadding(new Insets(24));
        infoCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; "
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 16; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.02), 15, 0, 0, 5);");

        Label infoTitle = new Label("Résumé de la Commande");
        infoTitle.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        String nomClient = getClientName(commande.getUtilisateurId());

        HBox infoGrid = new HBox(100,
                createInfoBlock("NUMÉRO DE COMMANDE", "#ORD-" + commande.getId()),
                createInfoBlock("DATE DE CRÉATION", formatDate(commande.getCreatedAt())),
                createInfoBlock("CLIENT", nomClient.toUpperCase())
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

        root.getChildren().addAll(actionBar, infoCard, produitsCard);

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

    private String getClientName(int id) {
        try {
            return utilisateurService.recuperer().stream()
                    .filter(u -> u.getId() == id)
                    .map(models.User::getFirstName)
                    .findFirst()
                    .orElse("Client Inconnu");
        } catch (Exception e) {
            return "Client Anonyme";
        }
    }

    private void handleDownloadPdf(Commandes commande, List<LigneCommandes> lignes) {
        String fileName = "Facture_" + commande.getId() + ".pdf";
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Font styles
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.BLACK);
            Font headFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.BLACK);

            // Title
            Paragraph title = new Paragraph("FACTURE PHARMAX", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            // Order Info
            document.add(new Paragraph("Commande #: ORD-" + commande.getId(), normalFont));
            document.add(new Paragraph("Date: " + formatDate(commande.getCreatedAt()), normalFont));
            document.add(new Paragraph("Client: " + getClientName(commande.getUtilisateurId()), normalFont));
            document.add(new Paragraph("Statut: " + commande.getStatut(), normalFont));
            document.add(new Paragraph(" ")); // Spacer

            // Table
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(10);

            // Headers
            String[] headers = {"Produit", "Prix Unitaire", "Quantite", "Sous-total"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
                cell.setBackgroundColor(new BaseColor(109, 93, 252)); // Purple color from app
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                table.addCell(cell);
            }

            // Body
            for (LigneCommandes l : lignes) {
                table.addCell(new PdfPCell(new Phrase(l.getNom(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", l.getPrix()), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(l.getQuantite()), normalFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", l.getSousTotal()), normalFont)));
            }
            document.add(table);

            // Total
            Paragraph total = new Paragraph("TOTAL: " + String.format("%.2f DT", commande.getTotales()), 
                                           new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD));
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText("Facture PDF générée");
            alert.setContentText("Le fichier '" + fileName + "' a été créé à la racine du projet.");
            alert.show();

            // Optionnel: ouvrir le fichier automatiquement
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(new java.io.File(fileName));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération du PDF: " + e.getMessage(), ButtonType.OK);
            alert.show();
        }
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private String normalizeStatus(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a");
    }
}
