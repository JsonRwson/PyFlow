package com.PyFlow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel
{
    private final MutableLiveData<CustomEditText> selected = new MutableLiveData<CustomEditText>();

    public void select(CustomEditText item)
    {
        selected.setValue(item);
    }

    public LiveData<CustomEditText> getSelected()
    {
        return selected;
    }
}
