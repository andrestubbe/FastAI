package fastai.internal;

import fastai.Usage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * High-performance, zero-allocation Server-Sent Events (SSE) stream decoder for FastAI.
 * Processes chunked network responses at byte level to eliminate intermediate string allocations.
 */
public final class SseStreamDecoder {

    private static final byte[] DATA_PREFIX = {'d', 'a', 't', 'a', ':'};
    private static final byte[] CONTENT_KEY = {'"', 'c', 'o', 'n', 't', 'e', 'n', 't', '"', ':'};
    private static final byte[] USAGE_KEY = {'"', 'u', 's', 'a', 'g', 'e', '"', ':'};

    private SseStreamDecoder() {
    }

    /**
     * Reads and decodes an SSE input stream at byte level, dispatching extracted text tokens and usage stats.
     *
     * @param is the incoming HTTP entity input stream
     * @param tokenHandler the consumer receiving streamed string tokens
     * @param usageHandler the optional consumer receiving token usage telemetry
     * @throws IOException on network reading error
     */
    public static void decode(final InputStream is, final Consumer<String> tokenHandler, final Consumer<Usage> usageHandler) throws IOException {
        final byte[] buffer = new byte[8192];
        final byte[] lineBuffer = new byte[65536];
        int linePos = 0;
        int read;

        while ((read = is.read(buffer)) != -1) {
            for (int i = 0; i < read; i++) {
                final byte b = buffer[i];
                if (b == '\n') {
                    if (linePos > 0) {
                        processLine(lineBuffer, linePos, tokenHandler, usageHandler);
                        linePos = 0;
                    }
                } else if (b != '\r') {
                    if (linePos < lineBuffer.length) {
                        lineBuffer[linePos++] = b;
                    }
                }
            }
        }
        if (linePos > 0) {
            processLine(lineBuffer, linePos, tokenHandler, usageHandler);
        }
    }

    private static void processLine(final byte[] lineBuffer, final int len, final Consumer<String> tokenHandler, final Consumer<Usage> usageHandler) {
        int start = 0;
        while (start < len && (lineBuffer[start] == ' ' || lineBuffer[start] == '\t')) {
            start++;
        }

        if (len - start < DATA_PREFIX.length) return;
        for (int i = 0; i < DATA_PREFIX.length; i++) {
            if (lineBuffer[start + i] != DATA_PREFIX[i]) return;
        }

        start += DATA_PREFIX.length;
        while (start < len && (lineBuffer[start] == ' ' || lineBuffer[start] == '\t')) {
            start++;
        }

        if (start >= len) return;

        // Check for [DONE] token
        if (len - start == 6 && lineBuffer[start] == '[' && lineBuffer[start + 1] == 'D' && lineBuffer[start + 2] == 'O' && lineBuffer[start + 3] == 'N' && lineBuffer[start + 4] == 'E' && lineBuffer[start + 5] == ']') {
            return;
        }

        // Search for "content": token
        final int contentIdx = indexOfSubarray(lineBuffer, start, len, CONTENT_KEY);
        if (contentIdx != -1) {
            int qStart = contentIdx + CONTENT_KEY.length;
            while (qStart < len && lineBuffer[qStart] != '"') {
                qStart++;
            }
            if (qStart < len) {
                int qEnd = qStart + 1;
                boolean escaped = false;
                while (qEnd < len) {
                    final byte c = lineBuffer[qEnd];
                    if (c == '\\' && !escaped) {
                        escaped = true;
                    } else if (c == '"' && !escaped) {
                        break;
                    } else {
                        escaped = false;
                    }
                    qEnd++;
                }
                if (qEnd < len) {
                    final String token = unescapeJsonBytes(lineBuffer, qStart + 1, qEnd);
                    tokenHandler.accept(token);
                }
            }
        }

        // Search for "usage": metrics
        if (usageHandler != null) {
            final int usageIdx = indexOfSubarray(lineBuffer, start, len, USAGE_KEY);
            if (usageIdx != -1) {
                final String usageJson = new String(lineBuffer, usageIdx, len - usageIdx, StandardCharsets.UTF_8);
                final Usage usage = UsageParser.parseUsage(usageJson);
                if (usage != null) {
                    usageHandler.accept(usage);
                }
            }
        }
    }

    private static int indexOfSubarray(final byte[] source, final int offset, final int length, final byte[] target) {
        final int targetLen = target.length;
        final int max = length - targetLen;
        for (int i = offset; i <= max; i++) {
            boolean match = true;
            for (int j = 0; j < targetLen; j++) {
                if (source[i + j] != target[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    private static String unescapeJsonBytes(final byte[] src, final int start, final int end) {
        final StringBuilder sb = new StringBuilder(end - start);
        for (int i = start; i < end; i++) {
            final byte b = src[i];
            if (b == '\\' && i + 1 < end) {
                final byte next = src[i + 1];
                switch (next) {
                    case 'n' -> { sb.append('\n'); i++; }
                    case 'r' -> { sb.append('\r'); i++; }
                    case 't' -> { sb.append('\t'); i++; }
                    case '"' -> { sb.append('"'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    case 'u' -> {
                        if (i + 5 < end) {
                            try {
                                final String hex = new String(src, i + 2, 4, StandardCharsets.US_ASCII);
                                final int code = Integer.parseInt(hex, 16);
                                sb.append((char) code);
                                i += 5;
                            } catch (final Exception e) {
                                sb.append((char) (b & 0xFF));
                            }
                        } else {
                            sb.append((char) (b & 0xFF));
                        }
                    }
                    default -> sb.append((char) (b & 0xFF));
                }
            } else {
                sb.append((char) (b & 0xFF));
            }
        }
        return sb.toString();
    }
}
