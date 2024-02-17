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

// Selection Page ===========================================================
// Class for the selection page of the custom keyboard
// Allows the user to insert an if, elif or else statement
// Allows the user to use quick input keys for selection related symbols
public class selection_page
{
    private final SourcecodeEditor sourceCode;

    public selection_page(View view, SourcecodeTab activity, SourcecodeEditor source)
    {
        // References to the sourcecode editor widget and the activity for the fragment
        this.sourceCode = source;
        FragmentActivity fragmentActivity = activity.getActivity();
        // Store the soft input mode, original mode, opening the soft keyboard pushes up widgets on screen
        int originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        // References to UI elements on the page
        TableLayout selKeysTable = view.findViewById(R.id.sel_keys_table);
        Button newIfButton = view.findViewById(R.id.sel_if);
        Button newElifButton = view.findViewById(R.id.sel_elif);
        Button newElseButton = view.findViewById(R.id.sel_else);

        // Set functionality for the quick input keys
        activity.setOnclickForTableButtons(selKeysTable, null, null);

        // Button to add a new if statement
        newIfButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_if);

            if (dialog.getWindow() != null)
            {
                // Dim the background of the dialog
                // Set the soft input mode so the keyboard does not push up the dialog when opened
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // References to elements in the dialog
            EditText ifCondition = dialog.findViewById(R.id.if_condition);
            Button applyButton = dialog.findViewById(R.id.if_apply);
            Button cancelButton = dialog.findViewById(R.id.if_cancel);
            Button ifConditionVoice = dialog.findViewById(R.id.if_condition_voice);

            // On applying, insert the if statement at the current selection
            applyButton.setOnClickListener(v1 ->
            {
                // Get the condition from the edit text, surround with statement
                String text = "if " + ifCondition.getText().toString() + ":";

                // Insert if statement at current selection
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                dialog.dismiss();
            });

            // Add text to speech functionality for the if condition input
            ifConditionVoice.setOnClickListener(v1 -> activity.startVoiceInput(ifCondition, "value"));

            // On dismiss, return the soft input mode so that they keyboard doesnt overlay the utility or navigation buttons
            cancelButton.setOnClickListener(v1 -> dialog.dismiss());
            dialog.setOnDismissListener(v2 -> fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode));
            dialog.show();
        });

        // Insert new elif, essentially the same as if statement code, should optimize this
        newElifButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_elif);

            if(dialog.getWindow() != null)
            {
                // Dim the background of the dialog
                // Set the soft input mode so the keyboard does not push up the dialog when opened
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // References to ui elements in the dialog
            EditText elifCondition = dialog.findViewById(R.id.elif_condition);
            Button applyButton = dialog.findViewById(R.id.elif_apply);
            Button cancelButton = dialog.findViewById(R.id.elif_cancel);
            Button elifConditionVoice = dialog.findViewById(R.id.elif_condition_voice);

            // Insert the elif statement at the current cursor position
            applyButton.setOnClickListener(v1 ->
            {
                // Build the statement, fetching the condition from the input box
                String text = "elif " + elifCondition.getText().toString() + ":";

                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                dialog.dismiss();
            });

            // Text to speech functionality for the condition of the elif
            elifConditionVoice.setOnClickListener(v1 -> activity.startVoiceInput(elifCondition, "value"));

            // On dismiss, return the soft input mode so that they keyboard doesnt overlay the utility or navigation buttons
            cancelButton.setOnClickListener(v1 -> dialog.dismiss());
            dialog.setOnDismissListener(v2 -> fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode));
            dialog.show();
        });

        // Insert new else statement, just pasting in "else:"
        newElseButton.setOnClickListener(v ->
        {
            int start = sourceCode.getSelectionStart();
            sourceCode.getText().insert(start, "else:");
        });
    }
}
