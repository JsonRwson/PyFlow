package com.PyFlow.keyboard_pages;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.PyFlow.CustomEditText;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class functions_page extends Page
{
    private HashMap<String, Integer> functionDefinitions;
    private SourcecodeTab activity;
    private FragmentActivity fragmentActivity;

    private TableLayout functionsDefTable;
    private TableLayout functionCallsTable;

    private CustomEditText sourceCode;
    private View page;
    private TextView selectedTextView;

    private Button newFunctionButton;
    private Button refreshDefinitionsButton;
    private Button collapseDefinitionsButton;
    private Button collapseKeysButton;
    private Button gotoFuncButton;
    private Button callFuncButton;
    private Button pasteFuncButton;

    private List<EditText> paramTextList;
    private List<View> paramViewList;
    private LinearLayout paramInputContainer;

    private List<EditText> argumentTextList;
    private List<View> argumentViewList;
    private LinearLayout argumentInputContainer;

    private String selectedFunction;
    private int originalSoftInputMode;
    private static final int SPEECH_REQUEST_CODE = 0;

    public functions_page(View view, SourcecodeTab activity, CustomEditText source)
    {
        this.sourceCode = source;
        this.page = view;

        this.activity = activity;
        this.fragmentActivity = activity.getActivity();
        this.originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        this.functionsDefTable = page.findViewById(R.id.func_def_table);
        this.functionCallsTable = page.findViewById(R.id.func_keys_table);

        this.newFunctionButton = page.findViewById(R.id.new_func);
        this.refreshDefinitionsButton = page.findViewById(R.id.func_def_refresh);
        this.collapseDefinitionsButton = page.findViewById(R.id.func_def_collapse);
        this.collapseKeysButton = page.findViewById(R.id.func_keys_collapse);
        this.gotoFuncButton = page.findViewById(R.id.func_goto);
        this.callFuncButton = page.findViewById(R.id.func_call);
        this.pasteFuncButton = page.findViewById(R.id.func_paste);

        for (int i = 0; i < functionCallsTable.getChildCount(); i++)
        {
            View row = functionCallsTable.getChildAt(i);
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
                            // Insert the text at the current cursor position
                            int start = sourceCode.getSelectionStart();
                            sourceCode.getText().insert(start, text + "()");

                            sourceCode.setSelection(start + text.length() + 2 - 1);
                        });
                    }
                }
            }
        }

        // Use a one element array because lambda variables should be final
        // Toggle collapse the table for definitions
        final boolean[] isDefTableVisible = {true};
        collapseDefinitionsButton.setOnClickListener(v ->
        {
            if (isDefTableVisible[0])
            {
                // Hide the table layout
                functionsDefTable.setVisibility(View.GONE);
                // Switch to an up chevron icon
                collapseDefinitionsButton.setBackgroundResource(R.drawable.up_chevron_icon);
            }
            else
            {
                // Show the table layout
                functionsDefTable.setVisibility(View.VISIBLE);
                // Switch to a down chevron icon
                collapseDefinitionsButton.setBackgroundResource(R.drawable.down_chevron_icon);
            }
            // Toggle the state
            isDefTableVisible[0] = !isDefTableVisible[0];
        });

        final boolean[] isKeyTableVisible = {true};
        collapseKeysButton.setOnClickListener(v ->
        {
            if (isKeyTableVisible[0])
            {
                // Hide the table layout
                functionCallsTable.setVisibility(View.GONE);
                // Switch to an up chevron icon
                collapseKeysButton.setBackgroundResource(R.drawable.up_chevron_icon);
            }
            else
            {
                // Show the table layout
                functionCallsTable.setVisibility(View.VISIBLE);
                // Switch to a down chevron icon
                collapseKeysButton.setBackgroundResource(R.drawable.down_chevron_icon);
            }
            // Toggle the state
            isKeyTableVisible[0] = !isKeyTableVisible[0];
        });

        // Refresh definitions of functions
        // Fetch all first instances of function definitions, add them to the hash table with their position
        // Then add them as elements to be viewed in the table layout
        refreshDefinitionsButton.setOnClickListener(v ->
        {
            updateFunctionsTable();
        });

        newFunctionButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_newfunc);

            if(dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            EditText funcName = dialog.findViewById(R.id.func_name);

            Button applyButton = dialog.findViewById(R.id.func_apply);
            Button cancelButton = dialog.findViewById(R.id.func_cancel);
            Button functionNameVoice = dialog.findViewById(R.id.func_name_voice);

            Button addParameterButton = dialog.findViewById(R.id.add_param);
            Button remParameterButton = dialog.findViewById(R.id.remove_param);

            // Initialise array for input text boxes and fetch layout reference
            paramTextList = new ArrayList<>();
            paramViewList = new ArrayList<>();
            paramInputContainer = dialog.findViewById(R.id.parametersContainer);

            functionNameVoice.setOnClickListener(v1 ->
            {
                displaySpeechRecognizer(funcName);
            });

            addParameterButton.setOnClickListener(v1 ->
            {
                // Inflate the input field layout
                View inputFieldView = dialog.getLayoutInflater().inflate(R.layout.input_field, null);

                // Get the EditText and add it to the list
                EditText newInput = inputFieldView.findViewById(R.id.input_field);
                paramTextList.add(newInput);

                // Set the position number
                TextView inputNumber = inputFieldView.findViewById(R.id.input_number);
                inputNumber.setText(String.valueOf(paramTextList.size()));

                Button voiceButton = inputFieldView.findViewById(R.id.voice_button);
                voiceButton.setTag(paramTextList.size() - 1);  // Set the tag

                voiceButton.setOnClickListener(v2 ->
                {
                    displaySpeechRecognizer(newInput);
                });

                // Add the layout to the LinearLayout and the list
                paramInputContainer.addView(inputFieldView);
                paramViewList.add(inputFieldView);

            });

            remParameterButton.setOnClickListener(v1 ->
            {
                // Remove the last view from the LinearLayout and the list
                if (!paramViewList.isEmpty())
                {
                    View lastView = paramViewList.get(paramViewList.size() - 1);
                    paramInputContainer.removeView(lastView);
                    paramViewList.remove(lastView);

                    // Also remove the EditText from the list
                    if (!paramTextList.isEmpty())
                    {
                        paramTextList.remove(paramTextList.size() - 1);
                    }
                }
            });

            applyButton.setOnClickListener(v1 ->
            {
                // Start the function definition
                String text = "def " + funcName.getText().toString() + "(";

                // Iterate over the views and add each parameter to the function definition
                for (int i = 0; i < paramTextList.size(); i++)
                {
                    // Get the parameter name
                    String paramName = paramTextList.get(i).getText().toString();

                    // Add the parameter to the function definition
                    text += paramName;

                    // If this is not the last parameter, add a comma and a space
                    if (i < paramTextList.size() - 1)
                    {
                        text += ", ";
                    }
                }

                // Close the parenthesis
                text += "):\n";

                // Insert the func definition at the cursor position
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                // Dismiss the dialog
                dialog.dismiss();
            });

            cancelButton.setOnClickListener(v1 ->
            {
                dialog.dismiss();
            });

            dialog.setOnDismissListener(v2 ->
            {
                updateFunctionsTable();
                fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
            });

            dialog.show();
        });

        gotoFuncButton.setOnClickListener(v1 ->
        {
            if (selectedTextView != null)
            {
                selectedFunction = selectedTextView.getText().toString();
                if (functionDefinitions.containsKey(selectedFunction))
                {
                    int var_pos = functionDefinitions.get(selectedFunction);
                    sourceCode.setSelection(var_pos);
                }
            }
        });

        pasteFuncButton.setOnClickListener(v ->
        {
            if (selectedTextView != null)
            {
                selectedFunction = selectedTextView.getText().toString();
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, selectedFunction);
            }
        });

        callFuncButton.setOnClickListener(v ->
        {
            if (selectedTextView != null)
            {
                selectedFunction = selectedTextView.getText().toString();

                // Create a new dialog
                Dialog dialog = new Dialog(activity.getContext());
                // Set the custom layout for the dialog
                dialog.setContentView(R.layout.dialog_callfunc);

                if(dialog.getWindow() != null)
                {
                    dialog.getWindow().setDimAmount(0.6f);
                    fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }

                // Find the views
                TextView funcName = dialog.findViewById(R.id.func_call_name);
                Button applyButton = dialog.findViewById(R.id.func_call_apply);
                Button cancelButton = dialog.findViewById(R.id.func_call_cancel);
                Button addArgumentButton = dialog.findViewById(R.id.add_param);
                Button remArgumentButton = dialog.findViewById(R.id.remove_param);

                // Initialise array for input text boxes and fetch layout reference
                argumentTextList = new ArrayList<>();
                argumentViewList = new ArrayList<>();
                argumentInputContainer = dialog.findViewById(R.id.argumentsContainer);

                funcName.setText("Call: " + selectedFunction);

                addArgumentButton.setOnClickListener(v1 ->
                {
                    // Inflate the input field layout
                    View inputFieldView = dialog.getLayoutInflater().inflate(R.layout.input_field, null);

                    // Get the EditText and add it to the list
                    EditText newArgument = inputFieldView.findViewById(R.id.input_field);
                    argumentTextList.add(newArgument);

                    // Set the position number
                    TextView inputNumber = inputFieldView.findViewById(R.id.input_number);
                    inputNumber.setText(String.valueOf(argumentViewList.size()));

                    Button voiceButton = inputFieldView.findViewById(R.id.voice_button);
                    voiceButton.setTag(argumentTextList.size() - 1);  // Set the tag

                    voiceButton.setOnClickListener(v2 ->
                    {
                        displaySpeechRecognizer(newArgument);
                    });

                    // Add the layout to the LinearLayout and the list
                    argumentInputContainer.addView(inputFieldView);
                    argumentViewList.add(inputFieldView);
                });

                remArgumentButton.setOnClickListener(v1 ->
                {
                    // Remove the last view from the LinearLayout and the list
                    if (!argumentTextList.isEmpty())
                    {
                        View lastView = argumentViewList.get(argumentViewList.size() - 1);
                        argumentInputContainer.removeView(lastView);
                        argumentViewList.remove(lastView);

                        // Also remove the EditText from the list
                        if (!argumentTextList.isEmpty())
                        {
                            argumentTextList.remove(argumentTextList.size() - 1);
                        }
                    }
                });

                applyButton.setOnClickListener(v1 ->
                {
                    // Get the text from the EditText
                    String text = selectedFunction + "(";

                    // Iterate over the views and add each parameter to the function definition
                    for (int i = 0; i < argumentTextList.size(); i++)
                    {
                        // Get the parameter name
                        String paramName = argumentTextList.get(i).getText().toString();

                        // Add the parameter to the function definition
                        text += paramName;

                        // If this is not the last parameter, add a comma and a space
                        if (i < argumentTextList.size() - 1)
                        {
                            text += ", ";
                        }
                    }

                    // Close the parenthesis
                    text += ")";

                    // Insert the text at the current cursor position
                    int start = sourceCode.getSelectionStart();
                    sourceCode.getText().insert(start, text);

                    // Move the cursor back inside the parenthesis
                    sourceCode.setSelection(start + text.length() - 1);

                    // Dismiss the dialog
                    dialog.dismiss();
                });

                cancelButton.setOnClickListener(v1 ->
                {
                    dialog.dismiss();
                });

                dialog.setOnDismissListener(v1 ->
                {
                    updateFunctionsTable();
                    fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
                });

                dialog.show();
            }
        });
    }

    private void updateFunctionsTable()
    {
        functionDefinitions = updateFunctionsMap();

        // Reset selected values
        selectedFunction = null;
        selectedTextView = null;

        // Clear the table
        functionsDefTable.removeAllViews();

        // Create a new LinearLayout for every three functions
        LinearLayout linearLayout = null;

        int i = 0;
        for (String functionName : functionDefinitions.keySet())
        {
            if (i % 3 == 0)
            {
                linearLayout = new LinearLayout(activity.getActivity());
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                functionsDefTable.addView(linearLayout);
            }

            // Create a new TextView for the function
            TextView textView = new TextView(activity.getActivity());
            textView.setText(functionName);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(0, 10, 0, 10);
            textView.setMaxLines(1);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setBackground(ContextCompat.getDrawable(activity.requireActivity(), R.drawable.table_background));

            // Set layout parameters for equal spacing
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            textView.setLayoutParams(params);

            // Add an OnClickListener to the TextView
            textView.setClickable(true);
            textView.setOnClickListener(v ->
            {
                // Deselect the previously selected TextView
                if (selectedTextView != null)
                {
                    selectedTextView.setBackground(ContextCompat.getDrawable(activity.requireActivity(), R.drawable.table_background));;
                }

                // Select the clicked TextView
                selectedTextView = (TextView) v;
                selectedTextView.setBackgroundColor(Color.LTGRAY);
            });

            linearLayout.addView(textView);

            i++;
        }
    }

    private HashMap<String, Integer> updateFunctionsMap()
    {
        HashMap<String, Integer> functionDefinitions = new HashMap<>();
        String text = sourceCode.getText().toString();
        String[] lines = text.split("\\n");

        // Pattern to find functions, match def, whitespace, capture the name, parenthesis with anything in it, whitespace, then a colon
        // Capture group is the name of the function
        Pattern pattern = Pattern.compile("def\\s+(\\w+)\\s*\\(.*\\)\\s*:");

        int index = 0;
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i].trim();
            if (line.startsWith("#")) // ignore comments
            {
                index += line.length() + 1;
                continue;
            }

            Matcher matcher = pattern.matcher(line);

            if (matcher.find())
            {
                String functionName = matcher.group(1);

                if (!functionDefinitions.containsKey(functionName))
                {
                    functionDefinitions.put(functionName, index + line.indexOf(functionName));
                }
            }

            index += line.length() + 1;
        }

        return functionDefinitions;
    }

    // start the speech recognizer
    private void displaySpeechRecognizer(EditText editText)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        this.voiceEditText = editText;
        activity.startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }
}
