package services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Service to handle backend verification of the reCAPTCHA v3 token.
 */
public class RecaptchaService {

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    // Replace YOUR_SECRET_KEY with your actual Google reCAPTCHA v3 Secret Key
    private static final String SECRET_KEY = "6LdYl84sAAAAAN83C8aAkXYNV8O3l2dearMdwkcM";

    public static class RecaptchaResult {
        private final boolean success;
        private final double score;
        private final String action;
        private final String errorMessage;

        public RecaptchaResult(boolean success, double score, String action, String errorMessage) {
            this.success = success;
            this.score = score;
            this.action = action;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public double getScore() { return score; }
        public String getAction() { return action; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Verifies the token received from the client-side reCAPTCHA execution.
     *
     * @param token The user response token provided by the reCAPTCHA client-side integration.
     * @return RecaptchaResult containing success status and score.
     */
    public RecaptchaResult verifyToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return new RecaptchaResult(false, 0.0, null, "Token is missing.");
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            // The parameters must be URL-encoded form data
            String requestBody = "secret=" + URLEncoder.encode(SECRET_KEY, StandardCharsets.UTF_8) +
                                 "&response=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(VERIFY_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse JSON response
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                boolean success = jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean();
                
                double score = 0.0;
                if (jsonResponse.has("score")) {
                    score = jsonResponse.get("score").getAsDouble();
                }

                String action = null;
                if (jsonResponse.has("action")) {
                    action = jsonResponse.get("action").getAsString();
                }

                if (success) {
                    return new RecaptchaResult(true, score, action, null);
                } else {
                    String errorMsg = "Verification failed.";
                    if (jsonResponse.has("error-codes")) {
                        errorMsg += " Errors: " + jsonResponse.get("error-codes").toString();
                    }
                    return new RecaptchaResult(false, score, action, errorMsg);
                }
            } else {
                return new RecaptchaResult(false, 0.0, null, "HTTP Error: " + response.statusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new RecaptchaResult(false, 0.0, null, "Exception during verification: " + e.getMessage());
        }
    }
}
