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
import java.util.stream.Stream;

public class OpenAICompatibleClient implements AIProvider {

    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    protected final String baseUrl;
    protected final String model;
    protected final String apiKey;
    protected final HttpClient httpClient;

    public OpenAICompatibleClient(final String baseUrl, final String model, final String apiKey) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.apiKey = apiKey;
        this.httpClient = SHARED_HTTP_CLIENT;
    }

    @Override
    public AIResponse generate(final AIRequest request) {
        final String url = this.baseUrl + "/chat/completions";
        final String jsonBody = buildJsonRequest(request);

        final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(300))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (this.apiKey != null && !this.apiKey.isEmpty()) {
            builder.header("Authorization", "Bearer " + this.apiKey);
        }

        final HttpRequest httpRequest = builder.build();

        try {
            final HttpResponse<String> response = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
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
            throw new RuntimeException("Failed to call API at " + url + " - Cause: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(final AIRequest request, final Consumer<String> tokenHandler) {
        this.stream(request, tokenHandler, null);
    }

    @Override
    public void stream(final AIRequest request, final Consumer<String> tokenHandler, final Consumer<Usage> usageHandler) {
        final String url = this.baseUrl + "/chat/completions";
        // Build request with stream: true and stream_options to request usage token information
        final String jsonBody = this.buildJsonRequest(request).replace("\"stream\": false", "\"stream\": true");

        final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(300))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (this.apiKey != null && !this.apiKey.isEmpty()) {
            builder.header("Authorization", "Bearer " + this.apiKey);
        }

        final HttpRequest httpRequest = builder.build();

        try {
            final HttpResponse<Stream<String>> response = this.httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofLines()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException("API Stream Error: " + response.statusCode());
            }

            try (Stream<String> lines = response.body()) {
                lines.forEach(line -> {
                    String cleanLine = line.trim();
                    if (cleanLine.startsWith("data:")) {
                        String data = cleanLine.substring(5).trim();
                        if (data.isEmpty() || data.equals("[DONE]")) {
                            return;
                        }
                        
                        try {
                            // Extract token content manually to avoid native crash on
                            // incomplete UTF-8 sequences split across SSE chunks
                            int contentIdx = data.indexOf("\"content\":");
                            if (contentIdx != -1) {
                                int startQuote = data.indexOf('"', contentIdx + 10);
                                if (startQuote != -1) {
                                    int endQuote = startQuote + 1;
                                    boolean escaped = false;
                                    while (endQuote < data.length()) {
                                        char c = data.charAt(endQuote);
                                        if (c == '\\' && !escaped) {
                                            escaped = true;
                                        } else if (c == '"' && !escaped) {
                                            break;
                                        } else {
                                            escaped = false;
                                        }
                                        endQuote++;
                                    }
                                    if (endQuote < data.length()) {
                                        tokenHandler.accept(unescapeJsonChunk(data, startQuote + 1, endQuote));
                                    }
                                }
                            }

                            // Extract usage statistics if provided (typically in the final chunk)
                            if (data.contains("\"usage\":")) {
                                int usageIdx = data.indexOf("\"usage\":");
                                String usageStr = data.substring(usageIdx);
                                int ptIdx = usageStr.indexOf("\"prompt_tokens\":");
                                int ctIdx = usageStr.indexOf("\"completion_tokens\":");
                                int ttIdx = usageStr.indexOf("\"total_tokens\":");
                                
                                if (ptIdx != -1 && ctIdx != -1 && ttIdx != -1) {
                                    int ptEnd = usageStr.indexOf(',', ptIdx);
                                    int ctEnd = usageStr.indexOf(',', ctIdx);
                                    int ttEnd = usageStr.indexOf('}', ttIdx);
                                    if (ptEnd == -1) ptEnd = usageStr.indexOf('}', ptIdx);
                                    if (ctEnd == -1) ctEnd = usageStr.indexOf('}', ctIdx);
                                    
                                    try {
                                        int pt = Integer.parseInt(usageStr.substring(ptIdx + 16, ptEnd).trim());
                                        int ct = Integer.parseInt(usageStr.substring(ctIdx + 20, ctEnd).trim());
                                        int tt = Integer.parseInt(usageStr.substring(ttIdx + 15, ttEnd).trim());
                                        Usage usage = new Usage(pt, ct, tt);
                                        if (usageHandler != null) {
                                            usageHandler.accept(usage);
                                        }
                                    } catch (Exception ignored) {}
                                }
                            }
                        } catch (Exception e) {
                            // Ignore incomplete chunks
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
        final String url = this.baseUrl + "/models";
        final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();

        if (this.apiKey != null && !this.apiKey.isEmpty()) {
            builder.header("Authorization", "Bearer " + this.apiKey);
        }

        final HttpRequest httpRequest = builder.build();

        try {
            final HttpResponse<String> response = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Collections.emptyList();
            }

            final List<String> modelsList = new ArrayList<>();
            try (final FastJsonValue doc = FastJSON.parse(response.body())) {
                final FastJsonValue dataArray = doc.path("data");
                if (dataArray != null && dataArray.isArray()) {
                    for (int i = 0; i < dataArray.size(); i++) {
                        final FastJsonValue modelObj = dataArray.get(i);
                        if (modelObj != null) {
                            final FastJsonValue idVal = modelObj.path("id");
                            if (idVal != null && !idVal.isNull()) {
                                final String modelId = idVal.asString();
                                if (modelId != null) {
                                    String normId = modelId;
                                    final int colonIdx = normId.indexOf(" :");
                                    if (colonIdx != -1) {
                                        normId = normId.substring(0, colonIdx).trim();
                                    } else {
                                        normId = normId.trim();
                                    }
                                    if (!normId.toLowerCase().contains("embed") && !modelsList.contains(normId)) {
                                        modelsList.add(normId);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return modelsList;
        } catch (final Exception e) {
            return Collections.emptyList();
        }
    }

    private static String unescapeJsonChunk(final String src, final int start, final int end) {
        final StringBuilder sb = new StringBuilder(end - start);
        for (int i = start; i < end; i++) {
            final char c = src.charAt(i);
            if (c == '\\' && i + 1 < end) {
                final char next = src.charAt(i + 1);
                switch (next) {
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case '"' -> { sb.append('"'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case 'u' -> {
                        if (i + 5 < end) {
                            try {
                                final int code = Integer.parseInt(src.substring(i + 2, i + 6), 16);
                                sb.append((char) code);
                                i += 5;
                            } catch (final NumberFormatException e) {
                                sb.append(c);
                            }
                        } else {
                            sb.append(c);
                        }
                    }
                    default -> sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String buildJsonRequest(final AIRequest request) {
        final fastjson.FastJsonBuilder builder = fastjson.FastJSON.object()
                .add("model", this.model);

        final fastjson.FastJsonBuilder messages = fastjson.FastJSON.array();

        if (request.systemPrompt != null && !request.systemPrompt.isEmpty()) {
            messages.addObject(fastjson.FastJSON.object()
                    .add("role", "system")
                    .add("content", request.systemPrompt));
        }

        messages.addObject(fastjson.FastJSON.object()
                .add("role", "user")
                .add("content", request.userPrompt != null ? request.userPrompt : ""));

        builder.addArray("messages", messages);

        if (request.temperature() != null) {
            builder.add("temperature", request.temperature().doubleValue());
        }
        if (request.maxTokens() != null) {
            builder.add("max_tokens", request.maxTokens());
        }

        builder.add("stream", false);
        return builder.buildString();
    }
}
