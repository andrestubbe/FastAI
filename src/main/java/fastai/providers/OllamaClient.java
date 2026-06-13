package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
import fastjson.FastJSON;
import fastjson.FastJsonValue;

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

    public OllamaClient(String model) {
        this.model = model;
    }

    @Override
    public String generate(AIRequest request) {
        throw new UnsupportedOperationException("Ollama implementation coming in v1.0");
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        throw new UnsupportedOperationException("Ollama streaming coming in v1.0");
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

