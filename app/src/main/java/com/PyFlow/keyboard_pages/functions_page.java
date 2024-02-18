package com.PyFlow.keyboard_pages;

import android.app.Dialog;
import android.graphics.Color;
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

import com.PyFlow.SourcecodeEditor;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Functions Page ===========================================================
// Class for the functions page of the custom keyboard
// Allows the user to insert a new function and set parameters
// The page tracks defined functions and allows users to call, goto or paste them
// The page has a set of buttons to quickly call common functions
// The page has a set of quick input buttons for function related purposes

public class functions_page
{
    // Hashmap to store user defined functions and their location in the editor
    private HashMap<String, Integer> functionDefinitions;
    private final SourcecodeTab activity;
    private final FragmentActivity fragmentActivity;

    // Layouts for the tables that contain the quick input buttons and tracked function
    private final TableLayout functionsDefTable;
    private final TableLayout functionCallsTable;
    private final TableLayout functionKeysTable;

    // Reference for the editor widget and the currently selected function definition widget
    private final SourcecodeEditor sourceCode;
    private TextView selectedTextView;

    // Structures and view that relate to parameters in the new function dialog
    private List<EditText> paramTextList;
    private List<View> paramViewList;
    private LinearLayout paramInputContainer;

    // Structures and view that relate to arguments in the call function dialog
    private List<EditText> argumentTextList;
    private List<View> argumentViewList;
    private LinearLayout argumentInputContainer;

    // String to store the name of the currently selected function definition
    // Store the original soft input mode, to revert back to after a dialog is closed
    private String selectedFunction;
    private final int originalSoftInputMode;

