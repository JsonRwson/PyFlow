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
import android.provider.OpenableColumns;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
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

public class SourcecodeTab extends Fragment
{
    private int mode = 0; // 3 modes, custom keyboard, default keyboard, no keyboards
    private String currentFileName = "untitled";
    private Uri currentFileUri = null;

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
        toolbar.setOnMenuItemClickListener(item ->
        {
            int itemId = item.getItemId();
            if (itemId == R.id.action_undo)
            {
                sourceCode.undo();
                return true;
            }
            else if (itemId == R.id.action_redo)
            {
                sourceCode.redo();
                return true;
            }
            else if (itemId == R.id.action_save)
            {
                saveFile();
                return true;
            }
            else if (itemId == R.id.action_save_as)
            {
                saveFileAs();
                return true;
            }
            else if (itemId == R.id.action_load_from)
            {
                loadFromFile();
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
        sourceCode.setHorizontallyScrolling(true);

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
                // Some text is selected
                sourceCode.getText().delete(start, end);
            }
            else if (start > 0)
            {
                // No text is selected, so delete last char
                sourceCode.getText().delete(start - 1, start);
            }
        });

        enter.setOnClickListener(v ->
        {
            int start = sourceCode.getSelectionStart();
            int end = sourceCode.getSelectionEnd();

            if (start != end)
            {
                // Some text is selected replace with newline
                sourceCode.getText().replace(start, end, "\n");
            }
            else
            {
                // No text is selected insert newline at current position
                int line = sourceCode.getLayout().getLineForOffset(start);
                int lineStartPos = sourceCode.getLayout().getLineStart(line);
                int lineEndPos = sourceCode.getLayout().getLineEnd(line);

                String lineContent = sourceCode.getText().toString().substring(lineStartPos, lineEndPos);

                // Match the indentation of the line
                Matcher indentationPattern = Pattern.compile("^[ \\t]+").matcher(lineContent);
                String indentation = "";
                if (indentationPattern.find())
                {
                    indentation = indentationPattern.group();

                }

                // Insert a newline and the indentation
                sourceCode.getText().insert(start, "\n" + indentation);
            }
        });

        // Utility buttons, tab, space, copy, paste, comment =======================
        Button utilIndent = view.findViewById(R.id.util_tab);
        Button utilUnindent = view.findViewById(R.id.util_untab);
        Button utilSpace = view.findViewById(R.id.util_space);
        Button utilCopy = view.findViewById(R.id.util_copy);
        Button utilPaste = view.findViewById(R.id.util_paste);
        Button utilComment = view.findViewById(R.id.util_comment);
        Button utilString = view.findViewById(R.id.util_string);


