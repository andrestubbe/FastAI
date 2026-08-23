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
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Native Anthropic Claude Client supporting the Anthropic Messages API (/v1/messages).
 */
public class ClaudeClient implements AIProvider {

    private static final String DEFAULT_BASE_URL = "https://api.anthropic.com/v1";
    private static final String DEFAULT_MODEL = "claude-3-5-sonnet-20241022";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private static final HttpClient SHARED_HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String model;
    private final String apiKey;
    private final HttpClient httpClient;

    public ClaudeClient(final String model, final String apiKey) {
        this.model = model != null && !model.isEmpty() ? model : DEFAULT_MODEL;
        this.apiKey = apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("ANTHROPIC_API_KEY");
        this.httpClient = SHARED_HTTP_CLIENT;
    }

    @Override
    public AIResponse generate(final AIRequest request) {
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("Anthropic API key is missing (set ANTHROPIC_API_KEY)");
        }

        final String url = DEFAULT_BASE_URL + "/messages";
        final String jsonBody = this.buildJsonRequest(request, false);

        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-api-key", this.apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .timeout(Duration.ofSeconds(300))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            final HttpResponse<String> response = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException("Anthropic Claude Error: " + response.statusCode() + " - " + response.body());
            }

            try (final FastJsonValue doc = FastJSON.parse(response.body())) {
                String text = "";
                final FastJsonValue contentArray = doc.path("content");
                if (contentArray != null && contentArray.isArray() && contentArray.size() > 0) {
                    final FastJsonValue firstBlock = contentArray.get(0);
                    if (firstBlock != null) {
                        final FastJsonValue textNode = firstBlock.path("text");
                        if (textNode != null && !textNode.isNull()) {
                            text = textNode.asString();
                        }
                    }
                }

                final FastJsonValue usageNode = doc.path("usage");
                final int promptTokens = usageNode != null ? usageNode.getInt("input_tokens", 0) : 0;
                final int completionTokens = usageNode != null ? usageNode.getInt("output_tokens", 0) : 0;
                final int totalTokens = promptTokens + completionTokens;
                final Usage usage = new Usage(promptTokens, completionTokens, totalTokens);

                final ModelRegistry.Pricing pricing = ModelRegistry.getPricing(this.model);
                final double cost = ((double) promptTokens / 1_000_000.0) * pricing.inputPricePerM() +
                        ((double) completionTokens / 1_000_000.0) * pricing.outputPricePerM();

                return new AIResponse(text, usage, cost);
            }
        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException("Failed to call Anthropic Claude API: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(final AIRequest request, final Consumer<String> tokenHandler) {
        this.stream(request, tokenHandler, null);
    }

    @Override
    public void stream(final AIRequest request, final Consumer<String> tokenHandler, final Consumer<Usage> usageHandler) {
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("Anthropic API key is missing (set ANTHROPIC_API_KEY)");
        }

        final String url = DEFAULT_BASE_URL + "/messages";
        final String jsonBody = this.buildJsonRequest(request, true);

        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-api-key", this.apiKey)
                .header("anthropic-version", ANTHROPIC_VERSION)
                .timeout(Duration.ofSeconds(300))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            final HttpResponse<java.io.InputStream> response = this.httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException("Anthropic Claude Stream Error: " + response.statusCode());
            }

            try (final java.io.InputStream is = response.body()) {
                fastai.internal.SseStreamDecoder.decode(is, tokenHandler, usageHandler);
            }
        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException("Failed to stream from Anthropic Claude API: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getModels() {
        return Arrays.asList(
                "claude-3-5-sonnet-20241022",
                "claude-3-5-haiku-20241022",
                "claude-3-opus-20240229",
                "claude-3-sonnet-20240229",
                "claude-3-haiku-20240307"
        );
    }

    private String buildJsonRequest(final AIRequest request, final boolean stream) {
        final fastjson.FastJsonBuilder builder = fastjson.FastJSON.object()
                .add("model", this.model);

        if (request.systemPrompt != null && !request.systemPrompt.isEmpty()) {
            builder.add("system", request.systemPrompt);
        }

        final fastjson.FastJsonBuilder messages = fastjson.FastJSON.array();
        messages.addObject(fastjson.FastJSON.object()
                .add("role", "user")
                .add("content", request.userPrompt != null ? request.userPrompt : ""));

        builder.addArray("messages", messages);

        final int maxTokens = request.maxTokens() != null ? request.maxTokens() : 4096;
        builder.add("max_tokens", maxTokens);

        if (request.temperature() != null) {
            builder.add("temperature", request.temperature().doubleValue());
        }

        if (stream) {
            builder.add("stream", true);
        }

        return builder.buildString();
    }
}

