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

// Exceptions Page ===========================================================
// Class for the exceptions page of the custom keyboard
// Allows the user to insert a try / except statement
// Allows the user to quickly print the stacktrace
// Includes buttons to quickly catch common Exceptions

public class exceptions_page
{
    private final SourcecodeEditor sourceCode;

    public exceptions_page(View view, SourcecodeTab activity, SourcecodeEditor source)
    {
        // References to the sourcecode editor widget and the activity for the fragment
        sourceCode = source;
        FragmentActivity fragmentActivity = activity.getActivity();

        // Store the soft input mode, original mode, opening the soft keyboard pushes up widgets on screen
        int originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        // References to the tables for the quick input keys and common exceptions
        TableLayout exceptionKeysTable = view.findViewById(R.id.except_keys_table);
        TableLayout commonExceptionsTable = view.findViewById(R.id.common_except_table);

        // References to the buttons which are used to insert try / except and print the stacktrace
        Button catchExceptionButton = view.findViewById(R.id.catch_exception);
        Button printStackTraceButton = view.findViewById(R.id.print_stacktrace);

        // Make all the quick input keys simply insert the text of the button into the editor when clicked
        activity.setOnclickForTableButtons(exceptionKeysTable, null, null);

        // For all the buttons in the common exceptions table
        // Set an onclick so that they insert an except for the exception labelled on the button
        // Iterate through all the rows of the table
        for(int i = 0; i < commonExceptionsTable.getChildCount(); i++)
        {
            // Iterate through the buttons in the current table row
            TableRow tableRow = (TableRow) commonExceptionsTable.getChildAt(i);;
            for(int j = 0; j < tableRow.getChildCount(); j++)
            {
                View child = tableRow.getChildAt(j);
                Button button = (Button) child;

                // Set onclick so that it inserts an except statement
                // "except (text of the button)Error:\n"
                button.setOnClickListener(v ->
                {
                    String text = "except " + button.getText().toString() +"Error:\n" + sourceCode.getIndentationLevel() + "    ";

                    // There is a quick button to simply catch any exception
                    // Simply set the text to static exception statement
                    if(button.getText().toString().matches("Exception"))
                    {
                        text = "except Exception:\n";
                    }

                    // Insert the except at the cursor position
                    int start = sourceCode.getSelectionStart();
                    sourceCode.getText().insert(start, text);
                });
            }
        }

        // Button functionality to insert a try / except statement
        // The user can choose if they want to include a try statement or not
        catchExceptionButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_catchexception);

            // Dim the background of the dialog
            // Set the soft input mode so the keyboard does not push up the dialog when opened
            if(dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // References to the widgets on the dialog
            EditText exceptionToCatch = dialog.findViewById(R.id.exception_tocatch);
            EditText exceptionAs = dialog.findViewById(R.id.exception_as);
            CheckBox insertTryCheck = dialog.findViewById(R.id.insert_try_check);
            Button applyButton = dialog.findViewById(R.id.except_apply);
            Button cancelButton = dialog.findViewById(R.id.except_cancel);
            Button exceptionToCatchVoice = dialog.findViewById(R.id.exception_tocatch_voice);
            Button exceptionAsVoice = dialog.findViewById(R.id.exception_as_voice);

            // When the apply button is clicked
            applyButton.setOnClickListener(v1 ->
            {
                // Get the "as" variable name for the exception the user has inputted
                String exceptionAsText = exceptionAs.getText().toString();
                String text;

                // If they dont want to use "as" then catch the exception only
                if(exceptionAs.getText().toString().isEmpty())
                {
                    text = sourceCode.getIndentationLevel() + "except " + exceptionToCatch.getText().toString() + ":\n" + sourceCode.getIndentationLevel() + "    ";
                }
                else // Otherwise catch the exception as the name they inputted
                {
                    text = sourceCode.getIndentationLevel() + "except " + exceptionToCatch.getText().toString() + " as " + exceptionAsText + ":\n" + sourceCode.getIndentationLevel() + "    ";
                }

                int start = sourceCode.getSelectionStart();

                // If the user has checked to include a try statement as well
                // Insert the try statement before the except and update start point
                if(insertTryCheck.isChecked())
                {
                    sourceCode.getText().insert(start, "try:\n" + sourceCode.getIndentationLevel() + "    " + "\n");
                    start = sourceCode.getSelectionStart();
                }

                // Insert the except statement at the current position
                sourceCode.getText().insert(start, text);
                dialog.dismiss();
            });

            // Call method to add functionality to voice buttons for the input boxes of the dialog
            // Standard Exceptions are generally pascal case, post process voice data to achieve this automatically
            exceptionToCatchVoice.setOnClickListener(v1 -> activity.startVoiceInput(exceptionToCatch, "PascalCase"));
            exceptionAsVoice.setOnClickListener(v1 -> activity.startVoiceInput(exceptionAs, null));

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());
            // On dismiss, return the soft input mode so that they keyboard doesnt overlay the utility or navigation buttons
            dialog.setOnDismissListener(v1 -> fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode));
            dialog.show();
        });

        // Button to quickly print the stacktrace in python code
        printStackTraceButton.setOnClickListener(v ->
        {
            String sourceCodeText = sourceCode.getText().toString();

            // Check if traceback has been imported, which is needed for this
            if(!sourceCodeText.contains("import traceback"))
            {
                // If not imported, add it to the start of the file
                sourceCode.getText().insert(0, "import traceback\n");
            }

            // Then insert code to print the stacktrace
            int start = sourceCode.getSelectionStart();
            sourceCode.getText().insert(start, "print(traceback.format_exc())");
        });
    }
}
