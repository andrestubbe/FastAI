import fasttui.composable.Dropdown;
import fasttui.composable.TextBox;
import fasttui.component.MouseDispatcher;

public class UIComponents {

    public Dropdown providerDropdown;
    public Dropdown modelDropdown;
    public TextBox apiKeyTextBox;
    public TextBox promptTextBox;
    public final MouseDispatcher mouseDispatcher = new MouseDispatcher();

    public void init(AppState state, ProviderService providerService) {
        providerDropdown = new Dropdown(12, state.baseRow + 1, 30, state.providers, idx -> {
            state.selectedProvider = state.providers.get(idx);
            state.models = providerService.loadModels(state.selectedProvider, apiKeyTextBox.getText());
            state.selectedModel = state.models.get(0);
            modelDropdown.setSelectedIndex(0);
            modelDropdown.setOptions(state.models);

            updatePositions(state);
            int maxIdx = state.isCloudProvider() ? 3 : 2;
            if (state.focusedIndex > maxIdx) {
                state.focusedIndex = maxIdx;
            }
            updateFocusStates(state);
        });
        providerDropdown.setNormalBg(-1);
        providerDropdown.setHoverBg(0x27272A);

        modelDropdown = new Dropdown(12, state.baseRow + 2, 30, state.models, idx -> {
            if (state.models != null && idx >= 0 && idx < state.models.size()) {
                state.selectedModel = state.models.get(idx);
            }
        });
        modelDropdown.setNormalBg(-1);
        modelDropdown.setHoverBg(0x27272A);

        apiKeyTextBox = new TextBox(12, state.baseRow + 3, state.cols - 14);
        apiKeyTextBox.setBgColor(-1);
        apiKeyTextBox.setMasked(true);
        String envKey = System.getenv("GEMINI_API_KEY");
        if (envKey != null && !envKey.isEmpty()) {
            apiKeyTextBox.setText(envKey);
        }

        promptTextBox = new TextBox(12, state.baseRow + 3, state.cols - 14);
        promptTextBox.setFocused(true);

        updatePositions(state);
        updateFocusStates(state);
    }

    public void updatePositions(AppState state) {
        providerDropdown.setY(state.baseRow + 1);
        modelDropdown.setY(state.baseRow + 2);
        if (state.isCloudProvider()) {
            apiKeyTextBox.setY(state.baseRow + 3);
            apiKeyTextBox.setVisible(true);
            promptTextBox.setY(state.baseRow + 4);
        } else {
            apiKeyTextBox.setVisible(false);
            promptTextBox.setY(state.baseRow + 3);
        }
    }

    public void updateFocusStates(AppState state) {
        boolean showKey = state.isCloudProvider();
        providerDropdown.setFocused(state.focusedIndex == 0);
        modelDropdown.setFocused(state.focusedIndex == 1);
        if (showKey) {
            apiKeyTextBox.setFocused(state.focusedIndex == 2);
            promptTextBox.setFocused(state.focusedIndex == 3);
        } else {
            apiKeyTextBox.setFocused(false);
            promptTextBox.setFocused(state.focusedIndex == 2);
        }

        mouseDispatcher.unregister(providerDropdown);
        mouseDispatcher.unregister(modelDropdown);
        mouseDispatcher.unregister(apiKeyTextBox);
        mouseDispatcher.unregister(promptTextBox);

        if (providerDropdown.isExpanded()) {
            if (showKey) mouseDispatcher.register(apiKeyTextBox);
            mouseDispatcher.register(modelDropdown);
            mouseDispatcher.register(promptTextBox);
            mouseDispatcher.register(providerDropdown);
        } else if (modelDropdown.isExpanded()) {
            mouseDispatcher.register(providerDropdown);
            if (showKey) mouseDispatcher.register(apiKeyTextBox);
            mouseDispatcher.register(promptTextBox);
            mouseDispatcher.register(modelDropdown);
        } else {
            if (state.focusedIndex == 0) {
                if (showKey) mouseDispatcher.register(apiKeyTextBox);
                mouseDispatcher.register(modelDropdown);
                mouseDispatcher.register(promptTextBox);
                mouseDispatcher.register(providerDropdown);
            } else if (state.focusedIndex == 1) {
                mouseDispatcher.register(providerDropdown);
                if (showKey) mouseDispatcher.register(apiKeyTextBox);
                mouseDispatcher.register(promptTextBox);
                mouseDispatcher.register(modelDropdown);
            } else if (showKey && state.focusedIndex == 2) {
                mouseDispatcher.register(providerDropdown);
                mouseDispatcher.register(modelDropdown);
                mouseDispatcher.register(promptTextBox);
                mouseDispatcher.register(apiKeyTextBox);
            } else {
                mouseDispatcher.register(providerDropdown);
                mouseDispatcher.register(modelDropdown);
                if (showKey) mouseDispatcher.register(apiKeyTextBox);
                mouseDispatcher.register(promptTextBox);
            }
        }
    }
}
