package fastai.providers;

public class MistralClient extends OpenAICompatibleClient {
    public MistralClient(String model, String apiKey) {
        super("https://api.mistral.ai/v1",
                model != null && !model.isEmpty() ? model : "mistral-tiny",
                apiKey);
    }
}
