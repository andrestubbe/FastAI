package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
import java.util.List;
import java.util.function.Consumer;

public class DeepSeekClient implements AIProvider {

    private final String model;
    private final String apiKey;

    public DeepSeekClient(String model, String apiKey) {
        this.model = model;
        this.apiKey = apiKey;
    }

    @Override
    public String generate(AIRequest request) {
        throw new UnsupportedOperationException("DeepSeek implementation coming in v1.0");
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        throw new UnsupportedOperationException("DeepSeek streaming coming in v1.0");
    }

    @Override
    public List<String> getModels() {
        throw new UnsupportedOperationException("DeepSeek getModels coming in v1.0");
    }
}

