package playing.util;

public class StringUtil {
    public static String prefix(String a, String b) {
        int idx = 0;
        int aLength = a.length();
        int bLength = b.length();

        while (true) {
            if (idx == aLength || idx == bLength || a.charAt(idx) != b.charAt(idx)) {
                return a.substring(0, idx);
            } else {
                idx += 1;
            }
        }
    }
}
