package com.ariasaproject.cpuminingopt.customview;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;

public class CustomEditTextView extends EditText {
    public CustomEditTextView(Context context) {
        this(context, null);
    }
    public CustomEditTextView(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.editTextStyle);
    }
    public CustomEditTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    public CustomEditTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    @Override
    public boolean getFreezesText() {
        return true;
    }
    @Override
    protected boolean getDefaultEditable() {
        return true;
    }
    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }
    @Override
    public Editable getText() {
        CharSequence text = super.getText();
        if (text == null) {
            return null;
        }
        if (text instanceof Editable) {
            return (Editable) text;
        }
        super.setText(text, BufferType.EDITABLE);
        return (Editable) super.getText();
    }
    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }
    public void setSelection(int start, int stop) {
        Selection.setSelection(getText(), start, stop);
    }
    public void setSelection(int index) {
        Selection.setSelection(getText(), index);
    }
    public void selectAll() {
        Selection.selectAll(getText());
    }
    public void extendSelection(int index) {
        Selection.extendSelection(getText(), index);
    }
    @Override
    public void setEllipsize(TextUtils.TruncateAt ellipsis) {
        if (ellipsis == TextUtils.TruncateAt.MARQUEE) {
            throw new IllegalArgumentException("EditText cannot use the ellipsize mode " + "TextUtils.TruncateAt.MARQUEE");
        }
        super.setEllipsize(ellipsis);
    }
    @Override
    public CharSequence getAccessibilityClassName() {
        return EditText.class.getName();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setHandwritingArea(new Rect(0, 0, w, h));
    }
    @Override
    protected boolean supportsAutoSizeText() {
        return false;
    }
}