        utilIndent.setOnClickListener(v ->
        {
            int startSelection = sourceCode.getSelectionStart();
            int endSelection = sourceCode.getSelectionEnd();

            String selectedText = sourceCode.getText().toString().substring(startSelection, endSelection);
            if (selectedText.contains("\n"))
            {
                // Add a newline to the start of the selected text
                selectedText = "\n" + selectedText;

                // Replace all newlines with a newline followed by four spaces
                selectedText = selectedText.replaceAll("\n", "\n    ");

                // Remove the extra newline from the start of the selected text
                selectedText = selectedText.substring(1);

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
            int startSelection = sourceCode.getSelectionStart();
            int endSelection = sourceCode.getSelectionEnd();

            String selectedText = sourceCode.getText().toString().substring(startSelection, endSelection);
            if (selectedText.contains("\n"))
            {
                // Add a newline to the start of the selected text
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
                // Check if there are spaces before and delete up to four of them
                int spacesToDelete = Math.min(startSelection, 4);
                String beforeCursor = sourceCode.getText().toString().substring(startSelection - spacesToDelete, startSelection);
                int leadingSpaces = beforeCursor.length() - beforeCursor.replace(" ", "").length();

                if (leadingSpaces > 0)
                {
                    sourceCode.getText().delete(startSelection - leadingSpaces, startSelection);
                }
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
            // Create a new dialog
            Dialog dialog = new Dialog(getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_comment);

            if(dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            EditText commentContent = dialog.findViewById(R.id.comment_content);
            Button stringContentVoice = dialog.findViewById(R.id.comment_content_voice);
            Button applyButton = dialog.findViewById(R.id.comment_apply);
            Button cancelButton = dialog.findViewById(R.id.comment_cancel);

            stringContentVoice.setOnClickListener(v1 ->
                    startVoiceInput(commentContent, null));

            applyButton.setOnClickListener(v1 ->
            {
                String text = "# " + commentContent.getText().toString();
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                dialog.dismiss();
            });

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            dialog.show();
        });

        utilString.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_string);

            if(dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            EditText stringContent = dialog.findViewById(R.id.string_content);
            Button stringContentVoice = dialog.findViewById(R.id.string_content_voice);
            Button applyButton = dialog.findViewById(R.id.string_apply);
            Button cancelButton = dialog.findViewById(R.id.string_cancel);

            stringContentVoice.setOnClickListener(v1 -> startVoiceInput(stringContent, null));

            applyButton.setOnClickListener(v1 ->
            {
                String text = "\"" + stringContent.getText().toString() + "\"";
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                dialog.dismiss();
            });

            cancelButton.setOnClickListener(v1 ->
                    dialog.dismiss());

            dialog.show();
        });

        // Insert Symbol buttons onClicks ======================================================
        LinearLayout symbolButtons = view.findViewById(R.id.symbolButtons);

        for (int i = 0; i < symbolButtons.getChildCount(); i++)
        {
            View child = symbolButtons.getChildAt(i);

            // Check if the child is a Button
            if (child instanceof Button)
            {
                final Button button = (Button) child;

                // Set an OnClickListener for the button
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
        }


        // Keyboard Page navigation buttons onClicks =================================================
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

        // Create a list of pages
        List<Integer> pages = new ArrayList<>();
        pages.add(R.layout.page_import);
        pages.add(R.layout.page_variables);
        pages.add(R.layout.page_functions);
        pages.add(R.layout.page_loops);
        pages.add(R.layout.page_selection);
        pages.add(R.layout.page_operators);
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

                // Initialise classes that manage the code for each page, pass them required data
                switch (position)
                {
                    case 0:
                        page_import = new imports_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 1:
                        page_variables =  new variables_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 2:
                        page_functions = new functions_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 3:
                        page_loops = new loops_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 4:
                        page_selection = new selection_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 5:
                        page_operators = new operators_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 6:
                        page_OOP = new OOP_page(page, SourcecodeTab.this, sourceCode);
                        break;
                    case 7:
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

        // Set the pager adapter  to the view pager
        viewPager.setAdapter(keyboardPagerAdapter);

        // The keyboard opens on the import page first
        highlightButton(importPageButton);

        // Keyboard panel navigation onclick ==================================================================================
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
    public void startVoiceInput(EditText editText, String postProcessTag)
    {
        this.voiceEditText = editText;
        this.voiceEditText.setTag(postProcessTag);
        displaySpeechRecognizer();
    }

    private void displaySpeechRecognizer()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, speechCode);
    }

    // Activity results, for voice input and file saving
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            switch(requestCode)
            {
                case speechCode:
                    List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String spokenText = results.get(0);

                    if(voiceEditText != null)
                    {
                        String tag = (String) voiceEditText.getTag();
                        String processedText = postProcessVoiceData(spokenText, tag);

                        voiceEditText.setText(processedText);
                    }

                    break;

                case saveCode:
                    Uri save_uri = data.getData();
                    try
                    {
                        OutputStream output = getContext().getContentResolver().openOutputStream(save_uri);
                        output.write(sourceCode.getText().toString().getBytes());
                        output.flush();
                        output.close();

                        currentFileName = getFileNameFromUri(save_uri);
                        currentFileUri = save_uri;
                        toolbar.setTitle(currentFileName);
                    }
                    catch(IOException e)
                    {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }

                    break;

                case loadCode:
                    Uri load_uri = data.getData();
                    try
                    {
                        InputStream input = getContext().getContentResolver().openInputStream(load_uri);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;

                        while((line = reader.readLine()) != null)
                        {
                            stringBuilder.append(line);
                            stringBuilder.append('\n');
                        }

                        reader.close();
                        input.close();

                        sourceCode.setText(stringBuilder.toString());

                        currentFileName = getFileNameFromUri(load_uri);
                        currentFileUri = load_uri;
                        toolbar.setTitle(currentFileName);

                        if(page_variables != null && page_functions != null && page_OOP != null)
                        {
                            page_variables.updateVariablesTable();
                            page_functions.updateFunctionsTable();
                            page_OOP.updateClassesTable();
                            sourceCode.clearStacks();
                        }
                    }
                    catch(IOException e)
                    {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        }

    }

    public String postProcessVoiceData(String text, String tag)
    {
        String processedText = text;

        if(tag != null)
        {
            switch(tag)
            {
                case "snake_case":
                    processedText = processedText.replace(" ", "_");

                    break;

                case "PascalCase":
                    String[] words = processedText.split(" ");

                    for (int i = 0; i < words.length; i++)
                    {
                        words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
                    }
                    processedText = String.join("", words);

                    break;

                case "value":
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

        return processedText;
    }

    // File I/O related methods ==================================================================================================
    private String getFileNameFromUri(Uri uri)
    {
        String fileName = null;
        Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst())
        {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            fileName = cursor.getString(nameIndex);
        }

        return fileName;
    }
    
    public void loadFromFile()
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimetypes = {"text/*", "application/python"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

        startActivityForResult(intent, loadCode);
    }

    public void saveFileAs()
    {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/x-python"); // Set the type of file to save
        intent.putExtra(Intent.EXTRA_TITLE, currentFileName); // Set default filename
        startActivityForResult(intent, saveCode); // Start the intent with request code
    }

    public void saveFile()
    {
        if(currentFileUri != null)
        {
            try
            {
                // Open the file using the currentFileUri
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
        else
        {
            saveFileAs();
        }
    }

    // Button related methods =============================================================================================
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

    public void setOnclickForTableButtons(TableLayout table, String additionalText, String prefixText)
    {
        for (int i = 0; i < table.getChildCount(); i++)
        {
            View row = table.getChildAt(i);
            if (row instanceof TableRow)
            {
                TableRow tableRow = (TableRow) row;
                for (int j = 0; j < tableRow.getChildCount(); j++)
                {
                    View child = tableRow.getChildAt(j);
                    if (child instanceof Button)
                    {
                        Button button = (Button) child;
                        button.setOnClickListener(v ->
                        {
                            // Get the text from the Button
                            String text = button.getText().toString();

                            if(additionalText != null)
                            {
                                text = text + additionalText;
                            }

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
    }
}