package com.PyFlow.keyboard_pages;

import android.app.Dialog;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.PyFlow.CustomEditText;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OOP_page
{
    private HashMap<String, Integer> classDefinitions;
    private final SourcecodeTab activity;
    private final FragmentActivity fragmentActivity;

    private final TableLayout classDefTable;
    private final TableLayout classKeysTable;
    private final CustomEditText sourceCode;
    private TextView selectedTextView;

    private List<EditText> paramTextList;
    private List<View> paramViewList;
    private LinearLayout paramInputContainer;

    private List<EditText> argumentTextList;
    private List<View> argumentViewList;
    private LinearLayout argumentInputContainer;

    private String selectedClass;
    private final int originalSoftInputMode;

    public OOP_page(View view, SourcecodeTab activity, CustomEditText source)
    {
        this.sourceCode = source;

        this.activity = activity;
        this.fragmentActivity = activity.getActivity();
        this.originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        this.classDefTable = view.findViewById(R.id.class_def_table);
        this.classKeysTable = view.findViewById(R.id.oop_keys_table);

        Button newClassButton = view.findViewById(R.id.new_class);
        Button refreshDefinitionsButton = view.findViewById(R.id.class_def_refresh);
        Button collapseDefinitionsButton = view.findViewById(R.id.class_def_collapse);
        Button collapseKeysButton = view.findViewById(R.id.oop_keys_collapse);
        Button gotoClassButton = view.findViewById(R.id.oop_goto);
        Button newObjectButton = view.findViewById(R.id.oop_newObj);
        Button pasteClassButton = view.findViewById(R.id.oop_paste);

        activity.setOnclickForTableButtons(this.classKeysTable, null, null);

        // Use a one element array because lambda variables should be final
        // Toggle collapse the table for definitions
        final boolean[] isDefTableVisible = {true};
        collapseDefinitionsButton.setOnClickListener(v ->
        {
            if (isDefTableVisible[0])
            {
                // Hide the table layout
                classDefTable.setVisibility(View.GONE);
                // Switch to an up chevron icon
                collapseDefinitionsButton.setBackgroundResource(R.drawable.up_chevron_icon);
            }
            else
            {
                // Show the table layout
                classDefTable.setVisibility(View.VISIBLE);
                // Switch to a down chevron icon
                collapseDefinitionsButton.setBackgroundResource(R.drawable.down_chevron_icon);
            }
            // Toggle the state
            isDefTableVisible[0] = !isDefTableVisible[0];
        });

        final boolean[] isKeyTableVisible = {true};
        collapseKeysButton.setOnClickListener(v ->
        {
            if (isKeyTableVisible[0])
            {
                // Hide the table layout
                classKeysTable.setVisibility(View.GONE);
                // Switch to an up chevron icon
                collapseKeysButton.setBackgroundResource(R.drawable.up_chevron_icon);
            }
            else
            {
                // Show the table layout
                classKeysTable.setVisibility(View.VISIBLE);
                // Switch to a down chevron icon
                collapseKeysButton.setBackgroundResource(R.drawable.down_chevron_icon);
            }
            // Toggle the state
            isKeyTableVisible[0] = !isKeyTableVisible[0];
        });

        refreshDefinitionsButton.setOnClickListener(v -> updateClassesTable());

        newClassButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_newclass);

            if (dialog.getWindow() != null)
            {
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            EditText className = dialog.findViewById(R.id.class_name);

            Button applyButton = dialog.findViewById(R.id.oop_apply);
            Button cancelButton = dialog.findViewById(R.id.oop_cancel);
            Button classNameVoice = dialog.findViewById(R.id.class_name_voice);

            LinearLayout constructorLayout = dialog.findViewById(R.id.constructor_layout);
            CheckBox constructorCheckbox = dialog.findViewById(R.id.constructor_check);
            Button addParameterButton = dialog.findViewById(R.id.add_param);
            Button remParameterButton = dialog.findViewById(R.id.remove_param);

            constructorCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
            {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    if(isChecked)
                    {
                        // The checkbox is checked, show constructor related widgets
                        constructorLayout.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        // The checkbox is unchecked, hide constructor related widgets
                        constructorLayout.setVisibility(View.GONE);
                    }
                }
            });

            // Initialise array for input text boxes and fetch layout reference
            paramTextList = new ArrayList<>();
            paramViewList = new ArrayList<>();
            paramInputContainer = dialog.findViewById(R.id.parametersContainer);

            classNameVoice.setOnClickListener(v1 -> activity.startVoiceInput(className));

            addParameterButton.setOnClickListener(v1 ->
            {
                // Inflate the input field layout
                View inputFieldView = dialog.getLayoutInflater().inflate(R.layout.input_field, null);

                // Get the edit text and add it to the list
                EditText newInput = inputFieldView.findViewById(R.id.input_field);
                paramTextList.add(newInput);

                // Set the position number
                TextView inputNumber = inputFieldView.findViewById(R.id.input_number);
                inputNumber.setText(String.valueOf(paramTextList.size()));

                Button voiceButton = inputFieldView.findViewById(R.id.voice_button);
                voiceButton.setTag(paramTextList.size() - 1);  // Set the tag

                voiceButton.setOnClickListener(v2 -> activity.startVoiceInput(newInput));

                // Add the layout to the linear layout and the list
                paramInputContainer.addView(inputFieldView);
                paramViewList.add(inputFieldView);

            });

            remParameterButton.setOnClickListener(v1 ->
            {
                // Remove the last view from the linear layout and the list
                if (!paramViewList.isEmpty())
                {
                    View lastView = paramViewList.get(paramViewList.size() - 1);
                    paramInputContainer.removeView(lastView);
                    paramViewList.remove(lastView);

                    // Also remove the edit text from the list
                    if (!paramTextList.isEmpty())
                    {
                        paramTextList.remove(paramTextList.size() - 1);
                    }
                }
            });

            applyButton.setOnClickListener(v1 ->
            {
                // Start the class definition
                String text = "class " + className.getText().toString() + ":\n";

                if(constructorCheckbox.isChecked())
                {
                    text = text + "    def __init__(self, ";

                    for(int i = 0; i < paramTextList.size(); i++)
                    {
                        // Get the parameter name
                        String paramName = paramTextList.get(i).getText().toString();

                        // Add the parameter to the function definition
                        text += paramName;

                        // If this is not the last parameter, add a comma and a space
                        if (i < paramTextList.size() - 1)
                        {
                            text += ", ";
                        }
                    }

                    text = text + "):\n";
                }

                // Insert the func definition at the cursor position
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                // Dismiss the dialog
                dialog.dismiss();
            });

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            dialog.setOnDismissListener(v2 ->
            {
                updateClassesTable();
                fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
            });

            dialog.show();
        });

        gotoClassButton.setOnClickListener(v1 ->
        {
            if(selectedTextView != null)
            {
                selectedClass = selectedTextView.getText().toString();
                if(classDefinitions.containsKey(selectedClass))
                {
                    int var_pos = classDefinitions.get(selectedClass);
                    sourceCode.setSelection(var_pos);
                }
            }
        });

        pasteClassButton.setOnClickListener(v ->
        {
            if(selectedTextView != null)
            {
                selectedClass = selectedTextView.getText().toString();
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, selectedClass);
            }
        });

        newObjectButton.setOnClickListener(v ->
        {

        });

    }

    private HashMap<String, Integer> updateClassesMap()
    {
        classDefinitions = new HashMap<>();
        String text = sourceCode.getText().toString();
        String[] lines = text.split("\\n");

        // Pattern to find classes, match class, whitespace, capture the name, whitespace, then a colon
        // Capture group is the name of the class
        Pattern pattern = Pattern.compile("class\\s+(\\w+)\\s*:");

        int index = 0;
        for (String s : lines)
        {
            String line = s.trim();
            if (line.startsWith("#")) // ignore comments
            {
                index += line.length() + 1;
                continue;
            }

            Matcher matcher = pattern.matcher(line);

            if (matcher.find())
            {
                String className = matcher.group(1);

                if (!classDefinitions.containsKey(className))
                {
                    classDefinitions.put(className, index + line.indexOf(className));
                }
            }

            index += line.length() + 1;
        }

        return classDefinitions;
    }

    private void updateClassesTable()
    {
        classDefinitions = updateClassesMap();

        // Reset selected values
        selectedClass = null;
        selectedTextView = null;

        // Clear the table
        classDefTable.removeAllViews();

        // Create a new linear layout for every three classes
        LinearLayout linearLayout = null;

        int i = 0;
        for (String className : classDefinitions.keySet())
        {
            if (i % 3 == 0)
            {
                linearLayout = new LinearLayout(activity.getActivity());
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                classDefTable.addView(linearLayout);
            }

            // Create a new text view for the class
            TextView textView = new TextView(activity.getActivity());
            textView.setText(className);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(0, 10, 0, 10);
            textView.setMaxLines(1);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setBackground(ContextCompat.getDrawable(activity.requireActivity(), R.drawable.table_background));

            // Set layout parameters for equal spacing
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            textView.setLayoutParams(params);

            // Add an onclick to the text view
            textView.setClickable(true);
            textView.setOnClickListener(v ->
            {
                // Deselect the previously selected text view
                if (selectedTextView != null)
                {
                    selectedTextView.setBackground(ContextCompat.getDrawable(activity.requireActivity(), R.drawable.table_background));
                }

                // Select the clicked text view
                selectedTextView = (TextView) v;
                selectedTextView.setBackgroundColor(Color.LTGRAY);
            });

            linearLayout.addView(textView);

            i++;
        }
    }

}