    public functions_page(View view, SourcecodeTab activity, SourcecodeEditor source)
    {
        // References to the sourcecode editor widget and the activity for the fragment
        sourceCode = source;
        this.activity = activity;
        fragmentActivity = activity.getActivity();

        // Store the soft input mode, original mode, opening the soft keyboard pushes up widgets on screen
        originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        // References to the tables to store quick input buttons and user functions
        functionsDefTable = view.findViewById(R.id.func_def_table);
        functionCallsTable = view.findViewById(R.id.func_calls_table);
        functionKeysTable = view.findViewById(R.id.func_keys_table);

        // References to the non-table page buttons
        Button newFunctionButton = view.findViewById(R.id.new_func);
        Button callNewFunctionButton = view.findViewById(R.id.call_new_func);
        Button refreshDefinitionsButton = view.findViewById(R.id.func_def_refresh);
        Button collapseDefinitionsButton = view.findViewById(R.id.func_def_collapse);

        // Buttons to collapse the table buttons
        Button collapseCallsButton = view.findViewById(R.id.func_calls_collapse);
        Button collapseKeysButton = view.findViewById(R.id.func_keys_collapse);

        // Buttons related to the currently selected user defined function
        Button gotoFuncButton = view.findViewById(R.id.func_goto);
        Button callFuncButton = view.findViewById(R.id.func_call);
        Button pasteFuncButton = view.findViewById(R.id.func_paste);

        // For all the buttons in the common function calls table
        // Set an on click so that the function is called and the cursor is placed between the parenthesis
        // Iterate through all the rows of the table
        for(int i = 0; i < functionCallsTable.getChildCount(); i++)
        {
            // Iterate through all the buttons in the table row
            TableRow tableRow = (TableRow) functionCallsTable.getChildAt(i);
            for(int j = 0; j < tableRow.getChildCount(); j++)
            {
                View child = tableRow.getChildAt(j);
                Button button = (Button) child;

                // Set an onclick so that it inserts the function
                // "(text of the button)()" <-- then move cursor inside parenthesis
                button.setOnClickListener(v ->
                {
                    // Get the text from the button
                    String text = button.getText().toString();

                    // Insert the text at the current cursor position
                    int start = sourceCode.getSelectionStart();
                    sourceCode.getText().insert(start, text + "()");

                    // Move the cursor back to sit inside the parenthesis
                    sourceCode.setSelection(start + text.length() + 1);
                });
            }
        }

        // Set onclick for quick input keys so they insert the button text into the editor
        activity.setOnclickForTableButtons(functionKeysTable, null, null);

        // Use a one element array because lambda variables should be final
        // Toggle collapse the table for definitions
        final boolean[] isDefTableVisible = {true};
        collapseDefinitionsButton.setOnClickListener(v ->
        {
            if(isDefTableVisible[0])
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


        // Use a one element array because lambda variables should be final
        // Toggle collapse the table for common function calls
        final boolean[] isCallsTableVisible = {true};
        collapseCallsButton.setOnClickListener(v ->
        {
            if(isCallsTableVisible[0])
            {
                // Hide the table layout
                functionCallsTable.setVisibility(View.GONE);
                // Switch to an up chevron icon
                collapseCallsButton.setBackgroundResource(R.drawable.up_chevron_icon);
            }
            else
            {
                // Show the table layout
                functionCallsTable.setVisibility(View.VISIBLE);
                // Switch to a down chevron icon
                collapseCallsButton.setBackgroundResource(R.drawable.down_chevron_icon);
            }
            // Toggle the state
            isCallsTableVisible[0] = !isCallsTableVisible[0];
        });

        // Use a one element array because lambda variables should be final
        // Toggle collapse the table for quick input keys
        final boolean[] isKeyTableVisible = {true};
        collapseKeysButton.setOnClickListener(v ->
        {
            if(isKeyTableVisible[0])
            {
                // Hide the table layout
                functionKeysTable.setVisibility(View.GONE);
                // Switch to an up chevron icon
                collapseKeysButton.setBackgroundResource(R.drawable.up_chevron_icon);
            }
            else
            {
                // Show the table layout
                functionKeysTable.setVisibility(View.VISIBLE);
                // Switch to a down chevron icon
                collapseKeysButton.setBackgroundResource(R.drawable.down_chevron_icon);
            }
            // Toggle the state
            isKeyTableVisible[0] = !isKeyTableVisible[0];
        });

        // Refresh definitions of functions
        // Fetch all first instances of function definitions, add them to the hash table with their position
        // Then add them as elements to be viewed in the table layout
        refreshDefinitionsButton.setOnClickListener(v -> updateFunctionsTable());

        // Button to open a dialog to insert a new function
        // Allows user to set parameters for their function
        newFunctionButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_newfunc);

            // Dim the background of the dialog
            // Set the soft input mode so the keyboard does not push up the dialog when opened
            if(dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // References to widgets on the dialog
            EditText funcName = dialog.findViewById(R.id.func_name);

            Button applyButton = dialog.findViewById(R.id.func_apply);
            Button cancelButton = dialog.findViewById(R.id.func_cancel);
            Button functionNameVoice = dialog.findViewById(R.id.func_name_voice);

            // Buttons to add or remove parameters
            // Parameters are added as input boxes to a scrollable layout
            Button addParameterButton = dialog.findViewById(R.id.add_param);
            Button remParameterButton = dialog.findViewById(R.id.remove_param);

            // Initialise array for input text boxes and fetch layout reference
            paramTextList = new ArrayList<>();
            paramViewList = new ArrayList<>();
            paramInputContainer = dialog.findViewById(R.id.parametersContainer);

            // Call method to add functionality to voice button
            // Post process voice data so that function names automatically follow pep 8 naming conventions, snake case
            functionNameVoice.setOnClickListener(v1 -> activity.startVoiceInput(funcName, "snake_case"));

            // Add a parameter input box to the dialog
            addParameterButton.setOnClickListener(v1 ->
            {
                // Inflate the input field layout
                View inputFieldView = dialog.getLayoutInflater().inflate(R.layout.input_field, null);

                // Get the edit text and add it to the list of parameter input boxes
                EditText newInput = inputFieldView.findViewById(R.id.input_field);
                paramTextList.add(newInput);

                // Set the position number text view
                TextView inputNumber = inputFieldView.findViewById(R.id.input_number);
                inputNumber.setText(String.valueOf(paramTextList.size()));

                // Get the voice button for the new parameter input
                Button voiceButton = inputFieldView.findViewById(R.id.voice_button);
                voiceButton.setTag(paramTextList.size() - 1);  // Set the tag

                // Add text to speech functionality to the button, post process voice data to use snake case
                voiceButton.setOnClickListener(v2 -> activity.startVoiceInput(newInput, "snake_case"));

                // Add the layout to the linear layout and the list
                paramInputContainer.addView(inputFieldView);
                paramViewList.add(inputFieldView);

            });

            remParameterButton.setOnClickListener(v1 ->
            {
                // Remove the last view from the linear layout and the list
                if(!paramViewList.isEmpty())
                {
                    // Get the last parameter view in the list and remove it from both the input container and view list
                    View lastView = paramViewList.get(paramViewList.size() - 1);
                    paramInputContainer.removeView(lastView);
                    paramViewList.remove(lastView);

                    // Also remove the edit text from the list
                    if(!paramTextList.isEmpty())
                    {
                        paramTextList.remove(paramTextList.size() - 1);
                    }
                }
            });

            applyButton.setOnClickListener(v1 ->
            {
                // Start the function definition
                StringBuilder text = new StringBuilder("def " + funcName.getText().toString() + "(");

                // Iterate over the views and add each parameter to the function definition
                for(int i = 0; i < paramTextList.size(); i++)
                {
                    // Get the parameter name
                    String paramName = paramTextList.get(i).getText().toString();

                    // Add the parameter to the function definition
                    text.append(paramName);

                    // If this is not the last parameter, add a comma and a space
                    if(i < paramTextList.size() - 1)
                    {
                        text.append(", ");
                    }
                }

                // Close the parenthesis
                text.append("):");

                // Insert the func definition at the cursor position
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text.toString());

                // Dismiss the dialog
                dialog.dismiss();
            });

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            dialog.setOnDismissListener(v2 ->
            {
                updateFunctionsTable();
                fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
            });

            dialog.show();
        });

