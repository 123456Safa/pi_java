package controllers;

import Model.Reclamation;
import Model.Reponse;
import Service.ReponseService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.util.List;

public class DetailReclamationController {

    @FXML private Label lblTitre;
    @FXML private Label lblDate;
    @FXML private Label lblStatut;
    @FXML private TextArea txtDescription;
    @FXML private Label lblReponseCount;

    @FXML private VBox reponseContainer;
    @FXML private Button btnEditer;
    @FXML private Button btnSupprimer;

    private final ReponseService service = new ReponseService();
    private final Service.ReclamationService reclamationService = new Service.ReclamationService();
    private Reclamation currentReclamation;
    private HomeReclamationController homeController;

    public void setData(Reclamation r) {
        this.currentReclamation = r;

        lblTitre.setText(r.getTitre());
        lblDate.setText("📅 Créée le " + r.getDateCreation());
        lblStatut.setText(r.getStatut());
        
        // Style mta3 status
        String statusColor = getStatusColor(r.getStatut());
        lblStatut.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 5 10; -fx-border-radius: 3;");
        
        txtDescription.setText(r.getDescription());

        loadReponses(r.getId());
    }

    private String getStatusColor(String statut) {
        switch (statut.toUpperCase()) {
            case "EN ATTENTE":
                return "#FFA500";
            case "RÉSOLUE":
                return "#28a745";

            case "EN COURS":
                return "#17a2b8";
            default:
                return "#999999";
        }
    }
//reponse 7atinahoum fi vbox
    private void loadReponses(int reclamationId) {

        reponseContainer.getChildren().clear();

        List<Reponse> list = service.getByReclamationId(reclamationId);

        if (list == null || list.isEmpty()) {
            Label empty = new Label("Il y a pas de réponse");
            empty.setStyle("-fx-font-size: 12; -fx-text-fill: #999999; -fx-padding: 20;");
            reponseContainer.getChildren().add(empty);
            lblReponseCount.setText("(0)");
            return;
        }

        lblReponseCount.setText("(" + list.size() + ")");

        for (Reponse rep : list) {
            VBox reponseCard = createReponseCard(rep);
            reponseContainer.getChildren().add(reponseCard);
        }
    }

    private VBox createReponseCard(Reponse rep) {
        VBox card = new VBox(8);
        card.setStyle("-fx-border-color: transparent transparent transparent #28a745; -fx-border-width: 0 0 0 4; -fx-background-color: #f9f9f9; -fx-padding: 15;");
        
        // Header with date and translation button
        HBox headerBox = new HBox(10);
        headerBox.setStyle("-fx-alignment: center-left;");
        
        Label date = new Label("📅 " + rep.getDateReponse());
        date.setStyle("-fx-font-size: 11; -fx-text-fill: #999999;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        MenuButton translateMenu = new MenuButton("🌐");
        translateMenu.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 12; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 5 10;");
        
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
        
        headerBox.getChildren().addAll(date, spacer, translateMenu);
        
        // Content
        Label contenu = new Label(rep.getContenu());
        contenu.setWrapText(true);
        contenu.setStyle("-fx-font-size: 12; -fx-text-fill: #555555;");
        
        card.getChildren().addAll(headerBox, contenu, translationLabel);
        return card;
    }

    @FXML
    private void onBack() {
        Stage stage = (Stage) lblTitre.getScene().getWindow();
        stage.close();
    }

    public void setHomeController(HomeReclamationController c) {
        this.homeController = c;
    }

    @FXML
    private void onEdit() {
        if (currentReclamation == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/EditReclamation.fxml"));
            Parent root = loader.load();

            EditReclamationController c = loader.getController();
            c.setData(currentReclamation);
            if (homeController != null) {
                c.setHomeController(homeController);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait(); // Wait for the edit window to close
            
            // Refresh  UI
            setData(currentReclamation);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDelete() {
        if (currentReclamation == null) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la réclamation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette réclamation ?");

        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                reclamationService.delete(currentReclamation.getId());
                if (homeController != null) {
                    homeController.load();
                }
                onBack();
            }
        });
    }

    private String translateText(String text, String targetLang) {
        if (text == null || text.trim().isEmpty()) return "";
        try {
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
                        if (inString) break;
                        else inString = true;
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