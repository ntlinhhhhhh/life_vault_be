package com.moon.vault.util;

public final class MaskingUtil {

    private MaskingUtil() {
    }

    public static String maskText(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        if (value.length() <= 3) {
            return "***";
        }
        return value.substring(0, Math.min(3, value.length())) + "***";
    }

    public static String maskPassword() {
        return "********";
    }
}
