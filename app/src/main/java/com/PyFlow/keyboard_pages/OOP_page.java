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

import com.PyFlow.SourcecodeEditor;
import com.PyFlow.R;
import com.PyFlow.SourcecodeTab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// OOP Page ===========================================================
// Class for the object orient programming page of the custom keyboard
// Allows the user to quickly make new classes and objects from them
// Tracks definitions of classes allowing you to quickly goto, paste or create objects from them
// Has quick input keys for oop related symbols
public class OOP_page
{
    // Hashmap to store definitions of classes
    private HashMap<String, Integer> classDefinitions;
    private final SourcecodeTab activity;
    private final FragmentActivity fragmentActivity;

    // Layouts for the tables that contain the quick input buttons and tracked class definitions
    private final TableLayout classDefTable;
    private final TableLayout classKeysTable;

    private final SourcecodeEditor sourceCode;
    private TextView selectedTextView;

    // Structures and view that relate to parameters in the constructor for the new class dialog
    private List<EditText> paramTextList;
    private List<View> paramViewList;
    private LinearLayout paramInputContainer;

    // Structures and view that relate to arguments in the new object dialog
    private List<EditText> argTextList;
    private List<View> argViewList;
    private LinearLayout argInputContainer;

    // Track selected class definition
    private String selectedClass;
    private final int originalSoftInputMode;

    public OOP_page(View view, SourcecodeTab activity, SourcecodeEditor source)
    {
        // References to the sourcecode editor widget and the activity for the fragment
        this.sourceCode = source;
        this.activity = activity;
        this.fragmentActivity = activity.getActivity();
        // Store the soft input mode, original mode, opening the soft keyboard pushes up widgets on screen
        this.originalSoftInputMode = fragmentActivity.getWindow().getAttributes().softInputMode;

        // References to the tables for class definitions and quick input keys
        this.classDefTable = view.findViewById(R.id.class_def_table);
        this.classKeysTable = view.findViewById(R.id.oop_keys_table);

        // References to ui elements on the page
        Button newClassButton = view.findViewById(R.id.new_class);
        Button refreshDefinitionsButton = view.findViewById(R.id.class_def_refresh);
        Button collapseDefinitionsButton = view.findViewById(R.id.class_def_collapse);
        Button collapseKeysButton = view.findViewById(R.id.oop_keys_collapse);
        Button gotoClassButton = view.findViewById(R.id.oop_goto);
        Button newObjectButton = view.findViewById(R.id.oop_newObj);
        Button pasteClassButton = view.findViewById(R.id.oop_paste);

        // Set functionality for quick input keys
        activity.setOnclickForTableButtons(this.classKeysTable, null, null);

        // Use a one element array because lambda variables should be final
        // Toggle collapse the table for definitions
        final boolean[] isDefTableVisible = {true};
        collapseDefinitionsButton.setOnClickListener(v ->
        {
            if(isDefTableVisible[0])
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

        // Toggle collapse the table for quick input keys
        final boolean[] isKeyTableVisible = {true};
        collapseKeysButton.setOnClickListener(v ->
        {
            if(isKeyTableVisible[0])
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

        // Refresh definitions of classes using the method
        refreshDefinitionsButton.setOnClickListener(v -> updateClassesTable());

        // Button to create a new class
        newClassButton.setOnClickListener(v ->
        {
            // Create a new dialog
            Dialog dialog = new Dialog(activity.getContext());
            // Set the custom layout for the dialog
            dialog.setContentView(R.layout.dialog_newclass);

            if(dialog.getWindow() != null)
            {
                // Dim the background of the dialog
                // Set the soft input mode so the keyboard does not push up the dialog when opened
                dialog.getWindow().setDimAmount(0.6f);
                fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }

            // References to elements in the dialog
            EditText className = dialog.findViewById(R.id.class_name);
            Button applyButton = dialog.findViewById(R.id.oop_apply);
            Button cancelButton = dialog.findViewById(R.id.oop_cancel);
            Button classNameVoice = dialog.findViewById(R.id.class_name_voice);
            LinearLayout constructorLayout = dialog.findViewById(R.id.constructor_layout);
            CheckBox constructorCheckbox = dialog.findViewById(R.id.constructor_check);
            Button addParameterButton = dialog.findViewById(R.id.add_param);
            Button remParameterButton = dialog.findViewById(R.id.remove_param);

            // Checkbox, for if the user would like to add a constructor to their new class
            constructorCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
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
            });

            // Initialise array for input text boxes and fetch layout reference
            paramTextList = new ArrayList<>();
            paramViewList = new ArrayList<>();
            paramInputContainer = dialog.findViewById(R.id.parametersContainer);

            // Add text to speech functionality for class names, post process text to apply pascal case
            classNameVoice.setOnClickListener(v1 -> activity.startVoiceInput(className, "PascalCase"));

            // Button to add a parameter to the constructor
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

                // Add text to speech functionality for parameters
                voiceButton.setOnClickListener(v2 -> activity.startVoiceInput(newInput, "snake_case"));

                // Add the layout to the linear layout and the list
                paramInputContainer.addView(inputFieldView);
                paramViewList.add(inputFieldView);

            });

            // Button to remove a parameter from the constructor
            remParameterButton.setOnClickListener(v1 ->
            {
                // Remove the last view from the linear layout and the list
                if(!paramViewList.isEmpty())
                {
                    View lastView = paramViewList.get(paramViewList.size() - 1);
                    paramInputContainer.removeView(lastView);
                    paramViewList.remove(lastView);

                    // Also remove the edit text from the list
                    if(!paramTextList.isEmpty())
                    {
                        paramTextList.remove(paramTextList.size() - 1);
                    }
                }
            });

            // Insert the new class definition and constructor if required
            applyButton.setOnClickListener(v1 ->
            {
                // Start the class definition
                String text = "class " + className.getText().toString() + ":\n";

                // If the user has chosen to include a constructor
                if(constructorCheckbox.isChecked())
                {
                    // Start the constructor definition
                    text = text + sourceCode.getIndentationLevel() + "    def __init__(self";

                    // Iterate through all the constructor parameters and add them to the constructor
                    for(int i = 0; i < paramTextList.size(); i++)
                    {
                        // Get the parameter name
                        String paramName = paramTextList.get(i).getText().toString();

                        // Add a comma and a space before the parameter
                        text += ", ";

                        // Add the parameter to the function definition
                        text += paramName;
                    }
                    // Close parenthesis of the constructor
                    text = text + "):";
                }

                // Insert the func definition at the cursor position
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, text);

                // Dismiss the dialog
                dialog.dismiss();
            });

            cancelButton.setOnClickListener(v1 -> dialog.dismiss());

            // On dismissing the dialog, update the class definitions map
            // Restore original soft input mode
            dialog.setOnDismissListener(v2 ->
            {
                updateClassesTable();
                fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
            });

            dialog.show();
        });

