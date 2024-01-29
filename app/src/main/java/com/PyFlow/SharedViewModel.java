package com.PyFlow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
