package com.pharmax.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pharmax.model.Article;
import okhttp3.*;

import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * MailjetService handles sending emails via the Mailjet v3.1 Send API.
 * It uses OkHttp to make non-blocking background calls.
 */
public class MailjetService {

    private static final Logger LOG = Logger.getLogger(MailjetService.class.getName());
    private static MailjetService instance;

    private static final String API_KEY = "ff45102e6e5eb03516c82e48686e25a2";
    private static final String SECRET_KEY = "9cef1bf4f44887cddad1df7f6f2a2d56";
    private static final String MAILJET_API_URL = "https://api.mailjet.com/v3.1/send";
    
    // WARNING: Mailjet requires the sender email to be verified in your account.
    // If "nayrouzdaikhi@gmail.com" is not your verified sender email, you must change it here.
    private static final String SENDER_EMAIL = "nayrouzdaikhi@gmail.com";
    private static final String SENDER_NAME = "PharmaX System";
    
    private static final String TARGET_EMAIL = "Daikhi.Nayrouz@esprit.tn";
    private static final String TARGET_NAME = "Daikhi Nayrouz";

    private final OkHttpClient client;

    private MailjetService() {
        this.client = new OkHttpClient();
    }

    public static synchronized MailjetService getInstance() {
        if (instance == null) {
            instance = new MailjetService();
        }
        return instance;
    }

    /**
     * Sends an email notification asynchronously when a new article is created.
     * @param article The newly created article.
     */
    public void sendNewArticleNotification(Article article) {
        new Thread(() -> {
            try {
                JsonObject message = new JsonObject();
                
                // From
                JsonObject from = new JsonObject();
                from.addProperty("Email", SENDER_EMAIL);
                from.addProperty("Name", SENDER_NAME);
                message.add("From", from);

                // To
                JsonArray toArray = new JsonArray();
                JsonObject to = new JsonObject();
                to.addProperty("Email", TARGET_EMAIL);
                to.addProperty("Name", TARGET_NAME);
                toArray.add(to);
                message.add("To", toArray);

                // Subject & Content
                message.addProperty("Subject", "recommendation article");
                
                String contentSnippet = article.getContenu() != null ? article.getContenu() : "";
                if (contentSnippet.length() > 150) {
                    contentSnippet = contentSnippet.substring(0, 150) + "...";
                }
                
                String textContent = "New PharmaX Recommendation:\n\n" +
                                     "Title: " + article.getTitre() + "\n\n" +
                                     "Content:\n" + contentSnippet + "\n\n" +
                                     "Login to PharmaX to read more!";
                                     
                String htmlContent = "<!DOCTYPE html>" +
                                     "<html>" +
                                     "<head>" +
                                     "<style>" +
                                     "  body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 20px; }" +
                                     "  .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }" +
                                     "  .header { background: linear-gradient(135deg, #2d8659 0%, #1f5e42 100%); color: white; padding: 30px; text-align: center; }" +
                                     "  .header h1 { margin: 0; font-size: 24px; font-weight: 600; letter-spacing: 1px; }" +
                                     "  .badge { display: inline-block; background-color: #e6f4ea; color: #1e8e3e; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; margin-top: 10px; }" +
                                     "  .content { padding: 40px 30px; }" +
                                     "  .title { color: #2c3e50; font-size: 22px; font-weight: 700; margin-top: 0; line-height: 1.4; }" +
                                     "  .snippet { color: #5f6368; font-size: 15px; line-height: 1.6; background-color: #f8f9fa; border-left: 4px solid #2d8659; padding: 15px 20px; border-radius: 0 8px 8px 0; margin: 25px 0; font-style: italic; }" +
                                     "  .cta-container { text-align: center; margin-top: 35px; }" +
                                     "  .btn { display: inline-block; background-color: #2d8659; color: #ffffff !important; text-decoration: none; padding: 14px 32px; border-radius: 30px; font-weight: bold; font-size: 15px; transition: background-color 0.3s; }" +
                                     "  .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #9aa0a6; font-size: 12px; border-top: 1px solid #e8eaed; }" +
                                     "</style>" +
                                     "</head>" +
                                     "<body>" +
                                     "  <div class='email-container'>" +
                                     "    <div class='header'>" +
                                     "      <h1>⚕️ PharmaX Insights</h1>" +
                                     "      <div class='badge'>New Recommendation</div>" +
                                     "    </div>" +
                                     "    <div class='content'>" +
                                     "      <h2 class='title'>" + article.getTitre() + "</h2>" +
                                     "      <p style='color: #80868b; font-size: 13px; margin-top: -10px;'>Published today by the PharmaX Medical Board</p>" +
                                     "      <div class='snippet'>\"" + contentSnippet.replace("\n", "<br>") + "\"</div>" +
                                     "      <p style='color: #3c4043; font-size: 15px; line-height: 1.6;'>We thought this article might be of great interest to you. It covers recent developments and critical information that aligns with your expertise.</p>" +
                                     "      <div class='cta-container'>" +
                                     "        <a href='#' class='btn'>📖 Read Full Article</a>" +
                                     "      </div>" +
                                     "    </div>" +
                                     "    <div class='footer'>" +
                                     "      <p>This is an automated recommendation from the PharmaX Blog System.</p>" +
                                     "      <p>&copy; 2026 PharmaX Inc. All rights reserved.</p>" +
                                     "    </div>" +
                                     "  </div>" +
                                     "</body>" +
                                     "</html>";

                message.addProperty("TextPart", textContent);
                message.addProperty("HTMLPart", htmlContent);

                JsonArray messagesArray = new JsonArray();
                messagesArray.add(message);

                JsonObject requestBodyJson = new JsonObject();
                requestBodyJson.add("Messages", messagesArray);

                RequestBody body = RequestBody.create(
                        requestBodyJson.toString(),
                        MediaType.parse("application/json; charset=utf-8")
                );

                String credentials = Credentials.basic(API_KEY, SECRET_KEY);

                Request request = new Request.Builder()
                        .url(MAILJET_API_URL)
                        .post(body)
                        .header("Authorization", credentials)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        LOG.info("✅ Mailjet notification sent successfully for article: " + article.getTitre());
                        // Optional: Show success message
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "✅ Email sent successfully to " + TARGET_EMAIL, ButtonType.OK);
                            alert.setTitle("Mailjet Success");
                            alert.setHeaderText("Email Sent");
                            alert.show();
                        });
                    } else {
                        String errorBody = response.body() != null ? response.body().string() : "No response body";
                        LOG.warning("⚠ Mailjet notification failed: HTTP " + response.code() + " - " + errorBody);
                        
                        // Show error alert
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Mailjet Error HTTP " + response.code() + "\n\n" + errorBody, ButtonType.OK);
                            alert.setTitle("Mailjet Error");
                            alert.setHeaderText("Failed to send email");
                            // Set alert to be resizable because Mailjet errors can be long JSONs
                            alert.getDialogPane().setExpanded(true);
                            alert.show();
                        });
                    }
                }

            } catch (IOException e) {
                LOG.log(Level.SEVERE, "❌ Failed to send Mailjet email.", e);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Network Error: " + e.getMessage(), ButtonType.OK);
                    alert.setTitle("Mailjet Error");
                    alert.setHeaderText("Failed to connect to Mailjet");
                    alert.show();
                });
            }
        }).start();
    }
}
