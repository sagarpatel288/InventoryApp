package com.library.android.common.utils;

public final class StringUtils {

    private StringUtils() {
    }

    public static boolean isNotNullNotEmpty(String s) {
        return s != null && !s.isEmpty();
    }

    public static String getDefaultString(String value, String defaultString) {
        if (isNotNullNotEmpty(value)) {
            return value;
        } else {
            return defaultString;
        }
    }

}
