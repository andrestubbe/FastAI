package fastai.providers;

public class CloudflareAIClient extends OpenAICompatibleClient {

    private static final String DEFAULT_MODEL = "@cf/meta/llama-3.1-8b-instruct";

    public CloudflareAIClient(String model, String apiKey, String accountId) {
        super("https://api.cloudflare.com/client/v4/accounts/" + (accountId != null ? accountId : System.getenv("CLOUDFLARE_ACCOUNT_ID")) + "/ai/v1",
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("CLOUDFLARE_API_TOKEN"));
    }
}
