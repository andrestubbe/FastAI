package fastai;

import java.util.List;
import java.util.function.Consumer;

/**
 * Underlying SPI contract for AI model providers and inference gateways in FastAI.
 * Implementations execute request payloads synchronously or stream tokens back in real-time.
 */
public interface AIProvider {

    /**
     * Executes a synchronous AI completion request.
     *
     * @param request the request containing prompts, attachments, and hyper-parameters
     * @return the resulting response containing output text and usage statistics
     */
    AIResponse generate(final AIRequest request);

    /**
     * Streams generated tokens in real-time.
     *
     * @param request the request containing prompts and configuration
     * @param tokenHandler consumer receiving text chunks as they arrive
     */
    void stream(final AIRequest request, final Consumer<String> tokenHandler);

    /**
     * Streams generated tokens and final token usage metrics.
     *
     * @param request the request containing prompts and configuration
     * @param tokenHandler consumer receiving text chunks as they arrive
     * @param usageHandler consumer receiving token usage metrics upon completion
     */
    default void stream(final AIRequest request, final Consumer<String> tokenHandler, final Consumer<Usage> usageHandler) {
        stream(request, tokenHandler);
    }

    /**
     * Lists available models on this provider endpoint.
     *
     * @return list of model IDs
     */
    List<String> getModels();
}

