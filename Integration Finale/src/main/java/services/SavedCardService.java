package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;

public class SavedCardService {
    private static final String CARD_FILE =
            System.getProperty("user.home") + File.separator + ".pharmax_saved_cards.properties";

    private static SavedCardService instance;
    private final Properties props = new Properties();

    private SavedCardService() {
        load();
    }

    public static SavedCardService getInstance() {
        if (instance == null) {
            instance = new SavedCardService();
        }
        return instance;
    }

    public Optional<SavedCard> getSavedCard(String email) {
        String raw = props.getProperty(normalizeEmail(email), "");
        if (raw.isBlank()) {
            return Optional.empty();
        }

        String[] parts = decode(raw).split("\\|", -1);
        if (parts.length < 3) {
            return Optional.empty();
        }

        return Optional.of(new SavedCard(parts[0], parts[1], parts[2]));
    }

    public void saveCard(String email, String cardNumber, String expiry, String cvc) {
        String normalizedCard = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
        if (!normalizedCard.matches("\\d{16}")) {
            return;
        }

        String value = normalizedCard + "|" + safe(expiry) + "|" + safe(cvc);
        props.setProperty(normalizeEmail(email), encode(value));
        save();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "client";
        }
        return email.trim().toLowerCase();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }

    private void load() {
        try (InputStream in = new FileInputStream(CARD_FILE)) {
            props.load(in);
        } catch (IOException ignored) {
        }
    }

    private void save() {
        try (OutputStream out = new FileOutputStream(CARD_FILE)) {
            props.store(out, "PHARMAX Saved Cards");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public record SavedCard(String cardNumber, String expiry, String cvc) {
        public String maskedNumber() {
            if (cardNumber == null || cardNumber.length() < 4) {
                return "Carte enregistree";
            }
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
    }
}
