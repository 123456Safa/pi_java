package controllers.frontoffice;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.PanierItem;
import models.Client;
import services.PanierService;
import services.CommandeService;

public class PanierController {

    @FXML
    private TableView<PanierItem> tablePanier;
    @FXML
    private TableColumn<PanierItem, String> colNom;
    @FXML
    private TableColumn<PanierItem, Double> colPrix;
    @FXML
    private TableColumn<PanierItem, Integer> colQuantite;
    @FXML
    private TableColumn<PanierItem, Double> colSousTotal;
    @FXML
    private Label totalLabel;
    @FXML
    private Button validerBtn;
    @FXML
    private Button supprimerBtn;

    private CommandeService commandeService = new CommandeService();

    @FXML
    public void initialize() {
        // Bind tableau au panier
        tablePanier.setItems(PanierService.getPanier());

        // Setup columns
        colNom.setCellValueFactory(cellData -> cellData.getValue().nomProperty());
        colPrix.setCellValueFactory(cellData -> cellData.getValue().prixProperty().asObject());
        colQuantite.setCellValueFactory(cellData -> cellData.getValue().quantiteProperty().asObject());
        colSousTotal.setCellValueFactory(cellData -> cellData.getValue().sousTotalProperty().asObject());

        // Make quantité editable
        colQuantite.setCellFactory(column -> new TableCell<PanierItem, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Spinner<Integer> spinner = new Spinner<>(1, 100, item);
                    spinner.setPrefWidth(60);
                    spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
                        getTableView().getItems().get(getIndex()).setQuantite(newVal);
                    });
                    setGraphic(spinner);
                }
            }
        });

        // Update total display
        PanierService.getPanier().addListener((javafx.collections.ListChangeListener<PanierItem>) c -> updateTotal());
        updateTotal();

        supprimerBtn.setOnAction(e -> supprimerProduit());
        validerBtn.setOnAction(e -> validerCommande());
    }

    private void updateTotal() {
        double total = PanierService.getTotal();
        totalLabel.setText(String.format("TOTAL: %.2f DT", total));
    }

    @FXML
    private void supprimerProduit() {
        PanierItem selected = tablePanier.getSelectionModel().getSelectedItem();
        if (selected != null) {
            PanierService.retirerDuPanier(selected);
        }
    }

    @FXML
    private void validerCommande() {
        if (PanierService.getPanier().isEmpty()) {
            showAlert("Panier vide", "Veuillez ajouter des produits avant de commander.");
            return;
        }

        // Open client form dialog
        showFormulaireClient();
    }

    private void showFormulaireClient() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Informations Client");

        VBox formVBox = new VBox();
        formVBox.setSpacing(10);
        formVBox.setPadding(new Insets(15));
        formVBox.setStyle("-fx-font-family: 'Arial';");

        TextField nomField = new TextField();
        nomField.setPromptText("Nom");
        nomField.setPrefHeight(35);

        TextField prenomField = new TextField();
        prenomField.setPromptText("Prénom");
        prenomField.setPrefHeight(35);

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefHeight(35);

        TextField telephoneField = new TextField();
        telephoneField.setPromptText("Téléphone");
        telephoneField.setPrefHeight(35);

        TextArea adresseArea = new TextArea();
        adresseArea.setPromptText("Adresse");
        adresseArea.setPrefHeight(80);
        adresseArea.setWrapText(true);

        Button commanderBtn = new Button("Passer la commande");
        commanderBtn.setStyle("-fx-font-size: 14; -fx-padding: 10px 20px; -fx-background-color: #27ae60; -fx-text-fill: white;");
        commanderBtn.setOnAction(e -> {
            if (validerFormulaire(nomField, prenomField, emailField, telephoneField, adresseArea)) {
                Client client = new Client(
                    nomField.getText(),
                    prenomField.getText(),
                    emailField.getText(),
                    telephoneField.getText(),
                    adresseArea.getText()
                );
                passerCommande(client, dialogStage);
            }
        });

        formVBox.getChildren().addAll(
            new Label("Informations de livraison:"),
            nomField, prenomField, emailField, telephoneField, adresseArea,
            commanderBtn
        );

        Scene scene = new Scene(formVBox, 400, 400);
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    private boolean validerFormulaire(TextField nom, TextField prenom, TextField email, TextField telephone, TextArea adresse) {
        if (nom.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer votre nom.");
            return false;
        }
        if (prenom.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer votre prénom.");
            return false;
        }
        if (email.getText().trim().isEmpty() || !email.getText().contains("@")) {
            showAlert("Erreur", "Veuillez entrer un email valide.");
            return false;
        }
        if (telephone.getText().trim().isEmpty() || telephone.getText().length() < 8) {
            showAlert("Erreur", "Veuillez entrer un téléphone valide.");
            return false;
        }
        if (adresse.getText().trim().isEmpty()) {
            showAlert("Erreur", "Veuillez entrer une adresse.");
            return false;
        }
        return true;
    }

    private void passerCommande(Client client, Stage dialogStage) {
        try {
            // Enregistrer la commande
            commandeService.enregistrerCommande(client, PanierService.getPanier());

            dialogStage.close();
            showAlert("Succès", "✅ Commande enregistrée avec succès!");

            // Afficher la facture
            showFacture(client);

            // Vider le panier
            PanierService.viderPanier();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showFacture(Client client) {
        Stage factureStage = new Stage();
        factureStage.setTitle("Facture - Pharmax Pharmacy");

        VBox factureVBox = new VBox();
        factureVBox.setSpacing(10);
        factureVBox.setPadding(new Insets(20));
        factureVBox.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11;");

        // Header
        Label headerLabel = new Label("═══════════════════════════════════════");
        Label titleLabel = new Label("PHARMAX PHARMACY - FACTURE");
        Label headerLabel2 = new Label("═══════════════════════════════════════");

        // Client info
        Label clientLabel = new Label("CLIENT:");
        Label clientInfoLabel = new Label(client.getNom() + " " + client.getPrenom());
        Label emailLabel = new Label("Email: " + client.getEmail());
        Label telLabel = new Label("Tél: " + client.getTelephone());
        Label adresseLabel = new Label("Adresse: " + client.getAdresse());

        // Details
        Label detailsHeader = new Label("\n═════════════════════════════════════════");
        Label productsHeader = new Label("ARTICLE          QTE      PRIX U.      TOTAL");
        Label detailsLine = new Label("═════════════════════════════════════════");

        TextArea productsArea = new TextArea();
        productsArea.setEditable(false);
        productsArea.setWrapText(true);
        productsArea.setPrefHeight(200);

        StringBuilder productText = new StringBuilder();
        for (PanierItem item : PanierService.getPanier()) {
            productText.append(String.format("%-20s %3d  %10.2f DT  %10.2f DT\n",
                item.getNom().substring(0, Math.min(20, item.getNom().length())),
                item.getQuantite(),
                item.getPrix(),
                item.getSousTotal()
            ));
        }
        productsArea.setText(productText.toString());

        // Total
        Label totalLine = new Label("═════════════════════════════════════════");
        double total = PanierService.getTotal();
        Label totalFinal = new Label(String.format("TOTAL GÉNÉRAL: %.2f DT", total));
        totalFinal.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        Label footerLabel = new Label("═════════════════════════════════════════");
        Label thanksLabel = new Label("Merci pour votre achat!");

        factureVBox.getChildren().addAll(
            headerLabel, titleLabel, headerLabel2,
            new Separator(),
            clientLabel, clientInfoLabel, emailLabel, telLabel, adresseLabel,
            detailsHeader, productsHeader, detailsLine,
            productsArea, totalLine, totalFinal,
            footerLabel, thanksLabel
        );

        ScrollPane scrollPane = new ScrollPane(factureVBox);
        Scene scene = new Scene(scrollPane, 500, 600);
        factureStage.setScene(scene);
        factureStage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

