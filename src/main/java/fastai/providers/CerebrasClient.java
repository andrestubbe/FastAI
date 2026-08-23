package fastai.providers;

public class CerebrasClient extends OpenAICompatibleClient {

    private static final String DEFAULT_BASE_URL = "https://api.cerebras.ai/v1";
    private static final String DEFAULT_MODEL = "llama3.1-70b";

    public CerebrasClient(String model, String apiKey) {
        super(DEFAULT_BASE_URL,
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("CEREBRAS_API_KEY"));
    }
}
