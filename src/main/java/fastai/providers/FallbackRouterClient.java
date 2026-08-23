package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
import fastai.AIResponse;
import fastai.Usage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FallbackRouterClient implements AIProvider {

    private final List<AIProvider> providers;

    public FallbackRouterClient(List<AIProvider> providers) {
        if (providers == null || providers.isEmpty()) {
            throw new IllegalArgumentException("FallbackRouterClient requires at least one active AIProvider");
        }
        this.providers = new ArrayList<>(providers);
    }

    public static FallbackRouterClient createFreeTierRouter() {
        List<AIProvider> list = new ArrayList<>();
        
        // 1. Groq (Ultra fast, permanent rate-limited free tier)
        if (System.getenv("GROQ_API_KEY") != null && !System.getenv("GROQ_API_KEY").isEmpty()) {
            list.add(new GroqClient("llama-3.3-70b-versatile", null));
        }

        // 2. Cerebras (Ultra fast inference, free tier)
        if (System.getenv("CEREBRAS_API_KEY") != null && !System.getenv("CEREBRAS_API_KEY").isEmpty()) {
            list.add(new CerebrasClient("llama3.1-70b", null));
        }

        // 3. SambaNova (Fast Llama 3.1 70B, free tier)
        if (System.getenv("SAMBANOVA_API_KEY") != null && !System.getenv("SAMBANOVA_API_KEY").isEmpty()) {
            list.add(new SambaNovaClient("Meta-Llama-3.1-70B-Instruct", null));
        }

        // 4. Gemini (High quality, free tier tokens)
        if (System.getenv("GEMINI_API_KEY") != null && !System.getenv("GEMINI_API_KEY").isEmpty()) {
            list.add(new GeminiClient("gemini-1.5-flash", null));
        }

        // 5. GitHub Models (Free gpt-4o-mini tier for GitHub users)
        String ghToken = System.getenv("GITHUB_TOKEN") != null ? System.getenv("GITHUB_TOKEN") : System.getenv("GH_TOKEN");
        if (ghToken != null && !ghToken.isEmpty()) {
            list.add(new GitHubModelsClient("gpt-4o-mini", null));
        }

        // 6. Mistral (Experiment tier)
        if (System.getenv("MISTRAL_API_KEY") != null && !System.getenv("MISTRAL_API_KEY").isEmpty()) {
            list.add(new MistralClient("mistral-small-latest", null));
        }

        // 7. OpenRouter (Free community models)
        if (System.getenv("OPENROUTER_API_KEY") != null && !System.getenv("OPENROUTER_API_KEY").isEmpty()) {
            list.add(new OpenRouterClient("meta-llama/llama-3.3-70b-instruct:free", null));
        }

        // 8. Local Ollama Fallback (if running)
        list.add(new OllamaClient("llama3.1"));

        return new FallbackRouterClient(list);
    }

    @Override
    public AIResponse generate(AIRequest request) {
        List<Throwable> errors = new ArrayList<>();
        for (AIProvider provider : providers) {
            try {
                return provider.generate(request);
            } catch (Throwable t) {
                errors.add(t);
            }
        }
        throw new RuntimeException("All fallback providers failed. Errors: " + errors);
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        stream(request, tokenHandler, null);
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler, Consumer<Usage> usageHandler) {
        List<Throwable> errors = new ArrayList<>();
        for (AIProvider provider : providers) {
            try {
                provider.stream(request, tokenHandler, usageHandler);
                return;
            } catch (Throwable t) {
                errors.add(t);
            }
        }
        throw new RuntimeException("All fallback stream providers failed. Errors: " + errors);
    }

    @Override
    public List<String> getModels() {
        for (AIProvider provider : providers) {
            try {
                List<String> models = provider.getModels();
                if (models != null && !models.isEmpty()) {
                    return models;
                }
            } catch (Exception ignored) {
            }
        }
        return List.of();
    }
}

