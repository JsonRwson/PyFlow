package com.PyFlow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

// Shared view model used between the sourcecode editing fragment and execution fragment
// Allows the execution fragment to access code inputted into the editor
public class SharedViewModel extends ViewModel
{
    private final MutableLiveData<SourcecodeEditor> selected = new MutableLiveData<SourcecodeEditor>();

    public void select(SourcecodeEditor item)
    {
        selected.setValue(item);
    }

    public LiveData<SourcecodeEditor> getSelected()
    {
        return selected;
    }
}
