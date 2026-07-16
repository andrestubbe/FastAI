package fastai.providers;

public class OllamaClient extends OpenAICompatibleClient {
    public OllamaClient(String model) {
        super("http://127.0.0.1:11434/v1",
                model != null && !model.isEmpty() ? model : "llama3.1",
                null);
    }
}
