package com.library.android.common.utils;

import android.widget.EditText;

public final class ValidationUtil {

    private ValidationUtil() {
    }

    public static boolean hasValue(EditText editText) {
        if (editText != null) {
            if (editText.getText() != null) {
                return !editText.getText().toString().isEmpty();
            }
        }
        return false;
    }
}
