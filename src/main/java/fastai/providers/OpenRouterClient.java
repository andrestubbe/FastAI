package fastai.providers;

public class OpenRouterClient extends OpenAICompatibleClient {

    private static final String DEFAULT_BASE_URL = "https://openrouter.ai/api/v1";
    private static final String DEFAULT_MODEL = "anthropic/claude-3.5-sonnet";

    public OpenRouterClient(String model, String apiKey) {
        super(DEFAULT_BASE_URL,
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("OPENROUTER_API_KEY"));
    }
}

