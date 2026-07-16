public class TextUtils {

    public static String[] wrap(String text, int maxW) {
        if (text == null || text.isEmpty()) return new String[0];
        if (maxW <= 0) maxW = 1;
        java.util.List<String> result = new java.util.ArrayList<>();
        for (String para : text.split("\n", -1)) {
            if (para.isEmpty()) { result.add(""); continue; }
            int start = 0;
            while (start < para.length()) {
                int end = Math.min(start + maxW, para.length());
                if (end < para.length()) {
                    int sp = para.lastIndexOf(' ', end);
                    if (sp > start) end = sp + 1;
                }
                result.add(para.substring(start, end));
                start = end;
            }
        }
        return result.toArray(new String[0]);
    }
}
