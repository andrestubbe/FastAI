import fastterminal.FastTerminalScene;

public class UIRenderer {

    private static final char[] BRAILLE = {'⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧', '⠇', '⠏'};

    public void render(AppState state, UIComponents ui, FastTerminalScene canvas) {
        int br = state.baseRow;
        text(canvas, 0, br, "FastAI");
        boolean showKey = state.isCloudProvider();

        if (state.stage == AppState.Stage.INPUT) {
            text(canvas, 0, br + 1, "Provider : ");
            text(canvas, 0, br + 2, "Model    : ");
            if (showKey) {
                text(canvas, 0, br + 3, "API Key  : ");
                text(canvas, 0, br + 4, "Prompt   : ");
            } else {
                text(canvas, 0, br + 3, "Prompt   : ");
            }

            if (!ui.providerDropdown.isExpanded() && !ui.modelDropdown.isExpanded()) {
                ui.providerDropdown.render(canvas);
                ui.modelDropdown.render(canvas);
                if (showKey) ui.apiKeyTextBox.render(canvas);
                ui.promptTextBox.render(canvas);
            } else if (ui.providerDropdown.isExpanded()) {
                ui.promptTextBox.render(canvas);
                if (showKey) ui.apiKeyTextBox.render(canvas);
                ui.modelDropdown.render(canvas);
                ui.providerDropdown.render(canvas);
            } else if (ui.modelDropdown.isExpanded()) {
                ui.promptTextBox.render(canvas);
                if (showKey) ui.apiKeyTextBox.render(canvas);
                ui.providerDropdown.render(canvas);
                ui.modelDropdown.render(canvas);
            }
        } else {
            text(canvas, 0, br + 1, "Provider : " + state.selectedProvider);
            text(canvas, 0, br + 2, "Model    : " + state.selectedModel);
            if (showKey) {
                String masked = ui.apiKeyTextBox.getText().replaceAll(".", "*");
                text(canvas, 0, br + 3, "API Key  : " + masked);
                text(canvas, 0, br + 4, "Prompt   : " + state.promptText);
            } else {
                text(canvas, 0, br + 3, "Prompt   : " + state.promptText);
            }
            int offset = showKey ? 5 : 4;
            int maxW = Math.max(1, state.cols - 11);
            String[] lines = TextUtils.wrap(state.responseText, maxW);
            boolean spinnerRunning = state.spinnerRunning;
            text(canvas, 0, br + offset, "Response : " + (lines.length > 0 ? lines[0] : "") + (spinnerRunning ? BRAILLE[state.spinIdx % BRAILLE.length] : ""));
            for (int i = 1; i < lines.length; i++) {
                text(canvas, 11, br + offset + i, lines[i]);
            }
        }
    }

    private void text(FastTerminalScene canvas, int x, int y, String s) {
        if (y < 0 || y >= canvas.getHeight() || s == null) return;
        for (int i = 0; i < s.length() && x + i < canvas.getWidth(); i++) {
            canvas.writeCell(x + i, y, s.codePointAt(i), -1, -1);
        }
    }
}
