package fastai.providers;

public class SiliconFlowClient extends OpenAICompatibleClient {

    private static final String DEFAULT_BASE_URL = "https://api.siliconflow.cn/v1";
    private static final String DEFAULT_MODEL = "deepseek-ai/DeepSeek-V3";

    public SiliconFlowClient(String model, String apiKey) {
        super(DEFAULT_BASE_URL,
                model != null && !model.isEmpty() ? model : DEFAULT_MODEL,
                apiKey != null && !apiKey.isEmpty() ? apiKey : System.getenv("SILICONFLOW_API_KEY"));
    }
}
