package services;

import models.CommandeConfirmation;
import models.LigneCommandes;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.*;
import java.util.*;
import java.util.Properties;

/**
 * Sends HTML confirmation emails via Gmail SMTP.
 *
 * Configuration: edit the file  email_config.properties  at the project root.
 * The file is created automatically with instructions on first run.
 */
public class EmailService {

    // ── Brevo API for reclamation emails ─────────────────────────────────────
    private static final String BREVO_API_KEY = "xkeysib-DUMMY_BREVO_API_KEY";
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String BREVO_FROM_EMAIL = "safabaalouch25@gmail.com";
    private static final String BREVO_FROM_NAME = "Support Système";

    public static void sendReclamationResolvedEmail(String toEmail, String toName, String reclamationTitle) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(BREVO_API_URL);
            post.setHeader("Accept", "application/json");
            post.setHeader("Content-Type", "application/json; charset=UTF-8");
            post.setHeader("api-key", BREVO_API_KEY);

            Map<String, Object> body = new LinkedHashMap<>();
            Map<String, String> sender = new LinkedHashMap<>();
            sender.put("email", BREVO_FROM_EMAIL);
            sender.put("name", BREVO_FROM_NAME);
            body.put("sender", sender);

            List<Map<String, String>> to = new ArrayList<>();
            Map<String, String> rec = new LinkedHashMap<>();
            rec.put("email", toEmail);
            rec.put("name", toName);
            to.add(rec);
            body.put("to", to);
            body.put("subject", "Réclamation Résolue ✓");

            String html = "<html><head><meta charset='UTF-8'></head><body style='font-family:Arial'>" +
                    "<h2 style='color:green'>Réclamation Résolue ✓</h2>" +
                    "<p>Bonjour " + toName + ",</p>" +
                    "<p>Votre réclamation a été traitée avec succès.</p>" +
                    "<p><b>Titre:</b> " + reclamationTitle + "</p>" +
                    "<p>Merci,<br/>Support système</p></body></html>";
            body.put("htmlContent", html);

            ObjectMapper mapper = new ObjectMapper();
            post.setEntity(new StringEntity(mapper.writeValueAsString(body), java.nio.charset.StandardCharsets.UTF_8));

