package controllers;

import models.Reclamation;
import services.ReclamationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class FormReclamationController {

    @FXML private TextField titre;
    @FXML private TextArea description;
    @FXML private Label titreError;
    @FXML private Label descError;
    @FXML private Label titreCharCount;
    @FXML private Label descCharCount;

    private HomeReclamationController homeController;
    private final ReclamationService service = new ReclamationService();

    @FXML
    public void initialize() {
        if (titre != null && titreCharCount != null) {
            titre.textProperty().addListener((obs, oldVal, newVal) -> titreCharCount.setText(newVal.length() + "/255"));
        }
        if (description != null && descCharCount != null) {
            description.textProperty().addListener((obs, oldVal, newVal) -> descCharCount.setText(newVal.length() + "/2000"));
        }
    }

    public void setHomeController(HomeReclamationController c) {
        this.homeController = c;
    }

    @FXML
    public void save() {
        String t = titre.getText();
        String d = description.getText();

        if (t == null || t.trim().length() < 5) {
            titreError.setText("Titre trop court");
            return;
        }
        if (d == null || d.trim().length() < 20) {
            descError.setText("Description trop courte");
            return;
        }

        titreError.setText("Vérification ...");
        descError.setText("");

        new Thread(() -> {
            boolean isDuplicate = isDuplicateReclamation(t, d);
            if (isDuplicate) {
                Platform.runLater(() -> {
                    titreError.setText("");
                    descError.setText("Une réclamation similaire a déjà été créée depuis moins de 2 jours.");
                    descError.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
                });
                return;
            }

            boolean titreBad = isTextInappropriate(t);
            boolean descBad = isTextInappropriate(d);

            Platform.runLater(() -> {
                if (titreBad || descBad) {
                    titreError.setText("");
                    descError.setText("");
                    String msg = "Votre réclamation contient des mots critiques, veuillez les modifier.";
                    if (descBad) {
                        descError.setText(msg);
                        descError.setStyle("-fx-text-fill: red;");
                    } else if (titreBad) {
                        titreError.setText(msg);
                        titreError.setStyle("-fx-text-fill: red;");
                    }
                    return;
                }

                Reclamation r = new Reclamation();
                r.setTitre(t);
                r.setDescription(d);
                r.setDateCreation(new java.sql.Date(System.currentTimeMillis()));
                r.setStatut("EN ATTENTE");

                // Attach the logged-in user's ID to the reclamation
                models.User currentUser = utils.SessionManager.getInstance().getCurrentUser();
                if (currentUser != null) {
                    r.setUserId(currentUser.getId());
                }

                service.add(r);

                if (homeController != null) homeController.load();
                ((Stage) titre.getScene().getWindow()).close();
            });
        }).start();
    }

    private boolean isDuplicateReclamation(String titre, String description) {
        try {
            List<Reclamation> allReclamations = service.getAll();
            long twoDaysInMillis = 2L * 24 * 60 * 60 * 1000;
            long currentTime = System.currentTimeMillis();
            for (Reclamation r : allReclamations) {
                if (r.getTitre().equalsIgnoreCase(titre.trim()) &&
                        r.getDescription().equalsIgnoreCase(description.trim())) {
                    if (currentTime - r.getDateCreation().getTime() < twoDaysInMillis) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isTextInappropriate(String text) {
        try {
            URL url = new URL("https://router.huggingface.co/hf-inference/models/unitary/toxic-bert");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer hf_DUMMY_HUGGINGFACE_KEY");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode json = mapper.createObjectNode();
            json.put("inputs", text);
            String body = mapper.writeValueAsString(json);

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int code = conn.getResponseCode();
            BufferedReader br = code >= 200 && code < 300 ?
                new BufferedReader(new InputStreamReader(conn.getInputStream())) :
                new BufferedReader(new InputStreamReader(conn.getErrorStream()));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);

            if (code != 200) return false;

            JsonNode root = mapper.readTree(sb.toString());
            if (root.isArray()) {
                for (JsonNode arr : root) {
                    if (arr.isArray()) {
                        for (JsonNode obj : arr) {
                            String label = obj.has("label") ? obj.get("label").asText() : "";
                            double score = obj.has("score") ? obj.get("score").asDouble() : 0;
                            if (score > 0.8 && (label.equalsIgnoreCase("severe_toxic")
                                    || label.equalsIgnoreCase("insult")
                                    || label.equalsIgnoreCase("threat"))) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @FXML
    public void cancel() {
        ((Stage) titre.getScene().getWindow()).close();
    }
}
