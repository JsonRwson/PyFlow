package com.PyFlow.keyboard_pages;

import android.app.Dialog;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.PyFlow.SourcecodeEditor;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Variables Page ===========================================================
// Class for the variables page of the custom keyboard
// Allows the user to create quickly make new classes and objects from them
// Tracks definitions of variables allowing you to quickly goto, paste or update them
// Has quick input keys for variable related symbols
public class variables_page
{
    // Hashmap to store user defined variables and their location in the editor
    private HashMap<String, Integer> variableDefinitions;
    private final SourcecodeTab activity;
    private final FragmentActivity fragmentActivity;

    private final SourcecodeEditor sourceCode;
    private TextView selectedTextView;

    // Layouts for the variable definitions and quick access keys
    private final TableLayout variablesDefTable;
    private final TableLayout variablesKeyTable;

    // Store the currently selected variable
    private String selectedVariable;
    private final int originalSoftInputMode;

    public variables_page(View view, SourcecodeTab activity, SourcecodeEditor source)
    {
        // References to the sourcecode editor widget and the activity for the fragment
        this.sourceCode = source;
        this.activity = activity;
        this.fragmentActivity = activity.getActivity();
        // Store the soft input mode, original mode, opening the soft keyboard pushes up widgets on screen
        this.originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        // Table layouts to store variable definitions and quick input keys
        this.variablesDefTable = view.findViewById(R.id.var_def_table);
        this.variablesKeyTable = view.findViewById(R.id.var_keys_table);

        // References to ui elements on the page
        Button newVariableButton = view.findViewById(R.id.new_var);
        Button refreshDefinitionsButton = view.findViewById(R.id.var_def_refresh);
        Button collapseDefinitionsButton = view.findViewById(R.id.var_def_collapse);
        Button collapseKeysButton = view.findViewById(R.id.var_keys_collapse);
        Button gotoVarButton = view.findViewById(R.id.var_goto);
        Button updateVarButton = view.findViewById(R.id.var_assign);
        Button pasteVarButton = view.findViewById(R.id.var_paste);

        // Set on clicks for quick input keys
        activity.setOnclickForTableButtons(variablesKeyTable, null, null);

        // Button to create a new variable
        newVariableButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_newvar);

