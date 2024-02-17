package com.PyFlow;

import static android.app.Activity.RESULT_OK;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
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
    public TextView codeOutputText; // Store the output of code execution
    public EditText codeInputText; // Store the code from the editor to run
    private List<View> inputTextList; // Store the data used for "input()" calls
    private List<View> inputViewList;
    private LinearLayout inputContainer;

    // Flag to prevent executing multiple programs at a time
    private boolean isCodeExecuting = false;

    public ExecuteCodeTab()
    {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        // Get the shared view model from the activity, set by the source code tab class
        SharedViewModel sharedModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        // Get the source code editor widget from the share view model, add it to the local reference
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

        // Start python, if not already
        if(!Python.isStarted())
        {
            Python.start(new AndroidPlatform(getActivity()));
        }

        // Allows the user to create input boxes used for "input()" calls in the python code
        // The text from each box is used in the corresponding input call
        // E.g. the first input box will be fed to the first input call
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

            // Add text to speech functionality to the input box
            voiceButton.setOnClickListener(v1 ->
            {
                int requestCode = (int) v1.getTag();  // Get the request code from the tag
                startVoiceRecognitionActivity(requestCode);
            });

            // Add the layout to the linear layout and the list
            inputContainer.addView(inputFieldView);
            inputViewList.add(inputFieldView);
        });

        // Allow the user to remove added input boxes
        removeInputButton.setOnClickListener(v ->
        {
            // Remove the last view from the linear layout and the list
            if(!inputViewList.isEmpty()) // If there is a view to remove
            {
                // Get the last view and remove it from the list and container
                View lastView = inputViewList.get(inputViewList.size() - 1);
                inputContainer.removeView(lastView);
                inputViewList.remove(lastView);

                // Also remove the edit text from the list
                if(!inputTextList.isEmpty())
                {
                    inputTextList.remove(inputTextList.size() - 1);
                }
            }
        });

        // Result receiver to handle the result of executing python code using the service
        // The text outputted to the console is returned with the result code
        ResultReceiver receiver = new ResultReceiver(new Handler())
        {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData)
            {
                super.onReceiveResult(resultCode, resultData);
                // Fetch the output of code execution from the result data
                String result = resultData.getString("result");

                // Split the output into lines
                String[] lines = result.split("\\r?\\n");

                // Set a limit for the number of lines to prevent excessive text sizes
                int maxLines = 5000;
                int currentLines = lines.length;

                // Check if the output exceeds the limit
                if(currentLines > maxLines)
                {
                    // If it does, take only the first maxLines lines and add a message about the output being shortened
                    result = String.join("\n", Arrays.copyOfRange(lines, 0, maxLines));

                    getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Showing only " + maxLines + " of " + currentLines + " lines", Toast.LENGTH_SHORT).show());
                }

                // Set code output widget to result of executing python code
                String finalOutput = result;
                codeOutputText.setText(finalOutput);
            }
        };


        // Run code button listener to fetch code, inputs and execute
        runCodeButton.setOnClickListener(view1 ->
        {
            // If code isnt already executing
            if(!isCodeExecuting)
            {
                try
                {
                    // Set the executing flag
                    isCodeExecuting = true;

                    // Set the text of the output window to indicate execution has started
                    codeOutputText.setText("Executing...");

                    // get python instance and script to run user code in
                    Python PyInstance = Python.getInstance();

                    // Get input data from the input boxes
                    List<String> inputLines = new ArrayList<>();
                    // Iterate through the input boxes
                    for(View inputFieldView : inputViewList)
                    {
                        // Add the text from each input box as a new element in the list
                        EditText inputField = inputFieldView.findViewById(R.id.input_field);
                        inputLines.add(inputField.getText().toString());
                    }

                    // Create an Intent for the service
                    Intent intent = new Intent(getActivity(), PythonService.class);
                    // Pass the code to execute and the array of input data to the intent
                    intent.putExtra(PythonService.codeIdentifier, codeInputText.getText().toString());
                    intent.putExtra(PythonService.inputIdentifier, inputLines.toArray(new String[0]));
                    // Pass the result receiver to ensure the output will be received in this class
                    intent.putExtra(PythonService.resultReceiver, receiver);

                    getActivity().startService(intent); // Start the execution service
                }
                catch(Exception e) // Catch any excpetions when executing code
                {
                    // Catch exceptions and notify the user
                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    Log.e("Py Code Exec Error", "Error: " + e.getMessage());
                }
                finally
                {
                    isCodeExecuting = false;
                }
            }
        });

        // Button to stop currently executing code
        stopCodeButton.setOnClickListener(v ->
        {
            // Create an intent for the python service
            Intent intent = new Intent(getActivity(), PythonService.class);

            // Call stop on the service, which will safely stop python
            getActivity().stopService(intent);

            // Notify the user that execution has successfully stopped
            codeOutputText.setText("Execution Halted");

            // Update the flag
            isCodeExecuting = false;
        });

        // Copy the output of code execution
        copyOutputButton.setOnClickListener(view1 ->
        {
            // Get the clipboard manager service
            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

            // Create a clip data object holding the text from output
            ClipData clip = ClipData.newPlainText("codeOutput", codeOutputText.getText().toString());

            // Set the primary clip to the output text
            clipboard.setPrimaryClip(clip);
        });

        return view;
    }

    // Display the voice recognizer
    private void startVoiceRecognitionActivity(int requestCode)
    {
        // Create a speech recognizer intent
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Configure the intent, setting the language model to free form, suited for dictation
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");
        startActivityForResult(intent, requestCode); // Start an activity to handle the resulting text
        // Pass it the code defined for speech activities, so the method knows how to handle the data
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // If the result was okay, and the data isnt null
        if(resultCode == RESULT_OK && data != null)
        {
            // Get the result from the intent as a string array list
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            // Determine which input box to add the text to by using the request code as an index for the list
            EditText editText = (EditText) inputTextList.get(requestCode);
            // Set the text of the input box to the recognized speech
            editText.setText(result.get(0));
        }
    }
}