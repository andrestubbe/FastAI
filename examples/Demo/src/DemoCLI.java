import fastai.AI;
import fastai.FastAI;

public class DemoCLI {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage:");
            System.out.println("  run-demo-cli ollama <modelname>");
            System.out.println("  run-demo-cli gemini <modelname> <apikey>");
            System.out.println("  run-demo-cli llama <path>");
            System.exit(1);
        }

        String provider = args[0].toLowerCase();
        String modelOrPath = args[1];

        // Ensure Windows console has Virtual Terminal support for clean printing (ANSI)
        System.out.print("\033[0G");
        System.out.flush();

            long connectStart = System.currentTimeMillis();
            try (AI ai = connectAI(provider, modelOrPath, args)) {
                long connectTime = System.currentTimeMillis() - connectStart;
                System.out.println("[BENCHMARK] Connection/loading completed in " + connectTime + " ms");
                if (ai == null) {
                    System.err.println("Failed to connect: AI is null");
                    System.exit(1);
                }

                System.out.println("Prompting AI: 'Explain quantum physics simply.'");
                System.out.println("\n--- STREAMING START ---");
                long streamStart = System.currentTimeMillis();
                java.util.concurrent.atomic.AtomicBoolean firstToken = new java.util.concurrent.atomic.AtomicBoolean(true);
                ai.stream("Explain quantum physics simply.", token -> {
                    if (firstToken.getAndSet(false)) {
                        long tftt = System.currentTimeMillis() - streamStart;
                        System.out.print("\n[BENCHMARK] Time-to-first-token: " + tftt + " ms\n");
                    }
                    System.out.print(token);
                    System.out.flush();
                });
                long totalStreamTime = System.currentTimeMillis() - streamStart;
                System.out.println("\n--- STREAMING END ---");
                System.out.println("[BENCHMARK] Total streaming response took " + totalStreamTime + " ms\n");
            } catch (Throwable t) {
            System.err.println("\nError: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static AI connectAI(String provider, String modelOrPath, String[] args) {
        if ("ollama".equals(provider)) {
            System.out.println("Connecting to Ollama model: " + modelOrPath);
            return FastAI.connect("ollama:" + modelOrPath);
        } else if ("gemini".equals(provider)) {
            if (args.length < 3) {
                System.err.println("API Key is required for Gemini: run-demo-cli gemini <modelname> <apikey>");
                System.exit(1);
            }
            String apiKey = args[2];
            System.out.println("Connecting to Gemini model: " + modelOrPath);
            return FastAI.connect("gemini:" + modelOrPath, apiKey);
        } else if ("llama".equals(provider)) {
            System.out.println("Connecting to local Llama model (GGUF): " + modelOrPath);
            return FastAI.connect("llama:" + modelOrPath);
        } else {
            System.err.println("Unknown provider: " + provider);
            System.exit(1);
            return null;
        }
    }
}
