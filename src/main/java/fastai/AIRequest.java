package fastai;

import java.io.File;

/**
 * Immutable request container for AI model prompting and generation hyper-parameters.
 */
public final class AIRequest {

    /** System prompt or persona instruction. */
    public final String systemPrompt;

    /** User input prompt. */
    public final String userPrompt;

    /** Optional file attachment for multimodal inference. */
    public final File attachment;

    private Double temperature;
    private Integer maxTokens;

    private AIRequest(final String systemPrompt, final String userPrompt, final File attachment) {
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.attachment = attachment;
    }

    public static AIRequest of(final String prompt) {
        return new AIRequest(null, prompt, null);
    }

    public static AIRequest of(final String systemPrompt, final String userPrompt) {
        return new AIRequest(systemPrompt, userPrompt, null);
    }

    public static AIRequest of(final String prompt, final File attachment) {
        return new AIRequest(null, prompt, attachment);
    }

    public Double temperature() {
        return this.temperature;
    }

    public Integer maxTokens() {
        return this.maxTokens;
    }

    public AIRequest temperature(final double temperature) {
        this.temperature = temperature;
        return this;
    }

    public AIRequest maxTokens(final int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }
}
