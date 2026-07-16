package fastai.providers;

public class DeepSeekClient extends OpenAICompatibleClient {
    public DeepSeekClient(String model, String apiKey) {
        super("https://api.deepseek.com/v1",
                model != null && !model.isEmpty() ? model : "deepseek-chat",
                apiKey);
    }
}
