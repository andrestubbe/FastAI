package fastai;

/**
 * Result of an AI generation request, containing text output, token usage stats, and estimated execution cost in USD.
 *
 * @param text the generated text response
 * @param usage token consumption telemetry
 * @param cost estimated monetary cost of the request
 */
public record AIResponse(String text, Usage usage, double cost) {
}
