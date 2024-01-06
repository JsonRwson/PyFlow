package com.PyFlow;
import static androidx.core.content.ContextCompat.getSystemService;

import com.PyFlow.keyboard_pages.*;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SourcecodeTab extends Fragment
{
    private int mode = 0; // 0 for keyboard closed, 1 for keyboard open, 2 for keyboard closed
    private CustomEditText sourceCode;
    private LinearLayout keyboardPanel;
    private Button[] pageNavButtons;

    public SourcecodeTab() {}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        SharedViewModel sharedModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        CustomEditText sourceEditor = getView().findViewById(R.id.sourceCode);
        sharedModel.select(sourceEditor);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sourcecode_tab, container, false);

        // References to ui elements
        TextView lineNumbers = view.findViewById(R.id.lineNumbers);
        LinearLayout keyboardNavLayout = view.findViewById(R.id.keyboardPanelButtons);
        this.sourceCode = view.findViewById(R.id.sourceCode);
        this.keyboardPanel = view.findViewById(R.id.keyboardPanel);


        // Fetch keyboard page buttons, resize them and add then to an array
        int keyboardButtonWidth = 150;
        pageNavButtons = new Button[keyboardNavLayout.getChildCount()];

        for (int i = 0; i < keyboardNavLayout.getChildCount(); i++)
        {
            View child = keyboardNavLayout.getChildAt(i);
            if (child instanceof Button)
            {
                pageNavButtons[i] = (Button) child;
                child.getLayoutParams().width = keyboardButtonWidth;
                child.requestLayout();
            }
        }

        // Stop the editor receiving touch inputs when the empty keyboard space is pressed
        keyboardPanel.setOnTouchListener((v, event) -> true);

        // Set the line number view
        sourceCode.setLineNumberView(lineNumbers);

        // Disable the default keyboard from popping up
        sourceCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        sourceCode.setImeOptions(EditorInfo.IME_ACTION_NONE);
        sourceCode.allowSoftInput(false);

        // Find the keyboardButton
        Button keyboardButton = view.findViewById(R.id.keyboardButton);
        keyboardButton.setOnClickListener(v ->
        {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (mode == 0) // Show custom keyboard panel
            {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                keyboardPanel.setVisibility(View.VISIBLE);
                sourceCode.requestFocus();
                sourceCode.allowSoftInput(false);

                mode = 1;
            }
            else if (mode == 1) // Show built in keyboard
            {
                keyboardPanel.setVisibility(View.GONE);
                sourceCode.requestFocus();
                imm.showSoftInput(sourceCode, InputMethodManager.SHOW_IMPLICIT);
                sourceCode.allowSoftInput(true);

                mode = 2;
            }
            else // Close both
            {
                keyboardPanel.setVisibility(View.GONE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                sourceCode.clearFocus();
                sourceCode.allowSoftInput(false);

                mode = 0;
            }
        });

        // References to Navigation and Enter + Del + Keyboard toggle buttons
        Button navLeft = view.findViewById(R.id.nav_left);
        Button navRight = view.findViewById(R.id.nav_right);
        Button navUp = view.findViewById(R.id.nav_up);
        Button navDown = view.findViewById(R.id.nav_down);
        Button delete = view.findViewById(R.id.delete);
        Button enter = view.findViewById(R.id.enter);

        // Navigation and Enter + Del + Keyboard toggle buttons onClicks ===========================================
        navLeft.setOnClickListener(v ->
        {
            int start = Math.max(sourceCode.getSelectionStart() - 1, 0);
            sourceCode.setSelection(start);
        });

        navRight.setOnClickListener(v ->
        {
            int end = Math.min(sourceCode.getSelectionStart() + 1, sourceCode.getText().length());
            sourceCode.setSelection(end);
        });

        navUp.setOnClickListener(v ->
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
        });

        navDown.setOnClickListener(v ->
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
        });

        // Single tap delete
        delete.setOnClickListener(v ->
        {
            int start = sourceCode.getSelectionStart();
            int end = sourceCode.getSelectionEnd();

            if (start != end)
            {
                // Some text is selected. Delete it.
                sourceCode.getText().delete(start, end);
            }
            else if (start > 0)
            {
                // No text is selected. Delete character before cursor.
                sourceCode.getText().delete(start - 1, start);
            }
        });

        // Press and hold delete
        delete.setOnTouchListener(new View.OnTouchListener()
        {
            final Handler handler = new Handler();
            long startTime;
            int delay = 200;
            private Runnable runnable;

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    startTime = System.currentTimeMillis();

                    if(runnable != null)
                    {
                        handler.removeCallbacks(runnable);
                    }

                    runnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            int start = sourceCode.getSelectionStart();
                            if (start > 0)
                            {
                                sourceCode.getText().delete(start - 1, start);
                            }

                            long duration = System.currentTimeMillis() - startTime;
                            delay = Math.max(50, (int)(200 - duration / 10));

                            handler.postDelayed(this, delay);
                        }
                    };

                    handler.postDelayed(runnable, delay);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    if(runnable != null)
                    {
                        handler.removeCallbacks(runnable);
                    }
                }
                return false;
            }
        });

        enter.setOnClickListener(v ->
        {
            int start = sourceCode.getSelectionStart();
            int end = sourceCode.getSelectionEnd();

            if (start != end)
            {
                // Some text is selected replace it with a newline.
                sourceCode.getText().replace(start, end, "\n");
            }
            else
            {
                // No text is selected insert a newline at the cursor position.
                sourceCode.getText().insert(start, "\n");
            }
        });

        // Utility buttons, tab, space, copy, paste, comment
        Button utilTab = view.findViewById(R.id.util_tab);
        Button utilSpace = view.findViewById(R.id.util_space);
        Button utilCopy = view.findViewById(R.id.util_copy);
        Button utilPaste = view.findViewById(R.id.util_paste);
        Button utilComment = view.findViewById(R.id.util_comment);

        utilTab.setOnClickListener(v ->
        {
            int startSelection = sourceCode.getSelectionStart();
            int endSelection = sourceCode.getSelectionEnd();

            String selectedText = sourceCode.getText().toString().substring(startSelection, endSelection);
            if (selectedText.contains("\n"))
            {
                selectedText = selectedText.replaceAll("\n", "\n\t");
                sourceCode.getText().replace(startSelection, endSelection, "\t" + selectedText);
            }
            else
            {
                sourceCode.getText().insert(startSelection, "\t");
            }
        });

        utilSpace.setOnClickListener(v ->
        {
            int startSelection = sourceCode.getSelectionStart();
            int endSelection = sourceCode.getSelectionEnd();
            if (startSelection != endSelection)
            {
                sourceCode.getText().replace(startSelection, endSelection, " ");
            }
            else
            {
                sourceCode.getText().insert(startSelection, " ");
            }
        });

        utilCopy.setOnClickListener(v ->
        {
            int startSelection = sourceCode.getSelectionStart();
            int endSelection = sourceCode.getSelectionEnd();

            String selectedText = sourceCode.getText().toString().substring(startSelection, endSelection);
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", selectedText);
            clipboard.setPrimaryClip(clip);
        });

        utilPaste.setOnClickListener(v ->
        {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = clipboard.getPrimaryClip();

            if (clip != null)
            {
                String pasteData = clip.getItemAt(0).getText().toString();
                int startSelection = sourceCode.getSelectionStart();
                int endSelection = sourceCode.getSelectionEnd();

                if (startSelection != endSelection)
                {
                    sourceCode.getText().replace(startSelection, endSelection, pasteData);
                }
                else
                {
                    sourceCode.getText().insert(startSelection, pasteData);
                }
            }
        });

        utilComment.setOnClickListener(v ->
        {

        });

        // Keyboard Page navigation buttons onClicks =================================================
        Button importPageButton = view.findViewById(R.id.key_import);
        Button variablesPageButton = view.findViewById(R.id.key_var);
        Button functionsPageButton = view.findViewById(R.id.key_func);
        Button loopPageButton = view.findViewById(R.id.key_loop);
        Button selectionPageButton = view.findViewById(R.id.key_selection);
        Button OOPPageButton = view.findViewById(R.id.key_OOP);
        Button exceptionsPageButton = view.findViewById(R.id.key_except);
        Button snippetsPageButton = view.findViewById(R.id.key_snip);

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
        pages.add(R.layout.page_snippets);

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

                // Initialise classes that manage the code for each page, pass them required data
                switch (position)
                {
                    case 0:
                        imports_page page_import = new imports_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 1:
                        variables_page page_variables = new variables_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 2:
                        functions_page page_functions = new functions_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 3:
                        loops_page page_loops = new loops_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 4:
                        selection_page page_selection = new selection_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 5:
                        OOP_page page_OOP = new OOP_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 6:
                        exceptions_page page_exceptions = new exceptions_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 7:
                        snippets_page page_snippets = new snippets_page(page, SourcecodeTab.this, sourceCode);
                        break;
                }
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
        highlightButton(importPageButton);

        // Keyboard panel navigation onclick ==========================================================================
        importPageButton.setOnClickListener(view18 ->
        {
            viewPager.setCurrentItem(0);
            resetButtonBackgrounds();
            highlightButton(importPageButton);

        });

        variablesPageButton.setOnClickListener(view17 ->
        {
            viewPager.setCurrentItem(1);
            resetButtonBackgrounds();
            highlightButton(variablesPageButton);
        });

        functionsPageButton.setOnClickListener(view16 ->
        {
            viewPager.setCurrentItem(2);
            resetButtonBackgrounds();
            highlightButton(functionsPageButton);
        });

        loopPageButton.setOnClickListener(view15 ->
        {
            viewPager.setCurrentItem(3);
            resetButtonBackgrounds();
            highlightButton(loopPageButton);
        });

        selectionPageButton.setOnClickListener(view14 ->
        {
            viewPager.setCurrentItem(4);
            resetButtonBackgrounds();
            highlightButton(selectionPageButton);
        });

        OOPPageButton.setOnClickListener(view13 ->
        {
            viewPager.setCurrentItem(5);
            resetButtonBackgrounds();
            highlightButton(OOPPageButton);
        });

        exceptionsPageButton.setOnClickListener(view12 ->
        {
            viewPager.setCurrentItem(6);
            resetButtonBackgrounds();
            highlightButton(exceptionsPageButton);
        });

        snippetsPageButton.setOnClickListener(view1 ->
        {
            viewPager.setCurrentItem(7);
            resetButtonBackgrounds();
            highlightButton(snippetsPageButton);
        });

        return view;
    }

    // Button styling methods =============================================================================================
    private void resetButtonBackgrounds()
    {
        for (Button button : pageNavButtons)
        {
            button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.keyboard_page_buttons, null));
        }
    }

    // Method to highlight a button
    private void highlightButton(Button button)
    {
        button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.keyboard_page_button_highlighted, null));
    }
}