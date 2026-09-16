# FastAI 0.1.14 [ALPHA-2026-08-23]: Unified AI Client for Java

[![Status](https://img.shields.io/badge/status-0.1.14-brightgreen.svg)](https://github.com/andrestubbe/FastAI/releases/tag/0.1.14)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.14-green.svg)](https://jitpack.io/#andrestubbe/FastAI)

---

**💡 One interface for all Local, Gateway, and Cloud AI models — No JSON, No HTTP, No Boilerplate.**

**FastAI** is a minimalist, hyper-fast Java AI library that unifies 20+ major LLM providers (OmniRoute, Groq, Cerebras, SambaNova, GitHub Models, NVIDIA NIM, SiliconFlow, Cloudflare, Together AI, Fireworks, LarpRouter, Ollama, LM Studio, OpenAI, OpenRouter, Claude, Mistral, DeepSeek, Gemini) behind a single, elegant interface. Built for Java developers who need **drop-in AI**, **multi-provider interchangeability**, and clean FastJava-style code without dependency hell.

[**Watch Demo (YouTube)**](https://youtu.be/kjfyZebSdj4)

[![FastAI Showcase](docs/screenshot.png)](https://youtu.be/kjfyZebSdj4)

---

## Quick Start

```java
import fastai.AI;
import fastai.FastAI;

public class Demo {
    public static void main(String[] args) {
        // 1. Direct Local GGUF Engine with full Vulkan/Metal GPU Offloading
        AI gpuAI = FastAI.connect("llama:models/qwen2.5-coder-1.5b.gguf")
                         .withGpu(true)
                         .withContextSize(2048);

        // 2. Fluid streaming with full sampling control
        gpuAI.withSystemPrompt("You are an expert Java performance engineer.")
             .withTemperature(0.7f)
             .withMaxTokens(256)
             .stream("Write a quicksort in Java:", token -> {
                 System.out.print(token);
                 System.out.flush();
             });

        // 3. Cloud provider — identical interface, one string change
        AI cloudAI = FastAI.connect("openai:gpt-4o", System.getenv("OPENAI_API_KEY"));
        cloudAI.withTemperature(0.2f)
               .withMaxTokens(500)
               .stream("Summarize the latest 2026 tech trends:", System.out::print);
    }
}
```

---

## Table of Contents

- [Why FastAI?](#why-fastai)
- [Quick Start](#quick-start)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Providers Supported](#providers-supported)
- [Performance Benchmarks](#performance-benchmarks)
- [API Quick Reference](#api-quick-reference)
- [Technical Demos & Benchmarks](#technical-demos--benchmarks)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)
- [License](#license)

---

## Why FastAI?

Current Java AI libraries (`LangChain4j`, `Spring AI`) are massive, framework-heavy, and come with dependency conflicts. Direct provider SDKs lock you into a single vendor.

| Feature | LangChain4j / Spring AI | FastAI |
|:---|:---|:---|
| **Dependencies** | 15-20+ transitive JARs | Zero external dependencies |
| **JAR Size** | 5-10 MB | ~50 KB |
| **Startup Time** | 2-10 seconds | <100 ms |
| **Provider Switch** | Refactor required | Change one string |
| **Local GPU Engine** | External process / IPC overhead | In-process GGUF via `FastAIModel` (zero IPC) |
| **Streaming** | Framework-specific callbacks | Unified `stream(prompt, handler)` across all providers |
| **Learning Curve** | Hours of documentation | 5 minutes |

---

## Key Features

- 🌐 **20+ Providers, One Interface**: Local Ollama, llama.cpp GPU, Groq, Cerebras, Gemini, OpenAI, Claude, OpenRouter, and more — all via `FastAI.connect(spec)`.
- ⚡ **In-Process Local GPU Engine**: Zero-IPC local GGUF inference via **FastAIModel** with Vulkan (Intel/AMD/NVIDIA) and Metal (Apple Silicon) GPU offloading.
- 🔄 **Auto-Fallback Free Router**: `FastAI.auto()` cascades across free-tier providers on rate limits or outages with circuit-breaking health caches.
- 🌊 **Streaming First**: Every provider exposes the same `stream(prompt, handler)` callback — no provider-specific setup.
- 📎 **Simple Attachments**: Pass a `java.io.File` directly for vision/multimodal requests. FastAI handles Base64/Multipart encoding.
- 🚀 **Zero Dependencies**: Pure Java 17+, no Jackson, no Spring, no Netty.
- 🎭 **System Prompt Support**: Native system vs. user prompt separation on all providers.

---

## Real-World Use Cases

- 🤖 **AI Agent Loops**: Run continuous LLM decision cycles against free-tier Groq or Cerebras endpoints without rate-limit interruptions using `FastAI.auto()`.
- 💻 **Local Air-Gapped Code Completion**: In-process GGUF inference (`llama:model.gguf`) with full GPU acceleration for zero-latency IDE completion in secure environments.
- 👁️ **Multimodal Vision Pipelines**: Pass `FastScreen` captures directly to vision models (`gemini:gemini-2.0-flash`) for real-time UI understanding and anomaly detection.
- 🧪 **Multi-Provider A/B Testing**: Compare response quality across Groq, Claude, and DeepSeek by switching only the provider string, keeping all prompts identical.

---

## Providers Supported

| Provider | Type | Tier | Features |
|:---|:---|:---|:---|
| **Ollama** | Local | Unlimited Local | Chat, Streaming, List Models |
| **llama.cpp** | Local | Unlimited Local | GGUF In-Process GPU Inference (Vulkan/Metal) |
| **LM Studio** | Local | Unlimited Local | Chat, Streaming via Local API |
| **Groq** | Cloud | Permanent Free | Ultra-Fast Inference, Rate-limited Free Tier |
| **Cerebras** | Cloud | Permanent Free | Ultra-Fast Inference, Free Tier |
| **SambaNova** | Cloud | Permanent Free | Fast Llama/Qwen Inference, Free Tier |
| **Gemini** | Cloud | Permanent Free | Chat, Streaming, Vision, List Models |
| **GitHub Models** | Cloud | Permanent Free | GPT-4o-mini, Llama, Free Rate-Limits |
| **Cloudflare AI** | Cloud | Permanent Free | Workers AI Llama, Daily Free Tokens |
| **Mistral** | Cloud | Permanent Free | Chat, Streaming, Free Experiment Tier |
| **OpenRouter** | Gateway | Free / Paid | Chat, Streaming, 200+ Models (`:free`) |
| **OmniRoute** | Gateway | Trial / Pro | 340+ Providers, Auto-Fallback, Compression |
| **LarpRouter** | Gateway | Trial (\$0.10) | Multi-Model Routing, OpenAI Compatible |
| **SiliconFlow** | Cloud | Trial / Free | DeepSeek V3/R1, Qwen Fast Endpoints |
| **NVIDIA NIM** | Cloud | Trial (1k req) | Enterprise Model APIs, Free Credits |
| **Together AI** | Cloud | Trial / Paid | Broad Open-Source Models, Fast Speed |
| **Fireworks AI** | Cloud | Trial / Paid | Optimized Inference, Function Calling |
| **DeepSeek** | Cloud | Trial / Paid | Chat, Streaming, Low Cost |
| **OpenAI** | Cloud | Paid | Chat, Streaming, Vision |
| **Anthropic Claude** | Cloud | Paid | Chat, Streaming |

> [!NOTE]
> **Unlimited Local**: Runs 100% locally, no external costs or rate limits. **Permanent Free**: Ongoing free request quota. **Trial**: Starts with free introductory credits. **Paid**: Requires a paid account from the first request.

---

## Performance Benchmarks

Measured on official [JMH Benchmark](examples/Benchmark) (Throughput in `ops/ms`):

```text
Benchmark                                  Mode  Cnt     Score   Units
FastAIBenchmark.benchmarkUsageParser      thrpt    3  12748.0   ops/ms
FastAIBenchmark.benchmarkSseStreamDecoder thrpt    3     63.98   ops/ms
```

> [!NOTE]
> **Environment**: Windows 11, Intel Core i5-1135G7 (Surface Pro 8), JDK 21.0.12. `UsageParser` achieves over **12.7 million ops/sec** via zero-allocation inline byte scanning. `SseStreamDecoder` processes over **63,900 chunks/sec** with byte-level SSE parsing and no intermediate String line allocations.

---

## API Quick Reference

### Connecting

| Factory Method | Return Type | Description | Docs |
|:---|:---|:---|:---|
| `FastAI.connect(spec)` | `AI` | Connects to a local provider (e.g. `"ollama:llama3"`, `"llama:model.gguf"`). | [Reference](docs/REFERENCE.md) |
| `FastAI.connect(spec, apiKey)` | `AI` | Connects to a cloud provider with API key (e.g. `"groq:llama-3.3-70b"`, `"openai:gpt-4o"`). | [Reference](docs/REFERENCE.md) |
| `FastAI.auto()` | `AI` | Auto-fallback free router cycling across Groq, Cerebras, Gemini, SambaNova. | [Reference](docs/REFERENCE.md) |

### Generation

| Method | Return Type | Description | Docs |
|:---|:---|:---|:---|
| `ai.ask(prompt)` | `String` | Sends a user prompt, returns the full response string. | [Reference](docs/REFERENCE.md) |
| `ai.ask(system, user)` | `String` | Sends a system + user prompt pair. | [Reference](docs/REFERENCE.md) |
| `ai.ask(prompt, file)` | `String` | Sends a prompt with a file attachment for vision/multimodal models. | [Reference](docs/REFERENCE.md) |
| `ai.stream(prompt, handler)` | `void` | Streams tokens to the provided `Consumer<String>` callback. | [Reference](docs/REFERENCE.md) |
| `ai.getModels()` | `List<String>` | Lists all models available from the connected provider. | [Reference](docs/REFERENCE.md) |

### Sampling Controls

| Method | Return Type | Description | Docs |
|:---|:---|:---|:---|
| `ai.withSystemPrompt(s)` | `AI` | Sets a persistent system prompt for all subsequent calls. | [Reference](docs/REFERENCE.md) |
| `ai.withTemperature(f)` | `AI` | Sets sampling temperature (0.0 = deterministic, 1.0 = creative). | [Reference](docs/REFERENCE.md) |
| `ai.withMaxTokens(n)` | `AI` | Limits the maximum number of output tokens. | [Reference](docs/REFERENCE.md) |
| `ai.withTopP(f)` | `AI` | Sets nucleus sampling probability mass. | [Reference](docs/REFERENCE.md) |
| `ai.withTopK(n)` | `AI` | Sets top-K token sampling. | [Reference](docs/REFERENCE.md) |
| `ai.withGpu(enabled)` | `AI` | Enables or disables GPU offloading for local GGUF models. | [Reference](docs/REFERENCE.md) |
| `ai.withContextSize(n)` | `AI` | Sets the context window size for local GGUF models. | [Reference](docs/REFERENCE.md) |

---

## Technical Demos & Benchmarks

| Case | Java Example | Launcher | Description |
|:---|:---|:---|:---|
| **Unified AI Demo** | [Demo.java](examples/Demo/src/Demo.java) | `run-demo.bat` | Universal CLI demo supporting all 20+ local and cloud providers with real-time streaming. |
| **JMH Microbenchmark Suite** | [FastAIBenchmark.java](examples/Benchmark/src/main/java/fastai/FastAIBenchmark.java) | `run-benchmark.bat` | JMH throughput benchmark for byte-level SSE streaming and usage parsing. |

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependencies to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastAI - Unified AI Client -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAI</artifactId>
        <version>0.1.14</version>
    </dependency>

    <!-- FastJSON - Required JSON Engine -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastJSON</artifactId>
        <version>0.1.4</version>
    </dependency>

    <!-- FastAIModel - Local In-Process GPU Engine (Optional) -->
    <dependency>
        <groupId>com.github.andrestubbe.FastAIModel</groupId>
        <artifactId>fastaimodel-llama</artifactId>
        <version>0.1.4</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:FastAI:0.1.14'
    implementation 'com.github.andrestubbe:FastJSON:0.1.4'
    implementation 'com.github.andrestubbe.FastAIModel:fastaimodel-llama:0.1.4' // Optional: local GPU engine
}
```

### Option 3: Direct Download (No Build Tool)

Download the release JARs directly from GitHub Releases:

1. ⚡ **[FastAI-0.1.14.jar](https://github.com/andrestubbe/FastAI/releases/tag/0.1.14)** (Unified AI Client)
2. 📦 **[FastJSON-0.1.4.jar](https://github.com/andrestubbe/FastJSON/releases/tag/0.1.4)** (Required JSON Engine)
3. 🧠 **[fastaimodel-llama-0.1.4.jar](https://github.com/andrestubbe/FastAIModel/releases/tag/v0.1.4)** (Optional: Local GPU Inference)
4. 🌋 **[fastgpu-0.1.1.jar](https://github.com/andrestubbe/FastGPU/releases/tag/v0.1.1)** (Optional: Vulkan GPU Acceleration)
5. ⚙️ **[FastCore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/tag/0.1.0)** (Optional: Native JNI Loader)

---

## Documentation

- **[COMPILE.md](docs/COMPILE.md)**: Full compilation and build guide.
- **[REFERENCE.md](docs/REFERENCE.md)**: API reference for factory methods, sampling controls, and streaming contracts.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Unified fluent design and zero-dependency architecture rationale.
- **[ROADMAP.md](docs/ROADMAP.md)**: Planned milestones, gateway extensions, and ecosystem integrations.
- **[CHANGELOG.md](docs/CHANGELOG.md)**: Complete version history and release notes.

---

## Platform Support

| Platform | Architecture | Status | Notes |
|:---|:---:|:---:|:---|
| **Windows 10 / 11** | x64 | ✅ Fully Supported | Full local GPU inference + all cloud providers |
| **Linux** | x64 / AArch64 | 🚧 Planned | Cloud providers work today; local GPU engine pending |
| **macOS** | Apple Silicon / x64 | 🚧 Planned | Metal GPU offloading via `FastAIModel` planned |

---

## Related Projects

- **[`FastAIAgent`](https://github.com/andrestubbe/FastAIAgent)**: Autonomous agent loop, intent-graphs, and tool execution
- **[`FastAIBot`](https://github.com/andrestubbe/FastAIBot)**: Zero-bloat bot harnesses and persona runtime
- **[`FastAIGraph`](https://github.com/andrestubbe/FastAIGraph)**: In-memory knowledge graph and multi-hop relationship engine
- **[`FastAIHybrid`](https://github.com/andrestubbe/FastAIHybrid)**: Dense-sparse hybrid search fusion (BM25 + Vectors)
- **[`FastAIMatcher`](https://github.com/andrestubbe/FastAIMatcher)**: Automated compliance and hybrid rule matching engine
- **[`FastAIMCP`](https://github.com/andrestubbe/FastAIMCP)**: Model Context Protocol (MCP) server & tool integration
- **[`FastAIMemory`](https://github.com/andrestubbe/FastAIMemory)**: Conversation history, sliding windows, and rolling summaries
- **[`FastAIMetrics`](https://github.com/andrestubbe/FastAIMetrics)**: Lock-free token, latency, and cost tracking engine
- **[`FastAIModel`](https://github.com/andrestubbe/FastAIModel)**: Native local inference runtime (GGUF/ONNX)
- **[`FastAIRag`](https://github.com/andrestubbe/FastAIRag)**: Ultra-fast document chunking and vector retrieval
- **[`FastAIReasoner`](https://github.com/andrestubbe/FastAIReasoner)**: Deterministic planning, chain-of-thought, and self-correction
- **[`FastAIRerank`](https://github.com/andrestubbe/FastAIRerank)**: Cross-encoder relevance filtering and Top-N prompt pruner
- **[`FastAIRuntime`](https://github.com/andrestubbe/FastAIRuntime)**: Sandboxed process runner and tool-calling execution pipeline
- **[`FastAIState`](https://github.com/andrestubbe/FastAIState)**: Lock-free shared agent state & blackboard memory
- **[`FastAIVectorDB`](https://github.com/andrestubbe/FastAIVectorDB)**: High-throughput SIMD/AVX2 vector database
- **[`FastAIVision`](https://github.com/andrestubbe/FastAIVision)**: High-speed local multimodal vision and screen-VLM engine
- **[`FastJSON`](https://github.com/andrestubbe/FastJSON)**: Zero-allocation Fast JSON Engine for Java
- **[`FastCore`](https://github.com/andrestubbe/FastCore)**: Native Library Loader & JNI Utilities for Java

---

## License

MIT License. See [LICENSE](LICENSE) file for details.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.* 🚀