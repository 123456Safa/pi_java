package com.pharmax.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class AIService {

    private static final Logger logger = Logger.getLogger(AIService.class.getName());

    // FIX 1: volatile ensures the instance is visible across threads immediately
    // after creation
    private static volatile AIService instance;
    private final OkHttpClient client;
    private final String apiKey;

    private static final String HF_URL = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private AIService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        this.apiKey = loadApiKey();
    }

    // FIX 2: double-checked locking — thread-safe without locking on every call
    public static AIService getInstance() {
        if (instance == null) {
            synchronized (AIService.class) {
                if (instance == null) {
                    instance = new AIService();
                }
            }
        }
        return instance;
    }

    private String loadApiKey() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                // FIX 3: warn instead of silently returning empty key
                logger.warning("db.properties not found on classpath — ai.api.key will be empty");
            }
        } catch (IOException e) {
            logger.warning("Failed to load db.properties: " + e.getMessage());
        }
        return props.getProperty("ai.api.key", "");
    }

    public String generateArticle(String keyword) throws IOException {

        String safeKeyword = keyword.replaceAll("[^\\w\\s\\-]", "").trim();

        JsonObject body = new JsonObject();
        body.addProperty("model", "mistral");
        body.addProperty("prompt",
                "Write a short French health article about: " + safeKeyword +
                        ". Max 300 words. Intro, 2 sections, conclusion.");
        body.addProperty("stream", false);

        JsonObject options = new JsonObject();
        options.addProperty("num_predict", 300);
        options.addProperty("temperature", 0.5);
        body.add("options", options);

        Request request = new Request.Builder()
                .url("http://localhost:11434/api/generate")
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        try (Response response = client.newCall(request).execute()) {

            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new IOException("Ollama error: " + responseBody);
            }

            JsonObject obj = JsonParser.parseString(responseBody).getAsJsonObject();

            return obj.get("response").getAsString().trim();
        }
    }
}
