package fastai;

import fastai.providers.*;

import java.io.File;
import java.util.function.Consumer;

public final class FastAI {

    private FastAI() {}

    public static AI connect(String spec, String... args) {
        String[] parts = spec.split(":", 2);
        String provider = parts[0].toLowerCase();
        String model = parts.length > 1 ? parts[1] : null;

        AIProvider impl = switch (provider) {
            case "ollama"   -> new OllamaClient(model);
            case "lmstudio" -> new LMStudioClient(model);
            case "openai"   -> new OpenAIClient(model, argOrNull(args, 0));
            case "claude"   -> new ClaudeClient(model, argOrNull(args, 0));
            case "mistral"  -> new MistralClient(model, argOrNull(args, 0));
            case "deepseek" -> new DeepSeekClient(model, argOrNull(args, 0));
            case "gemini"   -> new GeminiClient(model, argOrNull(args, 0));
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };

        return wrap(impl);
    }

    public static AI auto() {
        throw new UnsupportedOperationException("FastAI.auto() not implemented yet");
    }

    private static String argOrNull(String[] args, int index) {
        return args != null && args.length > index ? args[index] : null;
    }

    private static AI wrap(AIProvider provider) {
        return new AI() {
            @Override
            public String ask(String prompt) {
                return provider.generate(AIRequest.of(prompt));
            }

            @Override
            public String ask(String systemPrompt, String userPrompt) {
                return provider.generate(AIRequest.of(systemPrompt, userPrompt));
            }

            @Override
            public String ask(String prompt, File attachment) {
                return provider.generate(AIRequest.of(prompt, attachment));
            }

            @Override
            public void stream(String prompt, Consumer<String> tokenHandler) {
                provider.stream(AIRequest.of(prompt), tokenHandler);
            }
        };
    }
}
