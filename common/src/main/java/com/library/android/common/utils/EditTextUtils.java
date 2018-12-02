package com.library.android.common.utils;

import android.widget.EditText;

public final class EditTextUtils {
    private EditTextUtils() {
    }

    public static void clearEditText(EditText... editTexts) {
        if (editTexts != null && editTexts.length > 0) {
            for (EditText editText : editTexts) {
                editText.setText("");
            }
        }
    }

    public static String getString(EditText editText) {
        if (editText != null) {
            return editText.getText().toString();
        } else {
            return "";
        }
    }

    public static void setSelection(EditText et) {
        if (et != null) {
            if (et.getText() != null) {
                et.setSelection(et.getText().length());
            }
        }
    }
}
