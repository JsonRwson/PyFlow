package com.PyFlow.keyboard_pages;

import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.fragment.app.FragmentActivity;

import com.PyFlow.SourcecodeEditor;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

public class imports_page
{
    private final FragmentActivity fragmentActivity;
    private final int originalSoftInputMode;

    private final SourcecodeEditor sourceCode;

    public imports_page(View view, SourcecodeTab activity, SourcecodeEditor source)
    {
        this.sourceCode = source;
        this.fragmentActivity = activity.getActivity();
        this.originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        TableLayout importKeysTable = view.findViewById(R.id.imp_keys_table);
        Button newImportButton = view.findViewById(R.id.new_imp);
        Button insertImportText = view.findViewById(R.id.imp_import);
        Button insertDotText = view.findViewById(R.id.imp_dot);
        Button insertFromText = view.findViewById(R.id.imp_from);

        for (int i = 0; i < importKeysTable.getChildCount(); i++)
        {
            View row = importKeysTable.getChildAt(i);
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
                            String text = "import " + button.getText().toString();

                            sourceCode.getText().insert(0, text + "\n");
                        });
                    }
                }
            }
        }

        insertImportText.setOnClickListener(v ->
        {
            int start = sourceCode.getSelectionStart();
            sourceCode.getText().insert(start, "import");
        });

        insertDotText.setOnClickListener(v ->
        {
            int start = sourceCode.getSelectionStart();
            sourceCode.getText().insert(start, ".");
        });

        insertFromText.setOnClickListener(v ->
        {
            int start = sourceCode.getSelectionStart();
            sourceCode.getText().insert(start, "from");
        });

        newImportButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_import);

            if (dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // Find the views
            EditText importName = dialog.findViewById(R.id.imp_name);

            Button applyButton = dialog.findViewById(R.id.imp_apply);
            Button cancelButton = dialog.findViewById(R.id.imp_cancel);
            Button importVoiceButton = dialog.findViewById(R.id.imp_name_voice);

            applyButton.setOnClickListener(v1 ->
            {
                // Get the text from the field
                String text = "import " + importName.getText().toString();

                // Insert the text at the top of the file
                sourceCode.getText().insert(0, text + "\n");

                // Dismiss the dialog
                dialog.dismiss();
            });

            importVoiceButton.setOnClickListener(v1 -> activity.startVoiceInput(importName, null));

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            dialog.setOnDismissListener(v2 -> fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode));

            dialog.show();
        });
    }
}
