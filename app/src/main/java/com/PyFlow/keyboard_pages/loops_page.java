package com.PyFlow.keyboard_pages;

import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.fragment.app.FragmentActivity;

import com.PyFlow.CustomEditText;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

public class loops_page
{
    private final CustomEditText sourceCode;

    public loops_page(View view, SourcecodeTab activity, CustomEditText source)
    {
        this.sourceCode = source;
        FragmentActivity fragmentActivity = activity.getActivity();
        int originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        TableLayout loopKeysTable = view.findViewById(R.id.loop_keys_table);
        Button newForButton = view.findViewById(R.id.new_for);
        Button newWhileButton = view.findViewById(R.id.new_while);

        activity.setOnclickForTableButtons(loopKeysTable, null);

        newForButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_forloop);

            if (dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }


            EditText forVarName = dialog.findViewById(R.id.for_var_name);
            EditText forIterator = dialog.findViewById(R.id.for_iterator);

            Button applyButton = dialog.findViewById(R.id.for_apply);
            Button cancelButton = dialog.findViewById(R.id.for_cancel);
            Button forVarNameVoice = dialog.findViewById(R.id.for_var_name_voice);
            Button forIteratorVoice = dialog.findViewById(R.id.for_iterator_voice);

            applyButton.setOnClickListener(v1 ->
            {
                String text = "for " + forVarName.getText().toString() + " in " + forIterator.getText().toString() + ":\n";

                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                dialog.dismiss();
            });

            forVarNameVoice.setOnClickListener(v1 -> activity.startVoiceInput(forVarName));

            forIteratorVoice.setOnClickListener(v1 -> activity.startVoiceInput(forIterator));

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            dialog.setOnDismissListener(v2 -> fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode));

            dialog.show();
        });

        newWhileButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_whileloop);

            if (dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            EditText whileCondition = dialog.findViewById(R.id.while_condition);

            Button applyButton = dialog.findViewById(R.id.while_apply);
            Button cancelButton = dialog.findViewById(R.id.while_cancel);
            Button whileConditionVoice = dialog.findViewById(R.id.while_condition_voice);

            applyButton.setOnClickListener(v1 ->
            {
                String text = "while " + whileCondition.getText().toString() + ":\n";

                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                dialog.dismiss();
            });

            whileConditionVoice.setOnClickListener(v1 -> activity.startVoiceInput(whileCondition));

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            dialog.setOnDismissListener(v2 -> fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode));

            dialog.show();
        });

    }
}
