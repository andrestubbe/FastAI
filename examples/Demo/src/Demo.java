import fastai.AI;
import fastai.FastAI;

/**
 * FastAI Unified CLI Demo Application.
 * Supports both local and cloud AI providers via command line arguments.
 * 
 * Usage Modes:
 *   Mode 1 (Local - 3 parameters):
 *     run-demo.bat <service> <model> "<payload>"
 *     Example: run-demo.bat ollama llama3.1 "Explain quantum computing simply."
 *     Example: run-demo.bat llamacpp "models/ggml-model.gguf" "Hello Llama!"
 * 
 *   Mode 2 (Cloud - 4 parameters):
 *     run-demo.bat <service> <apikey> <model> "<payload>"
 *     Example: run-demo.bat openrouter %OPENROUTER_API_KEY% anthropic/claude-3.5-sonnet "Tell me a joke."
 *     Example: run-demo.bat gemini %GEMINI_API_KEY% gemini-2.0-flash "Write a haiku."
 */
public class Demo {

    public static void main(String[] args) {
        System.out.println("===================================================");
        System.out.println("   FastAI — Unified AI Execution Engine Demo      ");
        System.out.println("===================================================");

        String service;
        String apiKey = null;
        String model;
        String prompt;

        if (args.length == 3) {
            // Mode 1: Local / API key not needed
            // Usage: service model "payload"
            service = args[0].trim();
            model = args[1].trim();
            prompt = args[2].trim();
        } else if (args.length >= 4) {
            // Mode 2: Cloud / API key provided
            // Usage: service apikey model "payload"
            service = args[0].trim();
            apiKey = args[1].trim();
            model = args[2].trim();
            
            // Join any remaining arguments as prompt payload if quotes were split
            StringBuilder sb = new StringBuilder();
            for (int i = 3; i < args.length; i++) {
                if (i > 3) sb.append(" ");
                sb.append(args[i]);
            }
            prompt = sb.toString().trim();
        } else {
            printUsage();
            return;
        }

        String connSpec = service + ":" + model;
        System.out.println("Provider Spec : " + connSpec);
        if (apiKey != null && !apiKey.isEmpty()) {
            String masked = apiKey.length() > 8 ? apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4) : "***";
            System.out.println("API Key       : " + masked);
        }
        System.out.println("Prompt        : " + prompt);
        System.out.println("---------------------------------------------------");
        System.out.println("🤖 Generating Response (Streaming)...\n");

        long startTime = System.currentTimeMillis();

        try {
            AI ai;
            if (apiKey != null && !apiKey.isEmpty()) {
                ai = FastAI.connect(connSpec, apiKey);
            } else {
                ai = FastAI.connect(connSpec);
            }

            ai.stream(prompt, token -> {
                System.out.print(token);
                System.out.flush();
            });

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("\n\n---------------------------------------------------");
            System.out.println("⏱️ Total Processing Time: " + duration + " ms");
            System.out.println("===================================================");

        } catch (Exception e) {
            System.err.println("\n[ERROR] Generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("\n[USAGE INSTRUCTIONS]");
        System.out.println("  1) Local Provider Mode (3 arguments):");
        System.out.println("     run-demo.bat <service> <model> \"<payload>\"");
        System.out.println("     Example: run-demo.bat ollama llama3.1 \"Explain quantum physics simply.\"");
        System.out.println("     Example: run-demo.bat llamacpp \"models/model.gguf\" \"Hello Llama!\"");
        System.out.println();
        System.out.println("  2) Cloud Provider Mode (4 arguments):");
        System.out.println("     run-demo.bat <service> <apikey> <model> \"<payload>\"");
        System.out.println("     Example: run-demo.bat openrouter %OPENROUTER_API_KEY% anthropic/claude-3.5-sonnet \"Tell me a joke.\"");
        System.out.println("     Example: run-demo.bat gemini %GEMINI_API_KEY% gemini-2.0-flash \"Write a haiku.\"");
        System.out.println("===================================================");
    }
}
