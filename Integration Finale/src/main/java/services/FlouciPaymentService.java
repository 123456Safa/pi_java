package services;

import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Service pour l'intégration de Flouci (Passerelle de paiement tunisienne).
 */
public class FlouciPaymentService {

    // Les identifiants sont maintenant récupérés dynamiquement depuis api_config.properties
    private static final String APP_TOKEN = ConfigService.getProperty("flouci.app_token", "VOTRE_PUBLIC_APP_TOKEN");
    private static final String APP_SECRET = ConfigService.getProperty("flouci.app_secret", "VOTRE_APP_SECRET");

    /**
     * Appelle l'API Flouci pour générer un jeton de paiement.
     * @return L'ID du paiement généré par Flouci, ou null en cas d'échec.
     */
    public static String genererPaiement(double montantDt, int commandeId) {
        try {
            int montantMillimes = (int) Math.round(montantDt * 1000);

            String jsonBody = String.format(
                "{\"app_token\": \"%s\", \"app_secret\": \"%s\", \"amount\": %d, " +
                "\"accept_card\": \"true\", \"session_timeout_secs\": 1200, " +
                "\"success_link\": \"https://google.com/search?q=Paiement+Reussi\", " +
                "\"fail_link\": \"https://google.com/search?q=Paiement+Echoue\", " +
                "\"developer_tracking_id\": \"%d\"}",
                APP_TOKEN, APP_SECRET, montantMillimes, commandeId
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://developers.flouci.com/api/generate_payment"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            if (APP_TOKEN.equals("VOTRE_PUBLIC_APP_TOKEN")) {
                System.out.println("⚠️ Simulation Flouci : Redirection vers le site officiel.");
                openBrowser("https://www.flouci.com"); // Évite le 404 en mode démo
                return "SIMULATION_ID";
            }

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();
                String link = extractJsonValue(body, "link");
                String paymentId = extractJsonValue(body, "payment_id");
                
                if (link != null) {
                    System.out.println("✅ Lien Flouci généré avec succès.");
                    openBrowser(link);
                    return paymentId;
                }
            } else {
                System.err.println("❌ Erreur API Flouci (Code " + response.statusCode() + ") : " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Vérifie si un paiement a été complété avec succès.
     * @param paymentId L'ID récupéré lors de la génération du paiement
     * @return true si le statut est 'SUCCESS'
     */
    public static boolean verifierStatutPaiement(String paymentId) {
        if ("SIMULATION_ID".equals(paymentId)) return true;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://developers.flouci.com/api/verify_payment/" + paymentId))
                    .header("app_token", APP_TOKEN)
                    .header("app_secret", APP_SECRET)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // La réponse contient un champ "result" -> "status"
                // On simplifie l'extraction pour l'exemple
                return response.body().contains("\"status\":\"SUCCESS\"");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Ouvre une URL dans le navigateur par défaut du système.
     */
    private static void openBrowser(String url) throws Exception {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
        } else {
            System.err.println("⚠️ Impossible d'ouvrir le navigateur. URL : " + url);
        }
    }

    /**
     * Utilitaire simple pour extraire une valeur d'un JSON sans librairie externe.
     */
    private static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}
