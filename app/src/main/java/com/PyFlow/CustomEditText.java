package com.PyFlow;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomEditText extends androidx.appcompat.widget.AppCompatEditText
{
    private TextView lineNumbers;
    private String lineNumbersString = "";
    private final List<Integer> lineNumbersList = new ArrayList<>();
    private final Set<Integer> modifiedLines = new HashSet<>();
    private int previousLineCount = 0;
    private int currentLineCount = 0;

    public CustomEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();

        // Regex patterns for different python elements to highlight
        final String[] KEYWORDS = {"and", "as", "assert", "break", "class", "continue", "def", "del", "elif", "else", "except",
                "False", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda", "None", "nonlocal", "not", "or",
                "pass", "raise", "return", "True", "try", "while", "with", "yield", "async", "await"};

        // Match any words in the keywords array
        final Pattern PATTERN_KEYWORDS = Pattern.compile("\\b(" + String.join("|", KEYWORDS) + ")\\b");

        // Match zero or more characters after a hash, not include newlines
        final Pattern PATTERN_COMMENTS = Pattern.compile("#.*");

        // Match zero or more characters between double, triple and single quotes
        final Pattern PATTERN_STRINGS = Pattern.compile("\".*?\"|'.*?'|\"\"\".*?\"\"\"|'''.*?'''");

        // Match only whole words followed open and close brackets
        final Pattern PATTERN_FUNCTIONS = Pattern.compile("\\b\\w+(?=\\s*\\()");

        // Colours for highlighting different elements
        final int COLOR_KEYWORDS = Color.parseColor("#FFC66D");
        final int COLOR_COMMENTS = Color.parseColor("#A7A8A8");
        final int COLOR_STRINGS = Color.parseColor("#A5C261");
        final int COLOR_FUNCTIONS = Color.parseColor("#9C93F5");

        // Add a TextWatcher to update the line numbers
        addTextChangedListener(new TextWatcher()
        {
            // Syntax highlighting =========================================================
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                previousLineCount = getLineCount();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                int startLine = getLayout().getLineForOffset(start);
                int endLine = getLayout().getLineForOffset(start + count);

                for (int i = startLine; i <= endLine; i++)
                {
                    modifiedLines.add(i);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                currentLineCount = getLineCount();
                if(previousLineCount != currentLineCount)
                {
                    updateLineNumbers(currentLineCount);
                }

                for (int line : modifiedLines)
                {
                    int start = getLayout().getLineStart(line);
                    int end = getLayout().getLineEnd(line);
                    CharSequence lineText = s.subSequence(start, end);

                    // Remove all previous spans in this line to prevent overlapping styles
                    ForegroundColorSpan[] oldSpans = s.getSpans(start, end, ForegroundColorSpan.class);
                    for (ForegroundColorSpan span : oldSpans)
                    {
                        s.removeSpan(span);
                    }

                    // Apply syntax highlighting to this line
                    highlightSyntax(s, lineText, start, PATTERN_KEYWORDS, COLOR_KEYWORDS);
                    highlightSyntax(s, lineText, start, PATTERN_FUNCTIONS, COLOR_FUNCTIONS);
                    highlightSyntax(s, lineText, start, PATTERN_STRINGS, COLOR_STRINGS);
                    highlightSyntax(s, lineText, start, PATTERN_COMMENTS, COLOR_COMMENTS);
                }
                modifiedLines.clear();
            }

            private void highlightSyntax(Editable s, CharSequence lineText, int start, Pattern pattern, int color)
            {
                Matcher matcher = pattern.matcher(lineText);

                while (matcher.find())
                {
                    ForegroundColorSpan span = new ForegroundColorSpan(color);
                    s.setSpan(span, start + matcher.start(), start + matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
        updateLineNumbers(1);
    }

    public void updateLineNumbers(int lineCount)
    {
        if (lineNumbers != null)
        {
            // If the line count has increased, add the new line numbers
            for (int i = lineNumbersList.size() + 1; i <= lineCount; i++)
            {
                lineNumbersList.add(i);

                if (i == 1)
                {
                    lineNumbersString += i;
                }
                else
                {
                    lineNumbersString += "\n" + i;
                }
            }

            // If the line count has decreased, remove the extra line numbers
            while (lineNumbersList.size() > lineCount) {
                lineNumbersList.remove(lineNumbersList.size() - 1);
                lineNumbersString = lineNumbersString.substring(0, lineNumbersString.lastIndexOf("\n"));
            }

            // Update the line numbers view
            lineNumbers.setText(lineNumbersString);
        }
    }
}