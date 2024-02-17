package com.PyFlow;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the toolbar for the editor
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CustomViewPager viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        PagerAdapter pagerAdapter = new PagerAdapterMain(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        // Add icons to the tab to represent the editor and execution pages
        tabLayout.getTabAt(0).setIcon(R.drawable.code_icon);
        tabLayout.getTabAt(1).setIcon(R.drawable.play_icon);

        // Disable swiping to switch tabs between the sourcecode editor and execution tab
        viewPager.setPagingEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

}