        // Call any function desired
        callNewFunctionButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_call_newfunc);

            if(dialog.getWindow() != null)
            {
                // Dim the background and prevent the keyboard pushing up the dialog
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // Find the views in the dialog
            EditText funcName = dialog.findViewById(R.id.call_newfunc_name);
            Button funcNameVoice = dialog.findViewById(R.id.call_newfunc_name_voice);
            Button applyButton = dialog.findViewById(R.id.func_call_apply);
            Button cancelButton = dialog.findViewById(R.id.func_call_cancel);
            Button addArgumentButton = dialog.findViewById(R.id.add_param);
            Button remArgumentButton = dialog.findViewById(R.id.remove_param);

            // Initialise array for input text boxes and fetch layout reference
            argumentTextList = new ArrayList<>();
            argumentViewList = new ArrayList<>();
            argumentInputContainer = dialog.findViewById(R.id.argumentsContainer);

            // Button to add an argument to pass to function call
            addArgumentButton.setOnClickListener(v1 ->
            {
                // Inflate the input field layout
                View inputFieldView = dialog.getLayoutInflater().inflate(R.layout.input_field, null);

                // Get the edit text and add it to the list
                EditText newArgument = inputFieldView.findViewById(R.id.input_field);
                argumentTextList.add(newArgument);

                // Set the position number
                TextView inputNumber = inputFieldView.findViewById(R.id.input_number);
                inputNumber.setText(String.valueOf(argumentViewList.size()));

                // Create a voice button to voice type the argument
                Button voiceButton = inputFieldView.findViewById(R.id.voice_button);
                voiceButton.setTag(argumentTextList.size() - 1);  // Set the tag

                // Set text to speech functionality and post process to apply snake cake
                voiceButton.setOnClickListener(v2 -> activity.startVoiceInput(newArgument, "snake_case"));

                // Add the layout to the linear layout and the list
                argumentInputContainer.addView(inputFieldView);
                argumentViewList.add(inputFieldView);
            });

            // Button to remove an argument
            remArgumentButton.setOnClickListener(v1 ->
            {
                // Remove the last view from the linear layout and the list
                if(!argumentTextList.isEmpty()) // Check there's an argument to remove
                {
                    // Remove the last view in the list
                    View lastView = argumentViewList.get(argumentViewList.size() - 1);
                    argumentInputContainer.removeView(lastView);
                    argumentViewList.remove(lastView);

                    // Also remove the edit text from the list
                    if(!argumentTextList.isEmpty())
                    {
                        argumentTextList.remove(argumentTextList.size() - 1);
                    }
                }
            });

            // Apply the function call
            applyButton.setOnClickListener(v1 ->
            {
                // Get the text from the edit text
                StringBuilder text = new StringBuilder(funcName.getText().toString() + "(");

                // Iterate over the views and add each parameter to the function call
                for(int i = 0; i < argumentTextList.size(); i++)
                {
                    // Get the parameter name
                    String paramName = argumentTextList.get(i).getText().toString();

                    // Add the parameter to the function definition
                    text.append(paramName);

                    // If this is not the last parameter, add a comma and a space
                    if(i < argumentTextList.size() - 1)
                    {
                        text.append(", ");
                    }
                }

                // Close the parenthesis
                text.append(")");

                // Insert the text at the current cursor position
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text.toString());

                // Move the cursor back inside the parenthesis
                sourceCode.setSelection(start + text.length() - 1);

                // Dismiss the dialog
                dialog.dismiss();
            });

            funcNameVoice.setOnClickListener((v1 -> activity.startVoiceInput(funcName, "snake_case")));

            cancelButton.setOnClickListener(v2 -> dialog.dismiss());

            // On dismiss update the functions table and reset the soft input mode
            dialog.setOnDismissListener(v1 ->
            {
                updateFunctionsTable();
                fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
            });

            dialog.show();
        });

        // Button to jump to the position of the selected function definition in the editor
        gotoFuncButton.setOnClickListener(v1 ->
        {
            if(selectedTextView != null) // Check a definition is selected
            {
                selectedFunction = selectedTextView.getText().toString();
                // Check the function exists in the hashmap
                if(functionDefinitions.containsKey(selectedFunction))
                {
                    // If so, use the stored position and set the selection to it
                    int func_pos = functionDefinitions.get(selectedFunction);
                    sourceCode.setSelection(func_pos);
                }
            }
        });

        // Button to paste the name of the selected function definition in the editor
        pasteFuncButton.setOnClickListener(v ->
        {
            if(selectedTextView != null) // Check a definition is selected
            {
                // If so, get the name of the function from the text view
                selectedFunction = selectedTextView.getText().toString();
                int start = sourceCode.getSelectionStart();
                // Insert it into the editor at the current position
                sourceCode.getText().insert(start, selectedFunction);
            }
        });

        // Button to call a selected function from the definitions table
        callFuncButton.setOnClickListener(v ->
        {
            if(selectedTextView != null) // Check a definition is selected
            {
                // Get the name of the function
                selectedFunction = selectedTextView.getText().toString();

                // Create a new dialog
                Dialog dialog = new Dialog(activity.getContext());
                // Set the custom layout for the dialog
                dialog.setContentView(R.layout.dialog_callfunc);

                if(dialog.getWindow() != null)
                {
                    // Dim the background and prevent the keyboard pushing up the dialog
                    dialog.getWindow().setDimAmount(0.6f);
                    fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }

                // Find the views in the dialog
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

                // Button to add an argument to pass to function call
                addArgumentButton.setOnClickListener(v1 ->
                {
                    // Inflate the input field layout
                    View inputFieldView = dialog.getLayoutInflater().inflate(R.layout.input_field, null);

                    // Get the edit text and add it to the list
                    EditText newArgument = inputFieldView.findViewById(R.id.input_field);
                    argumentTextList.add(newArgument);

                    // Set the position number
                    TextView inputNumber = inputFieldView.findViewById(R.id.input_number);
                    inputNumber.setText(String.valueOf(argumentViewList.size()));

                    // Create a voice button to voice type the argument
                    Button voiceButton = inputFieldView.findViewById(R.id.voice_button);
                    voiceButton.setTag(argumentTextList.size() - 1);  // Set the tag

                    // Set text to speech functionality and post process to apply snake cake
                    voiceButton.setOnClickListener(v2 -> activity.startVoiceInput(newArgument, "snake_case"));

                    // Add the layout to the linear layout and the list
                    argumentInputContainer.addView(inputFieldView);
                    argumentViewList.add(inputFieldView);
                });

                // Button to remove an argument
                remArgumentButton.setOnClickListener(v1 ->
                {
                    // Remove the last view from the linear layout and the list
                    if(!argumentTextList.isEmpty()) // Check there's an argument to remove
                    {
                        // Remove the last view in the list
                        View lastView = argumentViewList.get(argumentViewList.size() - 1);
                        argumentInputContainer.removeView(lastView);
                        argumentViewList.remove(lastView);

                        // Also remove the edit text from the list
                        if(!argumentTextList.isEmpty())
                        {
                            argumentTextList.remove(argumentTextList.size() - 1);
                        }
                    }
                });

                // Apply the function call
                applyButton.setOnClickListener(v1 ->
                {
                    // Get the text from the edit text
                    StringBuilder text = new StringBuilder(selectedFunction + "(");

                    // Iterate over the views and add each parameter to the function definition
                    for(int i = 0; i < argumentTextList.size(); i++)
                    {
                        // Get the parameter name
                        String paramName = argumentTextList.get(i).getText().toString();

                        // Add the parameter to the function definition
                        text.append(paramName);

                        // If this is not the last parameter, add a comma and a space
                        if(i < argumentTextList.size() - 1)
                        {
                            text.append(", ");
                        }
                    }

                    // Close the parenthesis
                    text.append(")");

                    // Insert the text at the current cursor position
                    int start = sourceCode.getSelectionStart();
                    sourceCode.getText().insert(start, text.toString());

                    // Move the cursor back inside the parenthesis
                    sourceCode.setSelection(start + text.length() - 1);

                    // Dismiss the dialog
                    dialog.dismiss();
                });

                cancelButton.setOnClickListener(v1 -> dialog.dismiss());

                // On dismiss update the functions table and reset the soft input mode
                dialog.setOnDismissListener(v1 ->
                {
                    updateFunctionsTable();
                    fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
                });

                dialog.show();
            }
        });
    }

    // Method to refresh the table of function definitions
    public void updateFunctionsTable()
    {
        // Fetch function definitions in the editor
        functionDefinitions = updateFunctionsMap();

        // Reset selected values
        selectedFunction = null;
        selectedTextView = null;

        // Clear the table
        functionsDefTable.removeAllViews();

        // Create a new linear layout for every three functions
        LinearLayout linearLayout = null;

        int i = 0;
        // Iterate through the functions in the hashmap
        for(String functionName : functionDefinitions.keySet())
        {
            if(i % 3 == 0) // Ensure 3 per row
            {
                // Add a new layout if the previous is filled
                linearLayout = new LinearLayout(activity.getActivity());
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                functionsDefTable.addView(linearLayout);
            }

            // Create a new text view for the function
            // Set some default attributes e.g. size, alignment and truncation mode
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

            // Add an onclick to the text view to make it selectable
            textView.setClickable(true);
            textView.setOnClickListener(v ->
            {
                // Deselect the previously selected text view
                if(selectedTextView != null)
                {
                    selectedTextView.setBackground(ContextCompat.getDrawable(activity.requireActivity(), R.drawable.table_background));
                }

                // Select the clicked text view
                selectedTextView = (TextView) v;
                selectedTextView.setBackgroundColor(Color.LTGRAY);
            });

            // Add the new text views for each function definition
            linearLayout.addView(textView);
            i++;
        }
    }

    // Method to update the map of function definitions
    private HashMap<String, Integer> updateFunctionsMap()
    {
        // Clear the hashmap and fetch the text from the editor
        HashMap<String, Integer> functionDefinitions = new HashMap<>();
        String text = sourceCode.getText().toString();
        String[] lines = text.split("\\n");

        // Pattern to find functions, match def, whitespace, capture the name, parenthesis with anything in it, whitespace, then a colon
        // Capture group is the name of the function
        Pattern pattern = Pattern.compile("def\\s+(\\w+)\\s*\\(.*\\)\\s*:");

        int index = 0;
        // Iterate through the lines of the code
        for(String s : lines)
        {
            String line = s.trim();
            if(line.startsWith("#")) // ignore comments
            {
                index += line.length() + 1;
                continue;
            }

            // Find function definitions using regex pattern
            Matcher matcher = pattern.matcher(line);

            // If we find a definition on this line
            if(matcher.find())
            {
                // Get the captured name
                String functionName = matcher.group(1);

                // If we havent already added this function
                if(!functionDefinitions.containsKey(functionName))
                {
                    // Add the definition to the hashmap along with its position in the editor
                    functionDefinitions.put(functionName, index + line.indexOf(functionName));
                }
            }
            index += line.length() + 1;
        }
        return functionDefinitions;
    }
}
