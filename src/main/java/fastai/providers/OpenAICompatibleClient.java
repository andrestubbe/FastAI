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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class OpenAICompatibleClient implements AIProvider {

    protected final String baseUrl;
    protected final String model;
    protected final String apiKey;
    protected final HttpClient httpClient;

    public OpenAICompatibleClient(String baseUrl, String model, String apiKey) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public AIResponse generate(AIRequest request) {
        String url = baseUrl + "/chat/completions";
        String jsonBody = buildJsonRequest(request);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (apiKey != null && !apiKey.isEmpty()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }

        HttpRequest httpRequest = builder.build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("API Error: " + response.statusCode() + " - " + response.body());
            }

            try (FastJsonValue doc = FastJSON.parse(response.body())) {
                String text = "";
                FastJsonValue contentNode = doc.path("choices[0].message.content");
                if (contentNode != null && !contentNode.isNull()) {
                    text = contentNode.asString();
                }

                FastJsonValue usageNode = doc.path("usage");
                int promptTokens = usageNode != null ? usageNode.getInt("prompt_tokens", 0) : 0;
                int completionTokens = usageNode != null ? usageNode.getInt("completion_tokens", 0) : 0;
                int totalTokens = usageNode != null ? usageNode.getInt("total_tokens", 0) : 0;
                Usage usage = new Usage(promptTokens, completionTokens, totalTokens);

                ModelRegistry.Pricing pricing = ModelRegistry.getPricing(model);
                double cost = ((double) promptTokens / 1_000_000.0) * pricing.inputPricePerM() +
                        ((double) completionTokens / 1_000_000.0) * pricing.outputPricePerM();

                return new AIResponse(text, usage, cost);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to call API at " + url, e);
        }
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        stream(request, tokenHandler, null);
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler, Consumer<Usage> usageHandler) {
        String url = baseUrl + "/chat/completions";
        // Build request with stream: true and stream_options to request usage token information
        String jsonBody = buildJsonRequest(request).replace("\"stream\": false", "\"stream\": true");

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(java.time.Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (apiKey != null && !apiKey.isEmpty()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }

        HttpRequest httpRequest = builder.build();

        try {
            HttpResponse<java.util.stream.Stream<String>> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofLines()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException("API Stream Error: " + response.statusCode());
            }

            try (java.util.stream.Stream<String> lines = response.body()) {
                lines.forEach(line -> {
                    String cleanLine = line.trim();
                    if (cleanLine.startsWith("data:")) {
                        String data = cleanLine.substring(5).trim();
                        if (data.equals("[DONE]")) {
                            return;
                        }
                        try (FastJsonValue doc = FastJSON.parse(data)) {
                            // Extract token content
                            FastJsonValue deltaNode = doc.path("choices[0].delta.content");
                            if (deltaNode != null && !deltaNode.isNull()) {
                                tokenHandler.accept(deltaNode.asString());
                            }

                            // Extract usage statistics if provided (typically in the final chunk)
                            FastJsonValue usageNode = doc.path("usage");
                            if (usageNode != null && !usageNode.isNull() && usageHandler != null) {
                                int promptTokens = usageNode.getInt("prompt_tokens", 0);
                                int completionTokens = usageNode.getInt("completion_tokens", 0);
                                int totalTokens = usageNode.getInt("total_tokens", 0);
                                usageHandler.accept(new Usage(promptTokens, completionTokens, totalTokens));
                            }
                        } catch (Exception ignored) {
                        }
                    }
                });
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to call API stream at " + url, e);
        }
    }

    @Override
    public List<String> getModels() {
        String url = baseUrl + "/models";
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        if (apiKey != null && !apiKey.isEmpty()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }

        HttpRequest httpRequest = builder.build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Collections.emptyList();
            }

            List<String> modelsList = new ArrayList<>();
            try (FastJsonValue doc = FastJSON.parse(response.body())) {
                FastJsonValue dataArray = doc.path("data");
                if (dataArray != null && dataArray.isArray()) {
                    for (int i = 0; i < dataArray.size(); i++) {
                        FastJsonValue modelObj = dataArray.get(i);
                        if (modelObj != null) {
                            FastJsonValue idVal = modelObj.path("id");
                            if (idVal != null && !idVal.isNull()) {
                                modelsList.add(idVal.asString());
                            }
                        }
                    }
                }
            }
            return modelsList;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String buildJsonRequest(AIRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"model\": \"").append(escapeJson(model)).append("\",");
        sb.append("\"messages\": [");

        boolean hasSystem = request.systemPrompt != null && !request.systemPrompt.isEmpty();
        if (hasSystem) {
            sb.append("{\"role\": \"system\", \"content\": \"")
                    .append(escapeJson(request.systemPrompt))
                    .append("\"},");
        }

        sb.append("{\"role\": \"user\", \"content\": \"")
                .append(escapeJson(request.userPrompt))
                .append("\"}");

        sb.append("],");

        if (request.temperature() != null) {
            sb.append("\"temperature\": ").append(request.temperature()).append(",");
        }
        if (request.maxTokens() != null) {
            sb.append("\"max_tokens\": ").append(request.maxTokens()).append(",");
        }

        sb.append("\"stream\": false");
        // Request token usage metrics inside event stream for OpenAI API
        sb.append(",\"stream_options\": {\"include_usage\": true}");
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
