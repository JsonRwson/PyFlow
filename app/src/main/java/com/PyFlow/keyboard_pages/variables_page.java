package com.PyFlow.keyboard_pages;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.PyFlow.CustomEditText;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class variables_page
{
    private HashMap<String, Integer> variableDefinitions;
    private CustomEditText sourceCode;
    private View page;
    private TextView selectedTextView;

    public variables_page(View view, SourcecodeTab activity,CustomEditText source)
    {
        this.sourceCode = source;
        this.page = view;

        TableLayout variablesDefTable = page.findViewById(R.id.var_def_table);
        TableLayout variablesKeyTable = page.findViewById(R.id.var_keys_table);

        Button newVariableButton = page.findViewById(R.id.new_var);
        Button refreshDefinitionsButton = page.findViewById(R.id.var_def_refresh);
        Button collapseDefinitionsButton = page.findViewById(R.id.var_def_collapse);
        Button collapseKeysButton = page.findViewById(R.id.var_keys_collapse);
        Button gotoVarButton = page.findViewById(R.id.var_goto);

        for (int i = 0; i < variablesKeyTable.getChildCount(); i++)
        {
            View row = variablesKeyTable.getChildAt(i);
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
                            sourceCode.getText().insert(start, text + " ");
                        });
                    }
                }
            }
        }

        newVariableButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_newvar);
            if(dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
            }
            // Find the views in your layout
            EditText editText = dialog.findViewById(R.id.edit_text);
            Button okButton = dialog.findViewById(R.id.ok_button);
            // Set an OnClickListener for the OK button

            okButton.setOnClickListener(v1 ->
            {
                // Get the text from the EditText
                String text = editText.getText().toString();
                // Insert the text at the current cursor position
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);
                // Dismiss the dialog
                dialog.dismiss();
            });

            // Show the dialog
            dialog.show();
        });

        gotoVarButton.setOnClickListener(v ->
        {
            if (selectedTextView != null)
            {
                String selectedVariable = selectedTextView.getText().toString();
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
        refreshDefinitionsButton.setOnClickListener(v ->
        {
            variableDefinitions = updateVariableDefinitions();

            // Clear the table first
            variablesDefTable.removeAllViews();

            // Create a new LinearLayout for every three variables
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

                // Create a new TextView for the variable
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

                // Add an OnClickListener to the TextView
                textView.setClickable(true);
                textView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        // Deselect the previously selected TextView, if any
                        if (selectedTextView != null)
                        {
                            selectedTextView.setBackground(ContextCompat.getDrawable(activity.requireActivity(), R.drawable.table_background));;
                        }

                        // Select the clicked TextView
                        selectedTextView = (TextView) v;
                        selectedTextView.setBackgroundColor(Color.LTGRAY);
                    }
                });

                linearLayout.addView(textView);

                i++;
            }
        });

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

    private HashMap<String, Integer> updateVariableDefinitions()
    {
        HashMap<String, Integer> variableDefinitions = new HashMap<>();
        String text = sourceCode.getText().toString();
        String[] lines = text.split("\\n");
        Pattern pattern = Pattern.compile("(\\w+(, \\w+)*)\\s*=\\s*.*");

        int index = 0;
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i].trim();
            if (line.startsWith("#") || line.contains(" for ") || line.contains(" if ")) // ignore comments and complex expressions
            {
                index += line.length() + 1; // +1 for newline character
                continue;
            }

            Matcher matcher = pattern.matcher(line);

            if (matcher.find())
            {
                String[] variables = matcher.group(1).split(", ");

                for (String variable : variables)
                {
                    if (!variableDefinitions.containsKey(variable))
                    {
                        variableDefinitions.put(variable, index + line.indexOf(variable));
                    }
                }
            }

            index += line.length() + 1; // +1 for newline character
        }

        return variableDefinitions;
    }

}