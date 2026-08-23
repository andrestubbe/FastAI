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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class GeminiClient implements AIProvider {

    private static final String DEFAULT_MODEL = "gemini-1.5-flash";
    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String model;
    private final String apiKey;
    private final HttpClient httpClient;

    public GeminiClient(final String model, final String apiKey) {
        this.model = model != null && !model.isEmpty() ? model : DEFAULT_MODEL;
        this.apiKey = apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("GEMINI_API_KEY");
        this.httpClient = SHARED_HTTP_CLIENT;
    }

    @Override
    public AIResponse generate(final AIRequest request) {
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is missing");
        }

        final String url = "https://generativelanguage.googleapis.com/v1beta/models/" + this.model + ":generateContent?key=" + this.apiKey;
        final String jsonBody = this.buildJsonRequest(request);

        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            final HttpResponse<String> response = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API Error: " + response.statusCode() + " - " + response.body());
            }

            try (final FastJsonValue doc = FastJSON.parse(response.body())) {
                final StringBuilder textBuilder = new StringBuilder();
                final FastJsonValue partsArray = doc.path("candidates[0].content.parts");
                if (partsArray != null && partsArray.isArray()) {
                    for (int i = 0; i < partsArray.size(); i++) {
                        final FastJsonValue pNode = partsArray.get(i).path("text");
                        if (pNode != null && !pNode.isNull()) {
                            textBuilder.append(pNode.asString());
                        }
                    }
                }
                final String text = textBuilder.toString();

                final FastJsonValue usageNode = doc.path("usageMetadata");
                final int promptTokens = usageNode != null ? usageNode.getInt("promptTokenCount", 0) : 0;
                final int completionTokens = usageNode != null ? usageNode.getInt("candidatesTokenCount", 0) : 0;
                final int totalTokens = usageNode != null ? usageNode.getInt("totalTokenCount", 0) : 0;
                final Usage usage = new Usage(promptTokens, completionTokens, totalTokens);

                final ModelRegistry.Pricing pricing = ModelRegistry.getPricing(this.model);
                final double cost = ((double) promptTokens / 1_000_000.0) * pricing.inputPricePerM() +
                        ((double) completionTokens / 1_000_000.0) * pricing.outputPricePerM();

                return new AIResponse(text, usage, cost);
            }
        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(final AIRequest request, final Consumer<String> tokenHandler) {
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is missing");
        }

        final String url = "https://generativelanguage.googleapis.com/v1beta/models/" + this.model + ":generateContent?key=" + this.apiKey;
        final String jsonBody = this.buildJsonRequest(request);

        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            final HttpResponse<String> response = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API Error: " + response.statusCode() + " - " + response.body());
            }

            try (final FastJsonValue doc = FastJSON.parse(response.body())) {
                String text = "";
                final FastJsonValue textNode = doc.path("candidates[0].content.parts[0].text");
                if (textNode != null && !textNode.isNull()) {
                    text = textNode.asString();
                }
                if (text != null && !text.isEmpty()) {
                    tokenHandler.accept(text);
                } else {
                    tokenHandler.accept("[No response from Gemini]");
                }
            }
        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getModels() {
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is missing");
        }

        final String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + this.apiKey;
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .GET()
                .build();

        try {
            final HttpResponse<String> response = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API Error: " + response.statusCode() + " - " + response.body());
            }

            final List<String> modelsList = new ArrayList<>();
            try (final FastJsonValue doc = FastJSON.parse(response.body())) {
                final FastJsonValue modelsArray = doc.path("models");
                if (modelsArray != null && modelsArray.isArray()) {
                    for (int i = 0; i < modelsArray.size(); i++) {
                        final FastJsonValue modelObj = modelsArray.get(i);
                        if (modelObj != null) {
                            final FastJsonValue nameVal = modelObj.path("name");
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
        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException("Failed to list Gemini models", e);
        }
    }

    private String buildJsonRequest(final AIRequest request) {
        final fastjson.FastJsonBuilder builder = fastjson.FastJSON.object();

        if (request.systemPrompt != null && !request.systemPrompt.isEmpty()) {
            final fastjson.FastJsonBuilder sysParts = fastjson.FastJSON.array()
                    .addObject(fastjson.FastJSON.object().add("text", request.systemPrompt));
            builder.add("systemInstruction", fastjson.FastJSON.object().addArray("parts", sysParts));
        }

        final fastjson.FastJsonBuilder userParts = fastjson.FastJSON.array()
                .addObject(fastjson.FastJSON.object().add("text", request.userPrompt != null ? request.userPrompt : ""));
        
        final fastjson.FastJsonBuilder contents = fastjson.FastJSON.array()
                .addObject(fastjson.FastJSON.object().addArray("parts", userParts));

        builder.addArray("contents", contents);

        final boolean hasTemp = request.temperature() != null;
        final boolean hasMax = request.maxTokens() != null;
        if (hasTemp || hasMax) {
            final fastjson.FastJsonBuilder genConfig = fastjson.FastJSON.object();
            if (hasTemp) {
                genConfig.add("temperature", request.temperature().doubleValue());
            }
            if (hasMax) {
                genConfig.add("maxOutputTokens", request.maxTokens());
            }
            builder.add("generationConfig", genConfig);
        }

        return builder.buildString();
    }
}

