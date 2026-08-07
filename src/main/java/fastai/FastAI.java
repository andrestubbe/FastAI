package fastai;

import fastai.providers.*;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public final class FastAI {

    private FastAI() {
    }

    public static AI connect(String spec, String... args) {
        String[] parts = spec.split(":", 2);
        String provider = parts[0].toLowerCase();
        String model = parts.length > 1 ? parts[1] : null;

        AIProvider impl = switch (provider) {
            case "ollama" -> new OllamaClient(model);
            case "lmstudio" -> new LMStudioClient(model);
            case "llamacpp", "llama.cpp", "llama" -> createLlamaCppClient(model, args);
            case "openai" -> new OpenAIClient(model, argOrNull(args, 0));
            case "claude" -> new ClaudeClient(model, argOrNull(args, 0));
            case "mistral" -> new MistralClient(model, argOrNull(args, 0));
            case "deepseek" -> new DeepSeekClient(model, argOrNull(args, 0));
            case "gemini" -> new GeminiClient(model, argOrNull(args, 0));
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };

        return wrap(impl);
    }

    private static LlamaCppClient createLlamaCppClient(String modelSpec, String[] args) {
        if (modelSpec == null) return new LlamaCppClient(null);

        int ctxSize = 4096;
        int gpuLayers = 0;
        String modelPath = modelSpec;

        if (modelSpec.contains("?")) {
            String[] parts = modelSpec.split("\\?", 2);
            modelPath = parts[0];
            String queryString = parts[1];
            for (String param : queryString.split("&")) {
                String[] kv = param.split("=", 2);
                if (kv.length == 2) {
                    if ("ctx".equalsIgnoreCase(kv[0]) || "context".equalsIgnoreCase(kv[0])) {
                        try { ctxSize = Integer.parseInt(kv[1]); } catch (NumberFormatException ignored) {}
                    } else if ("gpu".equalsIgnoreCase(kv[0]) || "gpulayers".equalsIgnoreCase(kv[0])) {
                        try { gpuLayers = Integer.parseInt(kv[1]); } catch (NumberFormatException ignored) {}
                    }
                }
            }
        } else if (args != null && args.length > 0 && args[0] != null) {
            try { ctxSize = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
            if (args.length > 1 && args[1] != null) {
                try { gpuLayers = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
            }
        }

        return new LlamaCppClient(modelPath, ctxSize, gpuLayers);
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
            public AIResponse generate(AIRequest request) {
                return provider.generate(request);
            }

            @Override
            public void stream(String prompt, Consumer<String> tokenHandler) {
                provider.stream(AIRequest.of(prompt), tokenHandler);
            }

            @Override
            public void stream(String prompt, Consumer<String> tokenHandler, Consumer<Usage> usageHandler) {
                provider.stream(AIRequest.of(prompt), tokenHandler, usageHandler);
            }

            @Override
            public void stream(String systemPrompt, String userPrompt, Consumer<String> tokenHandler) {
                provider.stream(AIRequest.of(systemPrompt, userPrompt), tokenHandler);
            }

            @Override
            public void stream(String systemPrompt, String userPrompt, Consumer<String> tokenHandler, Consumer<Usage> usageHandler) {
                provider.stream(AIRequest.of(systemPrompt, userPrompt), tokenHandler, usageHandler);
            }

            @Override
            public List<String> getModels() {
                return provider.getModels();
            }

            @Override
            public void close() throws Exception {
                if (provider instanceof AutoCloseable) {
                    ((AutoCloseable) provider).close();
                }
            }
        };
    }
}

