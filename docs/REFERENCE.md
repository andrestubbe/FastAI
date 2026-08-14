# FastAI API Reference Manual

`FastAI` is a unified AI client interface for Java, seamlessly integrating local engines (Ollama, LM Studio, FastAIModel) and cloud providers (OpenAI, OpenRouter, Claude, Mistral, DeepSeek).

---

## Factory Interface: `fastai.FastAI`

### Connect Methods

- `public static AI connect(String providerSpec)`  
  Connects to a local model or provider without an API key (e.g. `"llama:models/qwen2.5.gguf"`, `"ollama:llama3.1"`).

- `public static AI connect(String providerSpec, String apiKey)`  
  Connects to a cloud provider or OpenRouter gateway (e.g. `"openai:gpt-4o"`, `"openrouter:anthropic/claude-3.5-sonnet"`).

---

## Core Interface: `fastai.AI`

- `String ask(String prompt)` — Executes synchronous text generation and returns response.
- `AI withSystemPrompt(String systemPrompt)` — Configures system persona.
- `AI withTemperature(float temperature)` — Sets sampling temperature (0.0 to 2.0).
- `AI withTopP(float topP)` — Sets nucleus sampling ratio (0.0 to 1.0).
- `AI withTopK(int topK)` — Sets top-k token selection filter.
- `AI withMaxTokens(int maxTokens)` — Limits maximum generated tokens.
- `AI withGpu(boolean enabled)` — Toggles GPU offloading for local GGUF models.
- `void stream(String prompt, TokenCallback callback)` — Streams real-time token output.