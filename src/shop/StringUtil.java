package shop;

public final class StringUtil {
    private StringUtil() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String capitalize(String s) {
        if (isBlank(s)) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static String padLeft(String s, int width) {
        return repeat(" ", Math.max(0, width - s.length())) + s;
    }
}
