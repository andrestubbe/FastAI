package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
import java.util.function.Consumer;

public class OpenAIClient implements AIProvider {

    private final String model;
    private final String apiKey;

    public OpenAIClient(String model, String apiKey) {
        this.model = model;
        this.apiKey = apiKey;
    }

    @Override
    public String generate(AIRequest request) {
        throw new UnsupportedOperationException("OpenAI implementation coming in v1.0");
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        throw new UnsupportedOperationException("OpenAI streaming coming in v1.0");
    }
}
