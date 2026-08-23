package fastai.providers;

public class FireworksAIClient extends OpenAICompatibleClient {

    private static final String DEFAULT_BASE_URL = "https://api.fireworks.ai/inference/v1";
    private static final String DEFAULT_MODEL = "accounts/fireworks/models/llama-v3p1-70b-instruct";

    public FireworksAIClient(String model, String apiKey) {
        super(DEFAULT_BASE_URL,
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("FIREWORKS_API_KEY"));
    }
}
