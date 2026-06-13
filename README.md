# FastAI 0.1.0 [ALPHA]  Unified AI client for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)](https://github.com/andrestubbe/FastAI/releases/tag/0.1.0)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)](https://jitpack.io/#andrestubbe)

---

**💡 One interface for
all Local and Cloud AI models — No JSON, No HTTP, No Boilerplate.**

FastAI is a **minimalist, hyper-fast Java AI library** that unifies all major LLM providers (Ollama, LM Studio, OpenAI,
Claude, Mistral, DeepSeek) behind a single, elegant interface. Built for **Java developers** who hate JSON parsing, HTTP
clients, and bloated frameworks.

If you need **a drop-in AI module**, **multi-provider interchangeability**, or **clean FastJava-style code**, FastAI is
your solution.
cloudAI.stream("Write a novel", token -> System.out.print(token));

---

[![FastFileIndex Showcase](docs/screenshot.png)](https://www.youtube.com/watch?v=BZsqQl7WqWk)

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

## Quick Start

```java
AI localAI = FastAI.connect("ollama:llama3.1");
System.out.println(localAI.ask("Explain quantum physics simply."));

AI cloudAI = FastAI.connect("openai:gpt-4o", System.getenv("OPENAI_API_KEY"));
System.out.println(cloudAI.ask("Explain quantum physics simply."));
```

---

## Why FastAI?

Current AI libraries in Java (`LangChain4j`, `Spring AI`) are huge, framework-heavy, and come with dependency hell.
Direct SDKs lock you into one provider.

FastAI solves this by providing:

- **Zero JSON handling**  everything is native Java Strings and Files.
- **Provider Interchangeability**  switch between `ollama` and `openai` by changing one string.
- **Zero Dependencies**  pure Java 17+, no Jackson, no Spring.
- **True Unified Interface**  `AI` is all you need to know.

---

## Key Features

- **🌐 Local + Cloud Support**  Use local models or cloud giants with the same code.
- **📎 Simple Attachments**  Pass a `java.io.File` and let FastAI handle the Base64/Multipart encoding.
- **🎭 System Prompts**  Native support for System vs User prompts.
- **⚡ Ultra-Lightweight**  Just drop the JAR into your project.
- **🌊 Streaming First**  Every provider supports unified streaming callbacks.

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
    <version>0.1.0</version>
</dependency>

<!-- FastJSON (Required Dependency) -->
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>FastJSON</artifactId>
    <version>0.1.0</version>
</dependency>

<!-- FastCore (Required Native Loader) -->
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fastcore</artifactId>
    <version>0.1.0</version>
</dependency>

<!-- FastString (Required Dependency) -->
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>FastString</artifactId>
    <version>0.1.0</version>
</dependency>

<!-- FastBytes (Required Dependency) -->
<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>FastBytes</artifactId>
    <version>0.1.0</version>
</dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastai:0.1.0'
    implementation 'com.github.andrestubbe:FastJSON:0.1.0'
    implementation 'com.github.andrestubbe:fastcore:0.1.0'
    implementation 'com.github.andrestubbe:FastString:0.1.0'
    implementation 'com.github.andrestubbe:FastBytes:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the latest JARs directly to add them to your classpath:

1. 🚀 **[fastai-0.1.0.jar](https://github.com/andrestubbe/FastAI/releases/download/0.1.0/fastai-0.1.0.jar)** (Core Library)
2. 📦 **[FastJSON-0.1.0.jar](https://github.com/andrestubbe/FastJSON/releases/download/0.1.0/FastJSON-0.1.0.jar)** (Required JSON Parser)
3. ⚙️ **[fastcore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/download/0.1.0/fastcore-0.1.0.jar)** (Mandatory Native JNI Loader)
4. 📦 **[FastString-0.1.0.jar](https://github.com/andrestubbe/FastString/releases/download/0.1.0/FastString-0.1.0.jar)** (Required String Dependency)
5. 📦 **[FastBytes-0.1.0.jar](https://github.com/andrestubbe/FastBytes/releases/download/0.1.0/FastBytes-0.1.0.jar)** (Required Bytes Dependency)

> [!IMPORTANT]
> All JARs must be in your classpath for the native JNI calls to function correctly.

---

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
ai.stream("Write a poem",System.out::print);
```

---

## Providers Supported

| Provider         | Type  | Status     | Features             |
|------------------|-------|------------|----------------------|
| Gemini           | Cloud | ✔️ Native  | Chat, List Models    |
| Ollama           | Local | 🚧 Partial | List Models          |
| LM Studio        | Local | 🚧 Planned | -                    |
| OpenAI           | Cloud | 🚧 Planned | -                    |
| Anthropic Claude | Cloud | 🚧 Planned | -                    |
| Mistral          | Cloud | 🚧 Planned | -                    |
| DeepSeek         | Cloud | 🚧 Planned | -                    |

---

## Performance

FastAI is **zero-dependency** and **zero-allocation** for the core connection layer:

| Metric              | LangChain4j | Spring AI | FastAI        |
|---------------------|-------------|-----------|---------------|
| **Dependencies**    | 15+         | 20+       | **0**         |
| **JAR Size**        | ~5MB        | ~10MB     | **~50KB**     |
| **Startup Time**    | 2-3s        | 5-10s     | **<100ms**    |
| **Memory Overhead** | High        | High      | **Minimal**   |
| **Learning Curve**  | Hours       | Hours     | **5 minutes** |

---

## Examples

Every feature has a standalone example in `examples/`:

```bash
cd examples/Demo
mvn compile exec:java    # Run demo
```

| Example          | Demonstrates                  |
|------------------|-------------------------------|
| `Demo`           | Local AI, Cloud AI, Streaming |

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

* **[COMPILE.md](COMPILE.md)**: Full compilation guide (MSVC C++17 build chain + JNI Setup).
* **[REFERENCE.md](docs/REFERENCE.md)**: Exhaustive catalog of SGR styles, OSC window parameters, and callback contracts.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Zero-allocation and low-overhead processing designs.
* **[ROADMAP.md](docs/ROADMAP.md)**: Planned milestone features and performance extensions.
* **[CHANGELOG.md](docs/CHANGELOG.md)**

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

- [FastFileIndex](https://github.com/andrestubbe/FastFileIndex) - Binary file indexing with mmap support
- [FastFileSearch](https://github.com/andrestubbe/FastFileSearch) - Prefix Trie, N-Gram index, and Ranking engine
- [FastFileWatch](https://github.com/andrestubbe/FastFileWatch) - USN Journal-based live file monitoring
- [FastCore](https://github.com/andrestubbe/FastCore) - Unified JNI loader and platform abstraction

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*

