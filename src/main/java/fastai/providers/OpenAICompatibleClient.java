package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
import fastai.AIResponse;
import fastai.Usage;
import fastai.ModelRegistry;
import fastjson.FastJSON;
import fastjson.FastJsonValue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
                .timeout(Duration.ofSeconds(300))
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
            throw new RuntimeException("Failed to call API at " + url + " - Cause: " + e.getMessage(), e);
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
        String jsonBody = buildJsonRequest(request).replace("\"stream\": false", "\"stream\": true, \"stream_options\": {\"include_usage\": true}");

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(300))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (apiKey != null && !apiKey.isEmpty()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }

        HttpRequest httpRequest = builder.build();

        try {
            try { Files.write(Paths.get("C:\\Users\\andre\\fastbot_debug.log"), ("\n=== NEW REQUEST ===\nURL: " + url + "\nBODY: " + jsonBody + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND); } catch(Exception ignored){}
            HttpResponse<Stream<String>> response = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofLines()
            );

            if (response.statusCode() != 200) {
                String err = "API Stream Error: " + response.statusCode();
                try { Files.write(Paths.get("C:\\Users\\andre\\fastbot_debug.log"), ("HTTP ERROR: " + response.statusCode() + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND); } catch(Exception ignored){}
                throw new RuntimeException(err);
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
                            try { Files.write(Paths.get("debug_stream.log"), (data + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND); } catch(Exception ignored){}
                            // Extract token content manually to avoid native crash on
                            // incomplete UTF-8 sequences split across SSE chunks
                            int contentIdx = data.indexOf("\"content\":");
                            if (contentIdx != -1) {
                                int startQuote = data.indexOf("\"", contentIdx + 10);
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
                                        String token = data.substring(startQuote + 1, endQuote);
                                        // Unescape basic json
                                        token = token.replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\");
                                        // Unescape unicode \\uXXXX
                                        if (token.contains("\\u")) {
                                            StringBuilder sb = new StringBuilder();
                                            int i = 0;
                                            while (i < token.length()) {
                                                if (token.charAt(i) == '\\' && i + 5 < token.length() && token.charAt(i + 1) == 'u') {
                                                    try {
                                                        int code = Integer.parseInt(token.substring(i + 2, i + 6), 16);
                                                        sb.append((char) code);
                                                        i += 6;
                                                        continue;
                                                    } catch (NumberFormatException ignored) {}
                                                }
                                                sb.append(token.charAt(i));
                                                i++;
                                            }
                                            token = sb.toString();
                                        }
                                        tokenHandler.accept(token);
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
            try { 
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Files.write(Paths.get("C:\\Users\\andre\\fastbot_debug.log"), ("EXCEPTION: " + e.getMessage() + "\n" + sw.toString() + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND); 
            } catch(Exception ignored){}
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
                                String modelId = idVal.asString();
                                if (modelId != null) {
                                    String normId = modelId;
                                    int colonIdx = normId.indexOf(" :");
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
