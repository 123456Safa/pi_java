package controllers;

import Model.Reclamation;
import Model.Reponse;
import Service.ReclamationService;
import Service.ReponseService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.List;

public class DetailReclamationControlleradmin {
    private final ReclamationService service = new ReclamationService();
    private final ReponseService reponseService = new ReponseService();
    
    @FXML private Label titre, description, date, statut, statusActuel, reponseCount;
    @FXML private VBox reponseContainer;
    @FXML private Button btnAjouterReponse, btnModifier, btnSupprimer;
    
    @FXML private VBox translationContainer;
    @FXML private Label translatedLangLabel;
    @FXML private Label translatedTitre;
    @FXML private Label translatedDescription;
    
    private HomeAdminController homeController;
    private Reclamation r;

    public void setHomeController(HomeAdminController c) {
        this.homeController = c;
    }

    public void refreshReponses() {
        if (r != null) {
            loadReponses(r.getId());
        }
        if (homeController != null) {
            homeController.refresh();
        }
    }

    public void refreshStatus(String newStatus) {
        if (r != null) {
            r.setStatut(newStatus);
            statut.setText(newStatus);
            statusActuel.setText(newStatus);
            
            // Style the status badge
            String statusColor = getStatusColor(newStatus);
            statut.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 5 10; -fx-border-radius: 3;");
        }
    }

    public void setData(Reclamation r) {
        this.r = r;

        titre.setText(r.getTitre());
        description.setText(r.getDescription());
        date.setText("📅 " + r.getDateCreation());
        statut.setText(r.getStatut());
        
        // Style the status badge
        String statusColor = getStatusColor(r.getStatut());
        statut.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 5 10; -fx-border-radius: 3;");
        
        statusActuel.setText(r.getStatut());
        
        loadReponses(r.getId());
    }

    private String getStatusColor(String statut) {
        switch (statut.toUpperCase()) {
            case "EN ATTENTE":
                return "#FFA500";
            case "RÉSOLUE":
                return "#28a745";
            case "FERMÉ":
                return "#6c757d";
            case "EN COURS":
                return "#17a2b8";
            default:
                return "#999999";
        }
    }

    private void loadReponses(int reclamationId) {
        reponseContainer.getChildren().clear();
        
        List<Reponse> list = reponseService.getByReclamationId(reclamationId);
        
        if (list == null || list.isEmpty()) {
            Label empty = new Label("Aucune réponse pour cette réclamation");
            empty.setStyle("-fx-font-size: 12; -fx-text-fill: #999999; -fx-padding: 20;");
            reponseContainer.getChildren().add(empty);
            reponseCount.setText("(0)");
            return;
        }
        
        reponseCount.setText("(" + list.size() + ")");
        
        for (Reponse rep : list) {
            VBox reponseCard = createReponseCard(rep);
            reponseContainer.getChildren().add(reponseCard);
        }
    }

    private VBox createReponseCard(Reponse rep) {
        VBox card = new VBox(8);
        card.setStyle("-fx-border-color: transparent transparent transparent #28a745; -fx-border-width: 0 0 0 4; -fx-background-color: #f9f9f9; -fx-padding: 15;");
        
        // Header with date
        HBox headerBox = new HBox(10);
        headerBox.setStyle("-fx-alignment: center-left;");
        
        Label dateLabel = new Label("📅 " + rep.getDateReponse());
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #999999;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        MenuButton translateMenu = new MenuButton("🌐");
        translateMenu.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 5 10;");
        //les langues
        String[][] languages = {
            {"English", "en"},
            {"Español", "es"},
            {"Deutsch", "de"},
            {"Italiano", "it"},
            {"Português", "pt"},
            {"日本語", "ja"},
            {"中文", "zh-CN"},
            {"العربية", "ar"},
            {"Русский", "ru"}
        };
        
        Label translationLabel = new Label();
        translationLabel.setWrapText(true);
        translationLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #333333; -fx-background-color: #e9ecef; -fx-padding: 10; -fx-border-radius: 4;");
        translationLabel.setVisible(false);
        translationLabel.setManaged(false);

        for (String[] lang : languages) {
            MenuItem item = new MenuItem(lang[0]);
            item.setOnAction(e -> {
                translationLabel.setText("Traduction en cours...");
                translationLabel.setVisible(true);
                translationLabel.setManaged(true);
                new Thread(() -> {
                    String translated = translateText(rep.getContenu(), lang[1]);
                    javafx.application.Platform.runLater(() -> {
                        translationLabel.setText("🌐 " + lang[0] + " :\n" + translated);
                    });
                }).start();
            });
            translateMenu.getItems().add(item);
        }
        
        headerBox.getChildren().addAll(dateLabel, spacer, translateMenu);
        
        // Content
        Label contenu = new Label(rep.getContenu());
        contenu.setWrapText(true);
        contenu.setStyle("-fx-font-size: 12; -fx-text-fill: #555555;");
        
        // Action buttons
        HBox buttonBox = new HBox(8);
        buttonBox.setStyle("-fx-alignment: center-left;");
        
        Button editBtn = new Button("✏️ Éditer");
        editBtn.setStyle("-fx-font-size: 11; -fx-padding: 6 12; -fx-background-color: #FFA500; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");
        editBtn.setOnAction(e -> openEditReponse(rep));
        
        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-font-size: 11; -fx-padding: 6 12; -fx-background-color: #FF4444; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteReponse(rep));
        
        buttonBox.getChildren().addAll(editBtn, deleteBtn);
        
        card.getChildren().addAll(headerBox, contenu, translationLabel, buttonBox);
        return card;
    }

