package com.PyFlow.keyboard_pages;

import android.view.View;
import android.widget.TableLayout;

import androidx.fragment.app.FragmentActivity;

import com.PyFlow.SourcecodeEditor;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

// Operators Page ===========================================================
// Class for the Operators page of the custom keyboard
// Allows the user to quickly insert python operators and numbers
public class operators_page
{
    private final SourcecodeEditor sourceCode;

    public operators_page(View view, SourcecodeTab activity, SourcecodeEditor source)
    {
        // Reference the source code editor
        this.sourceCode = source;

        // Tables for quick input of numbers and operators
        TableLayout numbersTable = view.findViewById(R.id.num_table);
        TableLayout operatorsTable = view.findViewById(R.id.op_table);

        // Simply make the keys insert the text of the key at the current cursor position
        activity.setOnclickForTableButtons(numbersTable, null, null);
        activity.setOnclickForTableButtons(operatorsTable, null, null);
    }
}
