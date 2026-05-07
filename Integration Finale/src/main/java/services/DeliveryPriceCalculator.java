package services;

public class DeliveryPriceCalculator {
    private static final double BASE_PRICE = 0.0;
    private static final double PRICE_PER_KM = 1.2;
    private static final double DEFAULT_DISTANCE_KM = 4.0;

    public DeliveryQuote calculate(String destinationAddress) {
        if (destinationAddress == null || destinationAddress.isBlank()) {
            return new DeliveryQuote(0.0, 0.0);
        }

        double distanceKm = estimateDistance(destinationAddress);
        double price = BASE_PRICE + distanceKm * PRICE_PER_KM;
        return new DeliveryQuote(round(distanceKm), round(price));
    }

    private double estimateDistance(String address) {
        String normalized = address.toLowerCase();

        if (normalized.contains("ariana")) {
            return 0.0;
        }
        if (normalized.contains("lac") || normalized.contains("berges")) {
            return 8.0;
        }
        if (normalized.contains("tunis")) {
            return 10.0;
        }
        if (normalized.contains("manouba")) {
            return 13.0;
        }
        if (normalized.contains("ben arous")) {
            return 16.0;
        }
        if (normalized.contains("marsa") || normalized.contains("carthage")) {
            return 18.0;
        }

        return DEFAULT_DISTANCE_KM;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public record DeliveryQuote(double distanceKm, double deliveryPrice) {
    }
}
