package services;

import models.Produit;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Random;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.temporal.ChronoUnit;

public class ProductWorkflowService {

    private static final String STRIPE_API_KEY = "sk_test_DUMMY_STRIPE_KEY_2";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public boolean isNearExpiry(Date expiryDate) {
        if (expiryDate == null) return false;

        LocalDate expiry;
        if (expiryDate instanceof java.sql.Date) {
            expiry = ((java.sql.Date) expiryDate).toLocalDate();
        } else {
            expiry = expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        LocalDate today = LocalDate.now();
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, expiry);
        return daysUntilExpiry >= 0 && daysUntilExpiry <= 30;
    }

    public void applyDiscount(Produit p) {
        double discount = 20.0;
        p.setDiscountPercentage(discount);
        double finalPrice = p.getPrix() * (1 - (discount / 100));
        p.setPrixFinal(finalPrice);
    }

    public String generatePromoCode(double discount) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("EXPIRE" + (int)discount + "-");
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public void sendCouponToStripe(String code, double percentOff) {
        System.out.println("Envoi du coupon à Stripe: " + code + " (" + percentOff + "%)");

        try {
            String requestBody = "id=" + code +
                               "&percent_off=" + (int)percentOff +
                               "&duration=once";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.stripe.com/v1/coupons"))
                    .header("Authorization", "Bearer " + STRIPE_API_KEY)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            System.out.println("Coupon Stripe créé: " + response.body());
                        } else {
                            System.err.println("Erreur Stripe (Code " + response.statusCode() + "): " + response.body());
                        }
                    })
                    .exceptionally(ex -> {
                        System.err.println("Erreur réseau Stripe: " + ex.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi à Stripe: " + e.getMessage());
        }
    }

    public void processProductWorkflow(Produit p) {
        p.setPrixFinal(p.getPrix());

        if (isNearExpiry(p.getDateExpiration())) {
            applyDiscount(p);
            String code = generatePromoCode(p.getDiscountPercentage());
            p.setPromoCode(code);
            sendCouponToStripe(code, p.getDiscountPercentage());
            System.out.println("Workflow Expiration: Promo appliquée " + code + " pour " + p.getNom());
        } else {
            p.setPromoCode(null);
            p.setDiscountPercentage(0);
            System.out.println("Workflow Normal: Pas de réduction pour " + p.getNom());
        }
    }
}
