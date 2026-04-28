/*
 * mais pourquoi ne travaille pas corriger package controllers;
 *
 * import Model.Reclamation;
 * import Service.ReclamationService;
 * import javafx.fxml.FXML;
 * import javafx.scene.control.*;
 * import javafx.stage.Stage;
 *
 * import java.util.Date;
 * import java.net.URL;
 * import java.net.HttpURLConnection;
 *
 * public class FormReclamationController {
 *
 * @FXML private TextField titre;
 *
 * @FXML private TextArea description;
 *
 * @FXML private Label titreError;
 *
 * @FXML private Label descError;
 *
 * @FXML private Label titreCharCount;
 *
 * @FXML private Label descCharCount;
 *
 * private HomeReclamationController homeController;
 * private final ReclamationService service = new ReclamationService();
 *
 * @FXML
 * public void initialize() {
 * // Add character counter listeners
 * titre.textProperty().addListener((obs, oldVal, newVal) -> {
 * if (titreCharCount != null) {
 * titreCharCount.setText(newVal.length() + "/255");
 * }
 * });
 *
 * description.textProperty().addListener((obs, oldVal, newVal) -> {
 * if (descCharCount != null) {
 * descCharCount.setText(newVal.length() + "/2000");
 * }
 * });
 * }
 *
 * public void setHomeController(HomeReclamationController c) {
 * this.homeController = c;
 * }
 *
 * @FXML
 * public void save() {
 *
 * boolean valid = true;
 *
 * titreError.setText("");
 * descError.setText("");
 *
 * String t = titre.getText();
 * String d = description.getText();
 *
 * if (t == null || t.length() < 5) {
 * titreError.setText("Min 5 caractères");
 * valid = false;
 * } else if (t.length() > 255) {
 * titreError.setText("Max 255 caractères");
 * valid = false;
 * }
 *
 * if (d == null || d.length() < 20) {
 * descError.setText("Min 20 caractères");
 * valid = false;
 * } else if (d.length() > 2000) {
 * descError.setText("Max 2000 caractères");
 * valid = false;
 * }
 *
 * if (!valid) return;
 *
 * // Show loading state during API check
 * titreError.setText("Vérification...");
 * titreError.setStyle("-fx-text-fill: #17a2b8;");
 * descError.setText("Vérification...");
 * descError.setStyle("-fx-text-fill: #17a2b8;");
 *
 * new Thread(() -> {
 * boolean titreInapp = isTextInappropriate(t);
 * boolean descInapp = isTextInappropriate(d);
 *
 * javafx.application.Platform.runLater(() -> {
 * boolean allGood = true;
 *
 * if (titreInapp) {
 * titreError.setText("Le titre contient des mots inappropriés.");
 * titreError.setStyle("-fx-text-fill: red;");
 * allGood = false;
 * } else {
 * titreError.setText("");
 * }
 *
 * if (descInapp) {
 * descError.setText("La description contient des mots inappropriés.");
 * descError.setStyle("-fx-text-fill: red;");
 * allGood = false;
 * } else {
 * descError.setText("");
 * }
 *
 * if (allGood) {
 * Reclamation r = new Reclamation();
 * r.setTitre(t);
 * r.setDescription(d);
 * r.setDateCreation(new java.sql.Date(System.currentTimeMillis()));
 * r.setStatut("EN ATTENTE");
 *
 * service.add(r);
 *
 * if (homeController != null) {
 * homeController.load();
 * }
 *
 * ((Stage) titre.getScene().getWindow()).close();
 * }
 * });
 * }).start();
 * }
 *
 * private boolean isTextInappropriate(String text) {
 * if (text == null || text.trim().isEmpty()) return false;
 * try {
 * URL url = new
 * URL("https://router.huggingface.co/hf-inference/models/unitary/toxic-bert");
 * HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 * conn.setRequestMethod("POST");
 * conn.setRequestProperty("Authorization",
 * "");
 * conn.setRequestProperty("Content-Type", "application/json");
 * conn.setDoOutput(true);
 *
 * String escapedText = text.replace("\"", "\\\"").replace("\n",
 * " ").replace("\r", "");
 * String jsonInputString = "{\"inputs\": \"" + escapedText + "\"}";
 *
 * try (java.io.OutputStream os = conn.getOutputStream()) {
 * byte[] input = jsonInputString.getBytes("utf-8");
 * os.write(input, 0, input.length);
 * }
 *
 * int responseCode = conn.getResponseCode();
 * if (responseCode == 200) {
 * try (java.io.BufferedReader br = new java.io.BufferedReader(new
 * java.io.InputStreamReader(conn.getInputStream(), "utf-8"))) {
 * StringBuilder response = new StringBuilder();
 * String responseLine;
 * while ((responseLine = br.readLine()) != null) {
 * response.append(responseLine.trim());
 * }
 * String json = response.toString();
 *
 * String[] badLabels = {"toxic", "severe_toxic", "obscene", "threat", "insult",
 * "identity_hate"};
 * for (String label : badLabels) {
 * int idx = json.indexOf("\"label\":\"" + label + "\"");
 * if (idx != -1) {
 * int scoreIdx = json.indexOf("\"score\":", idx);
 * if (scoreIdx != -1) {
 * int endIdx = json.indexOf(",", scoreIdx);
 * if (endIdx == -1) endIdx = json.indexOf("}", scoreIdx);
 * if (endIdx != -1) {
 * String scoreStr = json.substring(scoreIdx + 8, endIdx);
 * double score = Double.parseDouble(scoreStr);
 * if (score > 0.5) return true;
 * }
 * }
 * }
 * }
 * }
 * } else if (responseCode == 503) {
 * System.out.
 * println("Hugging Face model is loading... Skipping validation to not block user."
 * );
 * return false;
 * }
 * } catch (Exception e) {
 * e.printStackTrace();
 * }
 * return false;
 * }
 *
 * @FXML
 * public void cancel() {
 * ((Stage) titre.getScene().getWindow()).close();
 * }
 * }
 */

