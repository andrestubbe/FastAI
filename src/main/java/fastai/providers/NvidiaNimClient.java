package fastai.providers;

public class NvidiaNimClient extends OpenAICompatibleClient {

    private static final String DEFAULT_BASE_URL = "https://integrate.api.nvidia.com/v1";
    private static final String DEFAULT_MODEL = "meta/llama-3.1-70b-instruct";

    public NvidiaNimClient(String model, String apiKey) {
        super(DEFAULT_BASE_URL,
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("NVIDIA_API_KEY"));
    }
}
