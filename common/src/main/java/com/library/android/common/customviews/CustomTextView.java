package com.library.android.common.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;

import com.library.android.common.R;
import com.library.android.common.utils.TypefaceUtils;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.ColorUtils;

public class CustomTextView extends AppCompatTextView {

    /**
     * An int {@link android.graphics.Color} to set as for {@link android.widget.TextView#setTextColor(int)} with alpha color state list
     */
    private int btnTxtColor;
    private int btnTxtPressedColor;
    private boolean isAlphaPressedColor;
    private int btnTxtTypeface;
    private CharSequence originalText;
    private float letterSpacing = Spacing.NO_CHAR_SPACE;
    private float charSpacing = Spacing.NO_CHAR_SPACE;

    public CustomTextView(Context context) {
        super(context);
        originalText = super.getText();
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        originalText = super.getText();
        customizeView(context, attrs);
    }

    private void customizeView(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView, 0, 0);
        setCustomTypeface(typedArray); //Sets custom typeface
        setAlphaColorStates(typedArray); //Sets alpha color state list
        setCharSpacing(typedArray);
    }

    private void setCustomTypeface(TypedArray typedArray) {
        int typeface = typedArray.getInteger(R.styleable.CustomTextView_typeface, 0);
        setBtnTxtTypeface(typeface);
    }

    /**
     * Initializes our primary color customization
     * <p>
     * If {@link #isAlphaPressedColor} is true then our {@link CustomTextView} will have color state list for different states
     * <p>
     * Being used in {@link #CustomTextView(Context, AttributeSet)}
     *
     * @param typedArray A {@link TypedArray} from {@link #CustomTextView(Context, AttributeSet)}
     * @since 1.0
     */
    private void setAlphaColorStates(TypedArray typedArray) {
        setBtnTxtColor(typedArray.getInteger(R.styleable.CustomTextView_btnTxtColor, 0));
        setBtnTxtPressedColor(typedArray.getInteger(R.styleable.CustomTextView_btnTxtPressedColor, 0));
        setAlphaPressedColor(typedArray.getBoolean(R.styleable.CustomTextView_btnHasAlphaPressedColor, false));
//        typedArray.recycle();
    }

    private void setCharSpacing(TypedArray typedArray) {
        charSpacing = typedArray.getFloat(R.styleable.CustomTextView_charSpacing, 0);
        setCharSpacing(charSpacing);
        typedArray.recycle();
    }

    /**
     * Sets color state list (selector) programmatically
     * <p>
     * This works same as how selector drawable works.
     * <p>
     * Being used in {@link #setAlphaColorStates(TypedArray)}
     *
     * @param btnTxtColor An int color specified by custom property either in xml or by programmatically
     * @since 1.0
     */
    private void setColorStateList(int btnTxtColor) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_pressed},
                new int[]{android.R.attr.state_focused},
                new int[]{android.R.attr.state_enabled}
        };

        int[] colors = new int[]{
                getBtnTxtPressedColor() == 0 ? getAlphaTxtColor(btnTxtColor) : getBtnTxtPressedColor(),
                getBtnTxtPressedColor() == 0 ? getAlphaTxtColor(btnTxtColor) : getBtnTxtPressedColor(),
                btnTxtColor
        };
        ColorStateList colorStateList = new ColorStateList(states, colors);
        setTextColor(colorStateList);
    }

    /**
     * Gives the value of {@link #btnTxtColor} programmatically
     *
     * @since 1.0
     */
    private int getBtnTxtColor() {
        return btnTxtColor;
    }

    /**
     * Sets the value of {@link #btnTxtColor} programmatically
     *
     * @param btnTxtColor A {@link #btnTxtColor} to be set for {@link CustomTextView}
     * @since 1.0
     */
    public void setBtnTxtColor(int btnTxtColor) {
        setTextColor(btnTxtColor);
        this.btnTxtColor = btnTxtColor;
        setColorStateList(getBtnTxtColor());
    }

    private void applyLetterSpace() {
        // Note: 10/25/2018 by sagar  If text string value is null or empty, no need to go ahead, just return
        if (this.originalText == null) {
            return;
        }
        // Note: 10/25/2018 by sagar  Adding space to existing letters
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0, j = originalText.length(); i < j; i++) {
            String c = "" + originalText.charAt(i);
            stringBuilder.append(c);
            if (i + 1 < originalText.length()) {
                stringBuilder.append("\u00A0");
            }
        }
        // Note: 10/25/2018 by sagar  ScaleXSpan to resize text letters proportional to letter space
        SpannableString spannableString = new SpannableString(stringBuilder.toString());
        if (stringBuilder.length() > 1) {
            for (int i = 1, j = stringBuilder.toString().length(); i < j; i += 2) {
                spannableString.setSpan(new ScaleXSpan((charSpacing + 1) / 10), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        super.setText(spannableString, BufferType.SPANNABLE);
    }

    /**
     * Gives the value of {@link #btnTxtPressedColor} programmatically
     *
     * @since 1.0
     */
    public int getBtnTxtPressedColor() {
        return btnTxtPressedColor;
    }

    /**
     * Gives color with alpha channel
     * <p>
     * Sets 50% opacity and hence 50% transparency as we are using static int value 128 for alpha.
     * We have set it so to keep the consistency throughout the app.
     * <p>
     * Being used in {@link #setColorStateList(int)}
     *
     * @param color A {@link #btnTxtColor} either set through xml or by programmatically
     * @return New value for {@link #btnTxtColor} having alpha channel added
     * @since 1.0
     */
    private int getAlphaTxtColor(int color) {
        return ColorUtils.setAlphaComponent(color, 128);
    }

    /**
     * Sets the value of {@link #btnTxtPressedColor} programmatically
     *
     * @param btnTxtPressedColor A {@link #btnTxtPressedColor} to be set for {@link CustomTextView}
     * @since 1.0
     */
    public void setBtnTxtPressedColor(int btnTxtPressedColor) {
        this.btnTxtPressedColor = btnTxtPressedColor;
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr); //Do anything after super() here if we want to play with style (theme) as well
        originalText = super.getText();
        customizeView(context, attrs);
    }

    @Override
    public float getLetterSpacing() {
        return letterSpacing;
    }

    @Override
    public void setLetterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        originalText = text;
        if (getCharSpacing() > 0) {
            applyLetterSpace();
            this.invalidate();
        }
    }

    public float getCharSpacing() {
        return charSpacing;
    }

    public void setCharSpacing(float charSpacing) {
        // Note: 10/25/2018 by sagar  https://stackoverflow.com/questions/5133548/how-to-change-letter-spacing-in-a-textview
        this.charSpacing = charSpacing;
        applyLetterSpace();
    }

    /**
     * Gives the value of {@link #isAlphaPressedColor}
     *
     * @since 1.0
     */
    public boolean isAlphaPressedColor() {
        return isAlphaPressedColor;
    }

    /**
     * Sets the value of {@link #isAlphaPressedColor} programmatically
     *
     * @param hasAlphaPressedColor A {@link #isAlphaPressedColor} to be set for {@link CustomTextView}
     * @since 1.0
     */
    public void setAlphaPressedColor(boolean hasAlphaPressedColor) {
        this.isAlphaPressedColor = hasAlphaPressedColor;
    }

    /**
     * Gives the value of {@link #btnTxtTypeface} programmatically
     *
     * @since 1.0
     */
    public int getBtnTxtTypeface() {
        return btnTxtTypeface;
    }

    /**
     * Sets the value of {@link #btnTxtTypeface} programmatically
     *
     * @param btnTxtTypeface A {@link #btnTxtTypeface} to be set for {@link CustomTextView}
     * @since 1.0
     */
    public void setBtnTxtTypeface(int btnTxtTypeface) {
        this.btnTxtTypeface = btnTxtTypeface;
        switch (btnTxtTypeface) {
            case TypefaceUtils.INT_CODE_REGULAR:
                setTypeface(TypefaceUtils.getInstance().getRegularTypeface(getContext()));
                break;

            case TypefaceUtils.INT_CODE_REGULAR_ITALIC:
                setTypeface(TypefaceUtils.getInstance().getRegularItalicTypeface(getContext()));
                break;

            case TypefaceUtils.INT_CODE_LIGHT:
                setTypeface(TypefaceUtils.getInstance().getLightTypeface(getContext()));
                break;

            case TypefaceUtils.INT_CODE_LIGHT_ITALIC:
                setTypeface(TypefaceUtils.getInstance().getLightItalicTypeface(getContext()));
                break;

            case TypefaceUtils.INT_CODE_BOLD:
                setTypeface(TypefaceUtils.getInstance().getBoldTypeface(getContext()));
                break;

            case TypefaceUtils.INT_CODE_BOLD_ITALIC:
                setTypeface(TypefaceUtils.getInstance().getBoldItalicTypeface(getContext()));
                break;

            case TypefaceUtils.INT_CODE_BOLD_HEAVY:
                setTypeface(TypefaceUtils.getInstance().getBoldHeavyTypeface(getContext()));
                break;

            default:
                setTypeface(TypefaceUtils.getInstance().getRegularTypeface(getContext()));
                break;
        }
    }

    @Override
    public CharSequence getText() {
        return originalText;
    }

    public class Spacing {
        public static final float NO_CHAR_SPACE = 0;
        public static final float SMALL = 0.1f;
        public static final float MEDIUM = 0.2f;
        public static final float LARGE = 0.3f;
    }
}