package controllers;

import Model.Reclamation;
import Service.ReclamationService;
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

    @FXML
    private TextField titre;
    @FXML
    private TextArea description;
    @FXML
    private Label titreError;
    @FXML
    private Label descError;
    @FXML
    private Label titreCharCount;
    @FXML
    private Label descCharCount;

    private HomeReclamationController homeController;
    private final ReclamationService service = new ReclamationService();

    @FXML
    public void initialize() {
        if (titre != null && titreCharCount != null) {
            titre.textProperty().addListener((obs, oldVal, newVal) -> {
                titreCharCount.setText(newVal.length() + "/255");
            });
        }
        if (description != null && descCharCount != null) {
            description.textProperty().addListener((obs, oldVal, newVal) -> {
                descCharCount.setText(newVal.length() + "/2000");
            });
        }
    }

    public void setHomeController(HomeReclamationController c) {
        this.homeController = c;
    }

    @FXML
    public void save() {

        String t = titre.getText();
        String d = description.getText();

        // ================= VALIDATION SIMPLE =================
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

        // ================= THREAD =================
        new Thread(() -> {

            // ✔ CHECK DOUBLON (dans les 2 derniers jours)
            boolean isDuplicate = isDuplicateReclamation(t, d);

            if (isDuplicate) {
                Platform.runLater(() -> {
                    titreError.setText("");
                    descError.setText("⚠️ Une réclamation avec ce titre et cette description a déjà été créée depuis moins de 2 jours. Veuillez attendre ou modifier votre réclamation.");
                    descError.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
                });
                return;
            }

            // ✔ CHECK INDIVIDUEL
            boolean titreBad = isTextInappropriate(t);
            boolean descBad = isTextInappropriate(d);

            Platform.runLater(() -> {

                if (titreBad || descBad) {
                    titreError.setText("");
                    descError.setText("");
                    String msg = "Votre réclamation contient des mots critiques, veuillez les modifier s'il vous plaît.";

                    if (descBad) {
                        descError.setText(msg);
                        descError.setStyle("-fx-text-fill: red;");
                    } else if (titreBad) {
                        titreError.setText(msg);
                        titreError.setStyle("-fx-text-fill: red;");
                    }
                    return;
                }

                // ================= SAVE DB =================
                Reclamation r = new Reclamation();
                r.setTitre(t);
                r.setDescription(d);
                r.setDateCreation(new java.sql.Date(System.currentTimeMillis()));
                r.setStatut("EN ATTENTE");

                service.add(r);

                if (homeController != null) {
                    homeController.load();
                }

                ((Stage) titre.getScene().getWindow()).close();
            });

        }).start();
    }

    // ================= VÉRIFICATION DOUBLON =================
    private boolean isDuplicateReclamation(String titre, String description) {
        try {
            List<Reclamation> allReclamations = service.getAll();
            long twoDaysInMillis = 2L * 24 * 60 * 60 * 1000; // 2 jours en millisecondes
            long currentTime = System.currentTimeMillis();

            for (Reclamation r : allReclamations) {
                // Comparer titre ET description (case-insensitive)
                if (r.getTitre().equalsIgnoreCase(titre.trim()) && 
                    r.getDescription().equalsIgnoreCase(description.trim())) {
                    
                    // Vérifier si la réclamation a été créée depuis moins de 2 jours
                    long reclamationAge = currentTime - r.getDateCreation().getTime();
                    if (reclamationAge < twoDaysInMillis) {
                        return true; // Doublon trouvé dans les 2 derniers jours
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ================= IA  =================
    private boolean isTextInappropriate(String text) {

        try {
            URL url = new URL("https://router.huggingface.co/hf-inference/models/unitary/toxic-bert");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            // 🔐 حط التوكن متاعك هنا
            String token = ""; // <-- حط التوكن الصحيح

            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json"); // ✅ الحل هنا
            conn.setDoOutput(true);

            // ✅ JSON صحيح باستعمال ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode json = mapper.createObjectNode();
            json.put("inputs", text);

            String body = mapper.writeValueAsString(json);

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int code = conn.getResponseCode();

            // ✅ Debug (مهم)
            BufferedReader br;
            if (code >= 200 && code < 300) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            System.out.println("Response: " + sb.toString());

            if (code != 200) {
                System.out.println("API error: " + code);
                return false;
            }

            JsonNode root = mapper.readTree(sb.toString());

            // ================= SAFE CHECK =================
            if (root.isArray()) {
                for (JsonNode arr : root) {
                    if (arr.isArray()) {
                        for (JsonNode obj : arr) {

                            String label = obj.has("label") ? obj.get("label").asText() : "";
                            double score = obj.has("score") ? obj.get("score").asDouble() : 0;

                            if (score > 0.95 &&
                                    (label.equalsIgnoreCase("severe_toxic")
                                            || label.equalsIgnoreCase("insult")
                                            || label.equalsIgnoreCase("threat")))
                                             {
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


