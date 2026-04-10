package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
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
}
