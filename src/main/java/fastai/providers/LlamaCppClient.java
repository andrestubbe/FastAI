package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
import fastai.AIResponse;
import fastaimodel.FastAIModel;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class LlamaCppClient implements AIProvider, AutoCloseable {
    private final FastAIModel model;
    private final String modelPath;

    public LlamaCppClient(String modelPath) {
        if (modelPath == null || modelPath.isEmpty()) {
            throw new IllegalArgumentException("Model path must be specified for local llama inference");
        }
        this.modelPath = modelPath;
        // Native local Llama.cpp: 4096 context size, 0 GPU layers (CPU execution)
        this.model = new FastAIModel(modelPath, 4096, 0);
    }

    private String buildPrompt(AIRequest request) {
        String prompt = request.userPrompt;
        if (request.systemPrompt != null && !request.systemPrompt.isEmpty()) {
            prompt = "<|im_start|>system\n" + request.systemPrompt + "<|im_end|>\n<|im_start|>user\n" + request.userPrompt + "<|im_end|>\n<|im_start|>assistant\n";
        }
        return prompt;
    }

    private int getMaxTokens(AIRequest request) {
        return request.maxTokens() != null ? request.maxTokens() : 1000;
    }

    @Override
    public AIResponse generate(AIRequest request) {
        StringBuilder sb = new StringBuilder();
        model.predict(buildPrompt(request), getMaxTokens(request), sb::append);
        return new AIResponse(sb.toString(), null, 0.0);
    }

    @Override
    public void stream(AIRequest request, Consumer<String> tokenHandler) {
        model.predict(buildPrompt(request), getMaxTokens(request), tokenHandler::accept);
    }

    @Override
    public List<String> getModels() {
        return Collections.singletonList(modelPath);
    }

    @Override
    public void close() {
        model.close();
    }
}
