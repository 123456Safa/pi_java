package controllers;

import model.Produit;
import model.Categorie;
import service.ProduitService;
import service.CategorieService;
import java.io.IOException;
import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class ProduitController {
    private ProduitService produitService = new ProduitService();
    private CategorieService categorieService = new CategorieService();
    private java.util.Map<Integer, String> categorieMap = new java.util.HashMap<>();
    @FXML private TableView<Produit> tableProduits;
    @FXML private TableColumn<Produit, String> colNom;
    @FXML private TableColumn<Produit, String> colDescription;
    @FXML private TableColumn<Produit, Double> colPrix;
    @FXML private TableColumn<Produit, String> colImage;
    @FXML private TableColumn<Produit, java.util.Date> colDateExpiration;
    @FXML private TableColumn<Produit, String> colStatut;
    @FXML private TableColumn<Produit, Integer> colQuantite;
    @FXML private TableColumn<Produit, String> colCategorieId;
    @FXML private TableColumn<Produit, Void> colActions;
    @FXML private Button btnAjouter;

    @FXML private TextField tfRechercheProduit;
    @FXML private ComboBox<Categorie> cbCategorieProduit;
    @FXML private Button btnRechercherProduit;
    @FXML private Button btnResetProduit;
    @FXML private ComboBox<String> cbTriProduit;
    @FXML private ComboBox<String> cbOrdreProduit;

    private ObservableList<Produit> produitsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            // Configurer les colonnes
            colNom.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNom()));
            colDescription.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
            colPrix.setCellValueFactory(data -> new javafx.beans.property.SimpleDoubleProperty(data.getValue().getPrix()).asObject());
            colDateExpiration.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getDateExpiration()));
            colStatut.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatut()));
            colQuantite.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getQuantite()).asObject());

            // Afficher le nom de la catégorie au lieu de l'ID
            colCategorieId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                categorieMap.getOrDefault(data.getValue().getCategorieId(), "N/A")
            ));

            // Configurer la colonne image pour afficher les images
            colImage.setCellFactory(col -> new TableCell<Produit, String>() {
                private final ImageView imageView = new ImageView();

                {
                    imageView.setFitHeight(60);
                    imageView.setFitWidth(60);
                    imageView.setPreserveRatio(true);
                }

                @Override
                protected void updateItem(String imagePath, boolean empty) {
                    super.updateItem(imagePath, empty);
                    if (empty || imagePath == null || imagePath.isEmpty()) {
                        setGraphic(null);
                    } else {
                        try {
                            File file = new File(imagePath);
                            if (file.exists()) {
                                Image image = new Image(file.toURI().toString(), 60, 60, true, true);
                                imageView.setImage(image);
                                setGraphic(imageView);
                            } else {
                                setGraphic(null);
                                setText("N/A");
                            }
                        } catch (Exception e) {
                            setGraphic(null);
                            setText("N/A");
                        }
                    }
                }
            });
            colImage.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getImage()));

            // Ajouter les boutons d'actions
            addActionButtons();

            // Charger les catégories
            chargerCategories();

            // Configurer les ComboBox de tri
            cbTriProduit.getItems().addAll("Date d'ajout", "Nom", "Prix", "ID");
            cbTriProduit.setValue("Date d'ajout");
            cbTriProduit.setOnAction(e -> appliquerTri());

            cbOrdreProduit.getItems().addAll("↓ Décroissant", "↑ Croissant");
            cbOrdreProduit.setValue("↓ Décroissant");
            cbOrdreProduit.setOnAction(e -> appliquerTri());

            // Charger les données de la table
            rafraichirTable();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation", "Une erreur est survenue lors de l'initialisation : " + e.getMessage());
        }
    }

     private void addActionButtons() {
         colActions.setCellFactory(col -> new TableCell<Produit, Void>() {
             private final Button btnDetail = new Button("👁️");
             private final Button btnModifier = new Button("✏️");
             private final Button btnSupprimer = new Button("🗑️");
             private final HBox box = new HBox(8);

            {
                btnDetail.getStyleClass().addAll("action-btn", "action-btn-detail");
                btnModifier.getStyleClass().addAll("action-btn", "action-btn-edit");
                btnSupprimer.getStyleClass().addAll("action-btn", "action-btn-delete");

                box.setAlignment(Pos.CENTER);
                box.getChildren().addAll(btnDetail, btnModifier, btnSupprimer);

                btnDetail.setOnAction(e -> afficherDetailProduit(getTableView().getItems().get(getIndex())));
                btnModifier.setOnAction(e -> modifierProduit(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(e -> supprimerProduit(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(box);
                }
            }
         });
     }

     public void rafraichirTable() {
         try {
             produitsList.setAll(produitService.afficher());
             tableProduits.setItems(produitsList);
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }

     private void chargerCategories() {
         try {
             List<Categorie> categories = categorieService.afficher();
             cbCategorieProduit.getItems().clear();
             cbCategorieProduit.getItems().add(null); // Option "Toutes les catégories"
             cbCategorieProduit.getItems().addAll(categories);

             // Remplir la map pour le mapping ID -> Nom
             categorieMap.clear();
             for (Categorie cat : categories) {
                 categorieMap.put(cat.getId(), cat.getNom());
             }

             // Utiliser un custom cell factory pour afficher le nom de la catégorie
             cbCategorieProduit.setCellFactory(param -> new ListCell<Categorie>() {
                 @Override
                 protected void updateItem(Categorie item, boolean empty) {
                     super.updateItem(item, empty);
                     setText(empty || item == null ? "Toutes les catégories" : item.getNom());
                 }
             });

             // Afficher le nom de la catégorie sélectionnée
             cbCategorieProduit.setButtonCell(new ListCell<Categorie>() {
                 @Override
                 protected void updateItem(Categorie item, boolean empty) {
                     super.updateItem(item, empty);
                     setText(empty || item == null ? "Toutes les catégories" : item.getNom());
                 }
             });
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }

     @FXML
     private void onAjouter(ActionEvent event) {
         try {
             // Charger le fichier FXML du formulaire
             FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProduitForm.fxml"));
             if (loader.getLocation() == null) {
                 showError("Erreur", "Le fichier ProduitForm.fxml n'a pas pu être trouvé");
                 return;
             }

             BorderPane root = loader.load();

             // Obtenir le contrôleur du formulaire
             ProduitFormController formController = loader.getController();
             if (formController == null) {
                 showError("Erreur", "Le contrôleur ProduitFormController n'a pas pu être initialisé");
                 return;
             }

             // Créer une nouvelle fenêtre (Stage)
             Stage stage = new Stage();
             stage.setTitle("Ajouter un nouveau produit");
             stage.setScene(new Scene(root, 600, 800));
             stage.setResizable(true);

             // Passer la référence de la stage et du contrôleur parent
             formController.setStage(stage);
             formController.setParentController(this);

             // Afficher la fenêtre de manière modale
             stage.showAndWait();

         } catch (IOException e) {
             e.printStackTrace();
             showError("Erreur", "Impossible d'ouvrir la fenêtre du formulaire: " + e.getMessage());
         } catch (Exception e) {
             e.printStackTrace();
             showError("Erreur", "Une erreur est survenue: " + e.getMessage());
         }
     }

      private void modifierProduit(Produit produit) {
          if (produit == null) {
              showError("Erreur", "Veuillez sélectionner un produit à modifier");
              return;
          }
          try {
              // Charger le fichier FXML du formulaire
              FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProduitForm.fxml"));
              if (loader.getLocation() == null) {
                  showError("Erreur", "Le fichier ProduitForm.fxml n'a pas pu être trouvé");
                  return;
              }

              BorderPane root = loader.load();

              // Obtenir le contrôleur du formulaire
              ProduitFormController formController = loader.getController();
              if (formController == null) {
                  showError("Erreur", "Le contrôleur ProduitFormController n'a pas pu être initialisé");
                  return;
              }

              // Passer le produit pour édition AVANT de créer la stage
              formController.setProduitToEdit(produit);

              // Créer une nouvelle fenêtre (Stage)
              Stage stage = new Stage();
              stage.setTitle("Modifier le produit");
              stage.setScene(new Scene(root, 600, 800));
              stage.setResizable(true);

              // Passer la référence de la stage et du contrôleur parent
              formController.setStage(stage);
              formController.setParentController(this);

              // Afficher la fenêtre de manière modale
              stage.showAndWait();

          } catch (IOException e) {
              e.printStackTrace();
              showError("Erreur", "Impossible d'ouvrir la fenêtre du formulaire: " + e.getMessage());
          } catch (Exception e) {
              e.printStackTrace();
              showError("Erreur", "Une erreur est survenue: " + e.getMessage());
          }
      }

    private void supprimerProduit(Produit produit) {
        if (produit == null) {
            AlertUtil.showError("Erreur", "Veuillez sélectionner un produit à supprimer");
            return;
        }

        if (AlertUtil.showConfirmation("Confirmation", "Êtes-vous sûr de vouloir supprimer le produit \"" + produit.getNom() + "\" ?")) {
            try {
                produitService.supprimer(produit.getId());
                rafraichirTable();
                AlertUtil.showSuccess("Succès", "Le produit a été supprimé avec succès.");
            } catch (SQLException e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur", "Une erreur est survenue: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onRechercherProduit(ActionEvent event) {
        try {
            String recherche = tfRechercheProduit.getText().trim().toLowerCase();
            Categorie categorieSelectionnee = cbCategorieProduit.getValue();

            List<Produit> tousLesProduits = produitService.afficher();
            if (tousLesProduits == null) {
                tousLesProduits = new ArrayList<>();
            }

            ObservableList<Produit> resultatRecherche = FXCollections.observableArrayList();

            for (Produit prod : tousLesProduits) {
                boolean matchTexte = recherche.isEmpty() ||
                    prod.getNom().toLowerCase().contains(recherche) ||
                    prod.getDescription().toLowerCase().contains(recherche);

                boolean matchCategorie = categorieSelectionnee == null ||
                    prod.getCategorieId() == categorieSelectionnee.getId();

                if (matchTexte && matchCategorie) {
                    resultatRecherche.add(prod);
                }
            }

            tableProduits.setItems(resultatRecherche);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }

    @FXML
    private void onResetRechercheProduit(ActionEvent event) {
        try {
            tfRechercheProduit.clear();
            cbCategorieProduit.setValue(null);
            cbTriProduit.setValue("Date d'ajout");
            cbOrdreProduit.setValue("↓ Décroissant");
            rafraichirTable();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur s'est produite lors de la réinitialisation: " + e.getMessage());
        }
    }

    private void appliquerTri() {
        try {
            String triOption = cbTriProduit.getValue();
            String ordreOption = cbOrdreProduit.getValue();

            if (triOption == null || triOption.isEmpty()) {
                return;
            }

            boolean estCroissant = ordreOption != null && ordreOption.startsWith("↑");

            List<Produit> produits = new ArrayList<>(produitsList);

            // Trier selon l'option sélectionnée
            if ("Date d'ajout".equals(triOption)) {
                if (estCroissant) {
                    produits.sort((p1, p2) -> {
                        if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) return 0;
                        return p1.getCreatedAt().compareTo(p2.getCreatedAt());
                    });
                } else {
                    produits.sort((p1, p2) -> {
                        if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) return 0;
                        return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                    });
                }
            } else if ("Nom".equals(triOption)) {
                if (estCroissant) {
                    produits.sort((p1, p2) -> p1.getNom().compareTo(p2.getNom()));
                } else {
                    produits.sort((p1, p2) -> p2.getNom().compareTo(p1.getNom()));
                }
            } else if ("Prix".equals(triOption)) {
                if (estCroissant) {
                    produits.sort((p1, p2) -> Double.compare(p1.getPrix(), p2.getPrix()));
                } else {
                    produits.sort((p1, p2) -> Double.compare(p2.getPrix(), p1.getPrix()));
                }
            } else if ("ID".equals(triOption)) {
                if (estCroissant) {
                    produits.sort((p1, p2) -> Integer.compare(p1.getId(), p2.getId()));
                } else {
                    produits.sort((p1, p2) -> Integer.compare(p2.getId(), p1.getId()));
                }
            }

            tableProduits.setItems(FXCollections.observableArrayList(produits));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur s'est produite lors du tri: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.show();
    }

    private void afficherDetailProduit(Produit produit) {
        if (produit == null) {
            showError("Erreur", "Veuillez sélectionner un produit");
            return;
        }
        try {
            // Charger le fichier FXML du détail
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProduitDetail.fxml"));
            if (loader.getLocation() == null) {
                showError("Erreur", "Le fichier ProduitDetail.fxml n'a pas pu être trouvé");
                return;
            }

            BorderPane root = loader.load();

            // Obtenir le contrôleur du détail
            ProduitDetailController detailController = loader.getController();
            if (detailController == null) {
                showError("Erreur", "Le contrôleur ProduitDetailController n'a pas pu être initialisé");
                return;
            }

            // Passer le produit
            detailController.setProduit(produit);

            // Créer une nouvelle fenêtre (Stage)
            Stage stage = new Stage();
            stage.setTitle("Détail du produit - " + produit.getNom());
            stage.setScene(new Scene(root, 800, 600));
            stage.setResizable(true);

            // Passer la référence de la stage
            detailController.setStage(stage);

            // Afficher la fenêtre
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la fenêtre de détail: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }
}
