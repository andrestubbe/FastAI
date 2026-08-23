# FastAI Version Changelog

## [0.1.13] — 2026-08-23

### Added
- **Native Anthropic Claude Client**: Full implementation of Anthropic Messages API (`/v1/messages`) supporting `claude-3-5-sonnet`, `claude-3-5-haiku`, and `claude-3-opus` with live streaming and shared connection pooling.
- **JMH Microbenchmark Suite**: Added benchmark project (`examples/Benchmark`) measuring raw byte-level SSE parsing throughput and JSON deserialization speed.

---

## [0.1.12] — 2026-08-23

### Added & Updated
- **Comprehensive Javadocs**: Added Javadocs across all core interfaces, factory methods, records, and internal parsers.
- **ModelRegistry Expansion**: Added current 2026 pricing tables for OpenAI (o1, o1-mini, o3-mini), Anthropic Claude (3.5 Sonnet, 3.5 Haiku, Opus), DeepSeek (R1, V3, Reasoner), and Llama 3.1 / 3.3.
- **Architectural Refactoring**: Internalized `SseStreamDecoder` and `UsageParser` into `fastai.internal`.

---

## [0.1.11] — 2026-08-23

### Added & Optimized
- **Byte-Level SSE Streaming Parser**: Zero-allocation SSE parser operating directly on raw byte buffers without intermediate line-string instantiations.
- **Shared HTTP/2 Connection Pool**: Consolidated singleton `HttpClient` across all provider instances to reuse connections, TLS handshakes, and thread pools.
- **Circuit-Breaking Health Cache**: Rate-limit and outage tracking in `FallbackRouterClient` with cooldown management.
- **In-Process GGUF Model Cache**: Shared model registry for `FastAIModel` instances in `LlamaCppClient` preventing redundant RAM/VRAM reloads.
- **FastJsonBuilder Integration**: Low-allocation payload serialization across OpenAI-compatible and Gemini clients.
- **Removed Debug Disk I/O**: Completely eliminated raw file writes and disk logging for maximum throughput.

---

## [0.1.9] — 2026-08-23

### Added
- **New Cloud Inference Providers**: Added native zero-overhead clients for GitHub Models (`github:`), NVIDIA NIM (`nvidia:`), SiliconFlow (`siliconflow:`), Cloudflare AI (`cloudflare:`), Together AI (`together:`), and Fireworks AI (`fireworks:`).
- **Tier Categorization**: Introduced clear documentation of free/rate-limited tiers (`PERMANENT_FREE`, `TRIAL`, `UNLIMITED LOCAL`, `PAID`).

---

## [0.1.8] — 2026-08-23

### Added
- **Free-Tier & Gateway Providers**: Integrated Groq (`groq:`), Cerebras (`cerebras:`), SambaNova (`sambanova:`), and LarpRouter (`larprouter:`).

---

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