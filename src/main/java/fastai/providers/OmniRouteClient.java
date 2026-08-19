package fastai.providers;

/**
 * OmniRoute Gateway Client — Universal LLM gateway supporting 340+ providers,
 * quota-aware fallback routing, and token compression.
 */
public class OmniRouteClient extends OpenAICompatibleClient {

    public static final String DEFAULT_URL = "http://localhost:8000/v1";

    public OmniRouteClient(String model) {
        this(model, null, DEFAULT_URL);
    }

    public OmniRouteClient(String model, String apiKey) {
        this(model, apiKey, DEFAULT_URL);
    }

    public OmniRouteClient(String model, String apiKey, String baseUrl) {
        super(baseUrl != null && !baseUrl.isEmpty() ? baseUrl : DEFAULT_URL, model, apiKey);
    }
}
