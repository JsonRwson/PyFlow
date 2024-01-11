package com.PyFlow.keyboard_pages;

import android.app.Dialog;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.PyFlow.CustomEditText;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

public class imports_page extends Page
{
    private SourcecodeTab activity;
    private FragmentActivity fragmentActivity;
    private int originalSoftInputMode;
    private static final int SPEECH_REQUEST_CODE = 0;

    private TableLayout importKeysTable;

    private CustomEditText sourceCode;
    private View page;
    private TextView selectedTextView;

    private Button newImportButton;
    private Button insertImportText;
    private Button insertDotText;
    private Button insertFromText;

    public imports_page(View view, SourcecodeTab activity, CustomEditText source)
    {
        this.sourceCode = source;
        this.page = view;
        this.activity = activity;
        this.fragmentActivity = activity.getActivity();
        this.originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        this.newImportButton = page.findViewById(R.id.new_imp);
        this.importKeysTable = page.findViewById(R.id.imp_keys_table);
        this.insertImportText = page.findViewById(R.id.imp_import);
        this.insertDotText = page.findViewById(R.id.imp_dot);
        this.insertFromText = page.findViewById(R.id.imp_from);

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
                        if (newImportButton != null)
                        {
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

            if(dialog.getWindow() != null)
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

            importVoiceButton.setOnClickListener(v1 ->
            {
                displaySpeechRecognizer(importName);
            });

            cancelButton.setOnClickListener(v1 ->
            {
                dialog.dismiss();
            });

            dialog.setOnDismissListener(v2 ->
            {
                fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
            });

            dialog.show();
        });
    }

    // start the speech recognizer
    private void displaySpeechRecognizer(EditText editText)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        this.voiceEditText = editText;
        activity.startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }
}
