package Services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {

    // Credentials provided by user for development
    private static final String SENDER_EMAIL = "agrivisionconnectinc@gmail.com";
    private static final String SENDER_PASSWORD = "jxfshqfruvfjtkzr";

    public static void sendResetCode(String toEmail, String code) throws Exception {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SENDER_EMAIL, "PharmaX Support"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Password Reset Verification Code");

        String htmlContent = "<html><body>"
                + "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                + "<h2 style='color: #2c3e50; text-align: center;'>Password Reset Request</h2>"
                + "<p style='color: #34495e; font-size: 16px;'>Hello,</p>"
                + "<p style='color: #34495e; font-size: 16px;'>We received a request to reset your password. Please use the verification code below to proceed with the reset:</p>"
                + "<div style='background-color: #f8f9fa; padding: 15px; text-align: center; margin: 20px 0; border-radius: 5px;'>"
                + "<span style='font-size: 28px; font-weight: bold; letter-spacing: 5px; color: #2980b9;'>" + code + "</span>"
                + "</div>"
                + "<p style='color: #34495e; font-size: 14px;'>This code is valid for 10 minutes. If you did not request a password reset, please ignore this email.</p>"
                + "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'/>"
                + "<p style='color: #7f8c8d; font-size: 12px; text-align: center;'>&copy; 2026 PharmaX. All rights reserved.</p>"
                + "</div></body></html>";

        message.setContent(htmlContent, "text/html; charset=utf-8");

        Transport.send(message);
    }
}
