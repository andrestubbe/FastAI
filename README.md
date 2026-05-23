# FastAI â€” Unified AI client for Java [ALPHA] - v0.1.0
**âš¡ One interface for all Local and Cloud AI models â€” No JSON, No HTTP, No Boilerplate.**

[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://jitpack.io/v/andrestubbe/FastAI.svg)](https://jitpack.io/#andrestubbe/FastAI)

```java
// Quick Start â€” One interface, any provider
AI localAI = FastAI.connect("ollama:llama3.1");
System.out.println(localAI.ask("Explain quantum physics simply."));

// Transparent Streaming
AI cloudAI = FastAI.connect("openai:gpt-4o", System.getenv("OPENAI_API_KEY"));
cloudAI.stream("Write a novel", token -> System.out.print(token));
```

FastAI is a **minimalist, hyper-fast Java AI library** that unifies all major LLM providers (Ollama, LM Studio, OpenAI, Claude, Mistral, DeepSeek) behind a single, elegant interface. Built for **Java developers** who hate JSON parsing, HTTP clients, and bloated frameworks. 

**Keywords:** java ai client, ollama java, lmstudio java, openai java client, claude java client, fast ai framework java, langchain4j alternative, spring ai alternative, low learning curve ai, simple llm java

If you need **a drop-in AI module**, **multi-provider interchangeability**, or **clean FastJava-style code**, FastAI is your solution.

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

Current AI libraries in Java (`LangChain4j`, `Spring AI`) are huge, framework-heavy, and come with dependency hell. Direct SDKs lock you into one provider.

FastAI solves this by providing:
- **Zero JSON handling** â€” everything is native Java Strings and Files.
- **Provider Interchangeability** â€” switch between `ollama` and `openai` by changing one string.
- **Zero Dependencies** â€” pure Java 17+, no Jackson, no Spring.
- **True Unified Interface** â€” `AI` is all you need to know.

---

## Key Features

- **Local + Cloud Support** â€” Use local models or cloud giants with the same code.
- **Simple Attachments** â€” Pass a `java.io.File` and let FastAI handle the Base64/Multipart encoding.
- **System Prompts** â€” Native support for System vs User prompts.
- **Ultra-Lightweight** â€” Just drop the JAR into your project.
- **Streaming First** â€” Every provider supports unified streaming callbacks.

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
        <artifactId>fastai</artifactId>
        <version>v0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastai:v0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)
Download the latest JARs directly to add them to your classpath:

1. 📦 **[fastai-v0.1.0.jar](https://github.com/andrestubbe/FastAI/releases/download/v0.1.0/fastai-v0.1.0.jar)** (The Core Library)


## API Reference

### Connect
```java
// Local Providers
AI ai = FastAI.connect("ollama:llama3.1");
AI ai = FastAI.connect("lmstudio:phi3");

// Cloud Providers (requires API Key as second argument)
AI ai = FastAI.connect("openai:gpt-4o", "sk-...");
AI ai = FastAI.connect("claude:opus", "sk-ant-...");
AI ai = FastAI.connect("mistral:large", "key...");
AI ai = FastAI.connect("deepseek:chat", "key...");
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

---

## Providers Supported

| Provider | Type | Status | Features |
|----------|------|--------|----------|
| Ollama | Local | âœ… Native | Chat, Stream, Vision |
| LM Studio | Local | âœ… Native | Chat, Stream, Vision |
| OpenAI | Cloud | âœ… Native | Chat, Stream, Vision |
| Anthropic Claude | Cloud | âœ… Native | Chat, Stream, Vision |
| Mistral | Cloud | âœ… Native | Chat, Stream |
| DeepSeek | Cloud | âœ… Native | Chat, Stream |

---

## Performance

FastAI is **zero-dependency** and **zero-allocation** for the core connection layer:

| Metric | LangChain4j | Spring AI | FastAI |
|--------|-------------|-----------|--------|
| **Dependencies** | 15+ | 20+ | **0** |
| **JAR Size** | ~5MB | ~10MB | **~50KB** |
| **Startup Time** | 2-3s | 5-10s | **<100ms** |
| **Memory Overhead** | High | High | **Minimal** |
| **Learning Curve** | Hours | Hours | **5 minutes** |

---

## Examples

Every feature has a standalone example in `examples/`:

```bash
cd examples/00-basic-usage
mvn compile exec:java    # Run demo
```

| Example | Demonstrates |
|---------|-------------|
| `00-basic-usage` | Local AI, Cloud AI, Streaming |

---

## Project Structure

```
fastai/
â”œâ”€â”€ src/main/java/fastai/       # Main API
â”‚   â”œâ”€â”€ FastAI.java             # Connection factory
â”‚   â”œâ”€â”€ AI.java                 # Unified interface
â”‚   â””â”€â”€ providers/              # Provider implementations
â”œâ”€â”€ examples/00-basic-usage/    # Usage demo
â”‚   â”œâ”€â”€ pom.xml
â”‚   â””â”€â”€ src/main/java/fastai/examples/
â”œâ”€â”€ pom.xml                     # Maven config
â”œâ”€â”€ README.md                   # This file
â””â”€â”€ LICENSE                     # MIT License
```

**Why `examples/` on root level?**
- Not part of the library â†’ separate mini-projects
- Not tests â†’ tutorials for users
- Each example has its own `pom.xml` â†’ runnable standalone
- Copy-paste friendly â†’ users can use as starter template

---

## Roadmap

- **v1.0**: Core API, Text Generation, Local & Cloud Providers, Basic Streaming, System Prompts.
- **v1.1**: Embeddings, Image Attachments mapping to Vision APIs, Audio Input.
- **v1.2**: JNI bindings for local `llama.cpp`.
- **v2.0**: FastAI Agent System, Tool Calling, Memory.

---

## Build from Source

See [COMPILE.md](COMPILE.md) for detailed build instructions.

---

## License

MIT License â€” free for commercial and private use. See [LICENSE](LICENSE) for details.

---

**Part of the FastJava Ecosystem** â€” *Making the JVM faster.*


