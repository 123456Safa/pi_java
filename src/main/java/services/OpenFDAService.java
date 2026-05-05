package services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OpenFDAService {

    private static final String BASE_URL = "https://api.fda.gov/drug/label.json";

    public static boolean isProductValid(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return false;
        }

        try {
            String cleanName = productName.trim().toLowerCase();
            // Remove common dosage indicators and numbers
            cleanName = cleanName.replaceAll("[0-9]+(?:mg|ml|g|mcg|ui|iu|%)?", "").trim();
            // Take only the first word (usually the brand or generic name) to maximize match probability
            if (cleanName.contains(" ")) {
                cleanName = cleanName.split("\\s+")[0];
            }
            
            String query = "openfda.brand_name:\"" + cleanName + "\" OR openfda.generic_name:\"" + cleanName + "\"";
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());

            String urlString = BASE_URL + "?search=" + encodedQuery + "&limit=1";
            URL url = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                return jsonResponse.has("results") && jsonResponse.getAsJsonArray("results").size() > 0;
            } else {
                System.out.println("openFDA: Produit non trouvé ou erreur API (Code " + responseCode + ")");
                return false;
            }

        } catch (Exception e) {
            System.err.println("Erreur openFDA: " + e.getMessage());
            return false;
        }
    }
}
