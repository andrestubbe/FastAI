package fastai;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public interface AI extends AutoCloseable {

    AIResponse generate(AIRequest request);

    default String ask(String prompt) {
        return generate(AIRequest.of(prompt)).text();
    }

    default String ask(String systemPrompt, String userPrompt) {
        return generate(AIRequest.of(systemPrompt, userPrompt)).text();
    }

    default String ask(String prompt, File attachment) {
        return generate(AIRequest.of(prompt, attachment)).text();
    }

    void stream(String prompt, Consumer<String> tokenHandler);

    default void stream(String prompt, Consumer<String> tokenHandler, Consumer<Usage> usageHandler) {
        stream(prompt, tokenHandler);
    }

    default void stream(String systemPrompt, String userPrompt, Consumer<String> tokenHandler) {
        throw new UnsupportedOperationException("Streaming with system prompt is not supported by default");
    }

    default void stream(String systemPrompt, String userPrompt, Consumer<String> tokenHandler, Consumer<Usage> usageHandler) {
        stream(systemPrompt, userPrompt, tokenHandler);
    }

    List<String> getModels();

    @Override
    default void close() throws Exception {
        // default no-op
    }
}

