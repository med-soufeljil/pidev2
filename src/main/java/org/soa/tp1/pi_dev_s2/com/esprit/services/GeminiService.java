package org.soa.tp1.pi_dev_s2.com.esprit.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GeminiService {

    // TA CLÉ API
    private static final String API_KEY = "AIzaSyArNWy5299SAtsIUOBW0bGspsBwrJ_QAfg";

    // ✅ CORRECTION: Utiliser gemini-1.5-pro au lieu de gemini-pro
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent";

    // Alternative: gemini-1.0-pro
    // private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.0-pro:generateContent";

    private final OkHttpClient client;
    private final ObjectMapper mapper;
    private final StringBuilder conversationHistory;

    private long lastRequestTime = 0;
    private static final long MIN_REQUEST_INTERVAL = 2000; // 2 secondes

    public GeminiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.mapper = new ObjectMapper();
        this.conversationHistory = new StringBuilder();

        System.out.println("✅ GeminiService initialisé avec le modèle: gemini-1.5-pro");
    }

    private void attendreSiNecessaire() {
        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;

        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL) {
            try {
                long waitTime = MIN_REQUEST_INTERVAL - timeSinceLastRequest;
                System.out.println("⏳ Attente de " + waitTime + "ms (rate limiting)...");
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        lastRequestTime = System.currentTimeMillis();
    }

    public String poserQuestion(String question) {
        try {
            attendreSiNecessaire();

            // Ajouter le contexte des événements si ce n'est pas déjà fait
            if (conversationHistory.length() == 0) {
                conversationHistory.append("Contexte: Tu es un assistant spécialisé dans les événements culturels et sportifs. ")
                        .append("Tu aides les utilisateurs à trouver des informations sur les événements, ")
                        .append("les inscriptions, les lieux, etc. Réponds en français de manière conviviale.\n\n");
            }

            conversationHistory.append("Utilisateur: ").append(question).append("\n");

            // Construction de la requête JSON
            ObjectNode root = mapper.createObjectNode();
            ArrayNode contents = mapper.createArrayNode();
            ObjectNode content = mapper.createObjectNode();
            ArrayNode parts = mapper.createArrayNode();
            ObjectNode part = mapper.createObjectNode();

            // Inclure l'historique de la conversation pour plus de contexte
            String fullPrompt = conversationHistory.toString() + "\nAssistant: ";

            part.put("text", fullPrompt);
            parts.add(part);
            content.set("parts", parts);
            contents.add(content);
            root.set("contents", contents);

            // Configuration de sécurité
            ArrayNode safetySettings = mapper.createArrayNode();

            ObjectNode safetySetting1 = mapper.createObjectNode();
            safetySetting1.put("category", "HARM_CATEGORY_HARASSMENT");
            safetySetting1.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
            safetySettings.add(safetySetting1);

            ObjectNode safetySetting2 = mapper.createObjectNode();
            safetySetting2.put("category", "HARM_CATEGORY_HATE_SPEECH");
            safetySetting2.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
            safetySettings.add(safetySetting2);

            root.set("safetySettings", safetySettings);

            // Configuration de génération
            ObjectNode generationConfig = mapper.createObjectNode();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 1024);
            generationConfig.put("topP", 0.95);
            generationConfig.put("topK", 40);
            root.set("generationConfig", generationConfig);

            String jsonRequest = mapper.writeValueAsString(root);
            System.out.println("📤 Requête envoyée à Gemini...");

            RequestBody body = RequestBody.create(
                    jsonRequest,
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL + "?key=" + API_KEY)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    System.err.println("❌ Erreur " + response.code() + ": " + responseBody);

                    // Si gemini-1.5-pro ne fonctionne pas, essayer gemini-1.0-pro
                    if (response.code() == 404) {
                        return essayerModeleAlternative(question);
                    }

                    return "❌ Erreur API (" + response.code() + "). Vérifie ta connexion et réessaie.";
                }

                String reponse = parseResponse(responseBody);

                // Sauvegarder la réponse dans l'historique
                if (!reponse.startsWith("❌") && !reponse.startsWith("Erreur")) {
                    conversationHistory.append("Assistant: ").append(reponse).append("\n\n");
                }

                return reponse;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "❌ Erreur de connexion: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Erreur: " + e.getMessage();
        }
    }

    private String essayerModeleAlternative(String question) {
        try {
            System.out.println("🔄 Tentative avec modèle alternatif gemini-1.0-pro...");

            String alternativeUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.0-pro:generateContent";

            ObjectNode root = mapper.createObjectNode();
            ArrayNode contents = mapper.createArrayNode();
            ObjectNode content = mapper.createObjectNode();
            ArrayNode parts = mapper.createArrayNode();
            ObjectNode part = mapper.createObjectNode();

            part.put("text", question);
            parts.add(part);
            content.set("parts", parts);
            contents.add(content);
            root.set("contents", contents);

            String jsonRequest = mapper.writeValueAsString(root);

            RequestBody body = RequestBody.create(
                    jsonRequest,
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(alternativeUrl + "?key=" + API_KEY)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    return "❌ Les modèles Gemini ne sont pas disponibles. Vérifie que l'API est activée dans Google Cloud Console.";
                }

                return parseResponse(responseBody);
            }

        } catch (Exception e) {
            return "❌ Impossible de contacter l'API Gemini. Vérifie ta clé API et active l'API dans Google Cloud Console.";
        }
    }

    private String parseResponse(String jsonResponse) {
        try {
            JsonNode root = mapper.readTree(jsonResponse);

            if (root.has("error")) {
                return "Erreur API: " + root.path("error").path("message").asText();
            }

            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    String text = parts.get(0).path("text").asText().trim();
                    return text.isEmpty() ? "Je n'ai pas pu générer une réponse." : text;
                }
            }

            return "Désolé, je n'ai pas pu traiter ta demande.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur de traitement de la réponse.";
        }
    }

    public void resetConversation() {
        conversationHistory.setLength(0);
        System.out.println("🔄 Conversation réinitialisée");
    }
}