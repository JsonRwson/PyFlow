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
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.PyFlow.CustomEditText;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class variables_page
{
    private HashMap<String, Integer> variableDefinitions;
    private final SourcecodeTab activity;
    private final FragmentActivity fragmentActivity;

    private final CustomEditText sourceCode;
    private TextView selectedTextView;

    private final TableLayout variablesDefTable;
    private final TableLayout variablesKeyTable;

    private String selectedVariable;
    private final int originalSoftInputMode;

    public variables_page(View view, SourcecodeTab activity, CustomEditText source)
    {
        this.sourceCode = source;
        this.activity = activity;
        this.fragmentActivity = activity.getActivity();
        this.originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        this.variablesDefTable = view.findViewById(R.id.var_def_table);
        this.variablesKeyTable = view.findViewById(R.id.var_keys_table);

        Button newVariableButton = view.findViewById(R.id.new_var);
        Button refreshDefinitionsButton = view.findViewById(R.id.var_def_refresh);
        Button collapseDefinitionsButton = view.findViewById(R.id.var_def_collapse);
        Button collapseKeysButton = view.findViewById(R.id.var_keys_collapse);
        Button gotoVarButton = view.findViewById(R.id.var_goto);
        Button updateVarButton = view.findViewById(R.id.var_assign);
        Button pasteVarButton = view.findViewById(R.id.var_paste);

        activity.setOnclickForTableButtons(variablesKeyTable, null, null);

        newVariableButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_newvar);

            if(dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // Find the views
            EditText varName = dialog.findViewById(R.id.var_name);
            EditText varVal = dialog.findViewById(R.id.var_val);

            Button applyButton = dialog.findViewById(R.id.var_apply);
            Button cancelButton = dialog.findViewById(R.id.var_cancel);
            Button nameVoice = dialog.findViewById(R.id.var_name_voice);
            Button valueVoice = dialog.findViewById(R.id.var_val_voice);

            nameVoice.setOnClickListener(v1 -> activity.startVoiceInput(varName));

            valueVoice.setOnClickListener(v1 -> activity.startVoiceInput(varVal));

            applyButton.setOnClickListener(v1 ->
            {
                // Get the text from the edit text
                String text = varName.getText().toString() + " = " + varVal.getText().toString();

                // Insert the text at the current cursor position
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
                updateVariablesTable();
                fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
            });

            dialog.show();
        });

        updateVarButton.setOnClickListener(v ->
        {
            if(selectedTextView != null)
            {
                selectedVariable = selectedTextView.getText().toString();

                // Create a new dialog
                Dialog dialog = new Dialog(activity.getContext());
                // Set the custom layout for the dialog
                dialog.setContentView(R.layout.dialog_updatevar);

                if(dialog.getWindow() != null)
                {
                    dialog.getWindow().setDimAmount(0.6f);
                    fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }

                // Find the views
                TextView varName = dialog.findViewById(R.id.var_title);
                EditText varValUpdate = dialog.findViewById(R.id.var_value);

                Spinner varOper = dialog.findViewById(R.id.oper_dropdown);
                String[] items = new String[]{"=", "+=", "-=", "*=", "/=", "%=", "//=", "**=", "&=", "|=", "^=", ">>=", "<<="};
                ArrayAdapter<String> adapter = new ArrayAdapter<>(activity.getContext(), android.R.layout.simple_spinner_dropdown_item, items);
                varOper.setAdapter(adapter);

                Button applyButton = dialog.findViewById(R.id.var_apply);
                Button cancelButton = dialog.findViewById(R.id.var_cancel);
                Button valVoice = dialog.findViewById(R.id.var_val_voice);

                varName.setText("Update: " + selectedVariable);

                valVoice.setOnClickListener(v1 -> activity.startVoiceInput(varValUpdate));

                applyButton.setOnClickListener(v1 ->
                {
                    // Get the text from the edit text
                    String text = selectedVariable + " " + varOper.getSelectedItem().toString() + " " + varValUpdate.getText().toString();

                    // Insert the text at the current cursor position
                    int start = sourceCode.getSelectionStart();
                    sourceCode.getText().insert(start, text);
                    // Dismiss the dialog
                    dialog.dismiss();
                });

                cancelButton.setOnClickListener(v1 -> dialog.dismiss());

                dialog.setOnDismissListener(v1 ->
                {
                    updateVariablesTable();
                    fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
                });

                dialog.show();
            }
        });

        pasteVarButton.setOnClickListener(v ->
        {
            if (selectedTextView != null)
            {
                selectedVariable = selectedTextView.getText().toString();
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, selectedVariable);
            }
        });

        gotoVarButton.setOnClickListener(v ->
        {
            if (selectedTextView != null)
            {
                selectedVariable = selectedTextView.getText().toString();
                if (variableDefinitions.containsKey(selectedVariable))
                {
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
            if (isDefTableVisible[0])
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

        final boolean[] isKeyTableVisible = {true};
        collapseKeysButton.setOnClickListener(v ->
        {
            if (isKeyTableVisible[0])
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

    private void updateVariablesTable()
    {
        variableDefinitions = updateVariablesMap();

        // Reset selected values
        selectedVariable = null;
        selectedTextView = null;

        // Clear the table
        variablesDefTable.removeAllViews();

        // Create a new linear layout for every three variables
        LinearLayout linearLayout = null;

        int i = 0;
        for (String variable : variableDefinitions.keySet())
        {
            if (i % 3 == 0)
            {
                linearLayout = new LinearLayout(activity.getActivity());
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                variablesDefTable.addView(linearLayout);
            }

            // Create a new text ciew for the variable
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

            // Add an onclick to the text view
            textView.setClickable(true);
            textView.setOnClickListener(v ->
            {
                // Deselect the previously selected text view
                if (selectedTextView != null)
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

    private HashMap<String, Integer> updateVariablesMap()
    {
        HashMap<String, Integer> variableDefinitions = new HashMap<>();
        String text = sourceCode.getText().toString();
        String[] lines = text.split("\\n");

        // Pattern to find variables, match zero or more word characters, then whitespace, then equals, then whitespace followed by anything
        // Capture group is the name of the variable
        Pattern pattern = Pattern.compile("(\\w+)\\s*=\\s*.*");

        int index = 0;
        for (String s : lines)
        {
            String line = s.trim();
            if (line.startsWith("#") || line.contains("for ") || line.contains("if ") || line.contains("while ") || line.contains("elif ")) // ignore comments and complex expressions
            {
                index += line.length() + 1;
                continue;
            }

            Matcher matcher = pattern.matcher(line);

            if (matcher.find())
            {
                String variable = matcher.group(1);

                if (!variableDefinitions.containsKey(variable))
                {
                    variableDefinitions.put(variable, index + line.indexOf(variable));
                }
            }

            index += line.length() + 1;
        }

        return variableDefinitions;
    }
}