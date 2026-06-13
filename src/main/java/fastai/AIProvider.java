package fastai;

import java.util.List;
import java.util.function.Consumer;

public interface AIProvider {

    AIResponse generate(AIRequest request);

    void stream(AIRequest request, Consumer<String> tokenHandler);

    default void stream(AIRequest request, Consumer<String> tokenHandler, Consumer<Usage> usageHandler) {
        stream(request, tokenHandler);
    }

    List<String> getModels();
}

