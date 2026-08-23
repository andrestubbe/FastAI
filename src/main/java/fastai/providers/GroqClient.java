package fastai.providers;

public class GroqClient extends OpenAICompatibleClient {

    private static final String DEFAULT_BASE_URL = "https://api.groq.com/openai/v1";
    private static final String DEFAULT_MODEL = "llama-3.3-70b-versatile";

    public GroqClient(String model, String apiKey) {
        super(DEFAULT_BASE_URL,
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("GROQ_API_KEY"));
    }
}
