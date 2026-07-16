import fastai.FastAI;
import fastai.AI;
import fastai.Usage;
import fastaimemory.ConversationHistory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicReference;

public class AIExecutor {

    private static final Path LOG_FILE = Path.of("fastai-errors.log");

    private void logError(String message, Throwable t) {
        String logEntry = "[AIExecutor] " + message + "\n";
        if (t != null) {
            logEntry += t.toString() + "\n";
            for (StackTraceElement ste : t.getStackTrace()) {
                logEntry += "    at " + ste + "\n";
            }
            if (t.getCause() != null) {
                logEntry += "Caused by: " + t.getCause() + "\n";
            }
        }
        logEntry += "\n";
        try {
            Files.writeString(LOG_FILE, logEntry, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }

    public void runAsync(String promptContext, AppState state, UIComponents ui, ConversationHistory history, Runnable onDone) {
        new Thread(() -> {
            try {
                state.stage = AppState.Stage.LOADING;
                state.spinnerRunning = true;

                String provKey = state.selectedProvider.toLowerCase().replace(" ", "");
                String apiKey = state.isCloudProvider() ? ui.apiKeyTextBox.getText() : null;
                if (apiKey == null || apiKey.isEmpty()) {
                    apiKey = provKey.equals("gemini") ? System.getenv("GEMINI_API_KEY") : null;
                }

                logError("Connecting to " + provKey + ":" + state.selectedModel + (apiKey != null ? " (with API key)" : " (no API key)"), null);

                AI modelAI = FastAI.connect(provKey + ":" + state.selectedModel, apiKey != null ? new String[]{apiKey} : new String[0]);
                logError("USER INPUT: " + promptContext, null);
                AtomicReference<Usage> usageRef = new AtomicReference<>();
                AtomicReference<Throwable> errRef = new AtomicReference<>();

                Thread apiThread = new Thread(() -> {
                    try {
                        logError("Starting stream for: " + promptContext, null);
                        long startTime = System.currentTimeMillis();
                        modelAI.stream(promptContext, token -> {
                            if (token != null && !token.isEmpty()) {
                                logError("Received token (" + token.length() + " chars): " + token.substring(0, Math.min(50, token.length())), null);
                                if (state.spinnerRunning && (state.responseText == null || state.responseText.isEmpty())) {
                                    state.spinnerRunning = false;
                                }
                                state.responseText += token;
                            }
                        }, usage -> usageRef.set(usage));
                        long duration = System.currentTimeMillis() - startTime;
                        logError("Stream completed successfully in " + duration + "ms", null);
                    } catch (Throwable t) {
                        logError("Stream error", t);
                        errRef.set(t);
                    }
                });
                apiThread.start();
                apiThread.join();

                state.spinnerRunning = false;
                if (errRef.get() != null) {
                    state.errorText = errRef.get().getMessage();
                    state.stage = AppState.Stage.ERROR;
                    logError("Set ERROR stage: " + state.errorText, null);
                    return;
                }
                Usage usage = usageRef.get();
                if (usage != null) {
                    state.tokensText = "Tokens   : " + usage.totalTokens();
                }
                logError("GEMINI RESPONSE: " + (state.responseText != null ? state.responseText : "[empty]"), null);
                if (state.promptText != null && !state.promptText.isEmpty() && state.responseText != null && !state.responseText.isEmpty()) {
                    state.completedTurns.add(new AppState.ConversationTurn(state.promptText, state.responseText));
                }
                history.assistant(state.responseText);
                state.stage = AppState.Stage.RESULT;
            } catch (Throwable t) {
                logError("Top-level error in AIExecutor", t);
                state.errorText = t.getMessage();
                state.stage = AppState.Stage.ERROR;
            } finally {
                state.spinnerRunning = false;
                onDone.run();
            }
        }).start();
    }
}
