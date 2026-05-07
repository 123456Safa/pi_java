package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class PointsService {
    public static final int POINTS_PER_DINAR = 1;
    public static final int POINTS_PER_DISCOUNT_DINAR = 10;
    public static final int FREE_DELIVERY_POINTS = 50;

    private static final String POINTS_FILE =
            System.getProperty("user.home") + File.separator + ".pharmax_points.properties";

    private static PointsService instance;
    private final Properties props = new Properties();

    private PointsService() {
        load();
    }

    public static PointsService getInstance() {
        if (instance == null) {
            instance = new PointsService();
        }
        return instance;
    }

    public int getPoints(String email) {
        return Integer.parseInt(props.getProperty(normalizeEmail(email), "0"));
    }

    public int calculateEarnedPoints(double totalTtc) {
        return Math.max(0, (int) Math.floor(totalTtc * POINTS_PER_DINAR));
    }

    public int calculateDiscountPointsToUse(String email, double maxAmount) {
        int points = getPoints(email);
        int maxUsablePoints = ((int) Math.floor(Math.max(0.0, maxAmount))) * POINTS_PER_DISCOUNT_DINAR;
        return Math.min(points, maxUsablePoints);
    }

    public double pointsToDiscountAmount(int points) {
        return Math.floor(points / (double) POINTS_PER_DISCOUNT_DINAR);
    }

    public void applyOrderPoints(String email, int pointsSpent, int pointsEarned) {
        int current = getPoints(email);
        int updated = Math.max(0, current - Math.max(0, pointsSpent)) + Math.max(0, pointsEarned);
        props.setProperty(normalizeEmail(email), String.valueOf(updated));
        save();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "client";
        }
        return email.trim().toLowerCase();
    }

    private void load() {
        try (InputStream in = new FileInputStream(POINTS_FILE)) {
            props.load(in);
        } catch (IOException ignored) {
        }
    }

    private void save() {
        try (OutputStream out = new FileOutputStream(POINTS_FILE)) {
            props.store(out, "PHARMAX Loyalty Points");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
