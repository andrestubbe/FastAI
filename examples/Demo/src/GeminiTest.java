import fastai.FastAI;
import fastai.AI;
import fastai.AIRequest;
import fastai.AIResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class GeminiTest {
    public static void main(String[] args) {
        try {
            String apiKey = System.getenv("GEMINI_API_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                System.out.print("Enter your GEMINI_API_KEY: ");
                BufferedReader keyReader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
                apiKey = keyReader.readLine();
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    System.out.println("❌ Error: API Key is required.");
                    System.exit(1);
                }
                System.out.println();
            }

            // Defaults to gemini-1.5-flash as the standard low-cost flash model
            String model = "gemini-2.5-flash";
            if (args.length > 0) {
                model = args[0];
            }

            System.out.println("=================================================");
            System.out.println("      FastAI - Gemini Chat Test (Non-Stream)     ");
            System.out.println("      Model: " + model);
            System.out.println("      Type 'exit' or 'quit' to end session       ");
            System.out.println("=================================================");

            // Query and print all available models to see what this key supports
            try {
                AI listAI = FastAI.connect("gemini", new String[]{apiKey});
                java.util.List<String> availableModels = listAI.getModels();
                System.out.println("Available models: " + availableModels);
            } catch (Throwable t) {
                System.out.println("⚠️ Could not retrieve available models: " + t.getMessage());
            }

            AI ai = FastAI.connect("gemini:" + model, new String[]{apiKey});

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            while (true) {
                System.out.print("\nYou: ");
                String prompt = reader.readLine();
                if (prompt == null || prompt.equalsIgnoreCase("exit") || prompt.equalsIgnoreCase("quit")) {
                    break;
                }
                if (prompt.trim().isEmpty()) continue;

                System.out.print("Gemini: ");
                System.out.flush();
                
                try {
                    AIResponse response = ai.generate(AIRequest.of(prompt).temperature(0.7));
                    System.out.print(response.text());
                } catch (Throwable t) {
                    System.out.print("\n❌ Error: " + t.getMessage());
                }
                System.out.println();
            }
        } catch (Throwable t) {
            System.out.println("\n❌ Fatal Error: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
