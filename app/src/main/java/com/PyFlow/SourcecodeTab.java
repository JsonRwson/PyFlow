package com.PyFlow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.text.InputType;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SourcecodeTab extends Fragment {

    private int mode = 0; // 0 for keyboard closed, 1 for keyboard open, 2 for keyboard closed
    private CustomEditText sourceCode;
    private TextView lineNumbers;
    private LinearLayout keyboardPanel;

    private Button buttonImport;
    private Button buttonVar;
    private Button buttonFunc;

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
        this.keyboardPanel = view.findViewById(R.id.keyboardPanel);

        // Resize page buttons on keyboard
        LinearLayout keyboardPanelButtons = view.findViewById(R.id.keyboardPanelButtons);
        int keyboardButtonWidth = 150;

        for (int i = 0; i < keyboardPanelButtons.getChildCount(); i++)
        {
            View child = keyboardPanelButtons.getChildAt(i);
            if (child instanceof Button)
            {
                child.getLayoutParams().width = keyboardButtonWidth;
                child.requestLayout();
            }
        }

        // Stop the editor recieving touch inputs when the empty keyboard space is pressed
        keyboardPanel.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });

        // Set the line number view
        sourceCode.setLineNumberView(lineNumbers);

        // Disable the default keyboard from popping up
        sourceCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        sourceCode.setImeOptions(EditorInfo.IME_ACTION_NONE);
        sourceCode.setFocusable(false);
        sourceCode.setFocusableInTouchMode(false);


        // Test program inserted into editor
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
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    keyboardPanel.setVisibility(View.VISIBLE);
                    sourceCode.setFocusableInTouchMode(true);
                    sourceCode.requestFocus();

                    mode = 1;
                }
                else if (mode == 1)
                {
                    keyboardPanel.setVisibility(View.GONE);
                    sourceCode.setFocusableInTouchMode(true);
                    sourceCode.requestFocus();
                    imm.showSoftInput(sourceCode, InputMethodManager.SHOW_IMPLICIT);
                    mode = 2;
                }
                else
                {
                    keyboardPanel.setVisibility(View.GONE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    sourceCode.clearFocus();
                    sourceCode.setFocusable(false);
                    sourceCode.setFocusableInTouchMode(false);
                    mode = 0;
                }
            }
        });

        // References to Navigation and Enter + Del + Keyboard toggle buttons
        Button navLeft = view.findViewById(R.id.nav_left);
        Button navRight = view.findViewById(R.id.nav_right);
        Button navUp = view.findViewById(R.id.nav_up);
        Button navDown = view.findViewById(R.id.nav_down);
        Button delete = view.findViewById(R.id.delete);
        Button enter = view.findViewById(R.id.enter);

        // Navigation and Enter + Del + Keyboard toggle buttons onClicks ======
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

        // Keyboard Page navigation buttons onClicks
        Button importPageButton = view.findViewById(R.id.key_import);
        Button variablesPageButton = view.findViewById(R.id.key_var);
        Button functionsPageButton = view.findViewById(R.id.key_func);
        Button loopPageButton = view.findViewById(R.id.key_loop);
        Button selectionPageButton = view.findViewById(R.id.key_selection);
        Button OOPPageButton = view.findViewById(R.id.key_OOP);
        Button exceptionsPageButton = view.findViewById(R.id.key_except);

        // Find the ViewPager
        KeyboardViewPager viewPager = view.findViewById(R.id.keyboard_ViewPager);

        // Create a list of pages
        List<Integer> pages = new ArrayList<>();
        pages.add(R.layout.page_import);
        pages.add(R.layout.page_variables);
        pages.add(R.layout.page_functions);
        pages.add(R.layout.page_loops);
        pages.add(R.layout.page_selection);
        pages.add(R.layout.page_oop);
        pages.add(R.layout.page_except);

        // Create a PagerAdapter
        PagerAdapter keyboardPagerAdapter = new PagerAdapter()
        {
            @Override
            public int getCount()
            {
                return pages.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
            {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position)
            {
                LayoutInflater inflater = LayoutInflater.from(container.getContext());
                View page = inflater.inflate(pages.get(position), container, false);
                container.addView(page);

                return page;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object)
            {
                container.removeView((View) object);
            }
        };

        // Set the PagerAdapter to the ViewPager
        viewPager.setAdapter(keyboardPagerAdapter);

        importPageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                viewPager.setCurrentItem(0);
            }
        });

        variablesPageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                viewPager.setCurrentItem(1);
            }
        });

        functionsPageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                viewPager.setCurrentItem(2);
            }
        });

        loopPageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                viewPager.setCurrentItem(3);
            }
        });

        selectionPageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                viewPager.setCurrentItem(4);
            }
        });

        OOPPageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                viewPager.setCurrentItem(5);
            }
        });

        exceptionsPageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                viewPager.setCurrentItem(6);
            }
        });

        return view;
    }
}