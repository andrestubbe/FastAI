package fastai.providers;

import fastai.AIProvider;
import fastai.AIRequest;
import fastai.AIResponse;
import fastaimodel.FastAIModel;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LlamaCppClient implements AIProvider, AutoCloseable {

    private static final ConcurrentHashMap<String, FastAIModel> MODEL_CACHE = new ConcurrentHashMap<>();

    private final FastAIModel model;
    private final String modelPath;

    public LlamaCppClient(final String modelPath) {
        this(modelPath, 4096, 0);
    }

    public LlamaCppClient(final String modelPath, final int ctxSize) {
        this(modelPath, ctxSize, 0);
    }

    public LlamaCppClient(final String modelPath, final int ctxSize, final int gpuLayers) {
        if (modelPath == null || modelPath.isEmpty()) {
            throw new IllegalArgumentException("Model path must be specified for local llama inference");
        }
        this.modelPath = modelPath;
        final String cacheKey = modelPath + "@" + ctxSize + "@" + gpuLayers;
        this.model = MODEL_CACHE.computeIfAbsent(cacheKey, k -> new FastAIModel(modelPath, ctxSize, gpuLayers));
    }

    private String buildPrompt(final AIRequest request) {
        String prompt = request.userPrompt;
        if (request.systemPrompt != null && !request.systemPrompt.isEmpty()) {
            prompt = "<|im_start|>system\n" + request.systemPrompt + "<|im_end|>\n<|im_start|>user\n" + request.userPrompt + "<|im_end|>\n<|im_start|>assistant\n";
        }
        return prompt;
    }

    private int getMaxTokens(final AIRequest request) {
        return request.maxTokens() != null ? request.maxTokens() : 1000;
    }

    @Override
    public AIResponse generate(final AIRequest request) {
        final StringBuilder sb = new StringBuilder();
        this.model.predict(this.buildPrompt(request), this.getMaxTokens(request), sb::append);
        return new AIResponse(sb.toString(), null, 0.0);
    }

    @Override
    public void stream(final AIRequest request, final Consumer<String> tokenHandler) {
        this.model.predict(this.buildPrompt(request), this.getMaxTokens(request), tokenHandler::accept);
    }

    @Override
    public List<String> getModels() {
        return Collections.singletonList(this.modelPath);
    }

    @Override
    public void close() {
        // Shared cached instances are closed by VM lifecycle
    }
}
