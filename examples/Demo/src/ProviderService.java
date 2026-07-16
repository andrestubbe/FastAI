import fastai.FastAI;
import fastai.AI;

import java.util.List;

public class ProviderService {

    public List<String> loadModels(String provider, String apiKey) {
        String provKey = provider.toLowerCase().replace(" ", "");
        if (provKey.equals("-")) {
            return List.of("-");
        }
        if (provKey.equals("llamacpp") || provKey.equals("llama.cpp")) {
            java.util.List<String> ggufs = new java.util.ArrayList<>();
            java.io.File dir = new java.io.File("models");
            if (dir.exists() && dir.isDirectory()) {
                java.io.File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".gguf"));
                if (files != null) {
                    for (java.io.File f : files) {
                        ggufs.add(f.getName());
                    }
                }
            }
            if (ggufs.isEmpty()) {
                ggufs.add("no-models-found.gguf");
            }
            return ggufs;
        }
        if (provKey.equals("gemini") && (apiKey == null || apiKey.trim().isEmpty())) {
            return List.of("-");
        }

        // Try 1
        try {
            AI tempAI = FastAI.connect(provKey, apiKey != null ? new String[]{apiKey} : new String[0]);
            List<String> list = tempAI.getModels();
            if (list != null && !list.isEmpty()) {
                return list;
            }
        } catch (Throwable ignored) {
        }

        // Try starting the server
        boolean started = false;
        if (provKey.equals("ollama")) {
            try {
                new ProcessBuilder("cmd", "/c", "start", "/b", "ollama", "serve").start();
                started = true;
            } catch (Throwable ignored) {}
        } else if (provKey.equals("lmstudio")) {
            try {
                new ProcessBuilder("cmd", "/c", "lms", "server", "start").start();
                started = true;
            } catch (Throwable ignored) {}
        }

        if (started) {
            // Wait up to 3 seconds for server to start, checking every 1s
            for (int i = 0; i < 3; i++) {
                try {
                    Thread.sleep(1000);
                    AI tempAI = FastAI.connect(provKey, apiKey != null ? new String[]{apiKey} : new String[0]);
                    List<String> list = tempAI.getModels();
                    if (list != null && !list.isEmpty()) {
                        return list;
                    }
                } catch (Throwable ignored) {
                }
            }
        }

        return List.of("-");
    }

    public void selectModel(AppState state, String provider, String model) {
        if (provider != null && provider.equalsIgnoreCase("LM Studio") && model != null && !model.equals("-")) {
            new Thread(() -> {
                try {
                    state.modelLoadingProgress = 0;
                    Process process = new ProcessBuilder("cmd", "/c", "lms", "load", model).redirectErrorStream(true).start();
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)%").matcher(line);
                        if (matcher.find()) {
                            try {
                                int percent = Integer.parseInt(matcher.group(1));
                                state.modelLoadingProgress = percent;
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    process.waitFor();
                } catch (Throwable ignored) {
                } finally {
                    state.modelLoadingProgress = -1;
                }
            }).start();
        } else if (provider != null && provider.equalsIgnoreCase("Ollama") && model != null && !model.equals("-")) {
            new Thread(() -> {
                try {
                    state.modelLoadingProgress = -2; // Indeterminate
                    AI tempAI = FastAI.connect("ollama:" + model);
                    tempAI.generate(fastai.AIRequest.of("").temperature(0.0));
                } catch (Throwable ignored) {
                } finally {
                    state.modelLoadingProgress = -1;
                }
            }).start();
        } else if (provider != null && (provider.equalsIgnoreCase("llama.cpp") || provider.equalsIgnoreCase("llamacpp")) && model != null && !model.equals("-") && !model.equals("no-models-found.gguf")) {
            new Thread(() -> {
                try {
                    state.modelLoadingProgress = -2; // Indeterminate
                    // First kill any existing llama-server process
                    try {
                        new ProcessBuilder("cmd", "/c", "taskkill", "/f", "/im", "llama-server.exe").start().waitFor();
                    } catch (Throwable ignored) {}

                    // Now start llama-server with the new model
                    String modelPath = "models/" + model;
                    new ProcessBuilder("cmd", "/c", "start", "/b", "llama-server", "-m", modelPath, "-c", "2048", "--port", "8080").start();

                    // Wait for server to become responsive
                    for (int i = 0; i < 15; i++) {
                        try {
                            Thread.sleep(1000);
                            AI tempAI = FastAI.connect("llamacpp:" + model);
                            List<String> mList = tempAI.getModels();
                            if (mList != null && !mList.isEmpty()) {
                                break;
                            }
                        } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {
                } finally {
                    state.modelLoadingProgress = -1;
                }
            }).start();
        }
    }
}
