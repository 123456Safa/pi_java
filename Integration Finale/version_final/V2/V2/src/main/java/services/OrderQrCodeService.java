package services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class OrderQrCodeService {
    private static final String PREFIX = "PHARMAX_DELIVERY:";
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateToken() {
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < 18; i++) {
            token.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return token.toString();
    }

    public String buildPayload(String token) {
        return PREFIX + token;
    }

    public String buildQrImageUrl(String token) {
        String encoded = URLEncoder.encode(buildPayload(token), StandardCharsets.UTF_8);
        return "https://api.qrserver.com/v1/create-qr-code/?size=220x220"
                + "&color=0f172a&bgcolor=ffffff&qzone=1&data=" + encoded;
    }

    public String parseToken(String scannedValue) {
        if (scannedValue == null || scannedValue.isBlank()) {
            throw new IllegalArgumentException("Code QR vide.");
        }

        String value = scannedValue.trim();
        if (!value.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Code QR invalide.");
        }

        String token = value.substring(PREFIX.length()).trim();
        if (!token.matches("[A-Z2-9]{18}")) {
            throw new IllegalArgumentException("Token QR invalide.");
        }
        return token;
    }
}