        // Button to allow the user to jump to a defined class in the editor
        gotoClassButton.setOnClickListener(v1 ->
        {
            if(selectedTextView != null) // If a class is selected
            {
                // Get the name of the class, check if it is in the hashmap
                selectedClass = selectedTextView.getText().toString();
                if(classDefinitions.containsKey(selectedClass))
                {
                    // Move cursor to stored position of hashmap
                    int class_pos = classDefinitions.get(selectedClass);
                    sourceCode.setSelection(class_pos);
                }
            }
        });

        // Button to paste the name of a selected class
        pasteClassButton.setOnClickListener(v ->
        {
            if(selectedTextView != null) // If a class is selected
            {
                // Get the name of the class and insert it at the cursor position
                selectedClass = selectedTextView.getText().toString();
                int start = sourceCode.getSelectionStart();
                sourceCode.getText().insert(start, selectedClass);
            }
        });

        // Button to create a new object from a selected class
        newObjectButton.setOnClickListener(v ->
        {
            if(selectedTextView != null)
            {
                // Create a new dialog
                Dialog dialog = new Dialog(activity.getContext());
                // Set the custom layout for the dialog
                dialog.setContentView(R.layout.dialog_newobj);

                if(dialog.getWindow() != null)
                {
                    dialog.getWindow().setDimAmount(0.6f);
                    fragmentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                }

                // Initialise arrays for arguments of new object
                argTextList = new ArrayList<>();
                argViewList = new ArrayList<>();
                argInputContainer = dialog.findViewById(R.id.argumentsContainer);

                // Fetch the title of the dialog and add the selected class name to it
                TextView objectTitle = dialog.findViewById(R.id.new_obj_text);
                selectedClass = selectedTextView.getText().toString();
                String title = objectTitle.getText() + selectedClass;
                objectTitle.setText(title);

                // References to ui elements in the dialog
                EditText objectName = dialog.findViewById(R.id.object_name);
                Button objectNameVoice = dialog.findViewById(R.id.object_name_voice);
                Button addArgumentButton = dialog.findViewById(R.id.add_argument);
                Button remArgumentButton = dialog.findViewById(R.id.remove_argument);
                Button applyButton = dialog.findViewById(R.id.obj_apply);
                Button cancelButton = dialog.findViewById(R.id.obj_cancel);

                // Add text to speech functionality to the object name input
                objectNameVoice.setOnClickListener(v1 -> activity.startVoiceInput(objectName, "snake_case"));

                // Button to add an argument to the new object call
                addArgumentButton.setOnClickListener(v1 ->
                {
                    // Inflate the input field layout
                    View inputFieldView = dialog.getLayoutInflater().inflate(R.layout.input_field, null);

                    // Get the edit text and add it to the list
                    EditText newInput = inputFieldView.findViewById(R.id.input_field);
                    argTextList.add(newInput);

                    // Set the position number
                    TextView inputNumber = inputFieldView.findViewById(R.id.input_number);
                    inputNumber.setText(String.valueOf(argTextList.size()));

                    Button voiceButton = inputFieldView.findViewById(R.id.voice_button);
                    voiceButton.setTag(argTextList.size() - 1);  // Set the tag

                    // Text to speech for the argument name, post process to apply snake case
                    voiceButton.setOnClickListener(v2 -> activity.startVoiceInput(newInput, "snake_case"));

                    // Add the layout to the linear layout and the list
                    argInputContainer.addView(inputFieldView);
                    argViewList.add(inputFieldView);
                });

                // Button to remove an argument from the new object call
                remArgumentButton.setOnClickListener(v1 ->
                {
                    // Remove the last view from the linear layout and the list
                    if(!argViewList.isEmpty()) // Check there is something to remove
                    {
                        // Get the last argument in the list, remove the view from the container and list
                        View lastView = argViewList.get(argViewList.size() - 1);
                        argInputContainer.removeView(lastView);
                        argViewList.remove(lastView);

                        // Also remove the edit text from the list
                        if (!argTextList.isEmpty())
                        {
                            argTextList.remove(argTextList.size() - 1);
                        }
                    }
                });

                // Apply the new object, inserting it into the editor
                applyButton.setOnClickListener(v1 ->
                {
                    // Get the selected class name
                    selectedClass = selectedTextView.getText().toString();
                    // Start the class definition
                    String text = objectName.getText().toString() + " = " + selectedClass + "(";

                    // Iterate through the number of arguments provided
                    for(int i = 0; i < argTextList.size(); i++)
                    {
                        // Get the parameter name
                        String argName = argTextList.get(i).getText().toString();

                        // Add the parameter to the function definition
                        text += argName;

                        // If this is not the last parameter, add a comma and a space
                        if (i < argTextList.size() - 1)
                        {
                            text += ", ";
                        }
                    }

                    // Close the parenthesis
                    text = text + ")";

                    // Insert the new object at the cursor position
                    int start = sourceCode.getSelectionStart();
                    sourceCode.getText().insert(start, text);

                    // Dismiss the dialog
                    dialog.dismiss();
                });

                // On dismiss, restore the original soft input mode and update the class definitions map
                cancelButton.setOnClickListener(v1 -> dialog.dismiss());
                dialog.setOnDismissListener(v2 ->
                {
                    updateClassesTable();
                    fragmentActivity.getWindow().setSoftInputMode(originalSoftInputMode);
                });

                dialog.show();
            }
        });
    }

    // Function to identify and store the positions and names of class definitions in the editor
    // The functions, variables and classes definition methods are quite inefficient, optimize in future, use sparingly for now
    private HashMap<String, Integer> updateClassesMap()
    {
        // Clear the map and get the text from the editor
        classDefinitions = new HashMap<>();
        String text = sourceCode.getText().toString();
        String[] lines = text.split("\\n");

        // Pattern to find classes, match class, whitespace, capture the name, whitespace, then a colon
        // Capture group is the name of the class
        Pattern pattern = Pattern.compile("class\\s+(\\w+)\\s*:");

        int index = 0;
        // Iterate through lines in the editor
        for(String s : lines)
        {
            String line = s.trim();
            if(line.startsWith("#")) // ignore comments
            {
                index += line.length() + 1;
                continue;
            }

            // Identify if a class is defined in the line using regex pattern
            Matcher matcher = pattern.matcher(line);

            // If a class definition is found
            if(matcher.find())
            {
                // Store the captured name
                String className = matcher.group(1);

                // If the class hasnt already been added to the map, add it, along with its position in the editor
                if (!classDefinitions.containsKey(className))
                {
                    classDefinitions.put(className, index + line.indexOf(className));
                }
            }
            index += line.length() + 1;
        }
        return classDefinitions;
    }

    // Method to update the table of selectable class definitions
    public void updateClassesTable()
    {
        // Update the map of defined classes
        classDefinitions = updateClassesMap();

        // Reset selected values
        selectedClass = null;
        selectedTextView = null;

        // Clear the table
        classDefTable.removeAllViews();

        // Create a new linear layout for every three classes
        LinearLayout linearLayout = null;

        int i = 0;
        // Iterate through all class definitions
        for(String className : classDefinitions.keySet())
        {
            // Determine if new row is needed
            if(i % 3 == 0)
            {
                // Create a new layout for the new row
                linearLayout = new LinearLayout(activity.getActivity());
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                classDefTable.addView(linearLayout);
            }

            // Create a new text view for the class, set some text related attributes
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
                if(selectedTextView != null) // Check if there is a class selected
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
