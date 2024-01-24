package com.PyFlow.keyboard_pages;

import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.fragment.app.FragmentActivity;

import com.PyFlow.CustomEditText;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

public class operators_page
{
    private final CustomEditText sourceCode;

    public operators_page(View view, SourcecodeTab activity, CustomEditText source)
    {
        this.sourceCode = source;
        FragmentActivity fragmentActivity = activity.getActivity();
        int originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        TableLayout numbersTable = view.findViewById(R.id.num_table);
        TableLayout operatorsTable = view.findViewById(R.id.op_table);

        activity.setOnclickForTableButtons(numbersTable, null, null);
        activity.setOnclickForTableButtons(operatorsTable, null, null);
    }
}
