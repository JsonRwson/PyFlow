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
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SourcecodeEditor extends androidx.appcompat.widget.AppCompatEditText
{
    // List for tracking the number of lines needed, string to hold the actual numbers for the view
    private TextView lineNumbers;
    private String lineNumbersString = "";
    private final List<Integer> lineNumbersList = new ArrayList<>();

    private final Set<Integer> modifiedLines = new HashSet<>();
    private int previousLineCount = 0;
    private int currentLineCount = 0;

    // Stacks for states to go back to, or redo
    private final Stack<undoRedoState> undoStack = new Stack<>();
    private final Stack<undoRedoState> redoStack = new Stack<>();
    // Flag to track if a change is the result of an undo or redo
    // Dont want to push these changes to the stack
    private boolean isAppOperation = false;
    private int stackLimit = 100;

    // A static class to represent edited states for undo and redo operations
    // Also tracks the cursor position to avoid cursor jumps when undo/redo
    private static class undoRedoState
    {
        private final String text;
        private final int start;
        private final int end;
        private final int cursor;

        undoRedoState(String text, int start, int end, int cursor)
        {
            this.text = text;
            this.start = start;
            this.end = end;
            this.cursor = cursor;
        }
    }

    public SourcecodeEditor(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();

        // Push empty state to undo stack first
        undoStack.push(new undoRedoState(getText().toString(), 0, length(), getSelectionStart()));

        // Regex patterns for different python elements to highlight
        final String[] KEYWORDS = {"and", "as", "assert", "break", "class", "continue", "def", "del", "elif", "else", "except",
                "False", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda", "None", "nonlocal", "not", "or",
                "pass", "raise", "return", "True", "try", "while", "with", "yield", "async", "await"};

        // Match any words in the keywords array separate from other characters
        final Pattern keywords_pattern = Pattern.compile("\\b(" + String.join("|", KEYWORDS) + ")\\b");

        // Match zero or more characters after a hash, not include newlines
        final Pattern comments_pattern = Pattern.compile("#.*");

        // Match zero or more characters between double, triple and single quotes
        final Pattern strings_pattern = Pattern.compile("\".*?\"|'.*?'|\"\"\".*?\"\"\"|'''.*?'''");

        // Match words followed by open and close brackets separate from other characters
        final Pattern functions_pattern = Pattern.compile("\\b\\w+(?=\\s*\\()");

        // Match only one or more digits separate from other characters
        final Pattern numbers_pattern = Pattern.compile("\\b\\d+\\b");

        // Colours for highlighting different elements
        final int keywords_colour = Color.parseColor("#FFC66D");
        final int comments_colour = Color.parseColor("#A7A8A8");
        final int strings_colour = Color.parseColor("#A5C261");
        final int functions_colour = Color.parseColor("#9C93F5");
        final int numbers_colour = Color.parseColor("#93b8ff");

        // Add a text watcher to update the line numbers
        addTextChangedListener(new TextWatcher()
        {
            // Syntax highlighting =========================================================
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                previousLineCount = getLineCount();
            }

            // Hash set used to store modified lines
            // Unique elements, so a line is not added if modified multiple times
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                // Calculate the start and end of modifications per line basis
                int startLine = getLayout().getLineForOffset(start);
                int endLine = getLayout().getLineForOffset(start + count);

                // Add the changed lines to the set
                for (int i = startLine; i <= endLine; i++)
                {
                    modifiedLines.add(i);
                }

                // If the text change isnt an undo or redo operation
                // Clear the redo stack to prevent re-doing after new modifications are made
                if(!isAppOperation)
                {
                    redoStack.clear();
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                currentLineCount = getLineCount();

                // If the line count is different, update line numbers and push changes to undo
                if(previousLineCount != currentLineCount)
                {
                    updateLineNumbers(currentLineCount);

                    if(!isAppOperation)
                    {
                        redoStack.clear();

                        // Maintain limited stack size
                        if(undoStack.size() >= stackLimit)
                        {
                            undoStack.remove(0);
                        }

                        undoStack.push(new undoRedoState(s.toString(), 0, s.length(), getSelectionStart()));
                    }
                }

                // Iterate over the modified lines and apply syntax highlighting to them
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
                    highlightSyntax(s, lineText, start, numbers_pattern, numbers_colour);
                    highlightSyntax(s, lineText, start, keywords_pattern, keywords_colour);
                    highlightSyntax(s, lineText, start, functions_pattern, functions_colour);
                    highlightSyntax(s, lineText, start, strings_pattern, strings_colour);
                    highlightSyntax(s, lineText, start, comments_pattern, comments_colour);
                }

                // On applying syntax highlighting to the modified lines, clear for the next set of changes
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
        this.setShowSoftInputOnFocus(false); // Prevents keyboard from popping up initially
    }

    // Both undo and redo methods set the undo/redo flag to true while updating text
    // Ensures these changes are not pushed to the stack
    public void undo()
    {
        // If there are changes to revert back to
        if (!undoStack.empty())
        {
            isAppOperation = true;

            // Pop the state change and set it as the text
            // Before setting text, create a new edit object to be pushed to the redo stack
            // Ensures after undo, you can revert back
            undoRedoState edit = undoStack.pop();

            // Maintain stack limit
            if(redoStack.size() >= stackLimit)
            {
                redoStack.remove(0);
            }

            redoStack.push(new undoRedoState(getText().toString(), 0, length(), getSelectionStart()));
            setText(edit.text);
            setSelection(edit.cursor);

            isAppOperation = false;
        }
    }

    public void redo()
    {
        // If there are changes to re perform
        if(!redoStack.empty())
        {
            isAppOperation = true;

            // Similar logic as undo, pop the state change, push the current state to the undo stack
            // Then set the text to the redo state
            undoRedoState edit = redoStack.pop();
            undoStack.push(new undoRedoState(getText().toString(), 0, length(), getSelectionStart()));
            setText(edit.text);
            setSelection(edit.cursor);

            isAppOperation = false;
        }
    }

    public void clearStacks()
    {
        redoStack.clear();
        undoStack.clear();

        undoStack.push(new undoRedoState(getText().toString(), 0, length(), getSelectionStart()));
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

    // Called every time the line count changes
    public void updateLineNumbers(int lineCount)
    {
        if(lineNumbers != null)
        {
            // If the line count has increased, add the new line numbers
            for(int i = lineNumbersList.size() + 1; i <= lineCount; i++)
            {
                lineNumbersList.add(i);

                // If this is the first line in the editor
                // Don't need to add a preceding newline to the line numbers view
                if(i == 1)
                {
                    lineNumbersString += i;
                }
                else
                {
                    lineNumbersString += "\n" + i;
                }
            }

            // If the line count has decreased, remove the extra line numbers
            // Continually remove numbers from the string and list until the counts are equal
            while(lineNumbersList.size() > lineCount)
            {
                lineNumbersList.remove(lineNumbersList.size() - 1);
                lineNumbersString = lineNumbersString.substring(0, lineNumbersString.lastIndexOf("\n"));
            }

            // Update the line numbers view
            lineNumbers.setText(lineNumbersString);
        }
    }
}