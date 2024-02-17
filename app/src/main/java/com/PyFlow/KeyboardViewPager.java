package com.PyFlow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class KeyboardViewPager extends ViewPager
{

    public KeyboardViewPager(Context context)
    {
        super(context);
    }
    public KeyboardViewPager(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        // Never allow swiping to switch between pages on the keyboard
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        // Never allow swiping to switch between pages on the keyboard
        return false;
    }
}
