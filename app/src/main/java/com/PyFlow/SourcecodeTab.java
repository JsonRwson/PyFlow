package com.PyFlow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class SourcecodeTab extends Fragment {

    private int mode = 0; // 0 for keyboard closed, 1 for keyboard open, 2 for keyboard closed
    private CustomEditText sourceCode;
    private TextView lineNumbers;

    public SourcecodeTab() {}

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sourcecode_tab, container, false);

        // Find the CustomEditText and TextView
        this.sourceCode = view.findViewById(R.id.sourceCode);
        this.lineNumbers = view.findViewById(R.id.lineNumbers);

        // Set the line number view
        sourceCode.setLineNumberView(lineNumbers);

        // Disable the default keyboard from popping up
        sourceCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        sourceCode.setImeOptions(EditorInfo.IME_ACTION_NONE);
        sourceCode.setFocusable(false);
        sourceCode.setFocusableInTouchMode(false);


        // Test program
        String pythonCode = "import random\n\n" +
                "def guess_the_number():\n" +
                "    number_to_guess = random.randint(1, 100)\n" +
                "    guess = None\n" +
                "    while guess != number_to_guess:\n" +
                "        guess = int(input(\"Guess a number between 1 and 100: \"))\n" +
                "        if guess < number_to_guess:\n" +
                "            print(\"Too low!\")\n" +
                "        elif guess > number_to_guess:\n" +
                "            print(\"Too high!\")\n" +
                "    print(\"Congratulations! You guessed the number.\")\n\n" +
                "guess_the_number()";
        sourceCode.setText(pythonCode);
        sourceCode.updateLineNumbers();

        // Find the keyboardButton
        Button keyboardButton = view.findViewById(R.id.keyboardButton);
        keyboardButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (mode == 0)
                {
                    sourceCode.setFocusableInTouchMode(true);
                    sourceCode.requestFocus();
                    imm.showSoftInput(sourceCode, InputMethodManager.SHOW_IMPLICIT);
                    mode = 1;
                }
                else if (mode == 1)
                {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    sourceCode.clearFocus();
                    sourceCode.setFocusable(false);
                    sourceCode.setFocusableInTouchMode(false);
                    mode = 2;
                }
                else
                {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    sourceCode.clearFocus();
                    sourceCode.setFocusable(false);
                    sourceCode.setFocusableInTouchMode(false);
                    mode = 0;
                }
            }
        });

        // Find the navigation buttons
        Button navLeft = view.findViewById(R.id.nav_left);
        Button navRight = view.findViewById(R.id.nav_right);
        Button navUp = view.findViewById(R.id.nav_up);
        Button navDown = view.findViewById(R.id.nav_down);
        Button delete = view.findViewById(R.id.delete);
        Button enter = view.findViewById(R.id.enter);

        navLeft.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int start = Math.max(sourceCode.getSelectionStart() - 1, 0);
                sourceCode.setSelection(start);
            }
        });

        navRight.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int end = Math.min(sourceCode.getSelectionStart() + 1, sourceCode.getText().length());
                sourceCode.setSelection(end);
            }
        });

        navUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int pos = sourceCode.getSelectionStart();
                Layout layout = sourceCode.getLayout();

                if (layout != null)
                {
                    int line = layout.getLineForOffset(pos);
                    int offset = pos - layout.getLineStart(line);
                    if (line > 0)
                    {
                        int move = Math.min(layout.getLineStart(line - 1) + offset, layout.getLineEnd(line - 1));
                        sourceCode.setSelection(move);
                    }
                }
            }
        });

        navDown.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int pos = sourceCode.getSelectionStart();
                Layout layout = sourceCode.getLayout();

                if (layout != null)
                {
                    int line = layout.getLineForOffset(pos);
                    int offset = pos - layout.getLineStart(line);
                    if (line < layout.getLineCount() - 1)
                    {
                        int move = Math.min(layout.getLineStart(line + 1) + offset, layout.getLineEnd(line + 1));
                        sourceCode.setSelection(move);
                    }
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int start = sourceCode.getSelectionStart();
                if (start > 0) {
                    sourceCode.getText().delete(start - 1, start);
                }
            }
        });

        enter.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, "\n");
            }
        });

        return view;
    }
}