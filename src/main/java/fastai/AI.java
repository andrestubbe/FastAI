package fastai;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public interface AI {

    String ask(String prompt);

    String ask(String systemPrompt, String userPrompt);

    String ask(String prompt, File attachment);

    void stream(String prompt, Consumer<String> tokenHandler);

    default void stream(String systemPrompt, String userPrompt, Consumer<String> tokenHandler) {
        stream(systemPrompt + "\n\n" + userPrompt, tokenHandler);
    }

    List<String> getModels();
}

