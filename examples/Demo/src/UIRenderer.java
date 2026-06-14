import fastterminal.FastTerminalScene;

public class UIRenderer {

    private static final char[] BRAILLE = {'⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧', '⠇', '⠏'};

    public void render(AppState state, UIComponents ui, FastTerminalScene canvas) {
        int br = state.baseRow;
        text(canvas, 0, br, "FastAI");
        boolean showKey = state.isCloudProvider();

        if (state.stage == AppState.Stage.INPUT) {
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
            if (state.modelLoadingProgress != -1) {
                int y = br + 2;
                int startCol = 0;
                int width = UIConfig.PROGRESS_BAR_WIDTH;
                int canvasW = canvas.getWidth();
                int[] codepoints = canvas.getCodepointBuffer();
                if (state.modelLoadingProgress >= 0) {
                    int filledWidth = (int) Math.round((state.modelLoadingProgress / 100.0) * width);
                    for (int c = 0; c < width; c++) {
                        int col = startCol + c;
                        if (col >= 0 && col < canvasW && y >= 0 && y < canvas.getHeight()) {
                            int cp = codepoints[y * canvasW + col];
                            if (c < filledWidth) {
                                canvas.writeCell(col, y, cp, UIConfig.COLOR_PROGRESS_FILL_FG, UIConfig.COLOR_PROGRESS_FILL_BG);
                            }
                        }
                    }
                } else if (state.modelLoadingProgress == -2) {
                    int start = (state.spinIdx) % (width + UIConfig.INDETERMINATE_BLOCK_WIDTH) - UIConfig.INDETERMINATE_BLOCK_WIDTH;
                    for (int c = 0; c < width; c++) {
                        int col = startCol + c;
                        if (col >= 0 && col < canvasW && y >= 0 && y < canvas.getHeight()) {
                            int cp = codepoints[y * canvasW + col];
                            if (c >= start && c < start + UIConfig.INDETERMINATE_BLOCK_WIDTH) {
                                canvas.writeCell(col, y, cp, UIConfig.COLOR_INDETERMINATE_FG, UIConfig.COLOR_INDETERMINATE_BG);
                            }
                        }
                    }
                }
            }
        } else {
            text(canvas, 0, br + 1, state.selectedProvider);
            text(canvas, 0, br + 2, state.selectedModel);
            if (showKey) {
                String masked = ui.apiKeyTextBox.getText().replaceAll(".", "*");
                text(canvas, 0, br + 3, masked);
                text(canvas, 0, br + 4, state.promptText);
            } else {
                text(canvas, 0, br + 3, state.promptText);
            }
            int offset = showKey ? 5 : 4;
            int maxW = Math.max(1, state.cols - 1);
            String[] lines = TextUtils.wrap(state.responseText, maxW);
            boolean spinnerRunning = state.spinnerRunning;
            text(canvas, 0, br + offset, (lines.length > 0 ? lines[0] : "") + (spinnerRunning ? BRAILLE[state.spinIdx % BRAILLE.length] : ""));
            for (int i = 1; i < lines.length; i++) {
                text(canvas, 0, br + offset + i, lines[i]);
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
