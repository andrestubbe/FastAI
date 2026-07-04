import fastterminal.FastTerminal;
import fastterminal.FastTerminalRenderer;
import fastterminal.FastTerminalScene;
import fastansi.FastANSI;
import fastaimemory.ConversationHistory;
import fastaimemory.MemoryContextBuilder;
import fastaimemory.PlainTextFormatter;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Demo {

    private static final int KEY_ENTER = 0x0D;
    private static final int KEY_UP = 0x26;
    private static final int KEY_DOWN = 0x28;
    private static final int KEY_ESC = 0x1B;

    private static AppState state;
    private static ProviderService providerService;
    private static UIComponents ui;
    private static UIRenderer uiRenderer;
    private static AIExecutor aiExecutor;
    private static InputHandler inputHandler;
    private static FastTerminalRenderer renderer;
    private static FastTerminalScene bgScene;
    private static FastTerminalScene canvas;
    private static ConversationHistory history;
    private static MemoryContextBuilder memoryBuilder;
    private static final String SYSTEM_PROMPT = "You are a helpful AI assistant.";
    private static final AtomicBoolean logicDone = new AtomicBoolean(false);
    private static final boolean[] hasSnapshot = {false};

    public static void main(String[] args) throws Exception {
        state = new AppState();
        providerService = new ProviderService();
        ui = new UIComponents();
        uiRenderer = new UIRenderer();
        aiExecutor = new AIExecutor();
        history = new ConversationHistory();
        history.system(SYSTEM_PROMPT);
        memoryBuilder = new MemoryContextBuilder(new PlainTextFormatter());

        state.providers = List.of("-", "Ollama", "Gemini", "LM Studio", "llama.cpp");
        state.models = List.of("-");

        initTerminal();
        initRenderer();
        ui.init(state, providerService);

        new Thread(() -> {
            if (!"-".equals(state.selectedProvider)) {
                try {
                    List<String> initialModels = providerService.loadModels(state.selectedProvider, ui.apiKeyTextBox.getText());
                    if (initialModels != null && !initialModels.isEmpty()) {
                        state.models = initialModels;
                        state.selectedModel = initialModels.get(0);
                        ui.modelDropdown.setSelectedIndex(0);
                        ui.modelDropdown.setOptions(initialModels);
                    }
                } catch (Throwable ignored) {
                }
            }
        }).start();

        ui.updatePositions(state);
        ui.updateFocusStates(state);

        inputHandler = new InputHandler(state, ui);
        inputHandler.start();
        initShutdownHook();

        startLogicThread();
        runRenderLoop();
    }

    private static void initTerminal() {
        try {
            int[] size = FastTerminal.getTerminalSize();
            if (size != null && size[0] > 0 && size[1] > 0) {
                state.cols = size[0];
                state.rows = size[1];
            }
        } catch (Throwable ignored) {
        }

        bgScene = FastTerminal.captureScreen(state.cols, state.rows);
        hasSnapshot[0] = bgScene.getWidth() > 0 && bgScene.getHeight() > 0;

        final int[] startCursorRow = {state.rows - 1};
        try {
            int[] cp = FastTerminal.getCursorPosition();
            if (cp != null && cp[1] >= 0) startCursorRow[0] = cp[1];
        } catch (Throwable ignored) {
        }
        state.baseRow = startCursorRow[0];

        System.out.print(FastANSI.CURSOR_HIDE);
        System.out.flush();
    }

    private static void initRenderer() {
        renderer = new FastTerminalRenderer(state.cols, state.rows);
        renderer.setDiffRenderingEnabled(true);
        renderer.suppressInitialFullRedraw();

        canvas = new FastTerminalScene(0, 0, state.cols, state.rows);
        canvas.setTransparentBackground(true);
        renderer.addScene(bgScene);
        renderer.addScene(canvas);
    }

    private static void initShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (inputHandler != null) inputHandler.stop();
            try {
                if (hasSnapshot[0]) {
                    int[] cp = bgScene.getCodepointBuffer();
                    int[] fg = bgScene.getFgBuffer();
                    int[] bg = bgScene.getBgBuffer();
                    int w = bgScene.getWidth();
                    int h = bgScene.getHeight();
                    StringBuilder sb = new StringBuilder(w * h * 20);
                    int curFg = -2, curBg = -2;
                    for (int row = 0; row < h; row++) {
                        sb.append(FastANSI.CSI).append(row + 1).append(";1H");
                        curFg = -2;
                        curBg = -2;
                        for (int col = 0; col < w; col++) {
                            int i = row * w + col;
                            int c = cp[i], f = fg[i], b = bg[i];
                            if (c == -99) {
                                sb.append(' ');
                                continue;
                            }
                            if (f != curFg) {
                                if (f == -1) sb.append(FastANSI.FG_DEFAULT);
                                else {
                                    int r = (f >> 16) & 0xFF, g = (f >> 8) & 0xFF, bl = f & 0xFF;
                                    sb.append(FastANSI.CSI).append("38;2;").append(r).append(';').append(g).append(';').append(bl).append('m');
                                }
                                curFg = f;
                            }
                            if (b != curBg) {
                                if (b == -1) sb.append(FastANSI.BG_DEFAULT);
                                else {
                                    int r = (b >> 16) & 0xFF, g = (b >> 8) & 0xFF, bl = b & 0xFF;
                                    sb.append(FastANSI.CSI).append("48;2;").append(r).append(';').append(g).append(';').append(bl).append('m');
                                }
                                curBg = b;
                            }
                            if (Character.isValidCodePoint(c)) sb.appendCodePoint(c);
                            else sb.append(' ');
                        }
                    }
                    sb.append(FastANSI.RESET);
                    sb.append(FastANSI.CSI).append(state.baseRow + 1).append(";1H");
                    byte[] bytes = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    System.out.write(bytes, 0, bytes.length);
                    System.out.flush();
                }
            } catch (Throwable ignored) {
            }
            System.out.print(FastANSI.CURSOR_SHOW + FastANSI.RESET);
            try {
                FastTerminal.setSystemCursorVisible(true);
            } catch (Throwable ignored) {
            }
        }));
    }

    private static void startLogicThread() {
        Thread logicThread = new Thread(() -> {
            try {
                while (true) {
                    while (state.stage == AppState.Stage.INPUT) {
                        AppState.KeyEvent event = state.keyQueue.take();
                        handleInputKey(event);
                    }

                    if (state.stage == AppState.Stage.LOADING) {
                        logicDone.set(false);
                        String promptContext = memoryBuilder.build(history);
                        aiExecutor.runAsync(promptContext, state, ui, history, () -> logicDone.set(true));
                        while (!logicDone.get()) {
                            Thread.sleep(50);
                        }

                        // After the AI finished, leave the response visible but position the prompt below it
                        if (state.stage == AppState.Stage.RESULT || state.stage == AppState.Stage.ERROR) {
                            try {
                                int initialPromptY = state.isCloudProvider() ? state.baseRow + 4 : state.baseRow + 3;
                                int nextPromptY = initialPromptY;
                                int maxW = Math.max(1, state.cols - 2);
                                for (AppState.ConversationTurn turn : state.completedTurns) {
                                    String[] promptLines = TextUtils.wrap(turn.userPrompt(), maxW);
                                    String[] respLines = TextUtils.wrap(turn.responseText(), maxW);
                                    int blockHeight = Math.max(1, promptLines.length) + Math.max(1, respLines.length);
                                    nextPromptY += blockHeight;
                                }
                                nextPromptY = Math.min(nextPromptY, Math.max(state.baseRow + 1, state.rows - 2));
                                ui.promptTextBox.setY(nextPromptY);
                            } catch (Throwable ignored) {
                            }
                            state.stage = AppState.Stage.INPUT;
                            ui.updateFocusStates(state);
                        }
                    }
                }
            } catch (Throwable t) {
                state.errorText = t.getMessage();
                state.stage = AppState.Stage.ERROR;
                logicDone.set(true);
            }
        });
        logicThread.setDaemon(true);
        logicThread.start();
    }

    private static void handleInputKey(AppState.KeyEvent event) {
        int vKey = event.vKey();
        char keyChar = event.keyChar();
        boolean showKey = state.isCloudProvider();
        int maxIdx = showKey ? 3 : 2;

        if (vKey == KEY_ESC) {
            if (state.focusedIndex == 0 && ui.providerDropdown.isExpanded()) {
                ui.providerDropdown.setExpanded(false);
                ui.providerDropdown.setHoveredItemIndex(-1);
            } else if (state.focusedIndex == 1 && ui.modelDropdown.isExpanded()) {
                ui.modelDropdown.setExpanded(false);
                ui.modelDropdown.setHoveredItemIndex(-1);
            } else {
                System.exit(0);
            }
        } else if (vKey == KEY_UP) {
            if (state.focusedIndex == 0 && ui.providerDropdown.isExpanded()) {
                int idx = ui.providerDropdown.getHoveredItemIndex();
                if (idx == -1) idx = ui.providerDropdown.getSelectedIndex();
                idx = (idx - 1 + state.providers.size()) % state.providers.size();
                ui.providerDropdown.setHoveredItemIndex(idx);
            } else if (state.focusedIndex == 1 && ui.modelDropdown.isExpanded()) {
                int idx = ui.modelDropdown.getHoveredItemIndex();
                if (idx == -1) idx = ui.modelDropdown.getSelectedIndex();
                idx = (idx - 1 + state.models.size()) % state.models.size();
                ui.modelDropdown.setHoveredItemIndex(idx);
            } else {
                if (state.focusedIndex > 0) {
                    state.focusedIndex--;
                    ui.updateFocusStates(state);
                }
            }
        } else if (vKey == KEY_DOWN) {
            if (state.focusedIndex == 0 && ui.providerDropdown.isExpanded()) {
                int idx = ui.providerDropdown.getHoveredItemIndex();
                if (idx == -1) idx = ui.providerDropdown.getSelectedIndex();
                idx = (idx + 1) % state.providers.size();
                ui.providerDropdown.setHoveredItemIndex(idx);
            } else if (state.focusedIndex == 1 && ui.modelDropdown.isExpanded()) {
                int idx = ui.modelDropdown.getHoveredItemIndex();
                if (idx == -1) idx = ui.modelDropdown.getSelectedIndex();
                idx = (idx + 1) % state.models.size();
                ui.modelDropdown.setHoveredItemIndex(idx);
            } else {
                if (state.focusedIndex < maxIdx) {
                    state.focusedIndex++;
                    ui.updateFocusStates(state);
                }
            }
        } else if (vKey == KEY_ENTER) {
            if (state.focusedIndex == 0) {
                if (ui.providerDropdown.isExpanded()) {
                    int idx = ui.providerDropdown.getHoveredItemIndex();
                    if (idx >= 0 && idx < state.providers.size()) {
                        ui.providerDropdown.setSelectedIndex(idx);
                        state.selectedProvider = state.providers.get(idx);
                        state.models = providerService.loadModels(state.selectedProvider, ui.apiKeyTextBox.getText());
                        state.selectedModel = state.models.get(0);
                        ui.modelDropdown.setSelectedIndex(0);
                        ui.modelDropdown.setOptions(state.models);
                        ui.updatePositions(state);
                        if (state.focusedIndex > maxIdx) state.focusedIndex = maxIdx;
                    }
                    ui.providerDropdown.setExpanded(false);
                    ui.providerDropdown.setHoveredItemIndex(-1);
                    ui.updateFocusStates(state);
                } else {
                    ui.providerDropdown.setExpanded(true);
                    ui.providerDropdown.setHoveredItemIndex(ui.providerDropdown.getSelectedIndex());
                    ui.updateFocusStates(state);
                }
            } else if (state.focusedIndex == 1) {
                if (ui.modelDropdown.isExpanded()) {
                    int idx = ui.modelDropdown.getHoveredItemIndex();
                    if (idx >= 0 && idx < state.models.size()) {
                        ui.modelDropdown.setSelectedIndex(idx);
                        state.selectedModel = state.models.get(idx);
                    }
                    ui.modelDropdown.setExpanded(false);
                    ui.modelDropdown.setHoveredItemIndex(-1);
                    ui.updateFocusStates(state);
                } else {
                    ui.modelDropdown.setExpanded(true);
                    ui.modelDropdown.setHoveredItemIndex(ui.modelDropdown.getSelectedIndex());
                    ui.updateFocusStates(state);
                }
            } else if (showKey && state.focusedIndex == 2) {
                state.focusedIndex = 3;
                ui.updateFocusStates(state);
            } else if ((!showKey && state.focusedIndex == 2) || (showKey && state.focusedIndex == 3)) {
                state.promptText = ui.promptTextBox.getText();
                if (state.promptText != null && !state.promptText.trim().isEmpty()) {
                    history.user(state.promptText);
                    state.responseText = "";
                    state.stage = AppState.Stage.LOADING;
                    ui.promptTextBox.setText("");
                }
            }
        } else {
            if (showKey && state.focusedIndex == 2) {
                ui.apiKeyTextBox.handleKey(vKey, keyChar);
                triggerApiKeyChange();
            } else if ((!showKey && state.focusedIndex == 2) || (showKey && state.focusedIndex == 3)) {
                ui.promptTextBox.handleKey(vKey, keyChar);
            }
        }
    }

    private static final Object lock = new Object();
    private static long lastKeyTime = 0;
    private static boolean reloadPending = false;
    private static Thread reloadThread = null;

    public static void triggerApiKeyChange() {
        synchronized (lock) {
            lastKeyTime = System.currentTimeMillis();
            reloadPending = true;
            if (reloadThread == null || !reloadThread.isAlive()) {
                reloadThread = new Thread(() -> {
                    while (true) {
                        long now;
                        synchronized (lock) {
                            now = System.currentTimeMillis();
                            if (!reloadPending) {
                                reloadThread = null;
                                break;
                            }
                            if (now - lastKeyTime >= 600) { // 600ms debounce
                                reloadPending = false;
                            }
                        }
                        if (!reloadPending) {
                            try {
                                String provider = state.selectedProvider;
                                String apiKey = ui.apiKeyTextBox.getText();
                                List<String> newModels = providerService.loadModels(provider, apiKey);
                                if (newModels != null && !newModels.isEmpty()) {
                                    state.models = newModels;
                                    if (state.selectedModel == null || !newModels.contains(state.selectedModel) || state.selectedModel.equals("-")) {
                                        state.selectedModel = newModels.get(0);
                                        ui.modelDropdown.setSelectedIndex(0);
                                    } else {
                                        ui.modelDropdown.setSelectedIndex(newModels.indexOf(state.selectedModel));
                                    }
                                    ui.modelDropdown.setOptions(newModels);
                                }
                            } catch (Throwable ignored) {
                            }
                            synchronized (lock) {
                                if (!reloadPending) {
                                    reloadThread = null;
                                    break;
                                }
                            }
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {
                        }
                    }
                });
                reloadThread.setDaemon(true);
                reloadThread.start();
            }
        }
    }

    private static void runRenderLoop() {
        while (true) {
            long t0 = System.currentTimeMillis();
            int[] sz = FastTerminal.getWindowSize(state.cols, state.rows);
            if (renderer.resize(sz[0], sz[1])) {
                state.cols = sz[0];
                state.rows = sz[1];
                canvas.resize(state.cols, state.rows);
                bgScene.resize(state.cols, state.rows);
            }
            canvas.clear();
            uiRenderer.render(state, ui, canvas);
            renderer.render();

            if ((state.stage == AppState.Stage.RESULT || state.stage == AppState.Stage.ERROR) && logicDone.get()) {
                AppState.KeyEvent event = state.keyQueue.poll();
                if (event != null && event.vKey() == KEY_ESC) System.exit(0);
            }

            long elapsed = System.currentTimeMillis() - t0;
            long sleep = Math.max(0, (1000 / 60) - elapsed);
            if (state.spinnerRunning || state.modelLoadingProgress != -1) sleep = Math.min(sleep, 80);
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ignored) {
            }
            if (state.spinnerRunning || state.modelLoadingProgress != -1) state.spinIdx++;
        }
    }
}
