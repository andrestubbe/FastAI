package fastai.examples;

import fastai.FastAI;
import fastai.AI;
import java.io.File;

/**
 * Basic FastAI demo - unified AI client for Java.
 * 
 * Demonstrates connecting to local and cloud AI providers
 * with the same simple interface.
 */
public class BasicAIDemo {
    
    public static void main(String[] args) {
        System.out.println("FastAI - Basic Demo");
        System.out.println("====================\n");
        
        // Cloud AI (Gemini) - requires GEMINI_API_KEY env variable
        System.out.println("1. Cloud AI (Gemini) - requires GEMINI_API_KEY env variable");
        String geminiKey = System.getenv("GEMINI_API_KEY");
        if (geminiKey != null && !geminiKey.isEmpty()) {
            // Using gemini-2.5-flash as the cheapest/fastest default model
            AI geminiAI = FastAI.connect("gemini:gemini-2.5-flash", geminiKey);
            try {
                System.out.println("Available Gemini Models:");
                for (String m : geminiAI.getModels()) {
                    System.out.println(" - " + m);
                }
                System.out.println();
                String response = geminiAI.ask("Explain Java in one sentence.");
                System.out.println("Response: " + response);
            } catch (Exception e) {
                System.out.println("Gemini test failed: " + e.getMessage());
            }
        } else {
            System.out.println("Note: GEMINI_API_KEY not set. Skipping Gemini AI demo.");
        }
        // Local AI (Ollama)
        System.out.println("2. Local AI (Ollama)");
        try {
            AI ollamaAI = FastAI.connect("ollama");
            java.util.List<String> ollamaModels = ollamaAI.getModels();
            if (!ollamaModels.isEmpty()) {
                System.out.println("Available Ollama Models:");
                for (String m : ollamaModels) {
                    System.out.println(" - " + m);
                }
                System.out.println();
                // Connect with the first model found
                String firstModel = ollamaModels.get(0);
                System.out.println("Connecting to model '" + firstModel + "'...");
                AI ollamaModelAI = FastAI.connect("ollama:" + firstModel);
                String response = ollamaModelAI.ask("Explain Java in one sentence.");
                System.out.println("Response: " + response);
            } else {
                System.out.println("No local models found in Ollama.");
            }
        } catch (Exception e) {
            System.out.println("Note: Ollama is not running locally or failed: " + e.getMessage());
        }

        System.out.println();
        System.out.println("Demo complete!");
    }
}
