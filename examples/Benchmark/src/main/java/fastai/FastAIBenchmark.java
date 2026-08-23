package fastai;

import fastai.internal.SseStreamDecoder;
import fastai.internal.UsageParser;
import org.openjdk.jmh.annotations.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JMH Microbenchmark — FastAI Hot-Path Throughput and Latency.
 * Measures zero-allocation byte-level SSE parsing, request serialization, and usage parsing.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-XX:+UseG1GC", "-Xms256m", "-Xmx256m"})
public class FastAIBenchmark {

    private byte[] sseSampleBytes;
    private String usageJsonSample;
    private AIRequest sampleRequest;

    @Setup(Level.Trial)
    public void setup() {
        final StringBuilder sse = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            sse.append("data: {\"choices\":[{\"delta\":{\"content\":\" token_").append(i).append("\"}}]}\n\n");
        }
        sse.append("data: {\"usage\":{\"prompt_tokens\":120,\"completion_tokens\":50,\"total_tokens\":170}}\n\n");
        sse.append("data: [DONE]\n\n");
        this.sseSampleBytes = sse.toString().getBytes(StandardCharsets.UTF_8);

        this.usageJsonSample = "{\"prompt_tokens\":120,\"completion_tokens\":50,\"total_tokens\":170}";
        this.sampleRequest = AIRequest.of("You are an expert system.", "Calculate fibonacci(50)");
    }

    @Benchmark
    public int benchmarkSseByteLevelStreamDecoder() throws IOException {
        final AtomicInteger tokenCount = new AtomicInteger(0);
        final ByteArrayInputStream bais = new ByteArrayInputStream(this.sseSampleBytes);
        SseStreamDecoder.decode(bais, token -> tokenCount.incrementAndGet(), usage -> {});
        return tokenCount.get();
    }

    @Benchmark
    public Usage benchmarkUsageParser() {
        return UsageParser.parseUsage(this.usageJsonSample);
    }
}
