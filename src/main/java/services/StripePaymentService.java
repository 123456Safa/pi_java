package services;

import models.CommandeConfirmation;

import java.awt.Desktop;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Service pour générer un lien de paiement via l'API Stripe (Stripe Checkout).
 * Sans dépendance externe (utilise java.net.http.HttpClient).
 */
public class StripePaymentService {

    // La clé est maintenant récupérée dynamiquement depuis api_config.properties
    private static final String STRIPE_SECRET_KEY = ConfigService.getProperty("stripe.secret_key", "sk_test_VOTRE_CLE_STRIPE_ICI");

    /**
     * Crée une session de paiement Stripe et ouvre le navigateur.
     * @param totalTtc Le montant total en DT
     * @param commandeId L'ID de la commande
     * @return true si le lien a été généré et ouvert, false sinon.
     */
    public static boolean openPaymentLink(double totalTtc, int commandeId) {
        try {
            // Stripe accepte les montants en centimes. (ex: 10.50 DT -> 1050)
            // On utilise "usd" ou "eur" car "tnd" nécessite des configurations spécifiques.
            int montantCentimes = (int) (totalTtc * 100);

            String formData = "success_url=https://www.google.com/search?q=Paiement+Reussi+Pharmax" +
                    "&cancel_url=https://www.google.com/search?q=Paiement+Annule+Pharmax" +
                    "&payment_method_types[0]=card" +
                    "&mode=payment" +
                    "&line_items[0][price_data][currency]=usd" +
                    "&line_items[0][price_data][product_data][name]=Commande+PHARMAX+%23" + commandeId +
                    "&line_items[0][price_data][unit_amount]=" + montantCentimes +
                    "&line_items[0][quantity]=1";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.stripe.com/v1/checkout/sessions"))
                    .header("Authorization", "Bearer " + STRIPE_SECRET_KEY)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData, StandardCharsets.UTF_8))
                    .build();

            System.out.println("⏳ Création du lien de paiement Stripe...");

            // --- MODE MOCK POUR LES TESTS SANS CLÉ API ---
            if (STRIPE_SECRET_KEY.contains("VOTRE_CLE_STRIPE_ICI")) {
                System.out.println("⚠️ Clé Stripe non configurée. Mode DÉMO activé.");
                String dummyUrl = "https://www.google.com/search?q=Simulation+Paiement+Stripe+Pharmax+" + commandeId;
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(dummyUrl));
                    return true;
                }
                return false;
            }
            // ---------------------------------------------

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Extraire l'URL du JSON sans librairie externe (Gson/Jackson)
                String json = response.body();
                String urlPattern = "\"url\": \"";
                int start = json.indexOf(urlPattern);
                if (start != -1) {
                    start += urlPattern.length();
                    int end = json.indexOf("\"", start);
                    String checkoutUrl = json.substring(start, end);

                    System.out.println("✅ Lien Stripe généré : " + checkoutUrl);
                    
                    // Ouvrir le navigateur par défaut
                    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                        Desktop.getDesktop().browse(new URI(checkoutUrl));
                        return true;
                    } else {
                        System.err.println("⚠️ Impossible d'ouvrir le navigateur automatiquement.");
                    }
                }
            } else {
                System.err.println("❌ Erreur API Stripe : " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ Exception Stripe : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
