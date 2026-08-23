package fastai;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory registry containing token pricing metadata (in USD per 1M tokens) for known AI models.
 */
public final class ModelRegistry {

    /**
     * Pricing structure per 1 million tokens.
     *
     * @param inputPricePerM cost in USD per 1M input/prompt tokens
     * @param outputPricePerM cost in USD per 1M generated/output tokens
     */
    public record Pricing(double inputPricePerM, double outputPricePerM) {
    }

    private static final Map<String, Pricing> REGISTRY = new HashMap<>();

    static {
        // Gemini Models
        REGISTRY.put("gemini-2.5-flash", new Pricing(0.075, 0.30));
        REGISTRY.put("gemini-2.5-pro", new Pricing(1.25, 5.00));
        REGISTRY.put("gemini-2.0-flash", new Pricing(0.075, 0.30));
        REGISTRY.put("gemini-2.0-flash-lite", new Pricing(0.0375, 0.15));
        REGISTRY.put("gemini-1.5-flash", new Pricing(0.075, 0.30));
        REGISTRY.put("gemini-1.5-pro", new Pricing(1.25, 5.00));

        // OpenAI Models
        REGISTRY.put("gpt-4o", new Pricing(2.50, 10.00));
        REGISTRY.put("gpt-4o-mini", new Pricing(0.150, 0.60));
        REGISTRY.put("o1", new Pricing(15.00, 60.00));
        REGISTRY.put("o1-mini", new Pricing(1.10, 4.40));
        REGISTRY.put("o3-mini", new Pricing(1.10, 4.40));

        // Anthropic Claude Models
        REGISTRY.put("claude-3-5-sonnet", new Pricing(3.00, 15.00));
        REGISTRY.put("claude-3-5-haiku", new Pricing(0.80, 4.00));
        REGISTRY.put("claude-3-opus", new Pricing(15.00, 75.00));

        // DeepSeek Models
        REGISTRY.put("deepseek-chat", new Pricing(0.14, 0.28));
        REGISTRY.put("deepseek-reasoner", new Pricing(0.55, 2.19));
        REGISTRY.put("deepseek-r1", new Pricing(0.55, 2.19));
        REGISTRY.put("deepseek-v3", new Pricing(0.14, 0.28));

        // Open Source / Llama 3.1 & 3.3 Models
        REGISTRY.put("llama-3.3-70b", new Pricing(0.59, 0.79));
        REGISTRY.put("llama-3.1-70b", new Pricing(0.59, 0.79));
        REGISTRY.put("llama-3.1-8b", new Pricing(0.05, 0.08));
        REGISTRY.put("llama-3.2-3b", new Pricing(0.03, 0.05));
        REGISTRY.put("llama-3.2-1b", new Pricing(0.02, 0.04));

        // Mistral Models
        REGISTRY.put("mistral-large", new Pricing(2.00, 6.00));
        REGISTRY.put("mistral-small", new Pricing(0.20, 0.60));
        REGISTRY.put("codestral", new Pricing(0.30, 0.90));
    }

    private ModelRegistry() {
    }

    /**
     * Resolves token pricing for a given model identifier.
     *
     * @param model the model ID or name
     * @return the {@link Pricing} entry, or (0.0, 0.0) if unlisted/free
     */
    public static Pricing getPricing(final String model) {
        if (model == null) return new Pricing(0.0, 0.0);
        final String key = model.toLowerCase();
        for (final Map.Entry<String, Pricing> entry : REGISTRY.entrySet()) {
            if (key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new Pricing(0.0, 0.0);
    }
}
