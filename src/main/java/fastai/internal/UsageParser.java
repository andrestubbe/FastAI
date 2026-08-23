package fastai.internal;

import fastai.Usage;

/**
 * High-performance parser for token usage metrics extracted from JSON responses and SSE streams.
 */
public final class UsageParser {

    private UsageParser() {
    }

    /**
     * Parses token usage information from a JSON substring.
     *
     * @param usageJson the JSON text containing usage properties
     * @return the parsed {@link Usage} object or null if parsing fails
     */
    public static Usage parseUsage(final String usageJson) {
        if (usageJson == null || usageJson.isEmpty()) {
            return null;
        }
        try {
            final int ptIdx = usageJson.indexOf("\"prompt_tokens\":");
            final int ctIdx = usageJson.indexOf("\"completion_tokens\":");
            final int ttIdx = usageJson.indexOf("\"total_tokens\":");

            if (ptIdx != -1 && ctIdx != -1 && ttIdx != -1) {
                int ptEnd = usageJson.indexOf(',', ptIdx);
                int ctEnd = usageJson.indexOf(',', ctIdx);
                int ttEnd = usageJson.indexOf('}', ttIdx);
                if (ptEnd == -1) ptEnd = usageJson.indexOf('}', ptIdx);
                if (ctEnd == -1) ctEnd = usageJson.indexOf('}', ctIdx);

                final int pt = Integer.parseInt(usageJson.substring(ptIdx + 16, ptEnd).trim());
                final int ct = Integer.parseInt(usageJson.substring(ctIdx + 20, ctEnd).trim());
                final int tt = Integer.parseInt(usageJson.substring(ttIdx + 15, ttEnd).trim());
                return new Usage(pt, ct, tt);
            }
        } catch (final Exception ignored) {
        }
        return null;
    }
}
