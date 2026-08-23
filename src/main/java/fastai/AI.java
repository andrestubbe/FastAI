package fastai;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Unified high-level interface for synchronous prompting and streaming inference with LLMs.
 * Provides intuitive convenience methods for text-only, system prompt, multimodal, and streaming interactions.
 */
public interface AI extends AutoCloseable {

    /**
     * Executes a generation request against the underlying provider.
     *
     * @param request the configured {@link AIRequest}
     * @return the {@link AIResponse} containing generated text and usage stats
     */
    AIResponse generate(final AIRequest request);

    /**
     * Sends a simple user prompt and returns the generated text response.
     *
     * @param prompt the user message
     * @return the generated response string
     */
    default String ask(final String prompt) {
        return generate(AIRequest.of(prompt)).text();
    }

    /**
     * Sends a prompt with a configured system instruction.
     *
     * @param systemPrompt the system-level instruction/persona
     * @param userPrompt the user message
     * @return the generated response string
     */
    default String ask(final String systemPrompt, final String userPrompt) {
        return generate(AIRequest.of(systemPrompt, userPrompt)).text();
    }

    /**
     * Sends a prompt along with a file attachment (multimodal vision/document input).
     *
     * @param prompt the user prompt
     * @param attachment the file to include in the request
     * @return the generated response string
     */
    default String ask(final String prompt, final File attachment) {
        return generate(AIRequest.of(prompt, attachment)).text();
    }

    /**
     * Streams tokens dynamically as they are generated, with optional usage telemetry.
     *
     * @param prompt the user prompt
     * @param tokenHandler consumer receiving streamed string tokens
     * @param usageHandler consumer receiving final token usage metadata
     */
    default void stream(final String prompt, final Consumer<String> tokenHandler, final Consumer<Usage> usageHandler) {
        stream(prompt, tokenHandler);
    }

    /**
     * Streams tokens dynamically as they are generated.
     *
     * @param prompt the user prompt
     * @param tokenHandler consumer receiving streamed string tokens
     */
    void stream(final String prompt, final Consumer<String> tokenHandler);

    /**
     * Streams tokens dynamically with a separate system prompt and user prompt.
     *
     * @param systemPrompt the system prompt
     * @param userPrompt the user prompt
     * @param tokenHandler consumer receiving streamed string tokens
     */
    default void stream(final String systemPrompt, final String userPrompt, final Consumer<String> tokenHandler) {
        throw new UnsupportedOperationException("Streaming with system prompt is not supported by default");
    }

    /**
     * Streams tokens dynamically with system prompt and usage telemetry.
     *
     * @param systemPrompt the system prompt
     * @param userPrompt the user prompt
     * @param tokenHandler consumer receiving streamed string tokens
     * @param usageHandler consumer receiving token usage telemetry
     */
    default void stream(final String systemPrompt, final String userPrompt, final Consumer<String> tokenHandler, final Consumer<Usage> usageHandler) {
        stream(systemPrompt, userPrompt, tokenHandler);
    }

    @Override
    default void close() throws Exception {
        // default no-op
    }

    /**
     * Lists available model identifiers supported by the underlying provider or gateway.
     *
     * @return list of model ID strings
     */
    List<String> getModels();
}

