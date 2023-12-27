package com.PyFlow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class CustomEditText extends androidx.appcompat.widget.AppCompatEditText
{
    private TextView lineNumbers;

    public CustomEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // Add a TextWatcher to update the line numbers
        addTextChangedListener(new TextWatcher()
        {
            int cursorPosition;
            int previousLength = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {}

            @Override
            public void afterTextChanged(Editable s)
            {
                updateLineNumbers();
            }
        });
    }

//    @Override
//    public boolean onCheckIsTextEditor()
//    {
//        return false;
//    }

    public void setLineNumberView(TextView lineNumberView)
    {
        this.lineNumbers = lineNumberView;
    }

    public void updateLineNumbers()
    {
        if (lineNumbers != null)
        {
            StringBuilder numbers = new StringBuilder();
            int lineCount = getLineCount();

            for (int i = 1; i <= lineCount; i++)
            {
                numbers.append(i).append("\n");
            }
            lineNumbers.setText(numbers.toString());
        }
    }
}
