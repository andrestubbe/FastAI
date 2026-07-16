import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class AppState {

    public enum Stage {INPUT, LOADING, RESULT, ERROR}

    public volatile int cols = 120;
    public volatile int rows = 30;
    public volatile int baseRow = 0;

    public volatile Stage stage = Stage.INPUT;

    public volatile String selectedProvider = "-";
    public volatile String selectedModel = "-";
    public volatile String promptText = "";
    public volatile String responseText = "";
    public volatile String tokensText = "";
    public volatile String costText = "";
    public volatile String errorText = "";

    public volatile int spinIdx = 0;
    public volatile boolean spinnerRunning = false;

    public List<String> providers;
    public volatile List<String> models;

    public final LinkedBlockingQueue<KeyEvent> keyQueue = new LinkedBlockingQueue<>();
    public final List<ConversationTurn> completedTurns = new ArrayList<>();

    public int focusedIndex = 2; // 0 = Provider, 1 = Model, 2 = API Key / Prompt, 3 = Prompt (when Gemini is selected)

    public volatile int modelLoadingProgress = -1; // -1 = idle, -2 = indeterminate, 0-100 = percentage

    public static record KeyEvent(int vKey, char keyChar) {
    }

    public record ConversationTurn(String userPrompt, String responseText) {
    }

    public boolean isCloudProvider() {
        return selectedProvider != null && selectedProvider.equalsIgnoreCase("Gemini");
    }
}
