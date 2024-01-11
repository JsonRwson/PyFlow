package com.PyFlow.keyboard_pages;

import android.widget.EditText;

public abstract class Page
{
    public EditText voiceEditText;

    public void setEditText(String text)
    {
        this.voiceEditText.setText(text);
    }
}
