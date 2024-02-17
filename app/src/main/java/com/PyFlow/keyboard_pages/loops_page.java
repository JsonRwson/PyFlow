package com.PyFlow.keyboard_pages;

import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;

import androidx.fragment.app.FragmentActivity;

import com.PyFlow.SourcecodeEditor;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

// Loops Page ===========================================================
// Class for the loops page of the custom keyboard
// Allows the user to insert a while or for loop
// Allows the user to use quick input keys for loop related symbols
public class loops_page
{
    private final SourcecodeEditor sourceCode;

    public loops_page(View view, SourcecodeTab activity, SourcecodeEditor source)
    {
        // References to the sourcecode editor widget and the activity for the fragment
        this.sourceCode = source;
        FragmentActivity fragmentActivity = activity.getActivity();
        // Store the soft input mode, original mode, opening the soft keyboard pushes up widgets on screen
        int originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        // References to ui elements and the table for the quick input keys
        TableLayout loopKeysTable = view.findViewById(R.id.loop_keys_table);
        Button newForButton = view.findViewById(R.id.new_for);
        Button newWhileButton = view.findViewById(R.id.new_while);

        // Set on clicks for the quick inputs table
        activity.setOnclickForTableButtons(loopKeysTable, null, null);

        // Button to insert a new for loop
        newForButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_forloop);

            // Dim the background of the dialog
            // Set the soft input mode so the keyboard does not push up the dialog when opened
            if (dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // References to elements in the dialog
            EditText forVarName = dialog.findViewById(R.id.for_var_name);
            EditText forIterator = dialog.findViewById(R.id.for_iterator);

            Button applyButton = dialog.findViewById(R.id.for_apply);
            Button cancelButton = dialog.findViewById(R.id.for_cancel);
            Button forVarNameVoice = dialog.findViewById(R.id.for_var_name_voice);
            Button forIteratorVoice = dialog.findViewById(R.id.for_iterator_voice);

            // On apply, insert the new for loop
            applyButton.setOnClickListener(v1 ->
            {
                // Get the text from the editText for the variable name and iterator
                String text = "for " + forVarName.getText().toString() + " in " + forIterator.getText().toString() + ":";

                // Inser the for loop at the current selection
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                dialog.dismiss();
            });

            // Allow voice typing for the variable name and iterator
            // Post process the variable name to follow snake case
            // Post process the iterator to replace words with operators and more, see method in sourcecode tab class
            forVarNameVoice.setOnClickListener(v1 -> activity.startVoiceInput(forVarName, "snake_case"));
            forIteratorVoice.setOnClickListener(v1 -> activity.startVoiceInput(forIterator, "value"));

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());
            dialog.setOnDismissListener(v2 -> fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode));
            dialog.show();
        });

        // Button to insert a new while loop
        newWhileButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_whileloop);

            if (dialog.getWindow() != null)
            {
                // Dim the background and prevent keyboard from pushing up the dialog
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // References to dialog ui elements
            EditText whileCondition = dialog.findViewById(R.id.while_condition);
            Button applyButton = dialog.findViewById(R.id.while_apply);
            Button cancelButton = dialog.findViewById(R.id.while_cancel);
            Button whileConditionVoice = dialog.findViewById(R.id.while_condition_voice);

            // On applying, insert the while loop
            applyButton.setOnClickListener(v1 ->
            {
                // Use the text from the condition box
                String text = "while " + whileCondition.getText().toString() + ":";

                // Insert the while loop at the current cursor position
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                dialog.dismiss();
            });

            // Allow voice typing for loop condition, post process to replace spoken operators with symbols and more
            whileConditionVoice.setOnClickListener(v1 -> activity.startVoiceInput(whileCondition, "value"));

            // On dismiss, return the soft input mode so that they keyboard doesnt overlay the utility or navigation buttons
            cancelButton.setOnClickListener(v1 -> dialog.dismiss());
            dialog.setOnDismissListener(v2 -> fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode));
            dialog.show();
        });

    }
}
