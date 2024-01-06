package com.PyFlow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomEditText extends androidx.appcompat.widget.AppCompatEditText
{
    private TextView lineNumbers;

    public CustomEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();

        // Regex patterns for different python elements to highlight
        final String[] KEYWORDS = {"and", "as", "assert", "break", "class", "continue", "def", "del", "elif", "else", "except",
                "False", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda", "None", "nonlocal", "not", "or",
                "pass", "raise", "return", "True", "try", "while", "with", "yield", "async", "await"};
        final Pattern PATTERN_KEYWORDS = Pattern.compile("\\b(" + String.join("|", KEYWORDS) + ")\\b");
        final Pattern PATTERN_COMMENTS = Pattern.compile("#.*");
        final Pattern PATTERN_STRINGS = Pattern.compile("\".*?\"|'.*?'|\"\"\".*?\"\"\"|'''.*?'''");
        final Pattern PATTERN_FUNCTIONS = Pattern.compile("\\b\\w+(?=\\s*\\()");

        // Colours for highlighting different elements
        final int COLOR_KEYWORDS = Color.parseColor("#FFC66D");
        final int COLOR_COMMENTS = Color.parseColor("#A7A8A8");
        final int COLOR_STRINGS = Color.parseColor("#A5C261");
        final int COLOR_FUNCTIONS = Color.parseColor("#9C93F5");

        // Add a TextWatcher to update the line numbers
        addTextChangedListener(new TextWatcher()
        {
            int cursorPosition;
            int previousLength = 0;

            // Syntax highlighting =========================================================
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                previousLength = s.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {}

            List<ForegroundColorSpan> spans = new ArrayList<>();
            @Override
            public void afterTextChanged(Editable s)
            {
                // Check if a newline character was added
                if (s.length() > 0 && s.charAt(s.length() - 1) == '\n' || s.length() < previousLength)
                {
                    updateLineNumbers();
                }
                else
                {
                    // Get the current line count
                    int currentLineCount = getLineCount();

                    // Ensure at least 1st line number is shown
                    if (getText().length() == 0)
                    {
                        currentLineCount = 1;
                    }

                    // Only update the line numbers if the line count has changed
                    if (currentLineCount != lineNumbers.getLineCount())
                    {
                        updateLineNumbers();
                    }
                }

                // Syntax highlighting for python
                // Remove all previous spans to prevent overlapping styles
                for (ForegroundColorSpan span : spans)
                {
                    s.removeSpan(span);
                }
                spans.clear();

                // The ordering of highlighting is important here
                // Highlight Python keywords
                highlightSyntax(s, PATTERN_KEYWORDS, COLOR_KEYWORDS);

                // Highlight function calls
                highlightSyntax(s, PATTERN_FUNCTIONS, COLOR_FUNCTIONS);

                // Highlight strings
                highlightSyntax(s, PATTERN_STRINGS, COLOR_STRINGS);

                // Highlight comments
                highlightSyntax(s, PATTERN_COMMENTS, COLOR_COMMENTS);
            }

            private void highlightSyntax(Editable s, Pattern pattern, int color)
            {
                Matcher matcher = pattern.matcher(s);
                while (matcher.find())
                {
                    ForegroundColorSpan span = new ForegroundColorSpan(color);
                    s.setSpan(span, matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spans.add(span);
                }
            }
        });
    }

    private void init()
    {
        this.setShowSoftInputOnFocus(false); // Prevents keyboard from popping up
    }

    public void allowSoftInput(boolean allow)
    {
        this.setShowSoftInputOnFocus(allow);
    }

    public void setLineNumberView(TextView lineNumberView)
    {
        this.lineNumbers = lineNumberView;
        updateLineNumbers();
    }

    public void updateLineNumbers()
    {
        if (lineNumbers != null)
        {
            StringBuilder numbers = new StringBuilder();
            int lineCount = getLineCount();

            // Ensure at least 1st line number is shown
            if (getText().length() == 0)
            {
                lineCount = 1;
            }

            for (int i = 1; i <= lineCount; i++)
            {
                numbers.append(i).append("\n");
            }

            lineNumbers.setText(numbers.toString());
        }
    }
}
