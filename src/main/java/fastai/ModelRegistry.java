package fastai;

import java.util.HashMap;
import java.util.Map;

public final class ModelRegistry {
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
        REGISTRY.put("gemini-3.5-flash", new Pricing(0.075, 0.30));

        // OpenAI Models
        REGISTRY.put("gpt-4o", new Pricing(2.50, 10.00));
        REGISTRY.put("gpt-4o-mini", new Pricing(0.150, 0.60));

        // DeepSeek
        REGISTRY.put("deepseek-chat", new Pricing(0.14, 0.28));
    }

    private ModelRegistry() {
    }

    public static Pricing getPricing(String model) {
        if (model == null) return new Pricing(0.0, 0.0);
        String key = model.toLowerCase();
        for (Map.Entry<String, Pricing> entry : REGISTRY.entrySet()) {
            if (key.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return new Pricing(0.0, 0.0);
    }
}
