package controllers;

import models.Reclamation;
import models.Reponse;
import services.ReponseService;
import services.ReclamationService;
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
    private final ReclamationService reclamationService = new ReclamationService();
    private Reclamation currentReclamation;
    private HomeReclamationController homeController;

    public void setData(Reclamation r) {
        this.currentReclamation = r;
        lblTitre.setText(r.getTitre());
        lblDate.setText("📅 Créée le " + r.getDateCreation());
        lblStatut.setText(r.getStatut());
        lblStatut.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: "
                + getStatusColor(r.getStatut()) + "; -fx-padding: 5 10; -fx-border-radius: 3;");
        txtDescription.setText(r.getDescription());
        loadReponses(r.getId());
    }

    private String getStatusColor(String statut) {
        switch (statut.toUpperCase()) {
            case "EN ATTENTE": return "#FFA500";
            case "RÉSOLUE": return "#28a745";
            case "EN COURS": return "#17a2b8";
            default: return "#999999";
        }
    }

    private void loadReponses(int reclamationId) {
        reponseContainer.getChildren().clear();
        List<Reponse> list = service.getByReclamationId(reclamationId);
        if (list == null || list.isEmpty()) {
            Label empty = new Label("Il n'y a pas de réponse");
            empty.setStyle("-fx-font-size: 12; -fx-text-fill: #999999; -fx-padding: 20;");
            reponseContainer.getChildren().add(empty);
            lblReponseCount.setText("(0)");
            return;
        }
        lblReponseCount.setText("(" + list.size() + ")");
        for (Reponse rep : list) {
            reponseContainer.getChildren().add(createReponseCard(rep));
        }
    }

    private VBox createReponseCard(Reponse rep) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10; -fx-border-radius: 10;" +
                "-fx-border-color: #e2ece8 #e2ece8 #e2ece8 #0057A8; -fx-border-width: 1 1 1 5;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,80,40,0.05), 8, 0, 0, 2);");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label date = new Label("📅 Envoyée le " + rep.getDateReponse());
        date.setStyle("-fx-font-size: 12; -fx-text-fill: #90a8a0;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MenuButton translateMenu = new MenuButton("Traduire 🌐");
        translateMenu.getStyleClass().add("btn-header-action");
        translateMenu.setStyle("-fx-font-size: 11; -fx-padding: 4 10;");

        String[][] languages = {
            {"English", "en"}, {"Español", "es"}, {"Deutsch", "de"},
            {"Italiano", "it"}, {"Português", "pt"}, {"日本語", "ja"},
            {"中文", "zh-CN"}, {"العربية", "ar"}, {"Русский", "ru"}
        };

        Label translationLabel = new Label();
        translationLabel.setWrapText(true);
        translationLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #0d2b1e; -fx-background-color: #f0f9f5; -fx-padding: 12;");
        translationLabel.setVisible(false);
        translationLabel.setManaged(false);

        for (String[] lang : languages) {
            MenuItem item = new MenuItem(lang[0]);
            item.setOnAction(e -> {
                translationLabel.setText("⌛ Traduction en cours...");
                translationLabel.setVisible(true);
                translationLabel.setManaged(true);
                new Thread(() -> {
                    String translated = translateText(rep.getContenu(), lang[1]);
                    javafx.application.Platform.runLater(() -> translationLabel.setText("🌐 " + lang[0] + " :\n" + translated));
                }).start();
            });
            translateMenu.getItems().add(item);
        }

        headerBox.getChildren().addAll(date, spacer, translateMenu);
        Label contenu = new Label(rep.getContenu());
        contenu.setWrapText(true);
        contenu.setStyle("-fx-font-size: 14; -fx-text-fill: #2d4a3e;");
        card.getChildren().addAll(headerBox, contenu, translationLabel);
        return card;
    }

    @FXML
    private void onBack() {
        ((Stage) lblTitre.getScene().getWindow()).close();
    }

    public void setHomeController(HomeReclamationController c) {
        this.homeController = c;
    }

    @FXML
    private void onEdit() {
        if (currentReclamation == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reclamation/edit.fxml"));
            Parent root = loader.load();
            EditReclamationController c = loader.getController();
            c.setData(currentReclamation);
            if (homeController != null) c.setHomeController(homeController);
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 800, 650));
            stage.showAndWait();
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
                if (homeController != null) homeController.load();
                onBack();
            }
        });
    }

    private String translateText(String text, String targetLang) {
        if (text == null || text.trim().isEmpty()) return "";
        try {
            String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + targetLang
                    + "&dt=t&q=" + java.net.URLEncoder.encode(text, "UTF-8");
            java.net.URL url = new java.net.URL(urlStr);
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(con.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) response.append(inputLine);
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
                boolean inString = false, escape = false;
                StringBuilder str = new StringBuilder();
                for (int i = 0; i < part.length(); i++) {
                    char c = part.charAt(i);
                    if (escape) {
                        if (c == 'n') str.append('\n');
                        else if (c == 'r') str.append('\r');
                        else if (c == 't') str.append('\t');
                        else str.append(c);
                        escape = false;
                    } else if (c == '\\') { escape = true; }
                    else if (c == '"') { if (inString) break; else inString = true; }
                    else if (inString) str.append(c);
                }
                result.append(str.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
