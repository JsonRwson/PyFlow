package com.PyFlow;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

public class PythonService extends IntentService
{

    public static final String codeIdentifier = "code";
    public static final String inputIdentifier = "input_data";
    public static final String resultReceiver = "result_receiver";

    public PythonService()
    {
        super("PythonService");
    }

    // Reset the service,
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        System.exit(0);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null)
        {
            final String code = intent.getStringExtra(codeIdentifier);
            final String[] inputData = intent.getStringArrayExtra(inputIdentifier);
            final ResultReceiver receiver = intent.getParcelableExtra(resultReceiver);

            if(!Python.isStarted())
            {
                Python.start(new AndroidPlatform(this));
            }

            // Run the Python code here
            Python PyInstance = Python.getInstance();
            PyObject pyScriptObject = PyInstance.getModule("CodeScript"); // replace with your module name
            PyObject result = pyScriptObject.callAttr("main", code, inputData); // replace with your function name

            // Send the result back
            if (receiver != null)
            {
                Bundle bundle = new Bundle();
                bundle.putString("result", result.toString());
                receiver.send(0, bundle);
            }
        }
    }
}
