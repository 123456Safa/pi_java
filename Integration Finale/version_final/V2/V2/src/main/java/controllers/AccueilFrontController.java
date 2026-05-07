package controllers;

import controllers.frontoffice.PanierController;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import models.Categorie;
import models.PanierItem;
import models.Produit;
import services.CategorieService;
import services.NotificationService;
import services.PanierService;
import services.ProduitService;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AccueilFrontController {

    @FXML private FlowPane vboxProduitsAccueil;
    @FXML private FlowPane flowPaneTopPromos;
    @FXML private StackPane contentStack;
    @FXML private VBox homeContainer;
    @FXML private HBox sub_nav;
    @FXML private Label navPromos;
    @FXML private Label navNouveautes;
    @FXML private TextField searchNomField;
    @FXML private ComboBox<Categorie> categorieComboBox;
    @FXML private TextField prixMinField;
    @FXML private TextField prixMaxField;
    @FXML private Button btnRechercher;
    @FXML private Button btnReinitialiser;
    @FXML private Label panierBadge;
    @FXML private Label notifBadge;

    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();
    private final PanierService panierService = PanierService.getInstance();
    private final NotificationService notificationService = new NotificationService();

    private List<Produit> tousLesProduits;
    private boolean filterPromos = false;
    private boolean filterNouveautes = false;

    @FXML
    public void initialize() {
        chargerCategories();
        chargerProduits();
        configurerValidationsPrix();

        searchNomField.textProperty().addListener((obs, o, n) -> filtrerProduits());
        categorieComboBox.valueProperty().addListener((obs, o, n) -> filtrerProduits());
        prixMinField.textProperty().addListener((obs, o, n) -> filtrerProduits());
        prixMaxField.textProperty().addListener((obs, o, n) -> filtrerProduits());

        navPromos.setOnMouseClicked(e -> {
            filterPromos = !filterPromos;
            filterNouveautes = false;
            majStyleNav();
            filtrerProduits();
        });

        navNouveautes.setOnMouseClicked(e -> {
            filterNouveautes = !filterNouveautes;
            filterPromos = false;
            majStyleNav();
            filtrerProduits();
        });

        updatePanierBadge();
        panierService.getPanier().addListener((javafx.collections.ListChangeListener<PanierItem>) c -> updatePanierBadge());

        updateNotifBadge();
        Timeline notifTl = new Timeline(new KeyFrame(Duration.seconds(10), e -> updateNotifBadge()));
        notifTl.setCycleCount(Animation.INDEFINITE);
        notifTl.play();
    }

    // ────── BADGE UPDATES ──────

    private void updatePanierBadge() {
        if (panierBadge == null) return;
        int count = panierService.getPanier().size();
        panierBadge.setText(String.valueOf(count));
        panierBadge.setVisible(count > 0);
        panierBadge.setManaged(count > 0);
    }

    private void updateNotifBadge() {
        if (notifBadge == null) return;
        try {
            int count = notificationService.getUnreadCount(1);
            notifBadge.setText(String.valueOf(count));
            notifBadge.setVisible(count > 0);
            notifBadge.setManaged(count > 0);
        } catch (Exception ignored) {}
    }

    // ────── NAVIGATION TO SUB-VIEWS ──────

    @FXML
    public void showPanier() {
        loadSubView("/frontoffice/panier.fxml", ctrl -> {
            if (ctrl instanceof PanierController panierCtrl) {
                panierCtrl.setOnContinuerAchats(this::retourCatalogue);
            }
        });
    }

    @FXML
    public void showHistorique() {
        loadSubView("/frontoffice/historique.fxml", ctrl -> {
            try { ctrl.getClass().getMethod("refresh").invoke(ctrl); } catch (Exception ignored) {}
        });
    }

    @FXML
    public void showReclamations() {
        loadSubView("/reclamation/home.fxml", ctrl -> {});
    }

    @FXML
    public void showNotifications() {
        if (notifBadge != null) { notifBadge.setVisible(false); notifBadge.setManaged(false); }
        loadSubView("/frontoffice/notifications.fxml", ctrl -> {});
    }

    private void retourCatalogue() {
        contentStack.getChildren().removeIf(n -> n != homeContainer);
        homeContainer.setVisible(true);
        homeContainer.setManaged(true);
    }

    private void loadSubView(String fxmlPath, ControllerInitializer init) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            Object ctrl = loader.getController();
            if (init != null) init.initialize(ctrl);

            Button btnBack = new Button("← Retour au catalogue");
            btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #5856d6; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 10 16;");
            btnBack.setOnAction(e -> retourCatalogue());

            VBox wrapper = new VBox(btnBack, view);
            VBox.setVgrow(view, Priority.ALWAYS);
            wrapper.setFillWidth(true);

            homeContainer.setVisible(false);
            homeContainer.setManaged(false);
            contentStack.getChildren().removeIf(n -> n != homeContainer);
            contentStack.getChildren().add(wrapper);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ────── BACK OFFICE ──────

    @FXML
    private void goToBackOffice() {
        AppShellController shell = AppShellController.getInstance();
        if (shell != null) shell.showBackOffice();
    }

    // ────── CATEGORIES ──────

    private void chargerCategories() {
        try {
            List<Categorie> categories = categorieService.afficher();
            categorieComboBox.setItems(FXCollections.observableArrayList(categories));
            remplirSousNav(categories);
        } catch (SQLException e) {
            System.err.println("Erreur catégories: " + e.getMessage());
        }
    }

    private void remplirSousNav(List<Categorie> categories) {
        sub_nav.getChildren().clear();

        Label labelTous = new Label("TOUTES");
        labelTous.getStyleClass().addAll("sub-nav-item", "sub-nav-active");
        labelTous.setOnMouseClicked(e -> {
            sub_nav.getChildren().forEach(n -> n.getStyleClass().remove("sub-nav-active"));
            labelTous.getStyleClass().add("sub-nav-active");
            categorieComboBox.setValue(null);
            filtrerProduits();
        });
        sub_nav.getChildren().add(labelTous);

        for (Categorie cat : categories) {
            Label lbl = new Label(cat.getNom().toUpperCase());
            lbl.getStyleClass().add("sub-nav-item");
            lbl.setOnMouseClicked(e -> {
                sub_nav.getChildren().forEach(n -> n.getStyleClass().remove("sub-nav-active"));
                lbl.getStyleClass().add("sub-nav-active");
                categorieComboBox.setValue(cat);
            });
            sub_nav.getChildren().add(lbl);
        }
    }

    // ────── PRODUCTS ──────

    private void chargerProduits() {
        try {
            tousLesProduits = produitService.afficher();
            afficherProduits(tousLesProduits);
            afficherTopPromos();
        } catch (SQLException e) {
            e.printStackTrace();
            chargerProduitsDemo();
        }
    }

    private void chargerProduitsDemo() {
        tousLesProduits = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Produit p = new Produit();
            p.setId(i);
            p.setNom("Produit " + i);
            p.setDescription("Description du produit " + i);
            p.setPrix(i * 10.0);
            tousLesProduits.add(p);
        }
        afficherProduits(tousLesProduits);
    }

    private void filtrerProduits() {
        if (tousLesProduits == null || tousLesProduits.isEmpty()) return;

        String nom = searchNomField.getText().toLowerCase().trim();
        Categorie cat = categorieComboBox.getValue();
        double pMin = 0, pMax = Double.MAX_VALUE;

        try {
            if (!prixMinField.getText().trim().isEmpty())
                pMin = Double.parseDouble(prixMinField.getText().trim());
            if (!prixMaxField.getText().trim().isEmpty())
                pMax = Double.parseDouble(prixMaxField.getText().trim());
        } catch (NumberFormatException ignored) { return; }

        final double min = pMin, max = pMax;
        List<Produit> filtered = tousLesProduits.stream()
                .filter(p -> p.getNom().toLowerCase().contains(nom))
                .filter(p -> cat == null || p.getCategorieId() == cat.getId())
                .filter(p -> p.getPrix() >= min && p.getPrix() <= max)
                .filter(p -> !filterPromos || p.getDiscountPercentage() > 0)
                .filter(p -> !filterNouveautes || isRecent(p.getCreatedAt()))
                .collect(Collectors.toList());

        afficherProduits(filtered);
    }

    @FXML
    private void onRechercherProduit() { filtrerProduits(); }

    @FXML
    private void onReinitialiser() { reinitialiserFiltres(); }

    private void reinitialiserFiltres() {
        searchNomField.clear();
        categorieComboBox.setValue(null);
        prixMinField.clear();
        prixMaxField.clear();
        filterPromos = false;
        filterNouveautes = false;
        majStyleNav();
        sub_nav.getChildren().forEach(n -> n.getStyleClass().remove("sub-nav-active"));
        if (!sub_nav.getChildren().isEmpty())
            sub_nav.getChildren().get(0).getStyleClass().add("sub-nav-active");
        afficherProduits(tousLesProduits);
    }

    private void majStyleNav() {
        navPromos.getStyleClass().remove("nav-item-active");
        navNouveautes.getStyleClass().remove("nav-item-active");
        if (filterPromos) navPromos.getStyleClass().add("nav-item-active");
        if (filterNouveautes) navNouveautes.getStyleClass().add("nav-item-active");
    }

    private void configurerValidationsPrix() {
        prixMinField.setTextFormatter(new TextFormatter<>(c -> {
            String t = c.getControlNewText();
            return (t.isEmpty() || t.matches("\\d*(\\.\\d{0,2})?")) ? c : null;
        }));
        prixMaxField.setTextFormatter(new TextFormatter<>(c -> {
            String t = c.getControlNewText();
            return (t.isEmpty() || t.matches("\\d*(\\.\\d{0,2})?")) ? c : null;
        }));
        prixMinField.focusedProperty().addListener((obs, o, focused) -> {
            if (!focused && !prixMinField.getText().isEmpty()) validationChampPrix(prixMinField);
        });
        prixMaxField.focusedProperty().addListener((obs, o, focused) -> {
            if (!focused && !prixMaxField.getText().isEmpty()) validationChampPrix(prixMaxField);
        });
    }

    private void validationChampPrix(TextField champ) {
        try {
            double v = Double.parseDouble(champ.getText());
            champ.setStyle(v < 0 ? "-fx-border-color: #dc3545; -fx-border-width: 2;" : "-fx-border-color: #28a745; -fx-border-width: 2;");
        } catch (NumberFormatException e) {
            champ.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
        }
    }

    private boolean isRecent(java.util.Date date) {
        if (date == null) return false;
        return (new java.util.Date().getTime() - date.getTime()) / (1000 * 60 * 60 * 24) <= 30;
    }

    private void afficherProduits(List<Produit> produits) {
        vboxProduitsAccueil.getChildren().clear();
        if (produits == null || produits.isEmpty()) {
            Label lbl = new Label("Aucun produit trouvé");
            lbl.setStyle("-fx-font-size: 16; -fx-text-fill: #999; -fx-padding: 40;");
            vboxProduitsAccueil.setAlignment(Pos.CENTER);
            vboxProduitsAccueil.getChildren().add(lbl);
            return;
        }
        vboxProduitsAccueil.setAlignment(Pos.TOP_LEFT);
        for (Produit p : produits)
            vboxProduitsAccueil.getChildren().add(creerProduitCard(p));
    }

    private void afficherTopPromos() {
        if (tousLesProduits == null) return;
        flowPaneTopPromos.getChildren().clear();
        List<Produit> promos = tousLesProduits.stream()
                .filter(p -> p.getDiscountPercentage() > 0)
                .limit(4)
                .collect(Collectors.toList());
        if (promos.isEmpty()) {
            flowPaneTopPromos.getChildren().add(new Label("Aucune promotion en cours"));
            return;
        }
        for (Produit p : promos)
            flowPaneTopPromos.getChildren().add(creerProduitCard(p));
    }

    private VBox creerProduitCard(Produit p) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setPrefWidth(240);
        card.setMinWidth(240);

        StackPane imgContainer = new StackPane();
        imgContainer.getStyleClass().add("card-image-container");
        imgContainer.setPrefHeight(200);

        ImageView iv = new ImageView();
        try {
            if (p.getImage() != null && !p.getImage().isEmpty()) {
                Image img = new Image(new java.io.File(p.getImage()).toURI().toString(), 180, 180, true, true);
                iv.setImage(img.isError() ? new Image("https://via.placeholder.com/180", 180, 180, true, true) : img);
            } else {
                iv.setImage(new Image("https://via.placeholder.com/180", 180, 180, true, true));
            }
        } catch (Exception e) {
            iv.setImage(new Image("https://via.placeholder.com/180", 180, 180, true, true));
        }
        iv.setFitWidth(180);
        iv.setFitHeight(180);
        iv.setPreserveRatio(true);
        imgContainer.getChildren().add(iv);

        HBox badgesBox = new HBox(5);
        badgesBox.setAlignment(Pos.TOP_LEFT);
        badgesBox.setPadding(new Insets(10));
        if (p.getDiscountPercentage() > 0) {
            Label d = new Label("-" + (int) p.getDiscountPercentage() + "%");
            d.getStyleClass().add("card-badge-discount");
            badgesBox.getChildren().add(d);
        }
        if (isRecent(p.getCreatedAt())) {
            Label n = new Label("NOUVEAU");
            n.getStyleClass().add("card-badge-new");
            badgesBox.getChildren().add(n);
        }
        StackPane.setAlignment(badgesBox, Pos.TOP_LEFT);
        imgContainer.getChildren().add(badgesBox);

        VBox info = new VBox(8);
        info.getStyleClass().add("card-info-container");

        Label name = new Label(p.getNom());
        name.getStyleClass().add("card-title");
        name.setWrapText(true);
        name.setMinHeight(45);
        name.setMaxHeight(45);

        Label desc = new Label(p.getDescription() != null ? p.getDescription() : "");
        desc.getStyleClass().add("card-description");
        desc.setWrapText(true);
        desc.setMinHeight(35);
        desc.setMaxHeight(35);

        Label rating = new Label("★★★★☆ (4.5)");
        rating.getStyleClass().add("rating-stars");

        HBox priceRow = new HBox(10);
        priceRow.getStyleClass().add("card-price-row");
        double finalPrice = p.getPrixFinal() > 0 ? p.getPrixFinal() : p.getPrix();
        Label current = new Label(String.format("%.3f TND", finalPrice));
        current.getStyleClass().add("card-price-current");
        priceRow.getChildren().add(current);
        if (p.getDiscountPercentage() > 0) {
            Label old = new Label(String.format("%.3f TND", p.getPrix()));
            old.getStyleClass().add("card-price-old");
            priceRow.getChildren().add(old);
        }

        HBox actions = new HBox(8);
        actions.getStyleClass().add("card-actions");
        actions.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("ACHETER 🛒");
        addBtn.getStyleClass().add("btn-card-add");
        addBtn.setOnAction(e -> {
            PanierItem item = new PanierItem(
                    p.getId(), p.getNom(), finalPrice,
                    p.getDescription() != null ? p.getDescription() : "",
                    p.getImage() != null ? p.getImage() : "");
            panierService.ajouterProduit(item);
        });
        addBtn.setOnMouseClicked(e -> e.consume());
        HBox.setHgrow(addBtn, Priority.ALWAYS);
        addBtn.setMaxWidth(Double.MAX_VALUE);

        Button wishBtn = new Button("♡");
        wishBtn.getStyleClass().add("btn-card-icon");

        actions.getChildren().addAll(addBtn, wishBtn);
        info.getChildren().addAll(name, desc, rating, priceRow, actions);
        card.getChildren().addAll(imgContainer, info);

        card.setOnMouseClicked(e -> ouvrirDetailsProduit(p));
        card.setStyle("-fx-cursor: hand;");

        return card;
    }

    @FunctionalInterface
    private interface ControllerInitializer {
        void initialize(Object controller);
    }

    private void ouvrirDetailsProduit(Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gestion/produit-detail.fxml"));
            Parent root = loader.load();

            GestionProduitDetailController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Détails du produit");
            stage.initModality(Modality.APPLICATION_MODAL);

            controller.setStage(stage);
            controller.setProduit(produit);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ouvrir les détails du produit.");
            alert.showAndWait();
        }
    }
}
