package services;

import java.awt.Desktop;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Service Stripe Checkout - Intégration réelle via Checkout Session.
 */
public class    StripePaymentService {

    /**
     * Charge la clé Stripe en priorité depuis le fichier de configuration.
     */
    private static String getApiKey() {
        String key = ConfigService.getProperty("stripe.secret_key", "");
        
        // Si ConfigService ne trouve rien, on essaie de lire le fichier directement
        if (key == null || key.trim().isEmpty() || key.contains("VOTRE")) {
            try (InputStream in = StripePaymentService.class.getResourceAsStream("/api_config.properties")) {
                if (in != null) {
                    Properties props = new Properties();
                    props.load(in);
                    key = props.getProperty("stripe.secret_key", "");
                }
            } catch (Exception ignored) {}
        }
        
        if (key != null && key.startsWith("sk_")) {
            return key.trim();
        }

        return null;
    }

    /**
     * Crée une Stripe Checkout Session et ouvre le navigateur.
     * Retourne null si succès, sinon le message d'erreur.
     */
    public static String ouvrirCheckoutSession(double totalDt, int commandeId) {
        String apiKey = getApiKey();
        if (apiKey == null) {
            return "Clé API Stripe introuvable ou invalide.";
        }

        try {
            long montantCentimes = Math.round(totalDt * 100);

            String formData =
                param("mode", "payment") +
                "&" + param("line_items[0][quantity]", "1") +
                "&" + param("line_items[0][price_data][currency]", "eur") +
                "&" + param("line_items[0][price_data][unit_amount]", String.valueOf(montantCentimes)) +
                "&" + param("line_items[0][price_data][product_data][name]", "Commande Pharmax " + commandeId) +
                "&" + param("success_url", "https://example.com/success") +
                "&" + param("cancel_url", "https://example.com/cancel") +
                "&" + param("metadata[commande_id]", String.valueOf(commandeId));

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.stripe.com/v1/checkout/sessions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData, StandardCharsets.UTF_8))
                    .build();

            System.out.println("⏳ Appel API Stripe Checkout...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                String url = extraireUrl(body);
                if (url != null) {
                    url = url.replace("\\/", "/");
                    System.out.println("✅ URL Stripe : " + url);
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(url));
                        return null; // Succès
                    } else {
                        return "Impossible d'ouvrir le navigateur web.";
                    }
                }
                return "URL de paiement non trouvée.";
            } else {
                String message = extraireValeur(response.body(), "message");
                System.err.println("❌ Erreur Stripe (" + response.statusCode() + "): " + message);
                return message != null ? message : "Erreur " + response.statusCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur système : " + e.getMessage();
        }
    }

    private static String extraireUrl(String json) {
        String[] patterns = {"\"url\":\"", "\"url\": \""};
        for (String pattern : patterns) {
            int start = json.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                int end = json.indexOf("\"", start);
                if (end > start) return json.substring(start, end);
            }
        }
        return null;
    }

    private static String param(String key, String value) {
        return URLEncoder.encode(key, StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String extraireValeur(String json, String key) {
        String[] patterns = {"\"" + key + "\":\"", "\"" + key + "\": \""};
        for (String pattern : patterns) {
            int start = json.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                int end = json.indexOf("\"", start);
                if (end > start) return json.substring(start, end);
            }
        }
        return null;
    }
}
