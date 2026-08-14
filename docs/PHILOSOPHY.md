# FastAI Engineering Philosophy

## Core Architecture Principles

1. **Unified Fluent Interface**  
   One single interface `AI` connects all local engines and cloud AI providers without framework bloat or complex configuration classes.

2. **Zero Dependencies**  
   Pure Java 17+ with FastJSON — zero heavy Jackson, Spring, or LangChain4j dependencies.

3. **In-Process GPU Integration**  
   Seamlessly delegates local GGUF models directly to **FastAIModel** for zero-IPC native Vulkan & Metal GPU execution.