            client.execute(post, response -> {
                int code = response.getCode();
                System.out.println(code >= 200 && code < 300 ? "Email réclamation envoyé à " + toEmail : "Erreur email réclamation: " + code);
                return null;
            });
        } catch (Exception e) {
            System.err.println("Erreur envoi email réclamation: " + e.getMessage());
        }
    }

    // ── Gmail SMTP for order emails ───────────────────────────────────────────
    private static final String CONFIG_FILE = "email_config.properties";

    // ── Load config ──────────────────────────────────────────────────────────
    private static Properties loadConfig() {
        Properties cfg = new Properties();
        File file = new File(CONFIG_FILE);

        if (!file.exists()) {
            // Create a template file so the user knows what to fill in
            try (PrintWriter w = new PrintWriter(file)) {
                w.println("# PHARMAX Email Configuration");
                w.println("# Replace the values below with your real Gmail credentials.");
                w.println("#");
                w.println("# How to get an App Password:");
                w.println("#   1. Go to https://myaccount.google.com/security");
                w.println("#   2. Enable 2-Step Verification");
                w.println("#   3. Go to https://myaccount.google.com/apppasswords");
                w.println("#   4. Create a new App Password for 'Mail'");
                w.println("#   5. Copy the 16-character code here (no spaces)");
                w.println();
                w.println("sender.email=VOTRE_EMAIL@gmail.com");
                w.println("sender.password=VOTRE_APP_PASSWORD");
            } catch (IOException ignored) {}

            System.err.println("⚠️  Fichier de configuration email créé : " + file.getAbsolutePath());
            System.err.println("    → Ouvrez ce fichier et renseignez votre email Gmail et votre App Password.");
        }

        try (InputStream in = new FileInputStream(file)) {
            cfg.load(in);
        } catch (IOException e) {
            System.err.println("❌ Impossible de lire " + CONFIG_FILE + " : " + e.getMessage());
        }
        return cfg;
    }

    // ── Public API ───────────────────────────────────────────────────────────
    /**
     * Sends an order confirmation email in a background thread (non-blocking).
     */
    public static void sendConfirmationAsync(CommandeConfirmation confirmation, String recipientEmail) {
        new Thread(() -> {
            try {
                // Forcer l'utilisation d'IPv4 (règle les problèmes de timeout fréquents avec Gmail)
                System.setProperty("java.net.preferIPv4Stack", "true");
                
                Properties cfg = loadConfig();
                String email    = cfg.getProperty("sender.email",    "").trim();
                String password = cfg.getProperty("sender.password", "").trim();

                if (email.isBlank() || email.equals("VOTRE_EMAIL@gmail.com")) {
                    System.err.println("⚠️  Email non configuré. Éditez le fichier : " + new File(CONFIG_FILE).getAbsolutePath());
                    return;
                }
                if (password.isBlank() || password.equals("VOTRE_APP_PASSWORD")) {
                    System.err.println("⚠️  App Password non configuré. Éditez le fichier : " + new File(CONFIG_FILE).getAbsolutePath());
                    return;
                }

                sendConfirmation(confirmation, recipientEmail, email, password);
                System.out.println("✅ Email envoyé avec succès à : " + recipientEmail);

            } catch (AuthenticationFailedException e) {
                System.err.println("❌ Authentification Gmail échouée.");
                System.err.println("   → Vérifiez l'email et l'App Password dans : " + new File(CONFIG_FILE).getAbsolutePath());
                System.err.println("   → Détail : " + e.getMessage());
            } catch (MessagingException e) {
                System.err.println("❌ Erreur d'envoi email : " + e.getMessage());
                if (e.getCause() != null) System.err.println("   Cause : " + e.getCause().getMessage());
            } catch (Exception e) {
                System.err.println("❌ Erreur inattendue lors de l'envoi email : " + e.getMessage());
                e.printStackTrace();
            }
        }, "EmailSender").start();
    }

    // ── Core send logic ──────────────────────────────────────────────────────
    private static void sendConfirmation(CommandeConfirmation confirmation,
                                         String recipientEmail,
                                         String senderEmail,
                                         String appPassword)
            throws MessagingException, UnsupportedEncodingException {

        Properties props = new Properties();
        props.put("mail.smtp.auth",                "true");
        props.put("mail.smtp.ssl.enable",          "true");
        props.put("mail.smtp.host",                "smtp.gmail.com");
        props.put("mail.smtp.port",                "465");
        props.put("mail.smtp.ssl.trust",           "smtp.gmail.com");
        props.put("mail.smtp.connectiontimeout",   "10000");
        props.put("mail.smtp.timeout",             "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, appPassword);
            }
        });

        // Uncomment to debug SMTP conversation:
        // session.setDebug(true);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail, "PHARMAX", "UTF-8"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject("Votre commande PHARMAX #" + confirmation.getCommandeId() + " a été confirmée ✓", "UTF-8");
        message.setContent(buildHtmlBody(confirmation), "text/html; charset=UTF-8");

        Transport.send(message);
    }

    // ── HTML body ────────────────────────────────────────────────────────────
    private static String buildHtmlBody(CommandeConfirmation c) {
        StringBuilder rows = new StringBuilder();
        for (LigneCommandes l : c.getLignes()) {
            rows.append(String.format("""
                <tr>
                    <td style="padding:10px 14px;border-bottom:1px solid #f1f5f9;font-size:14px;color:#334155;">%s</td>
                    <td style="padding:10px 14px;border-bottom:1px solid #f1f5f9;text-align:center;font-size:14px;color:#64748b;">%d</td>
                    <td style="padding:10px 14px;border-bottom:1px solid #f1f5f9;text-align:right;font-size:14px;color:#64748b;">%.2f DT</td>
                    <td style="padding:10px 14px;border-bottom:1px solid #f1f5f9;text-align:right;font-size:14px;font-weight:bold;color:#0f172a;">%.2f DT</td>
                </tr>
                """, l.getNom(), l.getQuantite(), l.getPrix(), l.getSousTotal()));
        }

        String date = c.getDateCommande();
        if (date != null && date.length() >= 10) date = date.substring(0, 10);

        String statutTxt;
        String statutColor;
        if ("Carte bancaire".equalsIgnoreCase(c.getModePaiement())) {
            statutTxt = "&#10003; PAY&Eacute;E"; // ✓ PAYÉE
            statutColor = "#15803d"; // Green
        } else {
            statutTxt = "&#8987; EN ATTENTE"; // ⏳ EN ATTENTE
            statutColor = "#b45309"; // Orange
        }

        return String.format("""
            <!DOCTYPE html>
            <html lang="fr">
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#f1f5f9;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0">
                <tr><td style="background:#0f172a;padding:28px 40px;">
                  <span style="color:white;font-size:24px;font-weight:bold;letter-spacing:2px;">PHARMAX</span>
                  <br><span style="color:#64748b;font-size:11px;">E-Pharmacie &middot; Sant&eacute; &amp; Bien-&ecirc;tre</span>
                </td></tr>
              </table>
              <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:620px;margin:28px auto;">
                <tr><td style="background:white;border-radius:14px;padding:32px 40px;">
                  <div style="text-align:center;margin-bottom:24px;">
                    <div style="display:inline-block;background:#dcfce7;border-radius:50%%;width:52px;height:52px;line-height:52px;font-size:24px;">&#10003;</div>
                    <h2 style="margin:12px 0 4px;color:#0f172a;font-size:20px;">Commande confirm&eacute;e !</h2>
                    <p style="margin:0;color:#64748b;font-size:13px;">Merci <strong>%s</strong>, votre commande a &eacute;t&eacute; enregistr&eacute;e.</p>
                  </div>
                  <table width="100%%" style="background:#f8fafc;border-radius:10px;margin-bottom:24px;">
                    <tr>
                      <td style="padding:14px 20px;border-right:1px solid #e2e8f0;">
                        <div style="font-size:9px;font-weight:bold;color:#94a3b8;">COMMANDE N&deg;</div>
                        <div style="font-size:16px;font-weight:bold;color:#6d5dfc;">#%d</div>
                      </td>
                      <td style="padding:14px 20px;border-right:1px solid #e2e8f0;">
                        <div style="font-size:9px;font-weight:bold;color:#94a3b8;">DATE</div>
                        <div style="font-size:14px;font-weight:bold;color:#1e293b;">%s</div>
                      </td>
                      <td style="padding:14px 20px;border-right:1px solid #e2e8f0;">
                        <div style="font-size:9px;font-weight:bold;color:#94a3b8;">STATUT</div>
                        <div style="font-size:13px;font-weight:bold;color:%s;">%s</div>
                      </td>
                      <td style="padding:14px 20px;">
                        <div style="font-size:9px;font-weight:bold;color:#94a3b8;">PAIEMENT</div>
                        <div style="font-size:14px;font-weight:bold;color:#1e293b;">%s</div>
                      </td>
                    </tr>
                  </table>
                  <table width="100%%" style="margin-bottom:20px;">
                    <thead>
                      <tr style="background:#0f172a;">
                        <th style="padding:11px 14px;text-align:left;color:white;font-size:11px;">PRODUIT</th>
                        <th style="padding:11px 14px;text-align:center;color:white;font-size:11px;">QT&Eacute;</th>
                        <th style="padding:11px 14px;text-align:right;color:white;font-size:11px;">PRIX UNIT.</th>
                        <th style="padding:11px 14px;text-align:right;color:white;font-size:11px;">SOUS-TOTAL</th>
                      </tr>
                    </thead>
                    <tbody>%s</tbody>
                  </table>
                  <table width="100%%">
                    <tr><td style="padding:5px 0;color:#64748b;font-size:13px;">Sous-total (HT)</td>
                        <td style="padding:5px 0;text-align:right;font-weight:bold;color:#334155;">%.2f DT</td></tr>
                    <tr><td style="padding:5px 0;color:#64748b;font-size:13px;">TVA (19%%)</td>
                        <td style="padding:5px 0;text-align:right;font-weight:bold;color:#334155;">%.2f DT</td></tr>
                    <tr style="background:#f0fdf4;">
                      <td style="padding:10px 14px;font-weight:bold;color:#0f172a;font-size:15px;">TOTAL TTC</td>
                      <td style="padding:10px 14px;text-align:right;font-weight:bold;color:#16a34a;font-size:18px;">%.2f DT</td>
                    </tr>
                  </table>
                </td></tr>
                <tr><td style="text-align:center;padding:20px;color:#94a3b8;font-size:11px;">
                  Merci pour votre confiance &middot; pharmax.tn &middot; support@pharmax.tn
                </td></tr>
              </table>
            </body></html>
            """,
            c.getClient().getNom(),
            c.getCommandeId(),
            date,
            statutColor,
            statutTxt,
            c.getModePaiement(),
            rows.toString(),
            c.getSousTotal(),
            c.getTva(),
            c.getTotalTtc()
        );
    }
}
