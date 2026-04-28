package controllers;

import Model.Reclamation;
import Service.ReclamationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EditReclamationController {

    @FXML private TextField titre;
    @FXML private TextArea description;

    @FXML private Label titreError;
    @FXML private Label descError;
    @FXML private Label titreCharCount;
    @FXML private Label descCharCount;
    @FXML private Label lblStatusActuel;

    private Reclamation r;
    private final ReclamationService service = new ReclamationService();
    private HomeReclamationController homeController;

    @FXML
    public void initialize() {
        // Add character counter listeners
        titre.textProperty().addListener((obs, oldVal, newVal) -> {
            if (titreCharCount != null) {
                titreCharCount.setText(newVal.length() + "/255");
            }
        });

        description.textProperty().addListener((obs, oldVal, newVal) -> {
            if (descCharCount != null) {
                descCharCount.setText(newVal.length() + "/2000");
            }
        });
    }

    public void setHomeController(HomeReclamationController c) {
        this.homeController = c;
    }

    public void setData(Reclamation r) {
        this.r = r;
        titre.setText(r.getTitre());
        description.setText(r.getDescription());
        
        // Update character counts
        if (titreCharCount != null) {
            titreCharCount.setText(r.getTitre().length() + "/255");
        }
        if (descCharCount != null) {
            descCharCount.setText(r.getDescription().length() + "/2000");
        }
        
        // Display current status
        if (lblStatusActuel != null) {
            lblStatusActuel.setText(r.getStatut());
            String statusColor = getStatusColor(r.getStatut());
            lblStatusActuel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + statusColor + "; -fx-padding: 5 10; -fx-border-radius: 3;");
        }
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

    @FXML
    public void save() {

        boolean valid = true;

        titreError.setText("");
        descError.setText("");

        String t = titre.getText();
        String d = description.getText();

        //  validation titre
        if (t == null || t.trim().length() < 5) {
            titreError.setText("Le titre doit contenir au moins 5 caractères");
            valid = false;
        } else if (t.length() > 255) {
            titreError.setText("Le titre ne doit pas dépasser 255 caractères");
            valid = false;
        }

        //  validation description
        if (d == null || d.trim().length() < 20) {
            descError.setText("La description doit contenir au moins 20 caractères");
            valid = false;
        } else if (d.length() > 2000) {
            descError.setText("La description ne doit pas dépasser 2000 caractères");
            valid = false;
        }

        if (!valid) return;

        // Show loading state during API check
        titreError.setText("Vérification...");
        titreError.setStyle("-fx-text-fill: #17a2b8;");
        descError.setText("Vérification...");
        descError.setStyle("-fx-text-fill: #17a2b8;");

        new Thread(() -> {
            // ✔ CHECK DOUBLON (dans les 2 derniers jours) - excepté la réclamation actuelle
            boolean isDuplicate = isDuplicateReclamation(t, d);

            if (isDuplicate) {
                Platform.runLater(() -> {
                    titreError.setText("");
                    descError.setText("⚠️ Une réclamation avec ce titre et cette description a déjà été créée depuis moins de 2 jours. Veuillez attendre ou modifier votre réclamation.");
                    descError.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
                });
                return;
            }

            // ✔ CHECK MOTS CRITIQUES
            boolean titreInapp = isTextInappropriate(t);
            boolean descInapp = isTextInappropriate(d);

            Platform.runLater(() -> {
                boolean allGood = true;
                
                if (titreInapp || descInapp) {
                    titreError.setText("");
                    descError.setText("");
                    String msg = "Votre réclamation contient des mots critiques, veuillez les modifier s'il vous plaît.";

                    if (descInapp) {
                        descError.setText(msg);
                        descError.setStyle("-fx-text-fill: red;");
                    } else if (titreInapp) {
                        titreError.setText(msg);
                        titreError.setStyle("-fx-text-fill: red;");
                    }
                    return;
                }

                // ✅ update DB
                r.setTitre(t);
                r.setDescription(d);
                service.update(r);

                // 🔥 refresh home
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

            for (Reclamation rec : allReclamations) {
                // Ne vérifier que les autres réclamations (pas celle-ci)
                if (rec.getId() == r.getId()) {
                    continue;
                }
                
                // Comparer titre ET description (case-insensitive)
                if (rec.getTitre().equalsIgnoreCase(titre.trim()) && 
                    rec.getDescription().equalsIgnoreCase(description.trim())) {
                    
                    // Vérifier si la réclamation a été créée depuis moins de 2 jours
                    long reclamationAge = currentTime - rec.getDateCreation().getTime();
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

    // =================  DÉTECTION MOTS CRITIQUES =================
    private boolean isTextInappropriate(String text) {

        try {

            URL url = new URL("https://router.huggingface.co/hf-inference/models/unitary/toxic-bert");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            // 🔐 Token d'authentification
            String token = "";

            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // ✅ JSON valide avec ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode json = mapper.createObjectNode();
            json.put("inputs", text);

            String body = mapper.writeValueAsString(json);

            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int code = conn.getResponseCode();

            // ✅ Lecture de la réponse
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

            // ================= VÉRIFICATION SÉCURISÉE =================
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