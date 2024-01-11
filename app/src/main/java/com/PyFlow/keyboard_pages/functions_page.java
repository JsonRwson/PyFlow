package com.PyFlow.keyboard_pages;

import android.view.View;
import android.widget.Button;

import com.PyFlow.CustomEditText;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

public class functions_page extends Page
{
    private CustomEditText sourceCode;
    private View page;

    public functions_page(View view, SourcecodeTab activity, CustomEditText source)
    {
        this.sourceCode = source;
        this.page = view;

//        Button newFunctionButton = page.findViewById(R.id.new_func);
//        Button callFunctionButton = page.findViewById(R.id.call_func);

//        newFunctionButton.setOnClickListener(view110 ->
//        {
//            int start = sourceCode.getSelectionStart();
//            sourceCode.getText().insert(start, "def newFunction():\n\tprint(\"hello world\")");
//        });
//
//        callFunctionButton.setOnClickListener(view19 ->
//        {
//            int start = sourceCode.getSelectionStart();
//            sourceCode.getText().insert(start, "newFunction()");
//        });
    }
}
