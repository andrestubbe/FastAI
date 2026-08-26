# FastAI 0.1.14 — Unified AI client for Java

[![Status](https://img.shields.io/badge/status-0.1.14-brightgreen.svg)](https://github.com/andrestubbe/FastAI/releases/tag/0.1.14)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe/FastAI)

---

**💡 One interface for all Local, Gateway and Cloud AI models — No JSON, No HTTP, No Boilerplate.**

FastAI is a **minimalist, hyper-fast Java AI library** that unifies 20+ major LLM providers (OmniRoute, Groq, Cerebras, SambaNova, GitHub Models, NVIDIA NIM, SiliconFlow, Cloudflare, Together AI, Fireworks, LarpRouter, Ollama, LM Studio, OpenAI, OpenRouter.ai, Claude, Mistral, DeepSeek, Gemini) behind a single, elegant interface. Built for **Java developers** who hate JSON parsing, HTTP clients, and bloated frameworks.

If you need **a drop-in AI module**, **multi-provider interchangeability**, or **clean FastJava-style code**, FastAI is your solution.


---

[![FastAI Showcase](docs/screenshot.png)](https://youtu.be/kjfyZebSdj4)

---

## Quick Start

```java
import fastai.AI;
import fastai.FastAI;

public class QuickStartDemo {
    public static void main(String[] args) {
        // 1. Direct Local GGUF Engine with full Vulkan/Metal GPU Offloading (ON)
        AI gpuAI = FastAI.connect("llama:models/qwen2.5-coder-1.5b.gguf")
                         .withGpu(true)        // Full GPU acceleration ON (99 layers)
                         .withContextSize(2048);

        // 2. Direct Local GGUF Engine on CPU Only (GPU OFF)
        AI cpuAI = FastAI.connect("llama:models/qwen2.5-coder-1.5b.gguf")
                        .withGpu(false);       // Force CPU execution (0 GPU layers)

        // 3. Fluid streaming with full sampling control
        gpuAI.withSystemPrompt("You are an expert Java performance engineer.")
             .withTemperature(0.7f)
             .withTopP(0.9f)
             .withTopK(40)
             .withMaxTokens(256)
             .stream("Write a quicksort in Java:", token -> {
                 System.out.print(token);
                 System.out.flush();
             });

        // 4. Cloud provider instance with identical sampling & streaming interface
        AI cloudAI = FastAI.connect("openai:gpt-4o", System.getenv("OPENAI_API_KEY"));
        cloudAI.withTemperature(0.2f)
               .withMaxTokens(500)
               .stream("Summarize the latest 2026 tech trends:", token -> {
                   System.out.print(token);
                   System.out.flush();
               });
    }
}
```

---

## Table of Contents

- [Why FastAI?](#why-fastai)
- [Key Features](#key-features)
- [Installation](#installation)
- [API Reference](#api-reference)
- [Providers Supported](#providers-supported)
- [Performance](#performance)
- [Examples](#examples)
- [Project Structure](#project-structure)
- [Roadmap](#roadmap)
- [License](#license)

---

## Why FastAI?

Current AI libraries in Java (`LangChain4j`, `Spring AI`) are huge, framework-heavy, and come with dependency hell.
Direct SDKs lock you into one provider.

FastAI solves this by providing:

- **Zero JSON handling** — everything is native Java Strings and Files.
- **Provider Interchangeability** — switch between `ollama`, `openrouter`, `groq`, `cerebras`, `sambanova` and `openai` by changing one string.
- **Zero Dependencies** — pure Java 17+, no Jackson, no Spring.
- **True Unified Interface** — `AI` is all you need to know.

---

## Key Features

- **🌐 Local + Cloud Support** — Use local models, Groq, Cerebras, Gemini, GitHub Models, or cloud giants with the exact same code.
- **⚡ In-Process Local GPU Engine** — Direct zero-IPC local LLM inference via **FastAIModel** with Vulkan (Intel/AMD/NVIDIA) and Metal (Apple Silicon) GPU offloading.
- **🔄 Auto-Fallback Free Router** — `FastAI.auto()` seamlessly cascades across free-tier providers on rate limits or outages with circuit-breaking health caches.
- **📎 Simple Attachments** — Pass a `java.io.File` and let FastAI handle the Base64/Multipart encoding.
- **🎭 System Prompts** — Native support for System vs User prompts.
- **⚡ Ultra-Lightweight** — Just drop the JAR into your project.
- **🌊 Streaming First** — Every provider supports unified streaming callbacks.

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
<!-- FastAI Library -->
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>FastAI</artifactId>
    <version>0.1.14</version>
</dependency>

<!-- FastJSON (Required Dependency) -->
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>FastJSON</artifactId>
    <version>0.1.4</version>
</dependency>

<!-- FastAIModel (Local In-Process GPU Engine) -->
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
    implementation 'com.github.andrestubbe.FastAIModel:fastaimodel-llama:0.1.4'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. ⚡ **[FastAI-0.1.14.jar](https://github.com/andrestubbe/FastAI/releases/download/0.1.14/FastAI-0.1.14.jar)** (Unified AI Client)
2. 📦 **[FastJSON-0.1.4.jar](https://github.com/andrestubbe/FastJSON/releases/download/0.1.4/FastJSON-0.1.4.jar)** (Fast JSON Engine)
3. 🧠 **[fastaimodel-llama-0.1.4.jar](https://github.com/andrestubbe/FastAIModel/releases/download/v0.1.4/fastaimodel-llama-0.1.4.jar)** (Local Inference Runtime)
4. 🌋 **[fastgpu-0.1.1.jar](https://github.com/andrestubbe/FastGPU/releases/download/v0.1.1/fastgpu-0.1.1.jar)** (Vulkan GPU Acceleration)
5. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (Mandatory Native JNI Loader)

---

## API Reference

### Connect

```java
// Auto-Fallback Free Router (Cycles through free providers on rate-limit/errors)
AI ai = FastAI.auto();
AI ai = FastAI.connect("auto:free");

// Local Providers & In-Process GPU Engine
AI ai = FastAI.connect("llama:models/qwen2.5-coder-1.5b.gguf"); // Native Vulkan/Metal In-Process GPU Engine
AI ai = FastAI.connect("ollama:llama3.1");
AI ai = FastAI.connect("lmstudio:phi3");

// Gateways
AI ai = FastAI.connect("omniroute:claude-3-5-sonnet"); 
AI ai = FastAI.connect("omniroute:deepseek-r1", "apiKey", "http://localhost:8000/v1");
AI ai = FastAI.connect("larprouter:gpt-5.6-sol", "sk-larp-...");

// Free-Tier / High-Speed Cloud Providers
AI ai = FastAI.connect("groq:llama-3.3-70b-versatile", "gsk_...");
AI ai = FastAI.connect("cerebras:llama3.1-70b", "csk-...");
AI ai = FastAI.connect("sambanova:Meta-Llama-3.1-70B-Instruct", "key...");

// OpenRouter Unified Gateway (200+ models)
AI ai = FastAI.connect("openrouter:anthropic/claude-3.5-sonnet", "sk-or-...");
AI ai = FastAI.connect("openrouter:deepseek/deepseek-r1", "sk-or-...");

// Cloud Providers (requires API Key as second argument)
AI ai = FastAI.connect("openai:gpt-4o", "sk-...");
AI ai = FastAI.connect("claude:opus", "sk-ant-...");
AI ai = FastAI.connect("mistral:large", "key...");
AI ai = FastAI.connect("deepseek:chat", "key...");
AI ai = FastAI.connect("gemini:gemini-1.5-flash", "AIzaSy...");
```

### Generation & Prompting

```java
// Simple prompt
String answer = ai.ask("Hello!");

// System + User prompt
String answer = ai.ask("You are a math expert.", "Explain integrals.");

// Multimodal (Vision/Files)
String answer = ai.ask("What is in this image?", new File("diagram.png"));
```

### Streaming

```java
ai.stream("Write a poem", System.out::print);
```

### Real-World Production Patterns

#### 1. Auto-Healing Resilient Cloud Agent
```java
// Automatically switches to next free provider if rate-limited
AI ai = FastAI.auto();
String result = ai.ask("Analyze this stack trace and suggest a patch:\n" + stackTrace);
```

#### 2. Local-First Air-Gapped Code Completion (0 IPC, GPU Accelerated)
```java
try (AI localEngine = FastAI.connect("llama:models/qwen2.5-coder-1.5b.gguf")) {
    localEngine.stream("public static <T> List<T> reverse(List<T> list) {", System.out::print);
}
```

#### 3. Real-Time Multimodal Vision Inspector
```java
AI visionAI = FastAI.connect("gemini:gemini-2.0-flash", apiKey);
String caption = visionAI.ask("Describe the UI anomalies in this rendering:", new File("render_output.png"));
```

---

## Providers Supported

| Provider         | Type    | Tier           | Features                               |
|------------------|---------|----------------|----------------------------------------|
| Ollama           | Local   | UNLIMITED LOCAL| Chat, Streaming, List Models           |
| llama.cpp        | Local   | UNLIMITED LOCAL| GGUF Local Inference (CPU/GPU)         |
| LM Studio        | Local   | UNLIMITED LOCAL| Chat, Streaming via Local API          |
| Groq             | Cloud   | PERMANENT_FREE | Ultra-Fast Inference, Rate-limited Free|
| Cerebras         | Cloud   | PERMANENT_FREE | Ultra-Fast Inference, Free Tier        |
| SambaNova        | Cloud   | PERMANENT_FREE | Fast Llama/Qwen Inference, Free Tier   |
| Gemini           | Cloud   | PERMANENT_FREE | Chat, Streaming, List Models           |
| GitHub Models    | Cloud   | PERMANENT_FREE | GPT-4o-mini, Llama, Free Rate-Limits   |
| Cloudflare AI    | Cloud   | PERMANENT_FREE | Workers AI Llama, Daily Free Tokens    |
| Mistral          | Cloud   | PERMANENT_FREE | Chat, Streaming, Free Experiment Tier  |
| OpenRouter       | Gateway | FREE / PAID    | Chat, Streaming, 200+ Models (:free)   |
| OmniRoute        | Gateway | TRIAL / PRO    | 340+ Providers, Auto-Fallback, Compress|
| LarpRouter       | Gateway | TRIAL ($0.10)  | Multi-Model Routing, OpenAI compatible |
| SiliconFlow      | Cloud   | TRIAL / FREE   | DeepSeek V3/R1, Qwen Fast Endpoints    |
| NVIDIA NIM       | Cloud   | TRIAL (1k req) | Enterprise Model APIs, Free Credits    |
| Together AI      | Cloud   | TRIAL / PAID   | Broad Open-Source Models, Fast Speed   |
| Fireworks AI     | Cloud   | TRIAL / PAID   | Optimized Inference, Function Calling  |
| DeepSeek         | Cloud   | TRIAL / PAID   | Chat, Streaming, Low Cost              |
| OpenAI           | Cloud   | PAID           | Chat, Streaming                        |
| Anthropic Claude | Cloud   | PAID           | Chat, Streaming                        |

> **Tier Legend:**
> - `UNLIMITED LOCAL`: Runs 100% locally on your machine without external costs or rate limits.
> - `PERMANENT_FREE`: Ongoing free request quota/rate limits (ideal for agent loops & tests without subscription).
> - `TRIAL`: Starts with free introductory balance/tokens before requiring a top-up.
> - `PAID`: Requires paid account/credit from the first request.

---

## Performance Benchmarks

FastAI is rigorously profiled using **JMH** to guarantee zero overhead on network parsing and request transformation.

| Metric / Hot-Path Operation | Score (ops/ms) | Ops per Second |
|-----------------------------|----------------|----------------|
| **Usage Parser**            | ~12,748 ops/ms | > 12.7 Million |
| **SSE Byte-Level Stream Decoder** | ~63.98 ops/ms  | > 63,900 ops/sec |

*Measured on Windows 11, Intel Core i5-1135G7 (Surface Pro 8), JDK 21.0.12. Evaluates continuous multi-chunk SSE decoding and inline usage metrics extraction without intermediate String line allocations.*

### Framework Comparison

FastAI is **zero-dependency** and **zero-allocation** for the core connection layer:

| Metric              | LangChain4j | Spring AI | FastAI        |
|---------------------|-------------|-----------|---------------|
| **Dependencies**    | 15+         | 20+       | **0**         |
| **JAR Size**        | ~5MB        | ~10MB     | **~50KB**     |
| **Startup Time**    | 2-3s        | 5-10s     | **<100ms**    |
| **Memory Overhead** | High        | High      | **Minimal**   |
| **Learning Curve**  | Hours       | Hours     | **5 minutes** |

---

## Technical Examples & Demos

| Case | Java Example | Launcher | Description |
|---|---|---|---|
| **Unified AI Demo** | [Demo.java](examples/Demo/src/Demo.java) | `run-demo.bat` | Universal CLI demo supporting all 20+ local and cloud providers with real-time streaming. |
| **JMH Microbenchmarks** | [FastAIBenchmark.java](examples/Benchmark/src/main/java/fastai/FastAIBenchmark.java) | `run-benchmark.bat` | JMH throughput benchmark for Byte-Level SSE Streaming and Usage parsing. |

---

## API Quick Reference

| Method / Factory | Return Type | Description |
|------------------|-------------|-------------|
| `FastAI.connect(spec, args...)` | `AI` | Connects to a provider (e.g. `"gemini"`, `"ollama"`) with optional API key. |
| `ai.ask(prompt)` | `String` | Sends a simple user prompt to the model and returns the response. |
| `ai.ask(systemPrompt, userPrompt)` | `String` | Sends a prompt with a configured system instruction. |
| `ai.ask(prompt, file)` | `String` | Sends a prompt along with a file attachment (images/vision). |
| `ai.stream(prompt, handler)` | `void` | Streams tokens back dynamically as they are generated. |
| `ai.getModels()` | `List<String>` | Lists all available models from the provider. |

---

## Documentation

* **[COMPILE.md](docs/COMPILE.md)**: Full compilation and build guide.
* **[REFERENCE.md](docs/REFERENCE.md)**: API reference for factory methods, sampling controls, and streaming contracts.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Unified fluent design and zero-dependency architecture.
* **[ROADMAP.md](docs/ROADMAP.md)**: Planned milestone features and gateway extensions.
* **[CHANGELOG.md](docs/CHANGELOG.md)**: Version history and release notes.

---

## Platform Support

| Platform      | Status            |
|---------------|-------------------|
| Windows 10/11 | ✅ Fully Supported |
| Linux         | 🚧 Planned        |
| macOS         | 🚧 Planned        |

---

## License

MIT License  See [LICENSE](LICENSE) file for details.

---

## Related Projects

- [FastAI](https://github.com/andrestubbe/FastAI) — Unified AI client interface for Java
- [FastAIAgent](https://github.com/andrestubbe/FastAIAgent) — Autonomous agent loop, intent-graphs, and tool execution
- [FastAIBot](https://github.com/andrestubbe/FastAIBot) — Zero-bloat bot harnesses and persona runtime
- [FastAIGraph](https://github.com/andrestubbe/FastAIGraph) — In-memory knowledge graph and multi-hop relationship engine
- [FastAIHybrid](https://github.com/andrestubbe/FastAIHybrid) — Dense-sparse hybrid search fusion (BM25 + Vectors)
- [FastAIMatcher](https://github.com/andrestubbe/FastAIMatcher) — Automated SOX compliance and hybrid rule matching engine
- [FastAIMCP](https://github.com/andrestubbe/FastAIMCP) — Model Context Protocol (MCP) server & tool integration
- [FastAIMemory](https://github.com/andrestubbe/FastAIMemory) — Conversation history, sliding windows, and rolling summaries
- [FastAIMetrics](https://github.com/andrestubbe/FastAIMetrics) — Ultra-fast lock-free token, latency, cost tracking and evaluation engine
- [FastAIModel](https://github.com/andrestubbe/FastAIModel) — Native local inference runtime (GGUF/ONNX)
- [FastAIRag](https://github.com/andrestubbe/FastAIRag) — Ultra-fast document chunking and vector retrieval
- [FastAIReasoner](https://github.com/andrestubbe/FastAIReasoner) — Deterministic planning, chain-of-thought, and self-correction
- [FastAIRerank](https://github.com/andrestubbe/FastAIRerank) — Cross-encoder relevance filtering and Top-N prompt pruner
- [FastAIRuntime](https://github.com/andrestubbe/FastAIRuntime) — Sandboxed process runner and tool-calling execution pipeline
- [FastAIState](https://github.com/andrestubbe/FastAIState) — Lock-free shared agent state & blackboard memory
- [FastAIVectorDB](https://github.com/andrestubbe/FastAIVectorDB) — High-throughput SIMD/AVX2 vector database
- [FastAIVision](https://github.com/andrestubbe/FastAIVision) — High-speed local multimodal vision, UI-element grounding, and screen-VLM engine
- [FastCore](https://github.com/andrestubbe/FastCore) — Unified JNI loader and platform abstraction

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*