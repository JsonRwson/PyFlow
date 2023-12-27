package com.PyFlow;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.List;

public class ExecuteCodeTab extends Fragment
{
    private SharedViewModel sharedModel;

    public Button runCodeButton;
    public TextView codeOutputText;
    public EditText codeInputText;
    private List<View> inputTextList;
    private List<View> inputViewList;
    private LinearLayout inputContainer;

    public ExecuteCodeTab()
    {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        sharedModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedModel.getSelected().observe(getViewLifecycleOwner(), new Observer<CustomEditText>()
        {
            @Override
            public void onChanged(@Nullable CustomEditText customEditText)
            {
                codeInputText = customEditText;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_execute_code_tab, container, false);

        // Initialise array for input text boxes and fetch layout reference
        inputTextList = new ArrayList<>();
        inputViewList = new ArrayList<>();
        inputContainer = view.findViewById(R.id.input_container);

        // References to ui elements
        runCodeButton = (Button) view.findViewById(R.id.run_code);
        codeOutputText = (TextView) view.findViewById(R.id.code_output);
        Button addInputButton = view.findViewById(R.id.add_input);
        Button removeInputButton = view.findViewById(R.id.remove_input);

        // Start Python
        if (! Python.isStarted())
        {
            Python.start(new AndroidPlatform(getActivity()));
        }

        // Add input listener, to add new input boxes to the tab
        addInputButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Inflate the input field layout
                View inputFieldView = getLayoutInflater().inflate(R.layout.input_field, null);

                // Get the EditText and add it to the list
                EditText newInput = inputFieldView.findViewById(R.id.input_field);
                inputTextList.add(newInput);

                // Set the position number
                TextView inputNumber = inputFieldView.findViewById(R.id.input_number);
                inputNumber.setText(String.valueOf(inputTextList.size()));

                Button voiceButton = inputFieldView.findViewById(R.id.voice_button);
                voiceButton.setTag(inputTextList.size() - 1);  // Set the tag

                voiceButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int requestCode = (int) v.getTag();  // Get the requestCode from the tag
                        startVoiceRecognitionActivity(requestCode);
                    }
                });

                // Add the layout to the LinearLayout and the list
                inputContainer.addView(inputFieldView);
                inputViewList.add(inputFieldView);
            }
        });

        // Remove input listener, to remove input boxes from the tab
        removeInputButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Remove the last view from the LinearLayout and the list
                if (!inputViewList.isEmpty())
                {
                    View lastView = inputViewList.get(inputViewList.size() - 1);
                    inputContainer.removeView(lastView);
                    inputViewList.remove(lastView);

                    // Also remove the corresponding EditText from the list
                    if (!inputTextList.isEmpty())
                    {
                        inputTextList.remove(inputTextList.size() - 1);
                    }
                }
            }
        });


        // Run code button listener to fetch code, inputs and execute
        runCodeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try
                {
                    // get python instance and script to run user code in
                    Python PyInstance = Python.getInstance();
                    PyObject pyScriptObject = PyInstance.getModule("CodeScript");

                    // Get input data from widgets
                    List<String> inputLines = new ArrayList<>();
                    for (View inputFieldView : inputViewList)
                    {
                        EditText inputField = inputFieldView.findViewById(R.id.input_field);
                        inputLines.add(inputField.getText().toString());
                    }

                    // call main function in python script, pass in user code and input data
                    PyObject pyObj = pyScriptObject.callAttr("main", codeInputText.getText().toString(), inputLines.toArray(new String[0]));

                    // set code output widget to result of executing python code
                    codeOutputText.setText(pyObj.toString());

                    // catch exceptions when running code and log error to logcat
                }
                catch (Exception e)
                {
                    Log.e("Py Code Exec Error", "Error: " + e.getMessage());
                }
            }
        });

        return view;
    }

    private void startVoiceRecognitionActivity(int requestCode)
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data)
        {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            EditText editText = (EditText) inputTextList.get(requestCode);
            editText.setText(result.get(0));
        }
    }
}