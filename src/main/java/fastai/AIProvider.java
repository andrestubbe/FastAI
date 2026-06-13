package fastai;

import java.util.List;
import java.util.function.Consumer;

public interface AIProvider {

    String generate(AIRequest request);

    void stream(AIRequest request, Consumer<String> tokenHandler);

    List<String> getModels();
}

