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

// Imports Page ===========================================================
// Class for the imports page of the custom keyboard
// Allows the user to import standard python libraries
// The page has quick keys for some common imports

public class imports_page
{
    private final FragmentActivity fragmentActivity;
    private final int originalSoftInputMode;

    private final SourcecodeEditor sourceCode;

    public imports_page(View view, SourcecodeTab activity, SourcecodeEditor source)
    {
        // References to the sourcecode editor widget and the activity for the fragment
        this.sourceCode = source;
        this.fragmentActivity = activity.getActivity();
        this.originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        // References to the elements on the page, and the quick import keys table
        TableLayout importKeysTable = view.findViewById(R.id.imp_keys_table);
        Button newImportButton = view.findViewById(R.id.new_imp);
        Button insertImportText = view.findViewById(R.id.imp_import);
        Button insertDotText = view.findViewById(R.id.imp_dot);
        Button insertFromText = view.findViewById(R.id.imp_from);

        // Iterate through the keys in the table and add onclick listeners to quickly import common libraries
        for (int i = 0; i < importKeysTable.getChildCount(); i++)
        {
            View row = importKeysTable.getChildAt(i);
            TableRow tableRow = (TableRow) row;
            // Iterate through the buttons in the current table row
            for (int j = 0; j < tableRow.getChildCount(); j++)
            {
                View child = tableRow.getChildAt(j);
                Button button = (Button) child;
                // Set an onclick so it imports the chosen library at the start of the file
                button.setOnClickListener(v ->
                {
                    // Get the text from the Button
                    String text = "import " + button.getText().toString();
                    // Insert the import at the top of the file
                    sourceCode.getText().insert(0, text + "\n");
                });
            }
        }

        // Button that quickly inserts the word "import" at current selection
        insertImportText.setOnClickListener(v ->
        {
            int start = sourceCode.getSelectionStart();
            sourceCode.getText().insert(start, "import");
        });

        // Button that quickly inserts "." at current selection
        insertDotText.setOnClickListener(v ->
        {
            int start = sourceCode.getSelectionStart();
            sourceCode.getText().insert(start, ".");
        });

        // Button that quickly inserts "from" at current selection
        insertFromText.setOnClickListener(v ->
        {
            int start = sourceCode.getSelectionStart();
            sourceCode.getText().insert(start, "from");
        });

        // Import something not on the quick keys
        newImportButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_import);

            if (dialog.getWindow() != null)
            {
                // Dim the background and prevent the keyboard from pushing the dialog up
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

            // Allow voice typing for the library name, no postprocessing
            importVoiceButton.setOnClickListener(v1 -> activity.startVoiceInput(importName, null));

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            // Reset the soft input mode
            dialog.setOnDismissListener(v2 -> fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode));

            dialog.show();
        });
    }
}
