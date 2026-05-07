package services;

import java.io.*;
import java.util.*;

/**
 * Persists checkout form field values to a local file
 * so the user can reuse previous entries via autocomplete.
 */
public class FormHistoryService {

    private static final String HISTORY_FILE =
            System.getProperty("user.home") + File.separator + ".pharmax_form_history.properties";

    private static final int MAX_ENTRIES = 8;

    private static FormHistoryService instance;
    private final Properties props = new Properties();

    private FormHistoryService() {
        load();
    }

    public static FormHistoryService getInstance() {
        if (instance == null) instance = new FormHistoryService();
        return instance;
    }

    /** Returns the list of saved values for a given field key. */
    public List<String> getHistory(String fieldKey) {
        String raw = props.getProperty(fieldKey, "");
        if (raw.isBlank()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split("\\|\\|")));
    }

    /** Saves a new value for a field (inserts at top, deduplicates, trims to MAX_ENTRIES). */
    public void saveValue(String fieldKey, String value) {
        if (value == null || value.isBlank()) return;
        List<String> list = getHistory(fieldKey);
        list.remove(value);          // remove duplicate if present
        list.add(0, value);          // insert at top
        if (list.size() > MAX_ENTRIES) list = list.subList(0, MAX_ENTRIES);
        props.setProperty(fieldKey, String.join("||", list));
        save();
    }

    private void load() {
        try (InputStream in = new FileInputStream(HISTORY_FILE)) {
            props.load(in);
        } catch (IOException ignored) { /* first run — file doesn't exist yet */ }
    }

    private void save() {
        try (OutputStream out = new FileOutputStream(HISTORY_FILE)) {
            props.store(out, "PHARMAX Form History");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
