package Services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TwoFactorAuthService {

    private final GoogleAuthenticator gAuth;

    public TwoFactorAuthService() {
        this.gAuth = new GoogleAuthenticator();
    }

    /**
     * Generates a new secret key for a user.
     */
    public String generateSecretKey() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    /**
     * Verifies a 6-digit code against a secret key.
     */
    public boolean verifyCode(String secret, int code) {
        if (secret == null || secret.trim().isEmpty()) {
            return false;
        }
        return gAuth.authorize(secret, code);
    }

    /**
     * Generates an otpauth URI for Google Authenticator.
     */
    public String generateGoogleAuthenticatorURI(String email, String secret) {
        String issuer = "PharmaX";
        try {
            String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString()).replace("+", "%20");
            return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                    encodedIssuer, encodedEmail, secret, encodedIssuer);
        } catch (Exception e) {
            throw new RuntimeException("Error encoding URI", e);
        }
    }

    /**
     * Generates a QR Code Image for JavaFX from a URI.
     */
    public Image generateQRCodeImage(String barcodeText, int width, int height) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, width, height);

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
}
