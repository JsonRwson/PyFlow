package com.PyFlow.keyboard_pages;

import android.app.Dialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.fragment.app.FragmentActivity;

import com.PyFlow.SourcecodeEditor;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Imports Page ===========================================================
// Class for the imports page of the custom keyboard
// Allows the user to import standard python libraries
// The page tracks imported libraries, allowing you to quickly paste them in
// The page has quick keys for some common imports

public class imports_page
{
    // Hashmap to store imported libraries
    private HashMap<String, String> importedLibraries;
    private final TableLayout importedLibrariesTable;

    private final SourcecodeTab activity;
    private final FragmentActivity fragmentActivity;
    private final int originalSoftInputMode;

    private final SourcecodeEditor sourceCode;

    public imports_page(View view, SourcecodeTab activity, SourcecodeEditor source)
    {
        // References to the sourcecode editor widget and the activity for the fragment
        this.sourceCode = source;
        this.fragmentActivity = activity.getActivity();
        this.activity = activity;
        this.originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        // References to the elements on the page, and the quick import keys table
        TableLayout importKeysTable = view.findViewById(R.id.imp_keys_table);
        this.importedLibrariesTable = view.findViewById(R.id.imports_table);
        Button collapseImportsButton = view.findViewById(R.id.imports_collapse);
        Button newImportButton = view.findViewById(R.id.new_imp);
        Button refreshImportsButton = view.findViewById(R.id.imports_refresh);
        Button insertImportText = view.findViewById(R.id.imp_import);
        Button insertDotText = view.findViewById(R.id.imp_dot);
        Button insertFromText = view.findViewById(R.id.imp_from);

        // Iterate through the keys in the table and add onclick listeners to quickly import common libraries
        for(int i = 0; i < importKeysTable.getChildCount(); i++)
        {
            View row = importKeysTable.getChildAt(i);
            TableRow tableRow = (TableRow) row;
            // Iterate through the buttons in the current table row
            for(int j = 0; j < tableRow.getChildCount(); j++)
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
                    updateImportsTable();
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

        // Refresh imported libraries
        // Fetch all instances of import statements and the name of the library imported
        // Add them as buttons in the table layout to quickly paste in the library name
        refreshImportsButton.setOnClickListener(v -> updateImportsTable());

        // Use a one element array because lambda variables should be final
        // Toggle collapse the table for definitions
        final boolean[] isImportTableVisible = {true};
        collapseImportsButton.setOnClickListener(v ->
        {
            if(isImportTableVisible[0])
            {
                // Hide the table layout
                importedLibrariesTable.setVisibility(View.GONE);
                // Switch to an up chevron icon
                collapseImportsButton.setBackgroundResource(R.drawable.up_chevron_icon);
            }
            else
            {
                // Show the table layout
                importedLibrariesTable.setVisibility(View.VISIBLE);
                // Switch to a down chevron icon
                collapseImportsButton.setBackgroundResource(R.drawable.down_chevron_icon);
            }
            // Toggle the state
            isImportTableVisible[0] = !isImportTableVisible[0];
        });

        // Import something not on the quick keys
        newImportButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_import);

            if(dialog.getWindow() != null)
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

    // Function to update the table of imported libraries
    public void updateImportsTable()
    {
        // Fetch imported libraries from the editor and update the hash map
        importedLibraries = updateImportsMap();

        // Clear the table
        importedLibrariesTable.removeAllViews();

        // Create a new linear layout for every three libraries
        LinearLayout linearLayout = null;

        int i = 0;
        // Iterate through each imported library in the map
        for (String library : importedLibraries.keySet())
        {
            // Check if a new row needs to be added, 3 per row
            if (i % 3 == 0)
            {
                // If so, add a new layout for the row
                linearLayout = new LinearLayout(activity.getActivity());
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                importedLibrariesTable.addView(linearLayout);
            }

            // Create a new button for the library
            Button button = new Button(activity.getActivity());
            button.setText(library);
            button.setOnClickListener(v ->
            {
                // On click, insert the library name at the current cursor position
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, library);
            });

            linearLayout.addView(button);

            i++;
        }
    }

    // Method to identify imported libraries from the editor using regex
    private HashMap<String, String> updateImportsMap()
    {
        // Clear the hashmap and fetch the text from the editor
        HashMap<String, String> importedLibraries = new HashMap<>();
        String text = sourceCode.getText().toString();
        String[] lines = text.split("\\n");

        // Pattern to find imports, match import followed by one or more word characters
        // Capture group is the name of the library
        Pattern pattern = Pattern.compile("import\\s+(\\w+)");

        // Iterate through the lines in the text of the editor
        for(String s : lines)
        {
            String line = s.trim();

            Matcher matcher = pattern.matcher(line);

            // If an import is found in the line
            if(matcher.find())
            {
                // Get the captured name
                String library = matcher.group(1);

                // If the library name isnt already stored, then store it
                if(!importedLibraries.containsKey(library))
                {
                    importedLibraries.put(library, library);
                }
            }
        }

        return importedLibraries;
    }
}
