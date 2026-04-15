package com.pharmax.service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CommentModerationService — corresponds to PHP
 * App\Service\CommentModerationService
 * Detects inappropriate/toxic content using:
 * 1. Rule-based blacklist (always works, fast)
 * 2. HuggingFace Toxic-BERT API (best effort, optional)
 */
public class CommentModerationService {

    private final String apiKey;

    // ─── Blacklist (fallback) ──────────────────────────────────
    private static final List<String> BAD_WORDS = Arrays.asList(
            // English words
            "fuck", "shit", "bitch", "asshole", "idiot", "stupid",
            "bastard", "hate", "terrible", "awful", "useless", "dumb",
            "worst", "disgusting", "offensive",
            // French words
            "connard", "connasse", "débile", "con", "salaud", "salope",
            "crétin", "imbécile", "putain", "foutre", "merde",
            "très con", "très débile", "très nul", "nul", "horrible",
            "dégueulasse", "ignoble", "immonde", "abominable",
            "haïr", "déteste", "détestable", "pire", "pourri",
            "pourrave", "craignos", "chelou", "chelou pas possible",
            "ouf", "t'es pas normal", "t'es fou", "es un fou",
            "c'est de la merde", "quelle merde", "vraiment nul",
            "archi nul", "super nul");

    // ─── Constructor ───────────────────────────────────────────
    public CommentModerationService(String huggingFaceApiKey) {
        this.apiKey = huggingFaceApiKey;
    }

    /** No-arg constructor (blacklist-only mode) */
    public CommentModerationService() {
        this.apiKey = "";
    }

    // ─── Main Analysis Method ──────────────────────────────────

    /**
     * Analyze text for toxic/inappropriate content.
     *
     * @param text the text to analyze
     * @return true if the content is toxic / should be blocked
     */
    public boolean analyze(String text) {
        // 🔴 1️⃣ FAST RULE-BASED CHECK (ALWAYS WORKS)
        String lowerText = text.toLowerCase();
        String normalizedText = removeAccents(lowerText);

        for (String word : BAD_WORDS) {
            String normalizedWord = removeAccents(word.toLowerCase());
            // Use word boundaries to avoid matching substrings (e.g., 'con' in 'contenue')
            String regex = "\\b" + Pattern.quote(normalizedWord) + "\\b";
            if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(normalizedText).find()) {
                return true; // BLOCKED immediately
            }
        }

        // 🟡 2️⃣ AI CHECK (BEST EFFORT) — HuggingFace Toxic-BERT
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("your_")) {
            return false; // Skip AI check if API key is not configured
        }

        try {
            return callHuggingFaceApi(text);
        } catch (Exception e) {
            // 🔵 FAIL SAFE — if AI API fails, allow the comment
            System.err.println("[AI MODERATION FAILED] " + e.getMessage());
            return false;
        }
    }

    // ─── HuggingFace API Call ──────────────────────────────────

    /**
     * Call the HuggingFace Toxic-BERT API for AI-based moderation.
     * Uses OkHttp for HTTP requests.
     *
     * @param text the text to classify
     * @return true if toxic content is detected above threshold
     */
    private boolean callHuggingFaceApi(String text) throws Exception {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        String jsonBody = "{\"inputs\": \"" + text.replace("\"", "\\\"") + "\"}";

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url("https://api-inference.huggingface.co/models/unitary/toxic-bert")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(okhttp3.RequestBody.create(jsonBody, okhttp3.MediaType.parse("application/json")))
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return false;
            }

            String responseBody = response.body().string();
            com.google.gson.JsonArray outerArray = com.google.gson.JsonParser
                    .parseString(responseBody).getAsJsonArray();

            if (outerArray.size() == 0)
                return false;

            com.google.gson.JsonArray results = outerArray.get(0).getAsJsonArray();
            List<String> blockedLabels = Arrays.asList(
                    "toxic", "severe_toxic", "obscene", "threat", "insult", "identity_hate");

            for (com.google.gson.JsonElement elem : results) {
                com.google.gson.JsonObject obj = elem.getAsJsonObject();
                String label = obj.get("label").getAsString();
                double score = obj.get("score").getAsDouble();

                if (blockedLabels.contains(label) && score > 0.4) {
                    return true;
                }
            }
        }
        return false;
    }

    // ─── Utility ───────────────────────────────────────────────

    /**
     * Remove accents from a string (é → e, à → a, etc.)
     */
    private String removeAccents(String text) {
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
