# The Philosophy of FastAI

> [!IMPORTANT]
> **"Zero JSON. Zero HTTP. Zero Boilerplate. Pure Java AI."**

FastAI is built on the principle that modern Java applications require **unified, zero-dependency AI integration** without the complexity of framework-heavy libraries or provider-specific SDKs.

## Core Tenets

1.  **Unified Interface**
    One `AI` interface for all providers — switch between local and cloud models by changing a single string.

2.  **Zero-Dependency Architecture**
    Pure Java 17+ implementation with no external dependencies — no Jackson, no Spring, no HTTP client libraries.

3.  **Provider Interchangeability**
    Seamlessly switch between Ollama, LM Studio, OpenAI, Claude, Mistral, and DeepSeek without code changes.

4.  **Native Java Types**
    Work with native Java Strings and Files — no JSON parsing, no Base64 encoding, no multipart form handling.

5.  **Streaming-First Design**
    Every provider supports unified streaming callbacks for real-time token generation.

6.  **Blueprint Consistency**
    As part of the **FastJava** ecosystem, FastAI adheres to standardized architecture:
    *   **Minimal Footprint**: ~50KB JAR size vs 5-10MB for alternatives
    *   **Fast Startup**: <100ms initialization vs 2-10s for framework-heavy libraries
    *   **Zero Learning Curve**: 5 minutes to mastery vs hours for complex frameworks

---
**⚡ FastAI — Making AI integration trivial for Java developers.**