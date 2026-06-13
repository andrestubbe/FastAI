package fastai;

import java.io.File;

public final class AIRequest {

    public final String systemPrompt;
    public final String userPrompt;
    public final File attachment;
    
    private Double temperature;
    private Integer maxTokens;

    private AIRequest(String systemPrompt, String userPrompt, File attachment) {
        this.systemPrompt = systemPrompt;
        this.userPrompt = userPrompt;
        this.attachment = attachment;
    }

    public static AIRequest of(String prompt) {
        return new AIRequest(null, prompt, null);
    }

    public static AIRequest of(String systemPrompt, String userPrompt) {
        return new AIRequest(systemPrompt, userPrompt, null);
    }

    public static AIRequest of(String prompt, File attachment) {
        return new AIRequest(null, prompt, attachment);
    }

    public Double temperature() {
        return temperature;
    }

    public Integer maxTokens() {
        return maxTokens;
    }

    public AIRequest temperature(double temperature) {
        this.temperature = temperature;
        return this;
    }

    public AIRequest maxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }
}
