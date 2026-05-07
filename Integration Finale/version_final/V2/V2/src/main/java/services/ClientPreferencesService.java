package services;

import java.util.prefs.Preferences;

public class ClientPreferencesService {
    private static final String PREFS_NODE = "pidev/client";
    private static final String KEY_NOM = "client.nom";
    private static final String KEY_EMAIL = "client.email";
    private static final String KEY_ADRESSE = "client.adresse";
    private static final String KEY_TELEPHONE = "client.telephone";

    private static final Preferences prefs = Preferences.userRoot().node(PREFS_NODE);

    /**
     * Sauvegarde les données du client
     */
    public static void saveClientData(String nom, String email, String adresse, String telephone) {
        prefs.put(KEY_NOM, nom != null ? nom : "");
        prefs.put(KEY_EMAIL, email != null ? email : "");
        prefs.put(KEY_ADRESSE, adresse != null ? adresse : "");
        prefs.put(KEY_TELEPHONE, telephone != null ? telephone : "");
        try {
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère le nom du client sauvegardé
     */
    public static String getNom() {
        return prefs.get(KEY_NOM, "");
    }

    /**
     * Récupère l'email du client sauvegardé
     */
    public static String getEmail() {
        return prefs.get(KEY_EMAIL, "");
    }

    /**
     * Récupère l'adresse du client sauvegardé
     */
    public static String getAdresse() {
        return prefs.get(KEY_ADRESSE, "");
    }

    /**
     * Récupère le téléphone du client sauvegardé
     */
    public static String getTelephone() {
        return prefs.get(KEY_TELEPHONE, "");
    }

    /**
     * Vérifie si des données de client sont disponibles
     */
    public static boolean hasClientData() {
        return !getNom().isEmpty();
    }

    /**
     * Efface toutes les données du client
     */
    public static void clearClientData() {
        prefs.remove(KEY_NOM);
        prefs.remove(KEY_EMAIL);
        prefs.remove(KEY_ADRESSE);
        prefs.remove(KEY_TELEPHONE);
        try {
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

