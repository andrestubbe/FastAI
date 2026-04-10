package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
import java.util.function.Consumer;

public class ClaudeClient implements AIProvider {

    private final String model;
    private final String apiKey;

    public ClaudeClient(String model, String apiKey) {
        this.model = model;
        this.apiKey = apiKey;
    }

    @Override
    public String generate(AIRequest request) {
        throw new UnsupportedOperationException("Claude implementation coming in v1.0");
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        throw new UnsupportedOperationException("Claude streaming coming in v1.0");
    }
}
