package com.PyFlow;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity
{
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private CustomViewPager viewPager;

    private SourcecodeTab sourcecodeTabFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(item ->
        {
            int id = item.getItemId();
            if (id == R.id.nav_item1)
            {

            }
            else if (id == R.id.nav_item2)
            {

            }
            drawerLayout.closeDrawers();
            return true;
        });

        viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        PagerAdapter pagerAdapter = new PagerAdapterMain(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        // Disable swiping to switch tabs
        viewPager.setPagingEnabled(false);
    }

    public void onBackPressed()
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }
}