import fastterminal.FastTerminalScene;

public class UIRenderer {

    private static final char[] BRAILLE = {'⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧', '⠇', '⠏'};

    public void render(AppState state, UIComponents ui, FastTerminalScene canvas) {
        int br = state.baseRow;
        text(canvas, 0, br, "FastAI");
        renderHeaderStatus(canvas, state, br);
        boolean showKey = state.isCloudProvider();

        if (state.stage == AppState.Stage.INPUT) {
            if (!ui.providerDropdown.isExpanded() && !ui.modelDropdown.isExpanded()) {
                ui.providerDropdown.render(canvas);
                ui.modelDropdown.render(canvas);
                if (showKey) ui.apiKeyTextBox.render(canvas);
                renderCompletedTurns(canvas, state, ui);
                drawPromptField(canvas, state, ui);
            } else if (ui.providerDropdown.isExpanded()) {
                drawPromptField(canvas, state, ui);
                if (showKey) ui.apiKeyTextBox.render(canvas);
                ui.modelDropdown.render(canvas);
                ui.providerDropdown.render(canvas);
            } else if (ui.modelDropdown.isExpanded()) {
                drawPromptField(canvas, state, ui);
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
            // Show provider/model and API key if Gemini is selected
            text(canvas, 0, br + 1, state.selectedProvider);
            text(canvas, 0, br + 2, state.selectedModel);
            if (showKey) {
                ui.apiKeyTextBox.render(canvas);
            }

            renderCompletedTurns(canvas, state, ui);

            int promptLine = ui.promptTextBox.getY();
            String promptPrefix = ">>> ";

            // Draw the user prompt (left aligned)
            text(canvas, 0, promptLine, promptPrefix + state.promptText);

            int maxW = Math.max(1, state.cols - 2);
            String[] lines = TextUtils.wrap(state.responseText, maxW);

            int respStart = promptLine + 1;

            if (state.spinnerRunning) {
                // Show spinner on the line directly below the prompt while the answer is streaming
                int spinnerLine = respStart;
                canvas.writeCell(0, spinnerLine, BRAILLE[state.spinIdx % BRAILLE.length], UIConfig.COLOR_DEFAULT_FG, UIConfig.COLOR_DEFAULT_BG);

                // Render any incoming response below the spinner
                for (int i = 0; i < lines.length; i++) {
                    text(canvas, 0, spinnerLine + 1 + i, lines[i]);
                }
            } else {
                // Render the AI response directly under the prompt and keep the next prompt hidden until the turn is finished
                for (int i = 0; i < lines.length; i++) {
                    text(canvas, 0, respStart + i, lines[i]);
                }
            }
        }
    }

    private void renderHeaderStatus(FastTerminalScene canvas, AppState state, int br) {
        try {
            String provider = state.selectedProvider != null && !state.selectedProvider.isEmpty() ? state.selectedProvider : "-";
            String model = state.selectedModel != null && !state.selectedModel.isEmpty() ? state.selectedModel : "-";
            String status = "  ⚙️ 📦 " + provider + " / " + model;
            int x = Math.max(0, Math.min(canvas.getWidth() - status.length(), 16));
            text(canvas, x, br, status);
        } catch (Throwable ignored) {
        }
    }

    private void renderCompletedTurns(FastTerminalScene canvas, AppState state, UIComponents ui) {
        try {
            int promptY = ui.promptTextBox.getY();
            int maxW = Math.max(1, state.cols - 2);
            int cursorY = promptY - 2;
            for (int i = state.completedTurns.size() - 1; i >= 0; i--) {
                AppState.ConversationTurn turn = state.completedTurns.get(i);
                String[] promptLines = TextUtils.wrap(turn.userPrompt(), maxW);
                String[] respLines = TextUtils.wrap(turn.responseText(), maxW);
                int blockHeight = Math.max(1, promptLines.length) + Math.max(1, respLines.length);
                int startY = cursorY - blockHeight + 1;
                text(canvas, 0, startY, ">>> " + turn.userPrompt());
                for (int line = 1; line < Math.max(1, promptLines.length); line++) {
                    text(canvas, 0, startY + line, promptLines[line]);
                }
                    int responseStartY = startY + Math.max(1, promptLines.length);
                for (int line = 0; line < Math.max(1, respLines.length); line++) {
                    text(canvas, 0, responseStartY + line, respLines[line]);
                }
                cursorY = startY - 3; // add one blank line between completed interactions
            }
        } catch (Throwable ignored) {
        }
    }

    private void drawPromptField(FastTerminalScene canvas, AppState state, UIComponents ui) {
        String prefix = ">>> ";
        int prefixX = 0;
        int y = ui.promptTextBox.getY();
        String text = ui.promptTextBox.getText();
        String placeholder = "Send a message (use UP+DOWN to select Service and model, ESC to exit)";

        text(canvas, prefixX, y, prefix);
        ui.promptTextBox.render(canvas);

        if (text == null || text.isEmpty()) {
            String display = placeholder;
            int width = ui.promptTextBox.getWidth();
            if (display.length() > width) {
                display = display.substring(0, width);
            }
            for (int i = 0; i < display.length(); i++) {
                canvas.writeCell(ui.promptTextBox.getX() + i, y, display.charAt(i), 0x757575, ui.promptTextBox.getBgColor());
            }
            // Draw a blinking caret at the start of the prompt textbox
            try {
                long t = System.currentTimeMillis() / 500;
                if ((t % 2) == 0) {
                    int caretX = ui.promptTextBox.getX();
                    canvas.writeCell(caretX, y, '|', 0xFFFFFF, ui.promptTextBox.getBgColor());
                }
            } catch (Throwable ignored) {
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
