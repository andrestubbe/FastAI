package fastai.benchmark;

import fastai.AIRequest;
import fastai.Usage;
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
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1, jvmArgs = {"-server", "-XX:+UseG1GC", "-Xms256m", "-Xmx256m"})
public class Benchmark {

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

    @org.openjdk.jmh.annotations.Benchmark
    public int benchmarkSseByteLevelStreamDecoder() throws IOException {
        final AtomicInteger tokenCount = new AtomicInteger(0);
        final ByteArrayInputStream bais = new ByteArrayInputStream(this.sseSampleBytes);
        SseStreamDecoder.decode(bais, token -> tokenCount.incrementAndGet(), usage -> {});
        return tokenCount.get();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public Usage benchmarkUsageParser() {
        return UsageParser.parseUsage(this.usageJsonSample);
    }
}
