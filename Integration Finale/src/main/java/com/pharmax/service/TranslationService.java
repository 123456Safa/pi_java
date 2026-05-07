package com.pharmax.service;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * TranslationService — Dedicated service for translating articles using local Ollama.
 * Separated from AIService to prevent any conflicts or confusion.
 */
public class TranslationService {

    private static volatile TranslationService instance;
    private final OkHttpClient client;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    private TranslationService() {
        this.client = new OkHttpClient.Builder()
                .callTimeout(java.time.Duration.ofSeconds(120)) // translations might take time
                .readTimeout(java.time.Duration.ofSeconds(120))
                .build();
    }

    public static TranslationService getInstance() {
        if (instance == null) {
            synchronized (TranslationService.class) {
                if (instance == null) {
                    instance = new TranslationService();
                }
            }
        }
        return instance;
    }

    /**
     * Translates the given text to the target language using Ollama (mistral model).
     */
    public String translateText(String text, String targetLanguage) throws IOException {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", "mistral");
        body.addProperty("prompt", 
                "Translate the following text into " + targetLanguage + ". " +
                "Provide ONLY the translated text, no other conversational text or explanations:\n\n" + 
                text);
        body.addProperty("stream", false);

        JsonObject options = new JsonObject();
        options.addProperty("num_predict", 800); // Allow longer output for translation
        options.addProperty("temperature", 0.3); // Lower temperature for more accurate translation
        body.add("options", options);

        Request request = new Request.Builder()
                .url(OLLAMA_URL)
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new IOException("Ollama translation error: " + responseBody);
            }

            JsonObject obj = JsonParser.parseString(responseBody).getAsJsonObject();
            return obj.get("response").getAsString().trim();
        }
    }
}
