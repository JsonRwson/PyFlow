package com.PyFlow;

import static android.app.Activity.RESULT_OK;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExecuteCodeTab extends Fragment
{

    public Button runCodeButton;
    public Button stopCodeButton;
    public TextView codeOutputText;
    public EditText codeInputText;
    private List<View> inputTextList;
    private List<View> inputViewList;
    private LinearLayout inputContainer;

    private Thread pythonExecutionThread = null;
    private boolean isCodeExecuting = false;

    public ExecuteCodeTab()
    {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        SharedViewModel sharedModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedModel.getSelected().observe(getViewLifecycleOwner(), customEditText -> codeInputText = customEditText);
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
        runCodeButton = view.findViewById(R.id.run_code);
        stopCodeButton = view.findViewById(R.id.stop_code);
        codeOutputText = view.findViewById(R.id.code_output);
        Button addInputButton = view.findViewById(R.id.add_input);
        Button removeInputButton = view.findViewById(R.id.remove_input);
        Button copyOutputButton = view.findViewById(R.id.copy_output);

        // Start Python
        if (!Python.isStarted())
        {
            Python.start(new AndroidPlatform(getActivity()));
        }

        // Add input listener, to add new input boxes to the tab
        addInputButton.setOnClickListener(v ->
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

            voiceButton.setOnClickListener(v1 ->
            {
                int requestCode = (int) v1.getTag();  // Get the requestCode from the tag
                startVoiceRecognitionActivity(requestCode);
            });

            // Add the layout to the LinearLayout and the list
            inputContainer.addView(inputFieldView);
            inputViewList.add(inputFieldView);
        });

        // Remove input listener, to remove input boxes from the tab
        removeInputButton.setOnClickListener(v ->
        {
            // Remove the last view from the LinearLayout and the list
            if (!inputViewList.isEmpty())
            {
                View lastView = inputViewList.get(inputViewList.size() - 1);
                inputContainer.removeView(lastView);
                inputViewList.remove(lastView);

                // Also remove the EditText from the list
                if (!inputTextList.isEmpty())
                {
                    inputTextList.remove(inputTextList.size() - 1);
                }
            }
        });


        // Run code button listener to fetch code, inputs and execute
        runCodeButton.setOnClickListener(view1 ->
        {
            // If a thread is already running, interrupt it
            if (pythonExecutionThread != null && pythonExecutionThread.isAlive())
            {
                pythonExecutionThread.interrupt();
            }
            if(!isCodeExecuting)
            {
                pythonExecutionThread = new Thread(() ->
                {
                    try
                    {
                        PyObject pyObj = null;
                        PyObject pyScriptObject = null;
                        isCodeExecuting = true;

                        getActivity().runOnUiThread(() -> codeOutputText.setText("Executing..."));

                        // get python instance and script to run user code in
                        Python PyInstance = Python.getInstance();
                        pyScriptObject = PyInstance.getModule("CodeScript");

                        // Get input data from widgets
                        List<String> inputLines = new ArrayList<>();
                        for (View inputFieldView : inputViewList)
                        {
                            EditText inputField = inputFieldView.findViewById(R.id.input_field);
                            inputLines.add(inputField.getText().toString());
                        }

                        // call main function in python script, pass in user code and input data
                        pyObj = pyScriptObject.callAttr("main", codeInputText.getText().toString(), inputLines.toArray(new String[0]));

                        // Get the output as a string
                        String output = pyObj.toString();

                        // Split the output into lines
                        String[] lines = output.split("\\r?\\n");

                        // Set a limit for the number of lines
                        int maxLines = 5000;  // Change this to your desired limit
                        int currentLines = lines.length;

                        // Check if the output exceeds the limit
                        if (currentLines > maxLines)
                        {
                            // If it does, take only the first maxLines lines and add a message about the output being shortened
                            output = String.join("\n", Arrays.copyOfRange(lines, 0, maxLines));

                            getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Showing only " + maxLines + " of " + currentLines + " lines", Toast.LENGTH_SHORT).show());
                        }

                        // set code output widget to result of executing python code
                        String finalOutput = output;
                        getActivity().runOnUiThread(() -> codeOutputText.setText(finalOutput));
                    }
                    catch (Exception e)
                    {
                        Log.e("Py Code Exec Error", "Error: " + e.getMessage());
                    }
                    finally
                    {
                        isCodeExecuting = false;
                        PyObject pyObj = null;
                        PyObject pyScriptObject = null;
                    }
                });

                pythonExecutionThread.start();
            }
        });

        stopCodeButton.setOnClickListener(view1 ->
        {
            if (pythonExecutionThread != null && pythonExecutionThread.isAlive())
            {
                pythonExecutionThread.interrupt();
                isCodeExecuting = false;
                codeOutputText.setText("Execution Stopped");
            }
        });


        copyOutputButton.setOnClickListener(view1 ->
        {
            // Get the ClipboardManager service
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

            // Create a ClipData object holding the text from codeOutputText
            ClipData clip = ClipData.newPlainText("codeOutput", codeOutputText.getText().toString());

            clipboard.setPrimaryClip(clip);
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