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
        
        // Example 1: Local AI (Ollama)
        System.out.println("1. Local AI (Ollama) - requires ollama running locally");
        try {
            AI localAI = FastAI.connect("ollama:llama3.1");
            String response = localAI.ask("Explain Java in one sentence.");
            System.out.println("Response: " + response);
        } catch (Exception e) {
            System.out.println("Note: Ollama not running locally. Skipping local AI demo.");
            System.out.println("      Install from https://ollama.com to test local AI.");
        }
        System.out.println();
        
        // Example 2: Cloud AI (requires API key)
        System.out.println("2. Cloud AI (OpenAI) - requires OPENAI_API_KEY env variable");
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey != null && !apiKey.isEmpty()) {
            AI cloudAI = FastAI.connect("openai:gpt-4o", apiKey);
            String response = cloudAI.ask("What is the capital of France?");
            System.out.println("Response: " + response);
        } else {
            System.out.println("Note: OPENAI_API_KEY not set. Skipping cloud AI demo.");
            System.out.println("      Set your API key to test cloud providers.");
        }
        System.out.println();
        
        // Example 3: Streaming (local AI)
        System.out.println("3. Streaming demo (Ollama)");
        try {
            AI streamAI = FastAI.connect("ollama:llama3.1");
            System.out.print("Streaming: ");
            streamAI.stream("Count from 1 to 5", token -> {
                System.out.print(token);
                System.out.flush();
            });
            System.out.println();
        } catch (Exception e) {
            System.out.println("Note: Ollama not running. Skipping streaming demo.");
        }
        System.out.println();
        
        System.out.println("Demo complete!");
        System.out.println("\nSupported providers: ollama, lmstudio, openai, claude, mistral, deepseek");
    }
}
