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
import java.util.function.Consumer;

public class GeminiClient implements AIProvider {

    private final String model;
    private final String apiKey;
    private final HttpClient httpClient;

    public GeminiClient(String model, String apiKey) {
        this.model = model != null && !model.isEmpty() ? model : "gemini-1.5-flash";
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder().build();
    }

    @Override
    public String generate(AIRequest request) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Gemini API key is missing");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        String jsonBody = buildJsonRequest(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Gemini API Error: " + response.statusCode() + " - " + response.body());
            }

            try (FastJsonValue doc = FastJSON.parse(response.body())) {
                FastJsonValue textNode = doc.path("candidates[0].content.parts[0].text");
                if (textNode != null && !textNode.isNull()) {
                    return textNode.asString();
                }
                return "";
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to call Gemini API", e);
        }
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        throw new UnsupportedOperationException("Streaming not yet implemented for Gemini in this iteration");
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
