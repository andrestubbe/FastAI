# FastAI Version Changelog

## [0.1.7] — 2026-08-19

### Added
- **OmniRoute Universal Gateway Support**: Integrated `OmniRouteClient` for connecting to 340+ providers via `FastAI.connect("omniroute:model")` with quota-aware auto-fallback and token compression.
- **Custom Gateway URL Query Parameter**: Support for query parameter URLs e.g. `FastAI.connect("omniroute:model?url=http://localhost:8000/v1")` or via constructor args.

---

## [0.1.6] — 2026-08-14

### Added
- **FastAIModel In-Process GPU Engine**: Added direct `llama:path/to/model.gguf` provider for zero-IPC local LLM inference via Vulkan & Metal.
- **Fluent Sampling Control**: Added `.withTemperature()`, `.withTopP()`, `.withTopK()`, `.withMaxTokens()`, `.withSystemPrompt()`, and `.withGpu(true/false)`.
- **OpenRouter Unified Gateway**: Support for 200+ cloud models with streaming.
- **Unified Streaming API**: Real-time token callbacks for both local and cloud providers.