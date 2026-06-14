package fastai.providers;

public class LlamaCppClient extends OpenAICompatibleClient {
    public LlamaCppClient(String model) {
        super("http://127.0.0.1:8080/v1",
                model != null && !model.isEmpty() ? model : "meta-llama-3-8b-instruct",
                null);
    }
}
