package fastai.providers;

public class TogetherAIClient extends OpenAICompatibleClient {

    private static final String DEFAULT_BASE_URL = "https://api.together.xyz/v1";
    private static final String DEFAULT_MODEL = "meta-llama/Meta-Llama-3.1-70B-Instruct-Turbo";

    public TogetherAIClient(String model, String apiKey) {
        super(DEFAULT_BASE_URL,
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("TOGETHER_API_KEY"));
    }
}
