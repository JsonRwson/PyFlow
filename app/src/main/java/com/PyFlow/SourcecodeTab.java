package com.PyFlow;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.PagerAdapter;

import com.PyFlow.keyboard_pages.OOP_page;
import com.PyFlow.keyboard_pages.exceptions_page;
import com.PyFlow.keyboard_pages.functions_page;
import com.PyFlow.keyboard_pages.imports_page;
import com.PyFlow.keyboard_pages.loops_page;
import com.PyFlow.keyboard_pages.operators_page;
import com.PyFlow.keyboard_pages.selection_page;
import com.PyFlow.keyboard_pages.variables_page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Class for the fragment of the source code editing tab
public class SourcecodeTab extends Fragment
{
    private int mode = 0; // 3 modes, custom keyboard, default keyboard, no keyboards
    private String currentFileName = "untitled"; // Store current file name, untitled by default
    private Uri currentFileUri = null; // Store current file uri, null by default

    private SourcecodeEditor sourceCode;
    public EditText voiceEditText;
    private LinearLayout keyboardPanel;
    private Button[] pageNavButtons;
    private Toolbar toolbar;

    // Request codes, for activity result
    private static final int speechCode = 0;
    private static final int saveCode = 1;
    private static final int loadCode = 2;

    // Page objects
    private imports_page page_import;
    private variables_page page_variables;
    private functions_page page_functions;
    private loops_page page_loops;
    private selection_page page_selection;
    private operators_page page_operators;
    private OOP_page page_OOP;
    private exceptions_page page_except;

    public SourcecodeTab() {}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        // Create the shared view model to share the source code editor widget to the execute code tab
        super.onViewCreated(view, savedInstanceState);
        SharedViewModel sharedModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        SourcecodeEditor sourceEditor = getView().findViewById(R.id.sourceCode);
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
        this.toolbar = getActivity().findViewById(R.id.toolbar);
        this.toolbar.setTitle(currentFileName);

        // Toolbar operations, undo, redo, save, save as, load from file
        // Call corresponding methods for toolbar actions
        toolbar.setOnMenuItemClickListener(item ->
        {
            int itemId = item.getItemId();
            if(itemId == R.id.action_undo) // If undo pressed
            {
                sourceCode.undo(); // Call undo on the editor widget
                return true;
            }
            else if(itemId == R.id.action_redo) // If redo pressed
            {
                sourceCode.redo(); // Call redo on the editor widget
                return true;
            }
            else if(itemId == R.id.action_save) // If save pressed
            {
                saveFile(); // Call saveFile in the current class
                return true;
            }
            else if(itemId == R.id.action_save_as) // If save as pressed
            {
                saveFileAs(); // Call saveFileAs in the current class
                return true;
            }
            else if(itemId == R.id.action_load_from) // If load from pressed
            {
                loadFromFile(); // Call load from in the current class
                return true;
            }
            else if(itemId == R.id.action_copy) // If copy pressed
            {
                copyToClipboard(); // Call copy in the current class
                return true;
            }
            else if(itemId == R.id.action_paste) // If paste pressed
            {
                pasteFromClipboard(); // Call paste in the current class
                return true;
            }
            else
            {
                return false;
            }
        });

        // Fetch keyboard page buttons, resize them and add then to an array
        int keyboardButtonWidth = 150;
        pageNavButtons = new Button[keyboardNavLayout.getChildCount()];

        // Store all page navigation buttons in the keyboard panel
        // Set their widths to fixed constant and visually update
        for(int i = 0; i < keyboardNavLayout.getChildCount(); i++)
        {
            View child = keyboardNavLayout.getChildAt(i);
            pageNavButtons[i] = (Button) child;
            child.getLayoutParams().width = keyboardButtonWidth;
            child.requestLayout();
        }

        // Stop the editor receiving touch inputs when the empty keyboard space is pressed
        keyboardPanel.setOnTouchListener((v, event) -> true);

