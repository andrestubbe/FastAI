import fastai.FastAI;
import fastai.AI;
import fastai.Usage;
import java.util.concurrent.atomic.AtomicReference;

public class AIExecutor {

    public void runAsync(AppState state, UIComponents ui, Runnable onDone) {
        new Thread(() -> {
            try {
                state.stage = AppState.Stage.LOADING;
                state.spinnerRunning = true;

                String provKey = state.selectedProvider.toLowerCase().replace(" ", "");
                String apiKey = state.isCloudProvider() ? ui.apiKeyTextBox.getText() : null;
                if (apiKey == null || apiKey.isEmpty()) {
                    apiKey = provKey.equals("gemini") ? System.getenv("GEMINI_API_KEY") : null;
                }

                AI modelAI = FastAI.connect(provKey + ":" + state.selectedModel, apiKey != null ? new String[]{apiKey} : new String[0]);
                AtomicReference<Usage> usageRef = new AtomicReference<>();
                AtomicReference<Throwable> errRef = new AtomicReference<>();
                
                Thread apiThread = new Thread(() -> {
                    try { modelAI.stream(state.promptText, token -> state.responseText += token, usage -> usageRef.set(usage)); }
                    catch (Throwable t) { errRef.set(t); }
                });
                apiThread.start();
                apiThread.join();
                
                state.spinnerRunning = false;
                if (errRef.get() != null) {
                    state.errorText = errRef.get().getMessage();
                    state.stage = AppState.Stage.ERROR;
                    return;
                }
                Usage usage = usageRef.get();
                if (usage != null) {
                    state.tokensText = "Tokens   : " + usage.totalTokens();
                }
                state.stage = AppState.Stage.RESULT;
            } catch (Throwable t) {
                state.errorText = t.getMessage();
                state.stage = AppState.Stage.ERROR;
            } finally {
                state.spinnerRunning = false;
                onDone.run();
            }
        }).start();
    }
}
