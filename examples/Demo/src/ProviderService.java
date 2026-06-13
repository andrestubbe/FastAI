import fastai.FastAI;
import fastai.AI;

import java.util.List;

public class ProviderService {

    public List<String> loadModels(String provider, String apiKey) {
        try {
            String provKey = provider.toLowerCase().replace(" ", "");
            if (provKey.equals("gemini") && (apiKey == null || apiKey.trim().isEmpty())) {
                return List.of("-");
            }
            AI tempAI = FastAI.connect(provKey, apiKey != null ? new String[]{apiKey} : new String[0]);
            List<String> list = tempAI.getModels();
            if (list != null && !list.isEmpty()) {
                return list;
            }
        } catch (Throwable ignored) {
        }

        return List.of("-");
    }
}
