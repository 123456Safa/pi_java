package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.*;

public class GeminiService {

    // IMPORTANT: Clé API configurée
    private static final String API_KEY = "AIzaSyDkVnUGUXSUXCDA5TjBETxhFvDJHjlDnRk";

    // Liste de modèles avec fallback (si le premier est surchargé, on teste le suivant)
    // NB: On utilise les versions 2.x car la 1.5 a été définitivement supprimée par Google
    private static final String[] MODELS = {
        "gemini-2.0-flash", // Souvent le plus stable et disponible
        "gemini-2.5-flash",
        "gemini-flash-latest"
    };

    public static String generateDescription(String productName) {
        int maxRetries = 2; // Nombre de tentatives par modèle

        for (String model : MODELS) {
            for (int i = 0; i < maxRetries; i++) {
                try {
                    System.out.println("Tentative avec le modèle : " + model + " (Essai " + (i+1) + "/" + maxRetries + ")");
                    String response = callAPI(productName, model);

                    // Si on a un succès ou une erreur différente de 503/429
                    if (!response.contains("\"code\": 503") && !response.contains("\"code\": 429")) {
                        if (response.startsWith("Erreur API") && response.contains("NOT_FOUND")) {
                            System.out.println("Modèle " + model + " introuvable. Passage au suivant...");
                            break; // On sort de la boucle de retry pour ce modèle, on passe au modèle suivant
                        }
                        return response;
                    }

                    // Si le serveur est surchargé (503) ou trop de requêtes (429)
                    System.out.println("Surcharge (503/429) sur " + model + ". Attente avant réessai...");
                    Thread.sleep(2000); // attendre 2 secondes avant de réessayer

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // Si tous les modèles et toutes les tentatives ont échoué
        return "Erreur: Service temporairement indisponible (serveurs Google surchargés). Veuillez réessayer dans quelques instants.";
    }

    private static String callAPI(String productName, String model) throws Exception {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + API_KEY;

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Échapper les guillemets dans le nom du produit
        String safeProductName = productName.replace("\"", "\\\"");
        String prompt = "Donne description + utilisation + précautions pour : " + safeProductName;

        String jsonInput = "{\n" +
                "  \"contents\": [{\n" +
                "    \"parts\": [{\"text\": \"" + prompt + "\"}]\n" +
                "  }]\n" +
                "}";

        OutputStream os = conn.getOutputStream();
        os.write(jsonInput.getBytes("UTF-8"));
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();
        
        java.io.InputStream inputStream;
        if (responseCode >= 200 && responseCode < 300) {
            inputStream = conn.getInputStream();
        } else {
            inputStream = conn.getErrorStream();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String output;
        StringBuilder response = new StringBuilder();

        while ((output = br.readLine()) != null) {
            response.append(output);
        }
        br.close();

        if (responseCode >= 200 && responseCode < 300) {
            System.out.println("Réponse " + model + ": Succès !");
            return response.toString(); // JSON de succès
        } else {
            System.err.println("Erreur API " + model + " (Code " + responseCode + ") : " + response.toString());
            return "Erreur API: " + response.toString();
        }
    }

    public static String extractText(String json) {
        if (json == null || json.contains("Erreur API") || json.startsWith("Erreur:")) {
            return json != null ? json : "Erreur: Impossible de contacter le serveur.";
        }
        try {
            JSONObject obj = new JSONObject(json);
            return obj.getJSONArray("candidates")
                      .getJSONObject(0)
                      .getJSONObject("content")
                      .getJSONArray("parts")
                      .getJSONObject(0)
                      .getString("text");
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de l'extraction du texte de la réponse.";
        }
    }
}
