package com.PyFlow.keyboard_pages;

import android.widget.EditText;

public abstract class Page
{
    public EditText voiceEditText;

    public void setEditText(String text)
    {
        if (this.voiceEditText != null)
        {
            this.voiceEditText.setText(text);
        }
    }
}
