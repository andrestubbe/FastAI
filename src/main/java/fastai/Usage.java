package fastai;

/**
 * Token usage telemetry metrics for an AI request.
 *
 * @param promptTokens number of input/prompt tokens consumed
 * @param completionTokens number of generated completion tokens
 * @param totalTokens combined total tokens
 */
public record Usage(int promptTokens, int completionTokens, int totalTokens) {
    public static final Usage ZERO = new Usage(0, 0, 0);
}
