package services;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigService {
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream in = new FileInputStream("api_config.properties")) {
            properties.load(in);
        } catch (IOException e) {
            System.err.println("⚠️ api_config.properties non trouvé.");
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}
