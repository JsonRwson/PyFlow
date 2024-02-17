package com.PyFlow;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

// Python service that is started when the user wants to run their code
public class PythonService extends IntentService
{
    public static final String codeIdentifier = "code";
    public static final String inputIdentifier = "input_data";
    public static final String resultReceiver = "result_receiver";

    public PythonService()
    {
        super("PythonService");
    }

    // Reset the service
    // After using the service, destroy is called, which calls system.exit on the service
    // Essentially re-starting Python, giving a clean state to execute a new set of code
    // Prevents subsequent executions from being affected by previous ones e.g. being left in an unstable state from aborting
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        System.exit(0);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if(intent != null)
        {
            // The service receives an intent containing:
            // The python code to be executed
            // The values used for "input()" calls in the python code
            // And the receiver to pass the code result back to (in the sourcecode tab class)
            final String code = intent.getStringExtra(codeIdentifier);
            final String[] inputData = intent.getStringArrayExtra(inputIdentifier);
            final ResultReceiver receiver = intent.getParcelableExtra(resultReceiver);

            // Start chaquopy
            if(!Python.isStarted())
            {
                Python.start(new AndroidPlatform(this));
            }

            Python PyInstance = Python.getInstance();
            // Code is executed in a python script
            // The java application calls the main function in the python script, passing it the code to run "exec()" on
            // Data for "input()" calls is also passed to use in the custom "input()" function, replacing the built-in one
            // The python object returns the resulting std output text
            PyObject pyScriptObject = PyInstance.getModule("CodeScript");
            PyObject result = pyScriptObject.callAttr("main", code, inputData);

            // Send the result back
            if(receiver != null)
            {
                Bundle bundle = new Bundle();
                // Put the resulting output string from executing the code in the intent
                // Send the bundle to the receiver which started the service
                bundle.putString("result", result.toString());
                receiver.send(0, bundle);
            }
        }
    }
}
