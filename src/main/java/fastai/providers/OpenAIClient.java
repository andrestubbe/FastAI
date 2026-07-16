package fastai.providers;

public class OpenAIClient extends OpenAICompatibleClient {
    public OpenAIClient(String model, String apiKey) {
        super("https://api.openai.com/v1",
                model != null && !model.isEmpty() ? model : "gpt-4o",
                apiKey);
    }
}
