package com.PyFlow.keyboard_pages;

import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.fragment.app.FragmentActivity;

import com.PyFlow.SourcecodeEditor;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

public class exceptions_page
{
    private final SourcecodeEditor sourceCode;

    public exceptions_page(View view, SourcecodeTab activity, SourcecodeEditor source)
    {
        this.sourceCode = source;
        FragmentActivity fragmentActivity = activity.getActivity();
        int originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        TableLayout exceptionKeysTable = view.findViewById(R.id.except_keys_table);
        TableLayout commonExceptionsTable = view.findViewById(R.id.common_except_table);
        Button catchExceptionButton = view.findViewById(R.id.catch_exception);
        Button printStackTraceButton = view.findViewById(R.id.print_stacktrace);

        activity.setOnclickForTableButtons(exceptionKeysTable, null, null);

        for (int i = 0; i < commonExceptionsTable.getChildCount(); i++)
        {
            View row = commonExceptionsTable.getChildAt(i);
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
                            String text = "except " + button.getText().toString() +"Error:\n";

                            if(button.getText().toString().matches("Exception"))
                            {
                                text = "except Exception:\n";
                            }

                            // Insert the text at the current cursor position
                            int start = sourceCode.getSelectionStart();
                            sourceCode.getText().insert(start, text);
                        });
                    }
                }
            }
        }


        catchExceptionButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_catchexception);

            if (dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            EditText exceptionToCatch = dialog.findViewById(R.id.exception_tocatch);
            EditText exceptionAs = dialog.findViewById(R.id.exception_as);

            CheckBox insertTryCheck = dialog.findViewById(R.id.insert_try_check);

            Button applyButton = dialog.findViewById(R.id.except_apply);
            Button cancelButton = dialog.findViewById(R.id.except_cancel);
            Button exceptionToCatchVoice = dialog.findViewById(R.id.exception_tocatch_voice);
            Button exceptionAsVoice = dialog.findViewById(R.id.exception_as_voice);

            applyButton.setOnClickListener(v1 ->
            {
                String exceptionAsText = exceptionAs.getText().toString();
                String text = "";

                if(exceptionAs.getText().toString().isEmpty())
                {
                    text = "except " + exceptionToCatch.getText().toString() + ":\n";
                }
                else
                {
                    text = "except " + exceptionToCatch.getText().toString() + " as " + exceptionAsText + ":\n";
                }

                int start = sourceCode.getSelectionStart();

                if(insertTryCheck.isChecked())
                {
                    sourceCode.getText().insert(start, "try:\n\n");
                    start = sourceCode.getSelectionStart();
                }

                sourceCode.getText().insert(start, text);
                dialog.dismiss();
            });

            exceptionToCatchVoice.setOnClickListener(v1 -> activity.startVoiceInput(exceptionToCatch, "PascalCase"));

            exceptionAsVoice.setOnClickListener(v1 -> activity.startVoiceInput(exceptionAs, null));

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            dialog.setOnDismissListener(v2 -> fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode));

            dialog.show();
        });

        printStackTraceButton.setOnClickListener(v ->
        {
            String sourceCodeText = sourceCode.getText().toString();

            // Check if traceback has been imported
            if (!sourceCodeText.contains("import traceback"))
            {
                sourceCode.getText().insert(0, "import traceback\n");
            }

            int start = sourceCode.getSelectionStart();
            sourceCode.getText().insert(start, "print(traceback.format_exc())");
        });
    }
}
