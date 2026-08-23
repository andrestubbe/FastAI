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
            final HttpResponse<java.io.InputStream> response = this.httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException("API Stream Error: " + response.statusCode());
            }

            try (final java.io.InputStream is = response.body()) {
                final byte[] buffer = new byte[8192];
                final byte[] lineBuffer = new byte[65536];
                int linePos = 0;
                int read;

                while ((read = is.read(buffer)) != -1) {
                    for (int i = 0; i < read; i++) {
                        final byte b = buffer[i];
                        if (b == '\n') {
                            if (linePos > 0) {
                                processSseLine(lineBuffer, linePos, tokenHandler, usageHandler);
                                linePos = 0;
                            }
                        } else if (b != '\r') {
                            if (linePos < lineBuffer.length) {
                                lineBuffer[linePos++] = b;
                            }
                        }
                    }
                }
                if (linePos > 0) {
                    processSseLine(lineBuffer, linePos, tokenHandler, usageHandler);
                }
            }
        } catch (final IOException | InterruptedException e) {
            throw new RuntimeException("Failed to call API stream at " + url, e);
        }
    }

    private static void processSseLine(final byte[] lineBuffer, final int len, final Consumer<String> tokenHandler, final Consumer<Usage> usageHandler) {
        // Skip leading whitespace
        int start = 0;
        while (start < len && (lineBuffer[start] == ' ' || lineBuffer[start] == '\t')) {
            start++;
        }

        // Must start with "data:" (100, 97, 116, 97, 58)
        if (len - start < 5) return;
        if (lineBuffer[start] != 'd' || lineBuffer[start + 1] != 'a' || lineBuffer[start + 2] != 't' || lineBuffer[start + 3] != 'a' || lineBuffer[start + 4] != ':') {
            return;
        }

        start += 5;
        while (start < len && (lineBuffer[start] == ' ' || lineBuffer[start] == '\t')) {
            start++;
        }

        if (start >= len) return;

        // Check for [DONE]
        if (len - start == 6 && lineBuffer[start] == '[' && lineBuffer[start + 1] == 'D' && lineBuffer[start + 2] == 'O' && lineBuffer[start + 3] == 'N' && lineBuffer[start + 4] == 'E' && lineBuffer[start + 5] == ']') {
            return;
        }

        // Search for "content": in lineBuffer
        final byte[] contentKey = {'"', 'c', 'o', 'n', 't', 'e', 'n', 't', '"', ':'};
        final int contentIdx = indexOfSubarray(lineBuffer, start, len, contentKey);
        if (contentIdx != -1) {
            int qStart = contentIdx + contentKey.length;
            while (qStart < len && lineBuffer[qStart] != '"') {
                qStart++;
            }
            if (qStart < len) {
                int qEnd = qStart + 1;
                boolean escaped = false;
                while (qEnd < len) {
                    final byte c = lineBuffer[qEnd];
                    if (c == '\\' && !escaped) {
                        escaped = true;
                    } else if (c == '"' && !escaped) {
                        break;
                    } else {
                        escaped = false;
                    }
                    qEnd++;
                }
                if (qEnd < len) {
                    final String token = unescapeJsonBytes(lineBuffer, qStart + 1, qEnd);
                    tokenHandler.accept(token);
                }
            }
        }

        // Search for "usage": if usageHandler is provided
        if (usageHandler != null) {
            final byte[] usageKey = {'"', 'u', 's', 'a', 'g', 'e', '"', ':'};
            final int usageIdx = indexOfSubarray(lineBuffer, start, len, usageKey);
            if (usageIdx != -1) {
                final String usageJson = new String(lineBuffer, usageIdx, len - usageIdx, java.nio.charset.StandardCharsets.UTF_8);
                try {
                    int ptIdx = usageJson.indexOf("\"prompt_tokens\":");
                    int ctIdx = usageJson.indexOf("\"completion_tokens\":");
                    int ttIdx = usageJson.indexOf("\"total_tokens\":");
                    if (ptIdx != -1 && ctIdx != -1 && ttIdx != -1) {
                        int ptEnd = usageJson.indexOf(',', ptIdx);
                        int ctEnd = usageJson.indexOf(',', ctIdx);
                        int ttEnd = usageJson.indexOf('}', ttIdx);
                        if (ptEnd == -1) ptEnd = usageJson.indexOf('}', ptIdx);
                        if (ctEnd == -1) ctEnd = usageJson.indexOf('}', ctIdx);
                        int pt = Integer.parseInt(usageJson.substring(ptIdx + 16, ptEnd).trim());
                        int ct = Integer.parseInt(usageJson.substring(ctIdx + 20, ctEnd).trim());
                        int tt = Integer.parseInt(usageJson.substring(ttIdx + 15, ttEnd).trim());
                        usageHandler.accept(new Usage(pt, ct, tt));
                    }
                } catch (final Exception ignored) {
                }
            }
        }
    }

    private static int indexOfSubarray(final byte[] source, final int offset, final int length, final byte[] target) {
        final int targetLen = target.length;
        final int max = length - targetLen;
        for (int i = offset; i <= max; i++) {
            boolean match = true;
            for (int j = 0; j < targetLen; j++) {
                if (source[i + j] != target[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    private static String unescapeJsonBytes(final byte[] src, final int start, final int end) {
        final StringBuilder sb = new StringBuilder(end - start);
        for (int i = start; i < end; i++) {
            final byte b = src[i];
            if (b == '\\' && i + 1 < end) {
                final byte next = src[i + 1];
                switch (next) {
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case '"' -> { sb.append('"'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case 'u' -> {
                        if (i + 5 < end) {
                            try {
                                final String hex = new String(src, i + 2, 4, java.nio.charset.StandardCharsets.US_ASCII);
                                final int code = Integer.parseInt(hex, 16);
                                sb.append((char) code);
                                i += 5;
                            } catch (final Exception e) {
                                sb.append((char) (b & 0xFF));
                            }
                        } else {
                            sb.append((char) (b & 0xFF));
                        }
                    }
                    default -> sb.append((char) (b & 0xFF));
                }
            } else {
                sb.append((char) (b & 0xFF));
            }
        }
        return sb.toString();
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
