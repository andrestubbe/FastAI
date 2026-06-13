package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
import java.util.List;
import java.util.function.Consumer;

public class LMStudioClient implements AIProvider {

    private final String model;

    public LMStudioClient(String model) {
        this.model = model;
    }

    @Override
    public String generate(AIRequest request) {
        throw new UnsupportedOperationException("LMStudio implementation coming in v1.0");
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        throw new UnsupportedOperationException("LMStudio streaming coming in v1.0");
    }

    @Override
    public List<String> getModels() {
        throw new UnsupportedOperationException("LMStudio getModels coming in v1.0");
    }
}