            if(dialog.getWindow() != null)
            {
                // Dim the background of the dialog
                // Set the soft input mode so the keyboard does not push up the dialog when opened
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // References to elements in the dialog
            EditText varName = dialog.findViewById(R.id.var_name);
            EditText varVal = dialog.findViewById(R.id.var_val);
            Button applyButton = dialog.findViewById(R.id.var_apply);
            Button cancelButton = dialog.findViewById(R.id.var_cancel);
            Button nameVoice = dialog.findViewById(R.id.var_name_voice);
            Button valueVoice = dialog.findViewById(R.id.var_val_voice);

            // Add text to speech functionality for the name of the variable and its value
            // Post process to apply snake case for the name and for the value, replace spoken operators with symbol versions
            nameVoice.setOnClickListener(v1 -> activity.startVoiceInput(varName, "snake_case"));
            valueVoice.setOnClickListener(v1 -> activity.startVoiceInput(varVal, "value"));

            // Apply new variable, inserting it into the editor
            applyButton.setOnClickListener(v1 ->
            {
                // Get the chosen name and set value for the variable, separate by an "="
                String text = varName.getText().toString() + " = " + varVal.getText().toString();

                // Insert the text at the current cursor position
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                // Dismiss the dialog
                dialog.dismiss();
            });

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            // On dismiss, update the variable definitions table and restore the original soft input mode
            dialog.setOnDismissListener(v2 ->
            {
                updateVariablesTable();
                fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
            });

            dialog.show();
        });

        // Button to update/assign to an existing variable
        updateVarButton.setOnClickListener(v ->
        {
            if(selectedTextView != null) // If a variable definition is selected
            {
                selectedVariable = selectedTextView.getText().toString();

                // Create a new dialog
                Dialog dialog = new Dialog(activity.getContext());
                // Set the custom layout for the dialog
                dialog.setContentView(R.layout.dialog_updatevar);

                if(dialog.getWindow() != null)
                {
                    // Dim the background of the dialog
                    // Set the soft input mode so the keyboard does not push up the dialog when opened
                    dialog.getWindow().setDimAmount(0.6f);
                    fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }

                // Find the views
                TextView varName = dialog.findViewById(R.id.var_title);
                EditText varValUpdate = dialog.findViewById(R.id.var_value);

                // Dropdown of all possible assignment operations on variables
                Spinner varOper = dialog.findViewById(R.id.oper_dropdown);
                String[] items = new String[]{"=", "+=", "-=", "*=", "/=", "%=", "//=", "**=", "&=", "|=", "^=", ">>=", "<<="};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(activity.getContext(), android.R.layout.simple_spinner_dropdown_item, items);
                varOper.setAdapter(adapter);

                Button applyButton = dialog.findViewById(R.id.var_apply);
                Button cancelButton = dialog.findViewById(R.id.var_cancel);
                Button valVoice = dialog.findViewById(R.id.var_val_voice);

                // Set the title for the dialog to indicate which variable is being updated
                varName.setText("Update: " + selectedVariable);

                // Add text to speech for the updated value of the variable
                valVoice.setOnClickListener(v1 -> activity.startVoiceInput(varValUpdate, "value"));

                // On apply, insert the assignment operation and new value
                applyButton.setOnClickListener(v1 ->
                {
                    // Get the operation from the dropdown, and the update value from the edit text
                    String text = selectedVariable + " " + varOper.getSelectedItem().toString() + " " + varValUpdate.getText().toString();

                    // Insert the text at the current cursor position
                    int start = sourceCode.getSelectionStart();
                    sourceCode.getText().insert(start, text);
                    // Dismiss the dialog
                    dialog.dismiss();
                });

                cancelButton.setOnClickListener(v1 -> dialog.dismiss());

                // On dismissing the dialog, update the class definitions map
                // Restore original soft input mode
                dialog.setOnDismissListener(v1 ->
                {
                    updateVariablesTable();
                    fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
                });

                dialog.show();
            }
        });

        // Button to paste the name of the selected variable definition
        pasteVarButton.setOnClickListener(v ->
        {
            if (selectedTextView != null) // If a variable is selected
            {
                // Get its name and insert at the cursor position
                selectedVariable = selectedTextView.getText().toString();
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, selectedVariable);
            }
        });

        // Button to jump to the first instance of a defined variable
        gotoVarButton.setOnClickListener(v ->
        {
            if (selectedTextView != null) // If a variable is selected
            {
                // Get the name of it
                selectedVariable = selectedTextView.getText().toString();
                // If the name exists in the definitions map, fetch its position in the editor
                if (variableDefinitions.containsKey(selectedVariable))
                {
                    // Jump to the variable definition
                    int var_pos = variableDefinitions.get(selectedVariable);
                    sourceCode.setSelection(var_pos);
                }
            }
        });

        // Refresh definitions of variables
        // Fetch all first instances of variable definitions, add them to the hash table with their position
        // Then add them as elements to be viewed in the table layout
        refreshDefinitionsButton.setOnClickListener(v -> updateVariablesTable());

        // Use a one element array because lambda variables should be final
        // Toggle collapse the table for definitions
        final boolean[] isDefTableVisible = {true};
        collapseDefinitionsButton.setOnClickListener(v ->
        {
            if(isDefTableVisible[0])
            {
                // Hide the table layout
                variablesDefTable.setVisibility(View.GONE);
                // Switch to an up chevron icon
                collapseDefinitionsButton.setBackgroundResource(R.drawable.up_chevron_icon);
            }
            else
            {
                // Show the table layout
                variablesDefTable.setVisibility(View.VISIBLE);
                // Switch to a down chevron icon
                collapseDefinitionsButton.setBackgroundResource(R.drawable.down_chevron_icon);
            }
            // Toggle the state
            isDefTableVisible[0] = !isDefTableVisible[0];
        });

        // Toggle collapse the table for quick input keys
        final boolean[] isKeyTableVisible = {true};
        collapseKeysButton.setOnClickListener(v ->
        {
            if(isKeyTableVisible[0])
            {
                // Hide the table layout
                variablesKeyTable.setVisibility(View.GONE);
                // Switch to an up chevron icon
                collapseKeysButton.setBackgroundResource(R.drawable.up_chevron_icon);
            }
            else
            {
                // Show the table layout
                variablesKeyTable.setVisibility(View.VISIBLE);
                // Switch to a down chevron icon
                collapseKeysButton.setBackgroundResource(R.drawable.down_chevron_icon);
            }
            // Toggle the state
            isKeyTableVisible[0] = !isKeyTableVisible[0];
        });
    }

    // Function to update the table of variable definitions
    public void updateVariablesTable()
    {
        // Fetch variable definitions from the editor and update the hash map
        variableDefinitions = updateVariablesMap();

        // Reset selected values
        selectedVariable = null;
        selectedTextView = null;

        // Clear the table
        variablesDefTable.removeAllViews();

        // Create a new linear layout for every three variables
        LinearLayout linearLayout = null;

        int i = 0;
        // Iterate through each variable definition in the map
        for (String variable : variableDefinitions.keySet())
        {
            // Check if a new row needs to be added, 3 per row
            if (i % 3 == 0)
            {
                // If so, add a new layout for the row
                linearLayout = new LinearLayout(activity.getActivity());
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                variablesDefTable.addView(linearLayout);
            }

            // Create a new text view for the variable
            // Set text related attributes
            TextView textView = new TextView(activity.getActivity());
            textView.setText(variable);
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

            // Add an onclick to the text view to allow selection
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

            linearLayout.addView(textView);

            i++;
        }
    }

    // Method to identify defined variables from the editor using regex
    private HashMap<String, Integer> updateVariablesMap()
    {
        // Clear the hashmap and fetch the text from the editor
        HashMap<String, Integer> variableDefinitions = new HashMap<>();
        String text = sourceCode.getText().toString();
        String[] lines = text.split("\\n");

        // Pattern to find variables, match zero or more word characters, then whitespace, then equals, then whitespace followed by anything
        // Capture group is the name of the variable
        Pattern pattern = Pattern.compile("(\\w+)\\s*=\\s*.*");

        int index = 0;
        // Iterate through the lines in the text of the editor
        for(String s : lines)
        {
            String line = s.trim();
            // Ignore comments, in for loops, in while loops, and in if or elif statements
            if(line.startsWith("#") || line.contains("for ") || line.contains("if ") || line.contains("while ") || line.contains("elif "))
            {
                index += line.length() + 1;
                continue;
            }

            Matcher matcher = pattern.matcher(line);

            // If a variable is found in the line
            if(matcher.find())
            {
                // Get the captured name
                String variable = matcher.group(1);

                // If the variable name isnt already stored, then store it along with its position in the editor
                if(!variableDefinitions.containsKey(variable))
                {
                    variableDefinitions.put(variable, index + line.indexOf(variable));
                }
            }

            index += line.length() + 1;
        }

        return variableDefinitions;
    }
}