    private void openEditReponse(Reponse rep) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/editreponse.fxml"));
            Parent root = loader.load();

            EditReponseController c = loader.getController();
            c.setReponse(rep, r.getTitre());
            c.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteReponse(Reponse rep) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la réponse");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réponse ?");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                reponseService.delete(rep.getId());
                refreshReponses();
            }
        });
    }

    @FXML
    public void repondre() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reponseAdmin.fxml"));
            Parent root = loader.load();

            ReponseFormController c = loader.getController();
            c.setReclamation(r);
            c.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void modifier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modstatus.fxml"));
            Parent root = loader.load();

            ModifierStatusController c = loader.getController();
            c.setData(r);
            c.setHomeController(homeController);
            c.setParentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void supprimer() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la réclamation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                service.delete(r.getId());
                if (homeController != null) {
                    homeController.refresh();
                }
                onBack();
            }
        });
    }

    @FXML
    public void onBack() {
        Stage stage = (Stage) titre.getScene().getWindow();
        stage.close();
    }

    @FXML public void translateToEnglish() { performTranslation("en", "ANGLAIS"); }
    @FXML public void translateToSpanish() { performTranslation("es", "ESPAGNOL"); }
    @FXML public void translateToGerman() { performTranslation("de", "ALLEMAND"); }
    @FXML public void translateToItalian() { performTranslation("it", "ITALIEN"); }
    @FXML public void translateToPortuguese() { performTranslation("pt", "PORTUGAIS"); }
    @FXML public void translateToJapanese() { performTranslation("ja", "JAPONAIS"); }
    @FXML public void translateToChinese() { performTranslation("zh-CN", "CHINOIS"); }
    @FXML public void translateToArabic() { performTranslation("ar", "ARABE"); }
    @FXML public void translateToRussian() { performTranslation("ru", "RUSSE"); }

    @FXML public void closeTranslation() {
        translationContainer.setVisible(false);
        translationContainer.setManaged(false);
    }

    private void performTranslation(String targetLang, String langName) {
        if (r == null) return;
        
        translatedLangLabel.setText(langName);
        
        // Show loading state temporarily
        translatedTitre.setText("Traduction en cours...");
        translatedDescription.setText("Traduction en cours...");
        translationContainer.setVisible(true);
        translationContainer.setManaged(true);
        
        new Thread(() -> {
            String titreTraduit = translateText(r.getTitre(), targetLang);
            String descTraduit = translateText(r.getDescription(), targetLang);
            
            javafx.application.Platform.runLater(() -> {
                translatedTitre.setText(titreTraduit);
                translatedDescription.setText(descTraduit);
            });
        }).start();
    }

    private String translateText(String text, String targetLang) {
        if (text == null || text.trim().isEmpty()) return "";
        try {
            // taduction
            String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + targetLang + "&dt=t&q=" + java.net.URLEncoder.encode(text, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            return extractTranslatedText(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de traduction";
        }
    }

    private String extractTranslatedText(String json) {
        StringBuilder result = new StringBuilder();
        try {
            int arrayStart = json.indexOf("[[[");
            if (arrayStart == -1) return json;
            
            int arrayEnd = json.indexOf("]],");
            if (arrayEnd == -1) return json;
            
            String arrayContent = json.substring(arrayStart + 2, arrayEnd + 1);
            
            String[] parts = arrayContent.split("\\],\\[");
            for (String part : parts) {
                if (part.startsWith("[")) part = part.substring(1);
                
                boolean inString = false;
                boolean escape = false;
                StringBuilder str = new StringBuilder();
                for (int i = 0; i < part.length(); i++) {
                    char c = part.charAt(i);
                    if (escape) {
                        if (c == 'n') str.append('\n');
                        else if (c == 'r') str.append('\r');
                        else if (c == 't') str.append('\t');
                        else str.append(c);
                        escape = false;
                    } else if (c == '\\') {
                        escape = true;
                    } else if (c == '"') {
                        if (inString) {
                            break; 
                        } else {
                            inString = true; 
                        }
                    } else if (inString) {
                        str.append(c);
                    }
                }
                result.append(str.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}