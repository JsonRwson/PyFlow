package com.PyFlow.keyboard_pages;

import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;

import androidx.fragment.app.FragmentActivity;

import com.PyFlow.CustomEditText;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

public class selection_page
{
    private final CustomEditText sourceCode;

    public selection_page(View view, SourcecodeTab activity, CustomEditText source)
    {
        this.sourceCode = source;
        FragmentActivity fragmentActivity = activity.getActivity();
        int originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        TableLayout selKeysTable = view.findViewById(R.id.sel_keys_table);
        Button newIfButton = view.findViewById(R.id.sel_if);
        Button newElifButton = view.findViewById(R.id.sel_elif);
        Button newElseButton = view.findViewById(R.id.sel_else);

        activity.setOnclickForTableButtons(selKeysTable, null, null);

        newIfButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_if);

            if (dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            EditText ifCondition = dialog.findViewById(R.id.if_condition);

            Button applyButton = dialog.findViewById(R.id.if_apply);
            Button cancelButton = dialog.findViewById(R.id.if_cancel);
            Button ifConditionVoice = dialog.findViewById(R.id.if_condition_voice);

            applyButton.setOnClickListener(v1 ->
            {
                String text = "if " + ifCondition.getText().toString() + ":\n";

                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                dialog.dismiss();
            });

            ifConditionVoice.setOnClickListener(v1 -> activity.startVoiceInput(ifCondition));

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            dialog.setOnDismissListener(v2 -> fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode));

            dialog.show();
        });

        newElifButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_elif);

            if (dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            EditText elifCondition = dialog.findViewById(R.id.elif_condition);

            Button applyButton = dialog.findViewById(R.id.elif_apply);
            Button cancelButton = dialog.findViewById(R.id.elif_cancel);
            Button elifConditionVoice = dialog.findViewById(R.id.elif_condition_voice);

            applyButton.setOnClickListener(v1 ->
            {
                String text = "elif " + elifCondition.getText().toString() + ":\n";

                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                dialog.dismiss();
            });

            elifConditionVoice.setOnClickListener(v1 -> activity.startVoiceInput(elifCondition));

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            dialog.setOnDismissListener(v2 -> fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode));

            dialog.show();
        });

        newElseButton.setOnClickListener(v ->
        {
            int start = sourceCode.getSelectionStart();
            sourceCode.getText().insert(start, "else:\n");
        });
    }
}
