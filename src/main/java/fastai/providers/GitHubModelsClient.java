package fastai.providers;

public class GitHubModelsClient extends OpenAICompatibleClient {

    private static final String DEFAULT_BASE_URL = "https://models.inference.ai.azure.com";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";

    public GitHubModelsClient(String model, String apiKey) {
        super(DEFAULT_BASE_URL,
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                apiKey != null && !apiKey.isEmpty() ? apiKey : (System.getenv("GITHUB_TOKEN") != null ? System.getenv("GITHUB_TOKEN") : System.getenv("GH_TOKEN")));
    }
}
