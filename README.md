# FastAI — Unified AI client for Java

**⚡ One interface for all Local and Cloud AI models — No JSON, No HTTP, No Boilerplate.**

[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![JitPack](https://jitpack.io/v/andrestubbe/FastAI.svg)](https://jitpack.io/#andrestubbe/FastAI)

```java
// Quick Start — One interface, any provider
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
- [Roadmap](#roadmap)
- [License](#license)

---

## Why FastAI?

Current AI libraries in Java (`LangChain4j`, `Spring AI`) are huge, framework-heavy, and come with dependency hell. Direct SDKs lock you into one provider.

FastAI solves this by providing:
- **Zero JSON handling** — everything is native Java Strings and Files.
- **Provider Interchangeability** — switch between `ollama` and `openai` by changing one string.
- **Zero Dependencies** — pure Java 17+, no Jackson, no Spring.
- **True Unified Interface** — `AI` is all you need to know.

---

## Key Features

- **Local + Cloud Support** — Use local models or cloud giants with the same code.
- **Simple Attachments** — Pass a `java.io.File` and let FastAI handle the Base64/Multipart encoding.
- **System Prompts** — Native support for System vs User prompts.
- **Ultra-Lightweight** — Just drop the JAR into your project.
- **Streaming First** — Every provider supports unified streaming callbacks.

---

## Installation

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.andrestubbe</groupId>
    <artifactId>fastai</artifactId>
    <version>v1.0.0</version>
</dependency>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastai:v1.0.0'
}
```

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
ai.stream("Write a poem", System.out::print);
```

---

## Providers Supported

| Provider | Type | Status | Features |
|----------|------|--------|----------|
| Ollama | Local | ✅ Native | Chat, Stream, Vision |
| LM Studio | Local | ✅ Native | Chat, Stream, Vision |
| OpenAI | Cloud | ✅ Native | Chat, Stream, Vision |
| Anthropic Claude | Cloud | ✅ Native | Chat, Stream, Vision |
| Mistral | Cloud | ✅ Native | Chat, Stream |
| DeepSeek | Cloud | ✅ Native | Chat, Stream |

---

## Roadmap

- **v1.0**: Core API, Text Generation, Local & Cloud Providers, Basic Streaming, System Prompts.
- **v1.1**: Embeddings, Image Attachments mapping to Vision APIs, Audio Input.
- **v1.2**: JNI bindings for local `llama.cpp`.
- **v2.0**: FastAI Agent System, Tool Calling, Memory.

---

## License

MIT License — free for commercial and private use. See [LICENSE](LICENSE) for details.

---

**Small package. Maximum speed. Zero bloat.** 🚀🧠
*Replace bloated frameworks with ultra-fast native AI client!*
