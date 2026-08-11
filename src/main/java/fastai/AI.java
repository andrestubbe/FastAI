package fastai;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public interface AI extends AutoCloseable {

    AIResponse generate(final AIRequest request);

    default String ask(final String prompt) {
        return generate(AIRequest.of(prompt)).text();
    }

    default String ask(final String systemPrompt, final String userPrompt) {
        return generate(AIRequest.of(systemPrompt, userPrompt)).text();
    }

    default String ask(final String prompt, final File attachment) {
        return generate(AIRequest.of(prompt, attachment)).text();
    }

    default void stream(final String prompt, final Consumer<String> tokenHandler, final Consumer<Usage> usageHandler) {
        stream(prompt, tokenHandler);
    }

    void stream(final String prompt, final Consumer<String> tokenHandler);

    default void stream(final String systemPrompt, final String userPrompt, final Consumer<String> tokenHandler) {
        throw new UnsupportedOperationException("Streaming with system prompt is not supported by default");
    }

    default void stream(final String systemPrompt, final String userPrompt, final Consumer<String> tokenHandler, final Consumer<Usage> usageHandler) {
        stream(systemPrompt, userPrompt, tokenHandler);
    }

    @Override
    default void close() throws Exception {
        // default no-op
    }

    List<String> getModels();
}

