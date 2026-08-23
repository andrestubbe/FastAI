package fastai.providers;

public class LarpRouterClient extends OpenAICompatibleClient {

    private static final String DEFAULT_BASE_URL = "https://api.larprouter.com/v1";
    private static final String DEFAULT_MODEL = "gpt-5.6-sol";

    public LarpRouterClient(String model, String apiKey) {
        super(DEFAULT_BASE_URL,
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("LARPROUTER_API_KEY"));
    }
}
