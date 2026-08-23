package fastai.providers;

public class SambaNovaClient extends OpenAICompatibleClient {

    private static final String DEFAULT_BASE_URL = "https://api.sambanova.ai/v1";
    private static final String DEFAULT_MODEL = "Meta-Llama-3.1-70B-Instruct";

    public SambaNovaClient(String model, String apiKey) {
        super(DEFAULT_BASE_URL,
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("SAMBANOVA_API_KEY"));
    }
}
