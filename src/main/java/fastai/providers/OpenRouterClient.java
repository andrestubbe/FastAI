package fastai.providers;

public class OpenRouterClient extends OpenAICompatibleClient {

    public OpenRouterClient(String model, String apiKey) {
        super("https://openrouter.ai/api/v1",
                model != null && !model.isEmpty() ? model : "anthropic/claude-3.5-sonnet",
                apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("OPENROUTER_API_KEY"));
    }
}
