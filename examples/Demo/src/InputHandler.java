import fastkeyboard.FastKeyboard;
import fastkeyboard.FastKeyboardImpl;
import fastterminal.FastTerminal;
import fastmouse.FastMouseListener;
import fastterminal.AnsiMouse;

public class InputHandler {

    private final AppState state;
    private final UIComponents ui;
    private FastKeyboard keyboard;
    private AnsiMouse mouseTracker;

    public InputHandler(AppState state, UIComponents ui) {
        this.state = state;
        this.ui = ui;
    }

    public void start() {
        keyboard = new FastKeyboardImpl();
        keyboard.startListening((deviceHandle, vKey, makeCode, isPressed, isE0, timestamp, keyChar) -> {
            if (isPressed && FastTerminal.isTerminalFocused()) {
                char ch = (keyChar != null && !keyChar.isEmpty()) ? keyChar.charAt(0) : '\0';
                state.keyQueue.offer(new AppState.KeyEvent(vKey, ch));
            }
        });

        mouseTracker = AnsiMouse.open(new FastMouseListener() {
            private int lastX = 0;
            private int lastY = 0;

            @Override
            public void onMouseMove(long deviceHandle, int deltaX, int deltaY, int absoluteX, int absoluteY) {
                lastX = absoluteX;
                lastY = absoluteY;
                ui.mouseDispatcher.onMouseMove(absoluteX, absoluteY);
            }

            @Override
            public void onMouseButton(long deviceHandle, int buttonId, boolean isPressed) {
                ui.mouseDispatcher.onMouseButton(lastX, lastY, isPressed);
                if (isPressed) {
                    if (ui.providerDropdown.contains(lastX, lastY)) {
                        state.focusedIndex = 0;
                    } else if (ui.modelDropdown.contains(lastX, lastY)) {
                        state.focusedIndex = 1;
                    } else if (state.isCloudProvider() && ui.apiKeyTextBox.contains(lastX, lastY)) {
                        state.focusedIndex = 2;
                    } else if (ui.promptTextBox.contains(lastX, lastY)) {
                        state.focusedIndex = state.isCloudProvider() ? 3 : 2;
                    } else {
                        ui.providerDropdown.setExpanded(false);
                        ui.modelDropdown.setExpanded(false);
                    }
                    ui.updateFocusStates(state);
                }
            }

            @Override
            public void onMouseWheel(long deviceHandle, int delta) {
                if (ui.providerDropdown.isExpanded() && ui.providerDropdown.contains(lastX, lastY)) {
                    ui.providerDropdown.handleMouseWheel(delta);
                } else if (ui.modelDropdown.isExpanded() && ui.modelDropdown.contains(lastX, lastY)) {
                    ui.modelDropdown.handleMouseWheel(delta);
                }
            }
        });
    }

    public void stop() {
        try { if (mouseTracker != null) mouseTracker.close(); } catch (Throwable ignored) {}
        try { if (keyboard != null) keyboard.stopListening(); } catch (Throwable ignored) {}
    }
}
