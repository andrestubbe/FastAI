package fastai.providers;

public class MistralClient extends OpenAICompatibleClient {

    private static final String DEFAULT_BASE_URL = "https://api.mistral.ai/v1";
    private static final String DEFAULT_MODEL = "mistral-tiny";

    public MistralClient(String model, String apiKey) {
        super(DEFAULT_BASE_URL,
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("MISTRAL_API_KEY"));
    }
}

