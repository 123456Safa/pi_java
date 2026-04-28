package service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service to interact with the openFDA API using GSON.
 */
public class OpenFDAService {

    private static final String BASE_URL = "https://api.fda.gov/drug/label.json";

    /**
     * Verifies if a product name exists in the FDA database using GSON.
     * 
     * @param productName The name of the product to verify.
     * @return true if the product is found (results field exists), false otherwise.
     */
    public static boolean isProductValid(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return false;
        }

        try {
            // Nettoyer le nom
            String cleanName = productName.trim().toLowerCase();
            
            // Recherche plus précise (brand_name ou generic_name)
            String query = "openfda.brand_name:\"" + cleanName + "\" OR openfda.generic_name:\"" + cleanName + "\"";
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            
            String urlString = BASE_URL + "?search=" + encodedQuery + "&limit=1";
            URL url = new URL(urlString);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                // Analyser la réponse avec GSON
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                
                // Si "results" existe -> médicament valide
                return jsonResponse.has("results") && jsonResponse.getAsJsonArray("results").size() > 0;
            } else {
                // 404 ou autre erreur -> invalide
                System.out.println("openFDA: Produit non trouvé ou erreur API (Code " + responseCode + ")");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Erreur openFDA: " + e.getMessage());
            return false;
        }
    }
}
