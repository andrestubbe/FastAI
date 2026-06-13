package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
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

public class OllamaClient implements AIProvider {

    private final String model;
    private final HttpClient httpClient;

    public OllamaClient(String model) {
        this.model = model != null && !model.isEmpty() ? model : "llama3.1";
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public String generate(AIRequest request) {
        String url = "http://localhost:11434/api/chat";
        String jsonBody = buildJsonRequest(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Ollama API Error: " + response.statusCode() + " - " + response.body());
            }

            try (FastJsonValue doc = FastJSON.parse(response.body())) {
                FastJsonValue textNode = doc.path("message.content");
                if (textNode != null && !textNode.isNull()) {
                    return unescapeJson(textNode.asString());
                }
                return "";
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to call Ollama API", e);
        }
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        throw new UnsupportedOperationException("Ollama streaming coming in v1.0");
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

    private String unescapeJson(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        int len = input.length();
        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (c == '\\' && i + 1 < len) {
                char next = input.charAt(i + 1);
                switch (next) {
                    case '"': sb.append('"'); i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    case '/': sb.append('/'); i++; break;
                    case 'b': sb.append('\b'); i++; break;
                    case 'f': sb.append('\f'); i++; break;
                    case 'n': sb.append('\n'); i++; break;
                    case 'r': sb.append('\r'); i++; break;
                    case 't': sb.append('\t'); i++; break;
                    default: sb.append(c); break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public List<String> getModels() {
        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/tags"))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Collections.emptyList();
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
                                modelsList.add(nameVal.asString());
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
}

