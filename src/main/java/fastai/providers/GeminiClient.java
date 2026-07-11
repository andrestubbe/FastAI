package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
import fastai.AIResponse;
import fastai.Usage;
import fastai.ModelRegistry;
import fastjson.FastJSON;
import fastjson.FastJsonValue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GeminiClient implements AIProvider {

    private static final Path LOG_FILE = Path.of("fastai-errors.log");

    private final String model;
    private final String apiKey;
    private final HttpClient httpClient;

    public GeminiClient(String model, String apiKey) {
        this.model = model != null && !model.isEmpty() ? model : "gemini-1.5-flash";
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder().build();
    }

    private void logError(String message, Throwable t) {
        StringBuilder entry = new StringBuilder("[GeminiClient] ").append(message).append("\n");
        if (t != null) {
            entry.append(t.toString()).append("\n");
            for (StackTraceElement ste : t.getStackTrace()) {
                entry.append("    at ").append(ste).append("\n");
            }
            if (t.getCause() != null) {
                entry.append("Caused by: ").append(t.getCause()).append("\n");
            }
        }
        entry.append("\n");
        try {
            Files.writeString(LOG_FILE, entry.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }

    @Override
    public AIResponse generate(AIRequest request) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is missing");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        String jsonBody = buildJsonRequest(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API Error: " + response.statusCode() + " - " + response.body());
            }

            // Save raw response to workspace for debugging
            try {
                java.nio.file.Files.writeString(
                        java.nio.file.Path.of("raw_response.json"),
                        response.body()
                );
            } catch (Exception ignored) {
            }

            try (FastJsonValue doc = FastJSON.parse(response.body())) {
                String text = "";
                FastJsonValue textNode = doc.path("candidates[0].content.parts[0].text");
                if (textNode != null && !textNode.isNull()) {
                    text = textNode.asString();
                }

                FastJsonValue usageNode = doc.path("usageMetadata");
                int promptTokens = usageNode != null ? usageNode.getInt("promptTokenCount", 0) : 0;
                int completionTokens = usageNode != null ? usageNode.getInt("candidatesTokenCount", 0) : 0;
                int totalTokens = usageNode != null ? usageNode.getInt("totalTokenCount", 0) : 0;
                Usage usage = new Usage(promptTokens, completionTokens, totalTokens);

                ModelRegistry.Pricing pricing = ModelRegistry.getPricing(model);
                double cost = ((double) promptTokens / 1_000_000.0) * pricing.inputPricePerM() +
                        ((double) completionTokens / 1_000_000.0) * pricing.outputPricePerM();

                return new AIResponse(text, usage, cost);
            }
        } catch (IOException | InterruptedException e) {
            logError("Failed to call Gemini API (generate)", e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is missing");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        String jsonBody = buildJsonRequest(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API Error: " + response.statusCode() + " - " + response.body());
            }

            try (FastJsonValue doc = FastJSON.parse(response.body())) {
                String text = "";
                FastJsonValue textNode = doc.path("candidates[0].content.parts[0].text");
                if (textNode != null && !textNode.isNull()) {
                    text = textNode.asString();
                }
                if (text != null && !text.isEmpty()) {
                    tokenHandler.accept(text);
                } else {
                    tokenHandler.accept("[No response from Gemini]");
                }
            }
        } catch (IOException | InterruptedException e) {
            logError("Failed to call Gemini API (stream)", e);
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getModels() {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is missing");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API Error: " + response.statusCode() + " - " + response.body());
            }

            List<String> modelsList = new ArrayList<>();
            try (FastJsonValue doc = FastJSON.parse(response.body())) {
                FastJsonValue modelsArray = doc.path("models");
                if (modelsArray != null && modelsArray.isArray()) {
                    for (int i = 0; i < modelsArray.size(); i++) {
                        FastJsonValue modelObj = modelsArray.get(i);
                        if (modelObj != null) {
                            FastJsonValue nameVal = modelObj.path("name");
                            if (nameVal != null && !nameVal.isNull()) {
                                String name = nameVal.asString();
                                if (name.startsWith("models/")) {
                                    name = name.substring("models/".length());
                                }
                                modelsList.add(name);
                            }
                        }
                    }
                }
            }
            return modelsList;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to list Gemini models", e);
        }
    }

    private String buildJsonRequest(AIRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean hasSystem = request.systemPrompt != null && !request.systemPrompt.isEmpty();

        if (hasSystem) {
            sb.append("\"systemInstruction\": {\"parts\": [{\"text\": \"")
                    .append(escapeJson(request.systemPrompt))
                    .append("\"}]},");
        }

        sb.append("\"contents\": [{\"parts\": [{\"text\": \"")
                .append(escapeJson(request.userPrompt))
                .append("\"}]}]");

        boolean hasTemp = request.temperature() != null;
        boolean hasMax = request.maxTokens() != null;
        if (hasTemp || hasMax) {
            sb.append(",\"generationConfig\": {");
            if (hasTemp) {
                sb.append("\"temperature\": ").append(request.temperature());
                if (hasMax) sb.append(",");
            }
            if (hasMax) {
                sb.append("\"maxOutputTokens\": ").append(request.maxTokens());
            }
            sb.append("}");
        }

        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