        // Set the line number view
        sourceCode.setLineNumberView(lineNumbers);

        // Disable the default keyboard from popping up, set input flags to disable text suggestions, auto capitalization
        sourceCode.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        sourceCode.setImeOptions(EditorInfo.IME_ACTION_NONE);
        sourceCode.allowSoftInput(false);
        sourceCode.setHorizontallyScrolling(true); // Allow horizontal scrolling of the editor

        // Find the keyboardButton
        Button keyboardButton = view.findViewById(R.id.keyboardButton);
        // Set onclick for the toggle keyboard mode button
        keyboardButton.setOnClickListener(v ->
        {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(mode == 0) // Show custom keyboard panel
            {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                keyboardPanel.setVisibility(View.VISIBLE);
                sourceCode.requestFocus();
                sourceCode.allowSoftInput(false);

                mode = 1;
            }
            else if(mode == 1) // Show built in keyboard
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
        // Move left one character on click
        navLeft.setOnClickListener(v ->
        {
            int start = Math.max(sourceCode.getSelectionStart() - 1, 0);
            sourceCode.setSelection(start);
        });

        // On long press, move all the way to the start of the current line
        navLeft.setOnLongClickListener(v ->
        {
            int pos = sourceCode.getSelectionStart();
            Layout layout = sourceCode.getLayout();

            if(layout != null)
            {
                // Get the current line and start position and update cursor
                int line = layout.getLineForOffset(pos);
                int start = layout.getLineStart(line);
                sourceCode.setSelection(start);
            }

            return true;
        });

        // Move right one character on click
        navRight.setOnClickListener(v ->
        {
            int end = Math.min(sourceCode.getSelectionStart() + 1, sourceCode.getText().length());
            sourceCode.setSelection(end);
        });

        // On long press, move all the way to the end of the current line
        navRight.setOnLongClickListener(v ->
        {
            // Get the current position of the cursor
            int pos = sourceCode.getSelectionStart();
            Layout layout = sourceCode.getLayout();

            if(layout != null)
            {
                // Get the current line the cursor is on
                int line = layout.getLineForOffset(pos);
                int end = layout.getLineEnd(line); // Get the end of the current line

                // If the current line is not the last line, meaning there's a linebreak at the end
                if(line < layout.getLineCount() - 1)
                {
                    end--; // Subtract 1 from the end position to not count the linebreak
                }

                sourceCode.setSelection(end); // Then set the cursor to the end of the line
            }

            return true;
        });

        // Move up a line
        navUp.setOnClickListener(v ->
        {
            // Get the current cursor position
            int pos = sourceCode.getSelectionStart();
            Layout layout = sourceCode.getLayout();

            if(layout != null)
            {
                // Get the line for the current cursor position
                int line = layout.getLineForOffset(pos);
                // Determine the offset of the cursor from the start of the line
                int offset = pos - layout.getLineStart(line);
                if(line > 0) // If this isnt the first line (cant move up to a line that isnt there)
                {
                    // Calculate the same cursor position in the line above using the offset
                    // If the previous line is shorter than the offset, move it to the end of the line
                    // Returning which is smaller, the offset, or the end of the previous line
                    int move = Math.min(layout.getLineStart(line - 1) + offset, layout.getLineEnd(line - 1));
                    sourceCode.setSelection(move); // Set the cursor to that position
                }
            }
        });

        navDown.setOnClickListener(v ->
        {
            // Get the current cursor position
            int pos = sourceCode.getSelectionStart();
            Layout layout = sourceCode.getLayout();

            if(layout != null)
            {
                // Get the line for the cursor position
                int line = layout.getLineForOffset(pos);
                // Determine the offset from the position to the start of the line
                int offset = pos - layout.getLineStart(line);
                if(line < layout.getLineCount() - 1) // If this isnt the last line (cant move down to a line that isnt there)
                {
                    // Calculate the same cursor position using the offset on the line below
                    // Return whichever is smaller, the offset or the end of the line
                    int move = Math.min(layout.getLineStart(line + 1) + offset, layout.getLineEnd(line + 1));
                    sourceCode.setSelection(move); // Set the cursor to that position
                }
            }
        });

        // Single tap delete
        delete.setOnClickListener(v ->
        {
            // Get the start and end of selection
            int start = sourceCode.getSelectionStart();
            int end = sourceCode.getSelectionEnd();

            // If there are multiple characters selected
            if(start != end)
            {
                // Delete all of the selected text
                sourceCode.getText().delete(start, end);
            }
            else if(start > 0) // Otherwise, if we arent at the first character in the editor
            {
                // Delete one character before the current cursor position
                sourceCode.getText().delete(start - 1, start);
            }
        });

        // Long press delete
        // Create a new handler to run multiple delete operations
        final Handler handler = new Handler();
        final int delay = 50; // Delay between deletes
        delete.setOnLongClickListener(v ->
        {
            // Post the runnable to run after the fixed delay
            handler.postDelayed(new Runnable()
            {
                public void run()
                {
                    // Get the current cursor position
                    int start = sourceCode.getSelectionStart();
                    if(start > 0) // If were not at the start, we can delete characters
                    {
                        // Delete the character before selection
                        sourceCode.getText().delete(start - 1, start);
                    }

                    // Keep posting the same runnable to constantly delete characters
                    handler.postDelayed(this, delay);
                }
            }, delay);
            return true;
        });

        // Detect if delete key is released
        delete.setOnTouchListener((v, event) ->
        {
            // If the user releases the delete button from a long click
            if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
            {
                // Remove any pending posted delete runnables to stop deleting characters
                handler.removeCallbacksAndMessages(null);
            }
            return false;
        });

        // Enter key, replace selected text with newline, or insert newline, matching indentation
        enter.setOnClickListener(v ->
        {
            int start = sourceCode.getSelectionStart();
            int end = sourceCode.getSelectionEnd();

            if(start != end) // If multiple characters are selected
            {
                // Replace all selected text with newline and match indentation
                sourceCode.getText().replace(start, end, "\n" + sourceCode.getIndentationLevel());
            }
            else
            {
                // Insert a newline and the indentation
                sourceCode.getText().insert(start, "\n" + sourceCode.getIndentationLevel());
            }
        });

        // Utility buttons, tab, space, copy, paste, comment =======================
        Button utilIndent = view.findViewById(R.id.util_tab);
        Button utilUnindent = view.findViewById(R.id.util_untab);
        Button utilSpace = view.findViewById(R.id.util_space);
        Button utilComment = view.findViewById(R.id.util_comment);
        Button utilString = view.findViewById(R.id.util_string);

        // Indent key, 4 spaces used for indentation
        // If multiple lines selected, add indentation level
        utilIndent.setOnClickListener(v ->
        {
            int startSelection = sourceCode.getSelectionStart();
            int endSelection = sourceCode.getSelectionEnd();

            String selectedText = sourceCode.getText().toString().substring(startSelection, endSelection);
            if(selectedText.contains("\n"))
            {
                // Add indentation to the first line, since it wont have a "\n" to replace
                selectedText = "    " + selectedText;

                // Replace all newlines with a newline followed by four spaces
                selectedText = selectedText.replaceAll("\n", "\n    ");

                // Replace the selected text in the edit text
                sourceCode.getText().replace(startSelection, endSelection, selectedText);
            }
            else
            {
                // Insert four spaces at the current position
                sourceCode.getText().insert(startSelection, "    ");
            }
        });

        utilUnindent.setOnClickListener(v ->
        {
            // Get the start and end of selection
            int startSelection = sourceCode.getSelectionStart();
            int endSelection = sourceCode.getSelectionEnd();

            String selectedText = sourceCode.getText().toString().substring(startSelection, endSelection);
            if(selectedText.contains("\n"))
            {
                // Add a newline to the start of the selected text so it can be unindented by the replaceAll
                selectedText = "\n" + selectedText;

                // Replace all newlines followed by one to four spaces with a newline
                selectedText = selectedText.replaceAll("\n {1,4}", "\n");

                // Remove the extra newline from the start of the selected text
                selectedText = selectedText.substring(1);

                // Replace the selected text in the edit text
                sourceCode.getText().replace(startSelection, endSelection, selectedText);
            }
            else
            {
                // If there are no newlines in the selected text, only one line is considered
                // Check if there are spaces before line content and delete up to four of them
                // Delete only up to 4 spaces per button press (one indentation level per press)
                String beforeCursor = sourceCode.getText().toString().substring(0, startSelection);
                int leadingSpaces = 0;

                // Iterate through the text before the current cursor position
                // Halt loop if 4 spaces are counter, or the start of the text before the cursor is reached
                for(int i = beforeCursor.length() - 1; i >= 0 && leadingSpaces < 4; i--)
                {
                    // If the character is a space, track this (up to 4)
                    if(beforeCursor.charAt(i) == ' ')
                    {
                        leadingSpaces++;
                    }
                    else // Ensure sequential spaces are counted, if interrupted by another character, stop counting spaces
                    {
                        break;
                    }
                }

                // If spaces have been identified, delete them
                if(leadingSpaces > 0)
                {
                    sourceCode.getText().delete(startSelection - leadingSpaces, startSelection);
                }
            }
        });

        // Insert a space at current position, or replace selected text with one
        utilSpace.setOnClickListener(v ->
        {
            // Find the start and end of selection
            int startSelection = sourceCode.getSelectionStart();
            int endSelection = sourceCode.getSelectionEnd();

            // If multiple characters are selected
            if(startSelection != endSelection)
            {
                // Replace them with one space
                sourceCode.getText().replace(startSelection, endSelection, " ");
            }
            else // Otherwise, just insert one space at the current position
            {
                sourceCode.getText().insert(startSelection, " ");
            }
        });

        // Quickly insert a comment, optional text to speech for speed
        utilComment.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_comment);

            if(dialog.getWindow() != null)
            {
                // Dim the background of the dialog
                // Set the soft input mode so the keyboard does not push up the dialog when opened
                dialog.getWindow().setDimAmount(0.6f);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // References to elements on the dialog
            EditText commentContent = dialog.findViewById(R.id.comment_content);
            Button commentContentVoice = dialog.findViewById(R.id.comment_content_voice);
            Button applyButton = dialog.findViewById(R.id.comment_apply);
            Button cancelButton = dialog.findViewById(R.id.comment_cancel);

            // Add text to speech functionality for the content of the comment
            commentContentVoice.setOnClickListener(v1 -> startVoiceInput(commentContent, null));

            // On applying, insert the comment content preceded by a hash
            applyButton.setOnClickListener(v1 ->
            {
                String text = "# " + commentContent.getText().toString();
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text); // Insert at current cursor position

                dialog.dismiss();
            });

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());
            dialog.show();
        });

        // Quickly insert a string, optional text to speech for speed
        utilString.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_string);

            if(dialog.getWindow() != null)
            {
                // Dim the background of the dialog
                // Set the soft input mode so the keyboard does not push up the dialog when opened
                dialog.getWindow().setDimAmount(0.6f);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // References to elements in the dialog
            EditText stringContent = dialog.findViewById(R.id.string_content);
            Button stringContentVoice = dialog.findViewById(R.id.string_content_voice);
            Button applyButton = dialog.findViewById(R.id.string_apply);
            Button cancelButton = dialog.findViewById(R.id.string_cancel);

            // Text to speech functionality for the content of the string
            stringContentVoice.setOnClickListener(v1 -> startVoiceInput(stringContent, null));

            // On apply surround the string content with quotes and insert
            applyButton.setOnClickListener(v1 ->
            {
                String text = "\"" + stringContent.getText().toString() + "\"";
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);  // Insert at current cursor position

                dialog.dismiss();
            });

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            dialog.show();
        });

        // Iterate through the quick symbol keys, add on clicks so they insert the text of the button
        LinearLayout symbolButtons = view.findViewById(R.id.symbolButtons);
        for(int i = 0; i < symbolButtons.getChildCount(); i++)
        {
            View child = symbolButtons.getChildAt(i);
            final Button button = (Button) child;

            button.setOnClickListener(v ->
            {
                // Get the current cursor position in the edit text
                int cursorPosition = sourceCode.getSelectionStart();

                // Get the button text
                String buttonText = button.getText().toString();

                // Insert the button text into the editor at the cursor position
                sourceCode.getText().insert(cursorPosition, buttonText);
            });
        }

        // Keyboard Page navigation buttons onClicks =================================================
        // Find all the buttons for the different pages on the custom keyboard
        Button importPageButton = view.findViewById(R.id.key_import);
        Button variablesPageButton = view.findViewById(R.id.key_var);
        Button functionsPageButton = view.findViewById(R.id.key_func);
        Button loopPageButton = view.findViewById(R.id.key_loop);
        Button selectionPageButton = view.findViewById(R.id.key_selection);
        Button operPageButton = view.findViewById(R.id.key_oper);
        Button OOPPageButton = view.findViewById(R.id.key_OOP);
        Button exceptionsPageButton = view.findViewById(R.id.key_except);

        // Find the view pager
        KeyboardViewPager viewPager = view.findViewById(R.id.keyboard_ViewPager);

        // Create a list of the pages, adding each layout for them
        List<Integer> pages = new ArrayList<>();
        pages.add(R.layout.page_import);
        pages.add(R.layout.page_variables);
        pages.add(R.layout.page_functions);
        pages.add(R.layout.page_loops);
        pages.add(R.layout.page_selection);
        pages.add(R.layout.page_operators);
        pages.add(R.layout.page_oop);
        pages.add(R.layout.page_except);

        // Create a PagerAdapter for the different keyboard pages
        PagerAdapter keyboardPagerAdapter = new PagerAdapter()
        {
            // Override to get the count of pages via the page list
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

            // Create the page for the given position, using the page list which contains the layouts
            // Also create the object associated with the page, which handles the code for that specific page
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position)
            {
                // Get the layout inflater from the given container
                LayoutInflater inflater = LayoutInflater.from(container.getContext());
                // Inflate the layout for the page corresponding to the position number
                // The layout is fetched from the array list using the position as the index
                View page = inflater.inflate(pages.get(position), container, false);
                container.addView(page); // Add the view to the container

                // Initialise classes that manage the code for each page, pass them required data
                switch(position)
                {
                    case 0: // Imports page
                        page_import = new imports_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 1: // Variables page
                        page_variables =  new variables_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 2: // Functions page
                        page_functions = new functions_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 3: // Loops page
                        page_loops = new loops_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 4: // Selection page
                        page_selection = new selection_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 5: // Operators page
                        page_operators = new operators_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 6: // Object Oriented Programming page
                        page_OOP = new OOP_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 7: // Exceptions page
                        page_except = new exceptions_page(page, SourcecodeTab.this, sourceCode);
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

        // Set the pager adapter to the view pager
        viewPager.setAdapter(keyboardPagerAdapter);

        // The keyboard opens on the import page first, have it highlighted by default
        highlightButton(importPageButton);

        // Keyboard panel navigation onclick ==================================================================================
        // For each of the page navigation buttons, set an onclick to update the viewpager position
        // Also highlight the button that corresponds to the current page open
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

        operPageButton.setOnClickListener(view13 ->
        {
            viewPager.setCurrentItem(5);
            resetButtonBackgrounds();
            highlightButton(operPageButton);
        });

        OOPPageButton.setOnClickListener(view13 ->
        {
            viewPager.setCurrentItem(6);
            resetButtonBackgrounds();
            highlightButton(OOPPageButton);
        });

        exceptionsPageButton.setOnClickListener(view12 ->
        {
            viewPager.setCurrentItem(7);
            resetButtonBackgrounds();
            highlightButton(exceptionsPageButton);
        });

        return view;
    }

    // Voice related methods =================================================================================================

    // Method called by other pages or within this class to start a voice input activity for the given edit text widget
    // A post process tag is also given, to determine what operations to perform on the resulting text from voice
    // Use a variable to track which is the current edit text targeted by voice input activities
    // Set a tag for the widget, to determine what post processing to do on the text
    public void startVoiceInput(EditText editText, String postProcessTag)
    {
        this.voiceEditText = editText;
        this.voiceEditText.setTag(postProcessTag);
        displaySpeechRecognizer(); // Start the speech recognizer activity
    }

    // Show the speech recognizer to the user
    private void displaySpeechRecognizer()
    {
        // Create a speech recognizer intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Configure the intent, setting the language model to free form, suited for dictation
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, speechCode); // Start an activity to handle the resulting text
        // Pass it the code defined for speech activities, so the method knows how to handle the data
    }

    // Activity results, for voice input and file saving
    // Called when the activity finishes, handles the result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // If the activity operation succeeded
        if(resultCode == RESULT_OK)
        {
            switch(requestCode) // Switch on the request code to determine how to handle the result
            {
                case speechCode: // If this was a voice recognition activity
                    // Get the recognized text from the speech activity
                    List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String spokenText = results.get(0);

                    // Check the edit text to insert the spoken text into exists
                    if(voiceEditText != null)
                    {
                        // If so,fetch the tag to determine how to apply operations on the spoken text
                        String tag = (String) voiceEditText.getTag();
                        // Process the text using the given tag
                        String processedText = postProcessVoiceData(spokenText, tag);

                        voiceEditText.setText(processedText); // Set the text of the edit text to the processed text
                    }

                    break;

                case saveCode: // If this was a saving activity
                    Uri save_uri = data.getData(); // Get the given uri to save to
                    try
                    {
                        // Open an output stream to the given uri
                        OutputStream output = getContext().getContentResolver().openOutputStream(save_uri);
                        // Write the code in the editor to the file
                        output.write(sourceCode.getText().toString().getBytes());
                        // Flush the output of any bytes to be written, and close it
                        output.flush();
                        output.close();

                        // Update the variables tracking the name and uri for the working file
                        currentFileName = getFileNameFromUri(save_uri);
                        currentFileUri = save_uri;
                        // Update the toolbar with the name of the current file
                        toolbar.setTitle(currentFileName);
                    }
                    catch(IOException e)
                    {
                        // Catch exceptions and notify the user
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }

                    break;

                case loadCode: // If this was a loading file activity
                    Uri load_uri = data.getData(); // Get the uri of the file to load
                    try
                    {
                        // Open an input stream from the given uri
                        InputStream input = getContext().getContentResolver().openInputStream(load_uri);
                        // Create a buffered reader, for the input stream, to read the contents of the file
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        // Create a string builder to append all the lines of text
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;

                        // While there are more lines to read
                        while((line = reader.readLine()) != null)
                        {
                            // Append the text of the line to the string builder followed by a newline
                            stringBuilder.append(line);
                            stringBuilder.append('\n');
                        }

                        // Close the buffered reader and input stream
                        reader.close();
                        input.close();

                        // Set the text of the editor, to the text from the read file
                        sourceCode.setText(stringBuilder.toString());

                        // Update the currently worked on file name and uri
                        currentFileName = getFileNameFromUri(load_uri);
                        currentFileUri = load_uri;

                        // Update the file name in the toolbar
                        toolbar.setTitle(currentFileName);

                        // If the variables, functions and oop pages have been instantiated
                        // Update the definitions tables for the new file content
                        if(page_variables != null && page_functions != null && page_OOP != null)
                        {
                            page_variables.updateVariablesTable();
                            page_functions.updateFunctionsTable();
                            page_OOP.updateClassesTable();

                            sourceCode.clearStacks(); // Clear the undo-redo stacks for the new file
                        }
                    }
                    catch(IOException e) // Catch exceptions and notify the user
                    {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        }

    }

    // Copy selected text to the android clipboard
    public void copyToClipboard()
    {
        // Get the start and end of selection
        int startSelection = sourceCode.getSelectionStart();
        int endSelection = sourceCode.getSelectionEnd();

        // Get the substring of selected text
        String selectedText = sourceCode.getText().toString().substring(startSelection, endSelection);
        // Get the clipboard service
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        // Create a clipboard data object, adding the selected text and a label to it
        ClipData clip = ClipData.newPlainText("Code Snippet", selectedText);
        clipboard.setPrimaryClip(clip); // Set the primary clipboard data to the one just created
    }

    // Paste text from the primary clipboard data
    public void pasteFromClipboard()
    {
        // Get the clipboard services
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        // Get the primary clipboard data
        ClipData clip = clipboard.getPrimaryClip();

        // If there is something to paste
        if(clip != null)
        {
            // Convert it to a string
            String pasteData = clip.getItemAt(0).getText().toString();
            // Get the start and end of selection
            int startSelection = sourceCode.getSelectionStart();
            int endSelection = sourceCode.getSelectionEnd();

            // If multiple characters are selected
            if(startSelection != endSelection)
            {
                // Replace them with the clipboard string
                sourceCode.getText().replace(startSelection, endSelection, pasteData);
            }
            else
            {
                // Otherwise just insert the clipboard string at the current position
                sourceCode.getText().insert(startSelection, pasteData);
            }
        }
    }

    // Method to apply specific changes to text, given by a voice recognition activity
    // Takes the text to perform on, and the tag of what set of operations to perform1
    // Used for applying naming conventions or replacing certain words e.g. spoken operators
    public String postProcessVoiceData(String text, String tag)
    {
        // Store the text to process
        String processedText = text;

        // Check if a tag is given, if not, do nothing to the text
        if(tag != null)
        {
            switch(tag)
            {
                case "snake_case": // Apply snake case, replacing all spaces with underscores
                    processedText = processedText.replace(" ", "_");

                    break;

                case "PascalCase": // Apply pascal case
                    // Split the text into an array of strings, split on spaces, removing them
                    String[] words = processedText.split(" ");

                    // Iterate through the words in the text
                    for(int i = 0; i < words.length; i++)
                    {
                        // Update the first letter of every word to its upper case version, and the rest to lower case
                        words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
                    }
                    // Join all the updated words back into a string
                    processedText = String.join("", words);

                    break;

                case "value": // Apply replacement of certain spoken values
                    // Generally used when an edit text may require inputting some code or operators e.g. conditions
                    // Replaces:
                    // Verbal operators with their symbol version
                    // true, false, none with True, False, None (python requires first letter to be capitalized)
                    processedText = processedText.replace("not equal to", "!=")
                            .replace("false", "False")
                            .replace("true", "True")
                            .replace("none", "None")
                            .replace("plus", "+")
                            .replace("minus", "-")
                            .replace("times", "*")
                            .replace("divided by", "/")
                            .replace("modulo", "%")
                            .replace("power", "**")
                            .replace("equals", "=")
                            .replace("greater than", ">")
                            .replace("less than", "<")
                            .replace("equal to", "==")
                            .replace("greater than or equal to", ">=")
                            .replace("less than or equal to", "<=");

                    break;
            }
        }

        return processedText; // Return the modified or unmodified(if tag was null) text
    }

    // File I/O related methods ==================================================================================================

    // Method to retrieve the file name as a string from a given file uri
    private String getFileNameFromUri(Uri uri)
    {
        String fileName = null;
        // Query the file uri given
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);

        // Check if the cursor is not null and move the cursor to the first row
        if(cursor != null && cursor.moveToFirst())
        {
            // Get the index of the display name column
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            fileName = cursor.getString(nameIndex); // Get the file name from the display name column
            cursor.close(); // Close the cursor
        }

        return fileName; // Return the filename
    }

    // Show the file explorer to allow a user to pick a file to load into the editor
    public void loadFromFile()
    {
        // Create a document picker intent
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // Set the category for the intent to open documents
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Use mime types to specify text or python files
        intent.setType("*/*");
        String[] mimetypes = {"text/*", "application/python"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

        startActivityForResult(intent, loadCode); // Start the activity for the result
    }

    // Show the file explore to allow a user to save a file to a directory and name it
    public void saveFileAs()
    {
        // Create a document creator intent
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        // Set the category to save an openable uri
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Set the file extension to .py
        intent.setType("text/x-python"); // Set the type of file to save
        // Originally set the chosen file name as the one in the toolbar, can be changed if needed, by default its "untitled.py"
        intent.putExtra(Intent.EXTRA_TITLE, currentFileName); // Set default filename
        startActivityForResult(intent, saveCode); // Start the activity for the result
    }

    // Run when the user hits save, if a uri is not already tracked then we need to save as
    public void saveFile()
    {
        // Check if a file uri exists
        // Meaning the user has either saved to a location before, or opened a file
        if(currentFileUri != null)
        {
            try
            {
                // If a uri already exists, this means we dont have to pick one
                // So open an output stream and overwrite the contents of the file with the text in the editor
                OutputStream output = getContext().getContentResolver().openOutputStream(currentFileUri);
                output.write(sourceCode.getText().toString().getBytes());
                output.flush();
                output.close();

                Toast.makeText(getContext(), "File saved successfully", Toast.LENGTH_SHORT).show();
            }
            catch(IOException e)
            {
                // Display an error message if something goes wrong
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        }
        else // Otherwise a uri hasnt be previously tracked
        {
            // So we need to pick a location to save to
            saveFileAs();
        }
    }

    // Button related methods =============================================================================================

    // Reset the button backgrounds of every page navigation button
    private void resetButtonBackgrounds()
    {
        // Iterate through the page navigation buttons
        for(Button button : pageNavButtons)
        {
            // Change the drawable for each button to the default page button drawable
            button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.keyboard_page_buttons, null));
        }
    }

    // Method to highlight a page navigation button
    private void highlightButton(Button button)
    {
        // Set the background of the button to the highlighted drawable version
        button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.keyboard_page_button_highlighted, null));
    }

    // Method to set onclick listener for quick input keys in tables
    // Essentially for every button in the table, it will add an onclick listener so that:
    // The button will insert text into the editor, equivalent to the text of the button
    public void setOnclickForTableButtons(TableLayout table, String additionalText, String prefixText)
    {
        // Iterate through the given table, through each row
        for(int i = 0; i < table.getChildCount(); i++)
        {
            // Get the current row
            View row = table.getChildAt(i);
            TableRow tableRow = (TableRow) row;

            // Iterate through all the buttons in the table row
            for(int j = 0; j < tableRow.getChildCount(); j++)
            {
                View child = tableRow.getChildAt(j);
                Button button = (Button) child;
                // Adding an onclick listener to them
                button.setOnClickListener(v ->
                {
                    // Get the text from the Button
                    String text = button.getText().toString();

                    // Append any additional text given
                    if(additionalText != null)
                    {
                        text = text + additionalText;
                    }

                    // Add any prefix text to before the button content
                    if(prefixText != null)
                    {
                        text = prefixText + text;
                    }

                    // Insert the text at the current cursor position
                    int start = sourceCode.getSelectionStart();
                    sourceCode.getText().insert(start, text);
                });
            }
        }
    }
}