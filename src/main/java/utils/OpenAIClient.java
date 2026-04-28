package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenAIClient {

    private static final String API_KEY = "";

    public static String generateResponse(String prompt) throws Exception {

        URL url = new URL("https://api.groq.com/openai/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        // 🔥 JSON REQUEST
        JsonObject root = new JsonObject();
        root.addProperty("model", "llama-3.1-8b-instant");

        JsonArray messages = new JsonArray();

        JsonObject msg = new JsonObject();
        msg.addProperty("role", "user");
        msg.addProperty("content", prompt);

        messages.add(msg);
        root.add("messages", messages);

        // 🔥 SEND REQUEST (UTF-8 FIX)
        try (OutputStream os = conn.getOutputStream()) {
            os.write(root.toString().getBytes("UTF-8"));
        }

        // 🔥 READ RESPONSE (UTF-8 FIX IMPORTANT)
        InputStream is = (conn.getResponseCode() < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        BufferedReader br = new BufferedReader(
                new InputStreamReader(is, "UTF-8")
        );

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        String response = sb.toString();

        System.out.println("RAW RESPONSE: " + response);

        // 🔥 PARSE JSON SAFE
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();

        // ❌ ERROR HANDLING GROQ
        if (json.has("error")) {
            return "Erreur IA: " + json.get("error").toString();
        }

        if (!json.has("choices")) {
            return "Erreur IA: réponse invalide";
        }

        return json.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content")
                .getAsString();
    }}