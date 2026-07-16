package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
import fastai.AIResponse;

import java.util.List;
import java.util.function.Consumer;

public class ClaudeClient implements AIProvider {

    private final String model;
    private final String apiKey;

    public ClaudeClient(String model, String apiKey) {
        this.model = model;
        this.apiKey = apiKey;
    }

    @Override
    public AIResponse generate(AIRequest request) {
        throw new UnsupportedOperationException("Claude implementation coming in v1.0");
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        throw new UnsupportedOperationException("Claude streaming coming in v1.0");
    }

    @Override
    public List<String> getModels() {
        throw new UnsupportedOperationException("Claude getModels coming in v1.0");
    }
}

