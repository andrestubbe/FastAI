package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
import java.util.List;
import java.util.function.Consumer;

public class MistralClient implements AIProvider {

    private final String model;
    private final String apiKey;

    public MistralClient(String model, String apiKey) {
        this.model = model;
        this.apiKey = apiKey;
    }

    @Override
    public String generate(AIRequest request) {
        throw new UnsupportedOperationException("Mistral implementation coming in v1.0");
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        throw new UnsupportedOperationException("Mistral streaming coming in v1.0");
    }

    @Override
    public List<String> getModels() {
        throw new UnsupportedOperationException("Mistral getModels coming in v1.0");
    }
}

