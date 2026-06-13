package fastai.providers;

public class LMStudioClient extends OpenAICompatibleClient {
    public LMStudioClient(String model) {
        super("http://127.0.0.1:1234/v1",
                model != null && !model.isEmpty() ? model : "meta-llama-3-8b-instruct",
                null);
    }
}
