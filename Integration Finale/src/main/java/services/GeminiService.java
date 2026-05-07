package services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.*;

public class GeminiService {

    private static final String API_KEY = "AIzaSyBMLoLSPbLXfZJjZromI1qTgPYm49plyoo";

    private static final String[] MODELS = {
        "gemini-2.0-flash",
        "gemini-2.5-flash",
        "gemini-flash-latest"
    };

    public static String generateDescription(String productName) {
        int maxRetries = 2;

        for (String model : MODELS) {
            for (int i = 0; i < maxRetries; i++) {
                try {
                    System.out.println("Tentative avec le modèle : " + model + " (Essai " + (i+1) + "/" + maxRetries + ")");
                    String response = callAPI(productName, model);

                    if (!response.contains("\"code\": 503") && !response.contains("\"code\": 429")) {
                        if (response.startsWith("Erreur API") && response.contains("NOT_FOUND")) {
                            System.out.println("Modèle " + model + " introuvable. Passage au suivant...");
                            break;
                        }
                        return response;
                    }

                    System.out.println("Surcharge (503/429) sur " + model + ". Attente avant réessai...");
                    Thread.sleep(2000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return "Erreur: Service temporairement indisponible (serveurs Google surchargés). Veuillez réessayer dans quelques instants.";
    }

    private static String callAPI(String productName, String model) throws Exception {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + API_KEY;

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

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
            return response.toString();
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
