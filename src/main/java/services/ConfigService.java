package services;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Service utilitaire pour charger les configurations depuis api_config.properties.
 */
public class ConfigService {
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream in = new FileInputStream("api_config.properties")) {
            properties.load(in);
        } catch (IOException e) {
            System.err.println("⚠️ Impossible de charger api_config.properties. Utilisation des valeurs par défaut.");
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
