package fastai;

public record Usage(int promptTokens, int completionTokens, int totalTokens) {
    public static final Usage ZERO = new Usage(0, 0, 0);
}
