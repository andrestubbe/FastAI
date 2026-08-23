package fastai.providers;

public class LMStudioClient extends OpenAICompatibleClient {

    private static final String DEFAULT_BASE_URL = "http://127.0.0.1:1234/v1";
    private static final String DEFAULT_MODEL = "meta-llama-3-8b-instruct";

    public LMStudioClient(String model) {
        super(DEFAULT_BASE_URL,
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                null);
    }
}

