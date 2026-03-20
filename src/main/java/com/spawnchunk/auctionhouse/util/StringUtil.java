package com.spawnchunk.auctionhouse.util;

public class StringUtil {
    public static String leftOf(String s, int pos) {
        return pos > 0 ? s.substring(0, pos) : "";
    }

    public static String rightOf(String s, int pos) {
        return pos + 1 < s.length() ? s.substring(pos + 1) : "";
    }

    public static String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"") || s.startsWith("'") && s.endsWith("'")) {
            if (s.length() <= 2) {
                return "";
            }
            return s.substring(1, s.length() - 2);
        }
        return s;
    }
}

