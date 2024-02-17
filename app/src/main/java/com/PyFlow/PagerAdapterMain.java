package com.PyFlow;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerAdapterMain extends FragmentPagerAdapter
{

    public PagerAdapterMain(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        // Only 2 tabs for the main activity
        // Return the fragment classes corresponding to the position
        // 0 for sourcecode tab
        // 1 for execution tab
        switch(position)
        {
            case 0:
                return new SourcecodeTab();
            case 1:
                return new ExecuteCodeTab();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 2; // Number of tabs
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return null;
    }
}