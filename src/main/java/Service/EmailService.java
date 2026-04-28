package Service;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class EmailService {

    private static final String API_KEY = "";
    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";

    private static final String FROM_EMAIL = "safabaalouch25@gmail.com";
    private static final String FROM_NAME = "Support Système";

    public static void sendReclamationResolvedEmail(
            String toEmail,
            String toName,
            String reclamationTitle
    ) {

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            HttpPost post = new HttpPost(API_URL);

            post.setHeader("Accept", "application/json");
            post.setHeader("Content-Type", "application/json; charset=UTF-8");
            post.setHeader("api-key", API_KEY);

            Map<String, Object> body = new LinkedHashMap<>();

            Map<String, String> sender = new LinkedHashMap<>();
            sender.put("email", FROM_EMAIL);
            sender.put("name", FROM_NAME);
            body.put("sender", sender);

            List<Map<String, String>> to = new ArrayList<>();
            Map<String, String> rec = new LinkedHashMap<>();
            rec.put("email", toEmail);
            rec.put("name", toName);
            to.add(rec);

            body.put("to", to);
            body.put("subject", "Réclamation Résolue ✓");

            String html =
                    "<html>" +
                            "<head><meta charset='UTF-8'></head>" +
                            "<body style='font-family:Arial'>" +
                            "<h2 style='color:green'>Réclamation Résolue ✓</h2>" +
                            "<p>Bonjour " + toName + ",</p>" +
                            "<p>Votre réclamation a été traitée avec succès.</p>" +
                            "<p><b>Titre:</b> " + reclamationTitle + "</p>" +
                            "<p>Merci,<br/>Support système</p>" +
                            "</body></html>";

            body.put("htmlContent", html);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(body);

            post.setEntity(new StringEntity(json, java.nio.charset.StandardCharsets.UTF_8));

            client.execute(post, response -> {
                int code = response.getCode();

                if (code >= 200 && code < 300) {
                    System.out.println("✅ Email envoyé à " + toEmail);
                } else {
                    System.out.println("❌ Erreur email: " + code);
                }
                return null;
            });

        } catch (Exception e) {
            System.err.println("❌ Email error: " + e.getMessage());
        }
    }
}